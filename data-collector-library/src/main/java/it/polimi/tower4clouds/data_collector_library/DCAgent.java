/**
 * Copyright (C) 2014 Politecnico di Milano (marco.miglierina@polimi.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.polimi.tower4clouds.data_collector_library;

import it.polimi.tower4clouds.common.net.DefaultRestClient;
import it.polimi.tower4clouds.common.net.RestClient;
import it.polimi.tower4clouds.common.net.RestMethod;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DCAgent extends Observable {

	private static final Logger logger = LoggerFactory.getLogger(DCAgent.class);

	private DCDescriptor dCDescriptor;
	private String dataCollectorId;
	private Map<String, Set<DCConfiguration>> dCConfigsByMetric = new HashMap<String, Set<DCConfiguration>>();
	private boolean registered = false;

	private final int connectionRetryPeriod = 5;
	private int timeout = 10000;

	private final ScheduledExecutorService syncExecService = Executors
			.newScheduledThreadPool(2);

	private ScheduledFuture<?> syncJob;
	private ScheduledFuture<?> keepAliveJob;

	private final Timer timer = new Timer();
	private final long delay = 1000;
	private final Map<String, JsonArray> dataByMetric = new HashMap<String, JsonArray>();
	private final ExecutorService sendDataExecService = Executors
			.newCachedThreadPool();
	private final ExecutorService registrationExecService = Executors
			.newFixedThreadPool(1);

	private Object asyncSenderLock = new Object();

	private boolean timerRunning = false;

	private ManagerAPI manager;

	private Future<?> registrationJob;

	private RestClient restClient = new DefaultRestClient();

	private boolean started = false;

	public DCAgent(ManagerAPI manager) {
		this.manager = manager;
		manager.setDefaultTimeout(timeout);
	}

	public synchronized boolean isStarted() {
		return started;
	}

	public void setDefaultRestClient(RestClient restClient) {
		this.restClient = restClient;
	}

	public void setDCDescriptor(DCDescriptor dCDescriptor) {
		validate(dCDescriptor);
		this.dCDescriptor = dCDescriptor;
	}

	private void registerDC() {
		if (dCDescriptor == null)
			throw new RuntimeException("DCDescriptor was not set");
		registered = false;
		while (!registered) {
			try {
				logger.info("Registering DC descriptor: {}",
						dCDescriptor.toString());
				if (dataCollectorId != null) {
					manager.registerDataCollector(dataCollectorId, dCDescriptor);
				} else {
					dataCollectorId = manager
							.registerDataCollector(dCDescriptor);
				}
				registered = true;
			} catch (IOException e) {
				logger.warn(
						"Could not connect to server: {}. Retrying in {} seconds",
						e.getMessage(), connectionRetryPeriod);
				try {
					Thread.sleep(connectionRetryPeriod * 1000);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void validate(DCDescriptor dCDescriptor) {
		if (dCDescriptor.getKeepAlive() > 0 && dCDescriptor.getKeepAlive() < 10) {
			throw new IllegalArgumentException(
					"Keep alive time is too short, use at least 10 seconds");
		}
		if (dCDescriptor.getConfigSyncPeriod() < 10) {
			throw new IllegalArgumentException(
					"Config sync period is too short, use at least 10 seconds");
		}
	}

	private void stopSyncing() {
		if (syncJob != null) {
			logger.info("Stopping DC configuration synchronization");
			syncJob.cancel(true);
		}
	}

	private void startSyncing() {
		int configSyncPeriod = dCDescriptor.getConfigSyncPeriod();

		stopSyncing();

		logger.info(
				"Starting DC configuration synchronization. Will run every {} seconds",
				configSyncPeriod);
		syncJob = syncExecService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, Set<DCConfiguration>> newConfig = getRemoteDCConfiguration();
					if (!newConfig.equals(dCConfigsByMetric)) {
						dCConfigsByMetric = newConfig;
						logger.debug("Downloaded new dc configuration: {}",
								dCConfigsByMetric);
						setChanged();
						notifyObservers();
					} else {
						logger.debug(
								"Downloaded dc configuration, nothing changed from previous config",
								dCConfigsByMetric);
					}
				} catch (UnexpectedAnswerFromServerException e) {
					logger.warn("DC does not seem to be registered anymore, re-registering DC");
					registerDC();
				} catch (IOException e) {
					logger.error(
							"Could not download new DC configuration, the server may be down, cancelling any local dc configuration: {}",
							e.getMessage());
					dCConfigsByMetric = new HashMap<String, Set<DCConfiguration>>();
					setChanged();
					notifyObservers();
				} catch (Exception e) {
					logger.error("Unknown Error", e);
				}
			}

		}, 0, configSyncPeriod, TimeUnit.SECONDS);
	}

	private void startKeepAlive() {
		long maxKeepAlivePeriod = Math
				.max(dCDescriptor.getKeepAlive() - 10, 10);
		if (maxKeepAlivePeriod >= dCDescriptor.getConfigSyncPeriod()) {
			logger.info("Keep alive is not required, config sync period is short enough for keeping the resources alive");
			return;
		} else if (dCDescriptor.getKeepAlive() <= 0) {
			logger.info("Keep alive is not required");
			return;
		}
		stopKeepAlive();

		logger.info("Starting keep alive. Will run every {} seconds",
				maxKeepAlivePeriod);
		keepAliveJob = syncExecService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					logger.debug("Keeping alive");
					keepAlive();
				} catch (UnexpectedAnswerFromServerException e) {
					logger.info("DC does not seem to be registered anymore, re-registering DC");
					registerDC();
				} catch (IOException e) {
					logger.error(
							"Error while trying to keeping alive, the server may be down: {}",
							e.getMessage());
				} catch (Exception e) {
					logger.error("Unknown Error", e);
				}
			}

		}, 0, (long) maxKeepAlivePeriod, TimeUnit.SECONDS);
	}

	private void stopKeepAlive() {
		if (keepAliveJob != null) {
			logger.info("Stopping existing keep alive job");
			keepAliveJob.cancel(true);
		}
	}

	public boolean shouldMonitor(Resource resource, String metric) {
		if (dCConfigsByMetric == null || !dCConfigsByMetric.containsKey(metric))
			return false;
		Set<DCConfiguration> dcconfigs = dCConfigsByMetric.get(metric);
		if (dcconfigs != null) {
			for (DCConfiguration dcConfiguration : dcconfigs) {
				if (dcConfiguration != null
						&& dcConfiguration.isAboutResource(resource))
					return true;
			}
		}
		return false;
	}

	public Map<String, String> getParameters(Resource resource, String metric) {
		if (dCConfigsByMetric == null || !dCConfigsByMetric.containsKey(metric))
			return null;
		Set<DCConfiguration> dcconfigs = dCConfigsByMetric.get(metric);
		if (dcconfigs != null) {
			for (DCConfiguration dcConfiguration : dcconfigs) {
				if (dcConfiguration != null
						&& dcConfiguration.isAboutResource(resource))
					return dcConfiguration.getParameters();
			}
		}
		return null;
	}

	public void send(Resource resource, String metric, Object value) {
		if (!started) {
			logger.error("The DCAgent is not started, data won't be sent");
			return;
		}
		if (!shouldMonitor(resource, metric)) {
			logger.error(
					"Monitoring is not required for the given resource and the given metric, datum [{},{},{}] won't be sent",
					resource.getId(), metric, value.toString());
			return;
		}
		JsonObject jsonDatum = new JsonObject();
		jsonDatum.addProperty(MOVocabulary.resourceId, resource.getId());
		jsonDatum.addProperty(MOVocabulary.metric, metric);
		if (value instanceof String) {
			jsonDatum.addProperty(MOVocabulary.value, (String) value);
		} else if (value instanceof Number) {
			jsonDatum.addProperty(MOVocabulary.value, (Number) value);
		} else if (value instanceof Boolean) {
			jsonDatum.addProperty(MOVocabulary.value, (Boolean) value);
		} else if (value instanceof Character) {
			jsonDatum.addProperty(MOVocabulary.value, (Character) value);
		} else {
			logger.error(
					"Value cannot be a {}. Only String, Number, Boolean or Character are allowed",
					value.getClass());
			return;
		}
		addToBuffer(metric, jsonDatum);
		startTimerIfNotStarted();
	}

	private void startTimerIfNotStarted() {
		synchronized (asyncSenderLock) {
			if (!timerRunning) {
				timerRunning = true;
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							timeToSend();
						} catch (Exception e) {
							logger.error("Unknown Error", e);
						}
					}
				}, delay);
			}
		}
	}

	private void addToBuffer(String metric, JsonObject jsonDatum) {
		synchronized (asyncSenderLock) {
			JsonArray data = dataByMetric.get(metric);
			if (data == null) {
				data = new JsonArray();
				dataByMetric.put(metric, data);
			}
			data.add(jsonDatum);
		}
	}

	private void timeToSend() {
		synchronized (asyncSenderLock) {
			for (String metric : dataByMetric.keySet()) {
				sendDataExecService.execute(new SenderTask(metric, dataByMetric
						.get(metric)));
			}
			dataByMetric.clear();
			timerRunning = false;
		}
	}

	private class SenderTask implements Runnable {

		private JsonArray data;
		private String metric;

		public SenderTask(String metric, JsonArray data) {
			this.data = data;
			this.metric = metric;
		}

		@Override
		public void run() {

			if (!dCConfigsByMetric.containsKey(metric)) {
				logger.debug(
						"Monitoring for metric {} no longer required, buffered data will be dropped",
						metric);
				return;
			}
			Set<DCConfiguration> dcConfigurations = dCConfigsByMetric.get(metric);

			// TODO the first URL is used (ok if we are using only one data analyzer) 
			DCConfiguration dcConfiguration = dcConfigurations.iterator().next();

			long ts = System.currentTimeMillis();
			logger.debug("Sending {} monitoring data", data.size());
			try {

				restClient.execute(RestMethod.POST, dcConfiguration.getDaUrl(),
						data.toString(), 200, timeout);
				logger.debug("Data sent in {} seconds",
						((double) (System.currentTimeMillis() - ts)) / 1000);
			} catch (UnexpectedAnswerFromServerException e) {
				logger.error(
						"Error while trying to send data. This may not be an error, "
								+ "monitoring for metric {} may be not required"
								+ " anymore, the stream may have been closed by the server "
								+ "and dc configuration may not have been synchronized yet. Error message: {}",
						metric, e.getMessage());
			} catch (Exception e) {
				logger.error(
						"Unknwon error while trying to send data for metric {}: {}",
						metric, e.getMessage());
			}
		}

	}

	private Map<String, Set<DCConfiguration>> getRemoteDCConfiguration()
			throws UnexpectedAnswerFromServerException, IOException {
		return manager.getDCConfigurationsByMetric(dataCollectorId);
	}

	private void keepAlive() throws UnexpectedAnswerFromServerException,
			IOException {
		manager.keepAlive(dataCollectorId);
	}

	public synchronized void start() {
		stopDCRegistration();
		registrationJob = registrationExecService.submit(new Runnable() {
			@Override
			public void run() {
				registerDC();
				startSyncing();
				startKeepAlive();
			}
		});
		started = true;
	}

	/**
	 * To be called when the DCDescriptor is updated to make changes happen
	 */
	public synchronized void refresh() {
		logger.info("Refreshing DCAgent based on new DCDescriptor");
		if (!started) {
			logger.warn("DCAgent was not started yet");
		} else {
			start();
		}
	}

	private void stopDCRegistration() {
		if (registrationJob != null) {
			registrationJob.cancel(true);
		}
	}

	public synchronized void stop() {
		stopDCRegistration();
		stopSyncing();
		stopKeepAlive();
		started = false;
	}

	public Set<String> getRequiredMetrics() {
		return dCConfigsByMetric.keySet();
	}
}
