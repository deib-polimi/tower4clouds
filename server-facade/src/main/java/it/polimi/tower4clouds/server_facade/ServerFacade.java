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
package it.polimi.tower4clouds.server_facade;

import it.polimi.tower4clouds.common.net.DefaultRestClient;
import it.polimi.tower4clouds.common.net.RestClient;
import it.polimi.tower4clouds.common.net.RestMethod;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ServerFacade extends Observable {

	private static final Logger logger = LoggerFactory
			.getLogger(ServerFacade.class);

	private DCDescriptor dCDescriptor;
	private String dcAssignedId;
	private Map<String, DCConfiguration> dCConfigs = new HashMap<String, DCConfiguration>();
	private boolean registered = false;

	private final int connectionRetryPeriod = 5;
	private final int timeout = 10000;

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

	private final String dcBaseUrl;

	private boolean timerRunning = false;

	private RestClient restClient;

	private Future<?> registrationJob;

	public ServerFacade(String monitoringManagerIP,
			String monitoringManagerPort) {
		dcBaseUrl = "http://" + monitoringManagerIP + ":"
				+ monitoringManagerPort + "/v1/data-collectors";
		restClient = new DefaultRestClient();
	}

	/**
	 * Replace the default rest client with a custom one
	 * 
	 * @param restClient
	 */
	public void setDefaultRestClient(RestClient restClient) {
		this.restClient = restClient;
	}

	
	public void setDCDescriptor(DCDescriptor dCDescriptor) {
		validate(dCDescriptor);
		this.dCDescriptor = dCDescriptor;
		if (registrationJob != null) {
			registrationJob.cancel(true);
		}
		registrationJob = registrationExecService.submit(new Runnable() {

			@Override
			public void run() {
				registerDC();
				startSyncing();
				startKeepAlive();
			}
		});
	}

	private void registerDC() {
		RestMethod method;
		String url;
		if (dcAssignedId != null) {
			method = RestMethod.PUT;
			url = dcBaseUrl + "/" + dcAssignedId;
		} else {
			method = RestMethod.POST;
			url = dcBaseUrl;
		}
		String jsonEntity = dCDescriptor.toJson();
		int expectedCode = 200;
		registered = false;
		String responseBody;
		while (!registered) {
			try {
				logger.info("Registering DC descriptor: {}",
						dCDescriptor.toString());
				responseBody = restClient.execute(method, url, jsonEntity,
						expectedCode, timeout);
				dcAssignedId = new JsonParser().parse(responseBody).getAsJsonObject()
						.get("id").getAsString();
				registered = true;
			} catch (Exception e) {
				logger.warn(
						"Could not connect to server: {}. Retrying in {} seconds",
						e.getMessage(), connectionRetryPeriod);
				try {
					Thread.sleep(connectionRetryPeriod * 1000);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
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

	private void startSyncing() {
		int configSyncPeriod = dCDescriptor.getConfigSyncPeriod();

		if (syncJob != null) {
			logger.info("Stopping DC configuration synchronization");
			syncJob.cancel(true);
		}

		logger.info(
				"Starting DC configuration synchronization. Will run every {} seconds",
				configSyncPeriod);
		syncJob = syncExecService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, DCConfiguration> newConfig = getRemoteDCConfiguration();
					if (!newConfig.equals(dCConfigs)){
						dCConfigs = newConfig;
						logger.debug("Downloaded new dc configuration: {}", dCConfigs);
						setChanged();
						notifyObservers();
					} else {
						logger.debug("Downloaded dc configuration, nothing changed from previous config", dCConfigs);
					}
				} catch (UnexpectedAnswerFromServerException e) {
					logger.warn("DC does not seem to be registered anymore, re-registering DC");
					registerDC();
				} catch (IOException e) {
					logger.error(
							"Could not download new DC configuration, the server may be down: {}",
							e.getMessage());
				}
			}

		}, 0, configSyncPeriod, TimeUnit.SECONDS);
	}

	private void startKeepAlive() {
		long maxKeepAlivePeriod = Math
				.max(dCDescriptor.getKeepAlive() - 10, 10);
		if (dCDescriptor.getKeepAlive() <= 0
				|| maxKeepAlivePeriod >= dCDescriptor.getConfigSyncPeriod()) {
			logger.info("Keep alive is not required, config sync period is short enough for keeping the resources alive");
			return;
		}
		if (keepAliveJob != null) {
			logger.info("Stopping existing keep alive job");
			keepAliveJob.cancel(true);
		}

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
				}
			}

		}, 0, (long) maxKeepAlivePeriod, TimeUnit.SECONDS);
	}
	
	

	public boolean shouldMonitor(Resource resource, String metric) {
		if (!dCConfigs.containsKey(metric))
			return false;
		if (dCConfigs.get(metric).getMonitoredResourcesIds()
				.contains(resource.getId()))
			return true;
		if (dCConfigs.get(metric).getMonitoredResourcesIds().isEmpty()
				&& dCConfigs.get(metric).getMonitoredResourcesTypes()
						.contains(resource.getType()))
			return true;
		if (dCConfigs.get(metric).getMonitoredResourcesTypes().isEmpty()
				&& dCConfigs.get(metric).getMonitoredResourcesClasses()
						.contains(resource.getClazz()))
			return true;
		return false;
	}

	public Map<String, String> getParameters(String metric) {
		if (dCConfigs == null) {
			return null;
		}
		DCConfiguration dcconfig = dCConfigs.get(metric);
		if (dcconfig == null) {
			return null;
		}
		return dcconfig.getParameters();
	}

	public void send(Resource resource, String metric, Object value) {
		if (!shouldMonitor(resource, metric)) {
			logger.error("Monitoring is not required for the given resource and the given metric, datum won't be sent");
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

	private synchronized void startTimerIfNotStarted() {
		if (!timerRunning) {
			timerRunning = true;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					timeToSend();
				}
			}, delay);
		}
	}

	private synchronized void addToBuffer(String metric, JsonObject jsonDatum) {
		JsonArray data = dataByMetric.get(metric);
		if (data == null) {
			data = new JsonArray();
			dataByMetric.put(metric, data);
		}
		data.add(jsonDatum);
	}

	private synchronized void timeToSend() {
		for (final String metric : dataByMetric.keySet()) {
			sendDataExecService.execute(new SenderTask(
					dataByMetric.get(metric), metric));
		}
		dataByMetric.clear();
		timerRunning = false;
	}

	private class SenderTask implements Runnable {

		private JsonArray data;
		private String metric;

		public SenderTask(JsonArray data, String metric) {
			this.data = data;
			this.metric = metric;
		}

		@Override
		public void run() {

			DCConfiguration dcConfiguration = dCConfigs.get(metric);
			if (dcConfiguration == null) {
				logger.info(
						"Monitoring for metric {} no longer required, buffered data will be dropped",
						metric);
				return;
			}
			long ts = System.currentTimeMillis();
			logger.debug("Sending {} monitoring data", data.size());
			try {
				restClient.execute(RestMethod.POST, dcConfiguration.getDaUrl(),
						data.toString(), 200, timeout);
				logger.debug("Data sent in {} seconds",
						((double) (System.currentTimeMillis() - ts)) / 1000);
			} catch (Exception e) {
				logger.error(
						"Error while trying to send data. This may not be an error, "
								+ "monitoring for metric {} may be not required"
								+ " anymore, the stream may have been closed by the server "
								+ "and dc configuration may not have been synchronized yet",
						metric, e);
			}
		}

	}

	private Map<String, DCConfiguration> getRemoteDCConfiguration()
			throws UnexpectedAnswerFromServerException, IOException {

		String responseJson = restClient.execute(RestMethod.GET, dcBaseUrl
				+ "/" + dcAssignedId + "/configuration", null, 200, timeout);

		return new Gson().fromJson(responseJson,
				new TypeToken<Map<String, DCConfiguration>>() {
				}.getType());
	}

	private void keepAlive() throws UnexpectedAnswerFromServerException,
			IOException {
		restClient.execute(RestMethod.GET, dcBaseUrl + "/" + dcAssignedId
				+ "/keepalive", null, 204, timeout);
	}
}
