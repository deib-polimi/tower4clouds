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
package it.polimi.tower4clouds.manager;

import it.polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import it.polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import it.polimi.deib.csparql_rest_api.exception.QueryErrorException;
import it.polimi.deib.csparql_rest_api.exception.ServerErrorException;
import it.polimi.deib.csparql_rest_api.exception.StreamErrorException;
import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.KbAPI;
import it.polimi.modaclouds.monitoring.kb.api.RspKbAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.qos_models.Problem;
import it.polimi.tower4clouds.common.net.NetUtil;
import it.polimi.tower4clouds.manager.api.IManagerAPI;
import it.polimi.tower4clouds.manager.api.NotFoundException;
import it.polimi.tower4clouds.manager.api.Observer;
import it.polimi.tower4clouds.manager.api.SocketProtocol;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.MO;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rdf_history_db.api.RdfHistoryDBAPI;
import it.polimi.tower4clouds.rules.AbstractAction;
import it.polimi.tower4clouds.rules.MonitoredTarget;
import it.polimi.tower4clouds.rules.MonitoringRule;
import it.polimi.tower4clouds.rules.MonitoringRules;
import it.polimi.tower4clouds.rules.Parameter;
import it.polimi.tower4clouds.rules.RulesValidator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MonitoringManager implements IManagerAPI {

	private static final int MAX_KEEP_ALIVE = Integer.MAX_VALUE / 1000;
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private final ScheduledExecutorService keepAliveScheduler;

	private QueryFactory queryFactory;

	private Map<String, String> streamsByMetric = new HashMap<String, String>();
	private Map<String, DCConfiguration> dCConfigByRuleId = new HashMap<String, DCConfiguration>();
	private Map<String, MonitoringRule> rulesByRuleId = new HashMap<String, MonitoringRule>();
	private Map<String, String> queryIdByRuleId = new HashMap<String, String>();
	private Map<String, Query> queryByQueryId = new HashMap<String, Query>();
	private Map<String, String> ruleIdByObservableMetric = new HashMap<String, String>();
	private Map<String, Set<String>> inputMetricsByRuleId = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> rulesIdsByInputMetric = new HashMap<String, Set<String>>();

	private Map<String, Set<String>> observersIdsByMetric = new HashMap<String, Set<String>>();
	private Map<String, Observer> observersById = new HashMap<String, Observer>();

	private final int keepAliveCheckPeriod = 60;

	private Map<String, DCDescriptor> registeredDCs;
	private Map<String, Integer> dcsKeepAlive;
	private Map<String, Long> dcsKATimestamp;
	private Map<String, Resource> registeredResources;
	private Map<String, Integer> resourcesKeepAlive;
	private Map<String, Long> resourcesKATimestamp;

	private final Object dcAndResourcesLock = new Object();

	private RulesValidator validator;

	private KbAPI knowledgeBase;
	private RSP_services_csparql_API dataAnalyzer;
	private RdfHistoryDBAPI rdfHistoryDB;

	private ManagerConfig config;

	private ExecutorService actionsExecutor;

	public MonitoringManager(ManagerConfig config) throws Exception {
		this.config = config;
		validator = new RulesValidator();

		RspKbAPI.KB_NAME_SPACE = MO.URI;
		RspKbAPI.KB_NS_PREFIX = MO.prefix;

		knowledgeBase = new RspKbAPI(config.getDaUrl());
		dataAnalyzer = new RSP_services_csparql_API(config.getDaUrl());

		logger.info("Checking if Data Analyzer is reachable");
		NetUtil.waitForResponseCode(config.getDaUrl() + "/queries", 200,
				MAX_KEEP_ALIVE, 5000);
		logger.info("Resetting KB");
		knowledgeBase.clearAll();
		logger.info("Resetting DA");
		resetDA();

		registeredDCs = new ConcurrentHashMap<String, DCDescriptor>();
		dcsKeepAlive = new ConcurrentHashMap<String, Integer>();
		dcsKATimestamp = new ConcurrentHashMap<String, Long>();
		registeredResources = new ConcurrentHashMap<String, Resource>();
		resourcesKeepAlive = new ConcurrentHashMap<String, Integer>();
		resourcesKATimestamp = new ConcurrentHashMap<String, Long>();

		queryFactory = new QueryFactory();

		actionsExecutor = Executors.newFixedThreadPool(1);
		keepAliveScheduler = Executors.newScheduledThreadPool(1);
		keepAliveScheduler.scheduleAtFixedRate(new keepAliveChecker(), 0,
				keepAliveCheckPeriod, TimeUnit.SECONDS);

		logger.info("Uploading ontology to KB");
		knowledgeBase.putModel(MO.model, ManagerConfig.MODEL_GRAPH_NAME);

		if (config.getRdfHistoryDbIP() != null) {
			rdfHistoryDB = new RdfHistoryDBAPI(config.getRdfHistoryDbIP(),
					config.getRdfHistoryDbPort());
			rdfHistoryDB.setAsync(true);
		}
		// initSelfMonitoring();
	}

	// private void initSelfMonitoring() {
	// Map<Property, String> applicationProperties = new HashMap<Property,
	// String>();
	// applicationProperties.put(Property.ID, "Manager");
	// applicationProperties.put(Property.TYPE, "Tower4Clouds");
	// Registry.initialize("localhost", config.getMmPort(),
	// applicationProperties, getClass().getPackage().getName());
	// Registry.startMonitoring();
	// }

	public synchronized void resetDA() {
		try {
			JsonParser parser = new JsonParser();
			JsonArray jsonQueriesInfoArray = parser.parse(
					dataAnalyzer.getQueriesInfo()).getAsJsonArray();
			for (JsonElement jsonElement : jsonQueriesInfoArray) {
				JsonObject queryInfoJson = jsonElement.getAsJsonObject();
				String queryId = queryInfoJson.get("id").getAsString();
				dataAnalyzer.unregisterQuery(prepareQueryURI(queryId));
			}
			JsonArray jsonStreamsInfoArray = parser.parse(
					dataAnalyzer.getStreamsInfo()).getAsJsonArray();
			for (JsonElement jsonElement : jsonStreamsInfoArray) {
				String streamId = jsonElement.getAsJsonObject()
						.get("streamIRI").getAsString();
				dataAnalyzer.unregisterStream(streamId);
			}
		} catch (Exception e) {
			logger.error("An error occurred while trying to clear the DDA", e);
		}
	}

	@Override
	public synchronized void installRules(MonitoringRules rules)
			throws RuleInstallationException {
		logger.info("{} rule(s) to install", rules.getMonitoringRules().size());
		logger.info("Validating rules");
		validate(rules);
		String installedRules = "";
		try {
			for (MonitoringRule rule : rules.getMonitoringRules()) {
				installRule(rule);
				installedRules += " " + rule.getId();
			}
		} catch (RuleInstallationException e) {
			throw new RuleInstallationException(
					"Error while installing rules, only the following rules were successfully installed:"
							+ installedRules + ". Problems: " + e.getMessage(),
					e);
		}
	}

	private void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		logger.info("Installing rule {}", rule.getId());
		try {
			Query query = queryFactory.prepareQuery(rule);

			queryIdByRuleId.put(rule.getId(), query.getId());

			Set<String> inputMetrics = query.getRequiredMetrics();
			logger.debug("Input metrics: {}", inputMetrics.toString());
			for (String inMetric : inputMetrics) {
				DCConfiguration newDCConfig = prepareDCConfig(rule, inMetric);
				if (!streamsByMetric.containsKey(inMetric)) {
					String newStream = prepareStreamURI(inMetric);
					logger.debug("Registering stream {}", inMetric, newStream,
							newDCConfig);
					dataAnalyzer.registerStream(newStream);
					streamsByMetric.put(inMetric, newStream);
				}
				addInputMetric(inMetric, rule.getId());
				synchronized (dcAndResourcesLock) {
					dCConfigByRuleId.put(rule.getId(), newDCConfig);
					logger.debug("Saving DC configuration: {}", newDCConfig);
				}
				String inputStream = streamsByMetric.get(inMetric);
				logger.debug("Adding input stream {} to query", inputStream);
				query.addInputStreamURI(inputStream);
			}
			String csparqlQuery = query.build();
			logger.debug("Installing query:\n{}", csparqlQuery);
			dataAnalyzer.registerQuery(query.getId(), csparqlQuery);
			queryByQueryId.put(query.getId(), query);
			if (query.hasOutputMetric()) {
				String outputMetric = query.getOutputMetric();
				logger.debug("Output metric: {}", outputMetric);
				if (streamsByMetric.containsKey(outputMetric)) {
					String message = "The metric " + outputMetric
							+ " is alread produced by ";
					if (ruleIdByObservableMetric.containsKey(outputMetric)) {
						message += "rule "
								+ ruleIdByObservableMetric.get(outputMetric);
					} else {
						message += "some data collector according to rules "
								+ getRulesIdsFromInputMetric(outputMetric)
										.toString();
					}
					throw new RuleInstallationException(message);
				}
				String outputStreamURI = prepareStreamURI(query.getId());
				streamsByMetric.put(outputMetric, outputStreamURI);
				ruleIdByObservableMetric.put(outputMetric, rule.getId());
			}
			if (query.hasActions()) {
				dataAnalyzer.addHttpObserver(prepareQueryURI(query.getId()),
						"http://" + config.getMmIP() + ":" + config.getMmPort()
								+ "/v1/monitoring-rules/" + rule.getId()
								+ "/actions", "TOWER/JSON");
			}
			rulesByRuleId.put(rule.getId(), rule);
		} catch (Exception e) {
			logger.error("Error while installing rule {}, rolling back",
					rule.getId(), e);
			try {
				cleanUpRule(rule.getId());
			} catch (IOException e1) {
				throw new RuleInstallationException(
						"Something went wrong while rolling back the installation of rule "
								+ rule.getId()
								+ ", try uninstalling and reinstalling the rule",
						e);
			}
			throw new RuleInstallationException(e);
		}
	}

	private void addInputMetric(String inMetric, String ruleId) {
		Set<String> iMetrics = inputMetricsByRuleId.get(ruleId);
		if (iMetrics == null) {
			iMetrics = new HashSet<String>();
			inputMetricsByRuleId.put(ruleId, iMetrics);
		}
		iMetrics.add(inMetric);

		Set<String> rulesIds = rulesIdsByInputMetric.get(inMetric);
		if (rulesIds == null) {
			rulesIds = new HashSet<String>();
			rulesIdsByInputMetric.put(inMetric, rulesIds);
		}
		rulesIds.add(ruleId);
	}

	private Set<String> getRulesIdsFromInputMetric(String iMetric) {
		HashSet<String> ids = new HashSet<String>();
		for (Query query : queryByQueryId.values()) {
			if (query.getRequiredMetrics().contains(iMetric)) {
				ids.add(query.getRuleId());
			}
		}
		return ids;
	}

	// private String getRuleIdFromQueryId(String queryId) {
	// for (Entry<String, Set<String>> entry : queryByRuleId.entrySet()) {
	// if (entry.getValue().contains(queryId))
	// return entry.getKey();
	// }
	// return null;
	// }

	// private void addQueryIdByRuleId(String ruleId, String queryId) {
	// Set<String> queriesIds = queryByRuleId.get(ruleId);
	// if (queriesIds == null) {
	// queriesIds = new HashSet<String>();
	// queryByRuleId.put(ruleId, queriesIds);
	// }
	// queriesIds.add(queryId);
	// }

	private DCConfiguration prepareDCConfig(MonitoringRule rule, String metric) {
		// TODO currently only one collected metric is allowed
		DCConfiguration dc = new DCConfiguration();
		for (Parameter parameter : rule.getCollectedMetric().getParameters()) {
			dc.addParameter(parameter.getName(), parameter.getValue());
		}
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			dc.addTargetResource(target.getClazz(), target.getType(),
					target.getId());
		}
		try {
			dc.setDaUrl(config.getDaUrl() + "/streams/"
					+ URLEncoder.encode(prepareStreamURI(metric), "UTF-8"));
			dc.setDataFormat("TOWER/JSON");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return dc;
	}

	private String prepareStreamURI(String metric) {
		return "http://www.modaclouds.eu/streams/" + metric;
	}

	private String prepareQueryURI(String queryId) {
		return config.getDaUrl() + "/queries/" + queryId;
	}

	private void validate(MonitoringRules rules)
			throws RuleInstallationException {
		Set<Problem> problems = new HashSet<Problem>();
		List<MonitoringRule> otherRules = new ArrayList<MonitoringRule>(
				rulesByRuleId.values());
		otherRules.addAll(rules.getMonitoringRules());
		MonitoringRule previousRule = null;
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			if (previousRule != null)
				otherRules.add(previousRule);
			otherRules.remove(rule);
			problems.addAll(validator.validateRule(rule, otherRules));
			previousRule = rule;
		}
		if (!problems.isEmpty()) {
			String message = "Rules could not be installed. Problems:";
			for (Problem p : problems) {
				message += " [Rule "
						+ p.getId()
						+ ", error: "
						+ p.getError()
						+ " at position "
						+ p.getTagName()
						+ (p.getDescription() != null ? ", details: "
								+ p.getDescription() : "") + "]";
			}
			throw new RuleInstallationException(message);
		}
	}

	@Override
	public synchronized void uninstallRule(String ruleId)
			throws NotFoundException, IOException {
		logger.info("Uninstalling rule {}", ruleId);
		if (!rulesByRuleId.containsKey(ruleId)) {
			throw new NotFoundException(ruleId);
		}
		cleanUpRule(ruleId);
		rulesByRuleId.remove(ruleId);
	}

	private void cleanUpRule(String ruleId) throws IOException {
		String queryId = queryIdByRuleId.get(ruleId);
		if (queryId == null)
			return;
		try {
			if (queryByQueryId.containsKey(queryId)) {
				String queryURI = prepareQueryURI(queryId);
				logger.debug("Uninstalling query {}", queryId);
				dataAnalyzer.unregisterQuery(queryURI);
				Query query = queryByQueryId.get(queryId);
				if (query.hasOutputMetric()) {
					ruleIdByObservableMetric.remove(query.getOutputMetric());
					streamsByMetric.remove(query.getOutputMetric());
					Set<String> observers = observersIdsByMetric.remove(query
							.getOutputMetric());
					if (observers != null) {
						for (String obsId : observers) {
							observersById.remove(obsId);
						}
					}
				}
				queryByQueryId.remove(queryId);
			}
			Set<String> inputMetrics = inputMetricsByRuleId.get(ruleId);
			if (inputMetrics != null) {
				for (String metric : inputMetrics) {
					rulesIdsByInputMetric.get(metric).remove(ruleId);
					if (rulesIdsByInputMetric.get(metric).isEmpty())
						rulesIdsByInputMetric.remove(metric);
					if (!ruleIdByObservableMetric.containsKey(metric)
							&& rulesIdsByInputMetric.get(metric) == null) {
						if (streamsByMetric.containsKey(metric)) {
							dataAnalyzer
									.unregisterStream(prepareStreamURI(metric));
							streamsByMetric.remove(metric);
						}
					}
				}
				inputMetricsByRuleId.remove(ruleId);
				synchronized (dcAndResourcesLock) {
					dCConfigByRuleId.remove(ruleId);
				}
			}
		} catch (ServerErrorException e) {
			throw new IOException(e);
		} catch (QueryErrorException | StreamErrorException e) {
			throw new RuntimeException(e);
		}
		queryIdByRuleId.remove(ruleId);
	}

	@Override
	public void enableRule(String id) throws NotFoundException, IOException {
		// TODO using startEnabled field, should be renamed to enabled
		logger.info("Enabling rule {}", id);
		MonitoringRule rule = rulesByRuleId.get(id);
		if (rule == null)
			throw new NotFoundException(id);
		if (rule.isStartEnabled())
			return;
		String queryId = queryIdByRuleId.get(id);
		if (queryId == null) {
			throw new RuntimeException(
					"No query found related to installed rule " + id);
		}
		String queryURI = prepareQueryURI(queryId);
		logger.debug("Enabling query {}", queryURI);
		try {
			dataAnalyzer.restartQuery(queryURI);
		} catch (Exception e) {
			throw new IOException(e);
		}
		rule.setStartEnabled(true);
	}

	@Override
	public void disableRule(String id) throws NotFoundException, IOException {
		logger.info("Disabling rule {}", id);
		MonitoringRule rule = rulesByRuleId.get(id);
		if (rule == null)
			throw new NotFoundException(id);
		if (!rule.isStartEnabled())
			return;
		String queryId = queryIdByRuleId.get(id);
		if (queryId == null) {
			throw new RuntimeException(
					"No query found related to installed rule " + id);
		}
		String queryURI = prepareQueryURI(queryId);
		logger.debug("Disabling query {}", queryURI);
		try {
			dataAnalyzer.pauseQuery(queryURI);
		} catch (Exception e) {
			throw new IOException(e);
		}
		rule.setStartEnabled(false);
	}

	// private boolean noOtherRuleIsUsingIt(String ruleId, String metric) {
	// HashMap<String, Set<String>> copy = new HashMap<String, Set<String>>(
	// inputMetricsByRuleId);
	// copy.remove(ruleId);
	// return !copy.containsValue(metric);
	// }

	// public synchronized DCConfiguration getDCConfig(String metric) {
	// dcLock.lock();
	// DCConfiguration dcConfiguration;
	// try {
	// dcConfiguration = dCConfigByMetric.get(metric);
	// } finally {
	// dcLock.unlock();
	// }
	// return dcConfiguration;
	// }

	public synchronized MonitoringRule getMonitoringRule(String ruleId)
			throws NotFoundException {
		if (!rulesByRuleId.containsKey(ruleId)) {
			throw new NotFoundException(ruleId);
		}
		return rulesByRuleId.get(ruleId);
	}

	public synchronized Set<String> getObservableMetrics() {
		return ruleIdByObservableMetric.keySet();
	}

	@Override
	public synchronized MonitoringRules getRules() {
		MonitoringRules rules = new MonitoringRules();
		rules.getMonitoringRules().addAll(rulesByRuleId.values());
		return rules;
	}

	@Override
	public synchronized Observer registerHttpObserver(String metric,
			String callbackUrl, String format) throws NotFoundException,
			IOException {
		logger.info(
				"Adding http observer with callbackURL {} to metric {} using format {}",
				callbackUrl, metric, format);
		if (!ruleIdByObservableMetric.containsKey(metric)) {
			throw new NotFoundException(metric);
		}
		String ruleId = ruleIdByObservableMetric.get(metric);
		String queryUri = prepareQueryURI(queryIdByRuleId.get(ruleId));
		String returnedObserverUri;
		try {
			returnedObserverUri = dataAnalyzer.addHttpObserver(queryUri,
					new URL(callbackUrl).toString(), format);
		} catch (ServerErrorException e) {
			throw new IOException();
		} catch (ObserverErrorException e) {
			throw new RuntimeException();
		}
		String observerId = returnedObserverUri.substring(returnedObserverUri
				.lastIndexOf("/") + 1);

		Observer observer = observersById.get(observerId);
		if (observer == null) {
			observer = new Observer(observerId, callbackUrl, "HTTP", format);
			observersById.put(observerId, observer);
		}
		observer.addObservedQueryUri(queryUri);

		Set<String> observersIds = observersIdsByMetric.get(metric);
		if (observersIds == null) {
			observersIds = new HashSet<String>();
			observersIdsByMetric.put(metric, observersIds);
		}
		observersIds.add(observer.getId());

		return observer;
	}

	@Override
	public synchronized Observer registerSocketObserver(String metric,
			String observerHost, int observerPort, SocketProtocol protocol,
			String format) throws NotFoundException, IOException {
		logger.info(
				"Adding socket observer with host {} and port {} to metric {} using protocol {} and format {}",
				observerHost, observerPort, metric, protocol, format);
		if (!ruleIdByObservableMetric.containsKey(metric)) {
			throw new NotFoundException(metric);
		}
		String ruleId = ruleIdByObservableMetric.get(metric);
		String queryUri = prepareQueryURI(queryIdByRuleId.get(ruleId));
		String returnedObserverUri;
		try {
			returnedObserverUri = dataAnalyzer.addSocketObserver(queryUri,
					observerHost, observerPort, protocol.name(), format);
		} catch (ServerErrorException e) {
			throw new IOException();
		} catch (ObserverErrorException e) {
			throw new RuntimeException(e);
		}
		String observerId = returnedObserverUri.substring(returnedObserverUri
				.lastIndexOf("/") + 1);

		Observer observer = observersById.get(observerId);
		if (observer == null) {
			observer = new Observer(observerId, observerHost, observerPort,
					protocol.name(), format);
			observersById.put(observerId, observer);
		}
		observer.addObservedQueryUri(queryUri);

		Set<String> observersIds = observersIdsByMetric.get(metric);
		if (observersIds == null) {
			observersIds = new HashSet<String>();
			observersIdsByMetric.put(metric, observersIds);
		}
		observersIds.add(observer.getId());

		return observer;
	}

	public synchronized Set<Observer> getObservers(String metric)
			throws NotFoundException {
		if (!ruleIdByObservableMetric.containsKey(metric)) {
			throw new NotFoundException(metric);
		}
		Set<String> observersIds = observersIdsByMetric.get(metric);
		HashSet<Observer> observers = new HashSet<Observer>();
		if (observersIds != null) {
			for (String id : observersIds) {
				observers.add(observersById.get(id));
			}
		}
		return observers;
	}

	public synchronized void unregisterObserver(String metric, String observerId)
			throws NotFoundException, IOException {
		logger.info("Removing observer {} from metric {}", observerId, metric);
		if (!ruleIdByObservableMetric.containsKey(metric)) {
			throw new NotFoundException(metric);
		}
		if (!isMetricObservedBy(metric, observerId)) {
			throw new NotFoundException(observerId);
		}
		try {
			String ruleId = ruleIdByObservableMetric.get(metric);
			String queryId = queryIdByRuleId.get(ruleId);
			String queryUri = prepareQueryURI(queryId);
			dataAnalyzer.deleteObserver(queryUri + "/observers/" + observerId);
			Observer observer = observersById.get(observerId);
			Set<String> observerdQueriesUris = observer
					.getObserverdQueriesUris();
			observerdQueriesUris.remove(queryUri);
			if (observerdQueriesUris.isEmpty())
				observersById.remove(observerId);
			observersIdsByMetric.get(metric).remove(observerId);
		} catch (ServerErrorException e) {
			throw new IOException();
		} catch (ObserverErrorException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isMetricObservedBy(String metric, String observerId) {
		return observersIdsByMetric.get(metric) != null
				&& observersIdsByMetric.get(metric).contains(observerId);
	}

	@Override
	public void deleteResource(String resourceId) throws IOException,
			NotFoundException {
		logger.info("Deleting resource {} from KB", resourceId);
		synchronized (dcAndResourcesLock) {
			if (!registeredResources.containsKey(resourceId)) {
				throw new NotFoundException(resourceId);
			}
			knowledgeBase.deleteEntitiesByPropertyValue(resourceId,
					MOVocabulary.idParameterName,
					ManagerConfig.MODEL_GRAPH_NAME);
			registeredResources.remove(resourceId);
			resourcesKeepAlive.remove(resourceId);
			resourcesKATimestamp.remove(resourceId);
		}
		if (rdfHistoryDB != null) {
			try {
				logger.info("Sending udpate to RDF History DB");
				rdfHistoryDB.deleteResource(resourceId);
			} catch (Exception e) {
				logger.error(
						"Error while sending udpate to RDF History DB: {}",
						e.getMessage());
			}
		}
	}

	public void replaceResources(Set<Resource> resources) throws IOException,
			SerializationException {
		logger.info("Clearing existing model if any");
		synchronized (dcAndResourcesLock) {
			knowledgeBase.clearGraph(ManagerConfig.MODEL_GRAPH_NAME);
			registeredResources.clear();
			resourcesKeepAlive.clear();
			resourcesKATimestamp.clear();
			long timestamp = System.currentTimeMillis();
			logger.info("Registering {} new resources", resources.size());
			knowledgeBase.addMany(resources, MOVocabulary.idParameterName,
					ManagerConfig.MODEL_GRAPH_NAME);
			for (Resource resource : resources) {
				registeredResources.put(resource.getId(), resource);
				resourcesKATimestamp.put(resource.getId(), timestamp);
				resourcesKeepAlive.put(resource.getId(), MAX_KEEP_ALIVE);
			}
		}
		if (rdfHistoryDB != null) {
			try {
				logger.info("Sending udpate to RDF History DB");
				rdfHistoryDB.replaceResources(resources);
			} catch (Exception e) {
				logger.error(
						"Error while sending udpate to RDF History DB: {}",
						e.getMessage());
			}
		}
	}

	public Resource getResource(String id) throws NotFoundException {
		synchronized (dcAndResourcesLock) {
			if (!registeredResources.containsKey(id))
				throw new NotFoundException(id);
			return registeredResources.get(id);
		}
	}

	@Override
	public Collection<Resource> getResources() throws DeserializationException,
			IOException {
		synchronized (dcAndResourcesLock) {
			return registeredResources.values();
		}
	}

	@Override
	public String registerDataCollector(DCDescriptor dCDescriptor)
			throws SerializationException, IOException {
		String dcId = UUID.randomUUID().toString();
		registerDataCollector(dcId, dCDescriptor);
		return dcId;
	}

	public void addResources(Set<Resource> resources)
			throws SerializationException, IOException {
		synchronized (dcAndResourcesLock) {
			long timestamp = System.currentTimeMillis();
			logger.info("Registering {} new resources", resources.size());
			updateExistingRelations(resources);
			knowledgeBase.addMany(resources, MOVocabulary.idParameterName,
					ManagerConfig.MODEL_GRAPH_NAME);
			for (Resource resource : resources) {
				registeredResources.put(resource.getId(), resource);
				resourcesKATimestamp.put(resource.getId(), timestamp);
				resourcesKeepAlive.put(resource.getId(), MAX_KEEP_ALIVE);
			}
		}
		if (rdfHistoryDB != null) {
			try {
				logger.info("Sending udpate to RDF History DB");
				rdfHistoryDB.addResources(resources);
			} catch (Exception e) {
				logger.error(
						"Error while sending udpate to RDF History DB: {}",
						e.getMessage());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateExistingRelations(Set<Resource> resources) {
		try {
			Map<String, Object> properties;
			for (Resource resource : resources) {
				if (registeredResources.containsKey(resource.getId())) {
					Resource registeredResource = registeredResources
							.get(resource.getId());
					properties = PropertyUtils.describe(resource);
					for (Entry<String, Object> property : properties.entrySet()) {
						if (property.getValue() instanceof Set) {
							((Set<Resource>) property.getValue())
									.addAll((Set<? extends Resource>) PropertyUtils
											.getProperty(registeredResource,
													property.getKey()));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// @Monitor(type = "registerDataCollector")
	public void registerDataCollector(String dcId, DCDescriptor dCDescriptor)
			throws SerializationException, IOException {
		// TODO validate dc descriptor!!!
		Set<Resource> resources;
		synchronized (dcAndResourcesLock) {
			long timestamp = System.currentTimeMillis();
			logger.debug("Registering DC: {}", dCDescriptor);
			resources = dCDescriptor.getResources();
			if (resources != null && !resources.isEmpty()) {
				updateExistingRelations(resources);
				logger.info("Adding {} to KB", resources);
				knowledgeBase.addMany(resources, MOVocabulary.idParameterName,
						ManagerConfig.MODEL_GRAPH_NAME);
				logger.info("{} added to KB", resources);
			}
			registeredDCs.put(dcId, dCDescriptor);
			int keepAlive = dCDescriptor.getKeepAlive();
			if (keepAlive <= 0) {
				keepAlive = MAX_KEEP_ALIVE;
			}
			dcsKeepAlive.put(dcId, keepAlive);
			dcsKATimestamp.put(dcId, timestamp);
			for (Resource resource : resources) {
				registeredResources.put(resource.getId(), resource);
				Integer currentKeepAlive = resourcesKeepAlive.get(resource
						.getId());
				if (currentKeepAlive == null || currentKeepAlive < keepAlive) {
					resourcesKeepAlive.put(resource.getId(), keepAlive);
				}
				resourcesKATimestamp.put(resource.getId(), timestamp);
			}
		}
		if (rdfHistoryDB != null) {
			try {
				logger.info("Sending udpate to RDF History DB");
				rdfHistoryDB.addResources(resources);
			} catch (Exception e) {
				logger.error(
						"Error while sending udpate to RDF History DB: {}",
						e.getMessage());
			}
		}
	}

	@Override
	public void unregisterDataCollector(String dcId) throws NotFoundException {
		synchronized (dcAndResourcesLock) {
			DCDescriptor dCDescriptor = registeredDCs.get(dcId);
			if (dCDescriptor == null) {
				throw new NotFoundException(dcId);
			}
			logger.debug("Unregistering DC: {}", dcId);
			registeredDCs.remove(dcId);
			dcsKeepAlive.remove(dcId);
			dcsKATimestamp.remove(dcId);
		}
	}

	// public void keepAlive(String dcId) throws NotFoundException,
	// SerializationException, IOException {
	// dcLock.lock();
	// try {
	// DCDescriptor dCDescriptor = registeredDCs.get(dcId);
	// if (dCDescriptor == null) {
	// throw new NotFoundException(dcId);
	// }
	// logger.info("Keep alive requested by DC {}", dcId);
	// Set<Resource> resources = dCDescriptor.getMonitoredResources();
	// int keepAlive = dCDescriptor.getKeepAlive();
	// if (keepAlive <= 0) {
	// keepAlive = Integer.MAX_VALUE;
	// }
	// for (Resource resource : resources) {
	// if (!registeredResources.containsKey(resource.getId())) {
	// knowledgeBase.add(resource, MOVocabulary.idParameterName,
	// ManagerConfig.MODEL_GRAPH_NAME);
	// registeredResources.put(resource.getId(), resource);
	// resourcesKeepAlive.put(resource.getId(), keepAlive);
	// } else {
	// int newKeepAlive = Math.max(keepAlive,
	// resourcesKeepAlive.get(resource.getId()));
	// resourcesKeepAlive.put(resource.getId(), newKeepAlive);
	// }
	// }
	// dcsKeepAlive.put(dcId, keepAlive);
	// } finally {
	// dcLock.unlock();
	// }
	// }

	// @Monitor(type = "keepAlive")
	@Override
	public void keepAlive(String dcId) throws NotFoundException,
			SerializationException, IOException {
		synchronized (dcAndResourcesLock) {
			long timestamp = System.currentTimeMillis();
			DCDescriptor dCDescriptor = registeredDCs.get(dcId);
			if (dCDescriptor == null) {
				throw new NotFoundException(dcId);
			}
			logger.debug("Keep alive requested by DC {}", dcId);
			Set<Resource> resources = dCDescriptor.getResources();
			int keepAlive = dCDescriptor.getKeepAlive();
			if (keepAlive <= 0) {
				keepAlive = MAX_KEEP_ALIVE;
			}
			for (Resource resource : resources) {
				if (!registeredResources.containsKey(resource.getId())) {
					logger.info("Restoring resource {}", resource.getId());
					knowledgeBase.add(resource, MOVocabulary.idParameterName,
							ManagerConfig.MODEL_GRAPH_NAME);
					registeredResources.put(resource.getId(), resource);
					resourcesKeepAlive.put(resource.getId(), keepAlive);
					if (rdfHistoryDB != null) {
						try {
							logger.info("Sending udpate to RDF History DB");
							rdfHistoryDB.addResource(resource);
						} catch (Exception e) {
							logger.error(
									"Error while sending udpate to RDF History DB: {}",
									e.getMessage());
						}
					}
				}
				resourcesKATimestamp.put(resource.getId(), timestamp);
			}
			dcsKATimestamp.put(dcId, timestamp);
		}
	}

	@Override
	public Map<String, DCDescriptor> getRegisteredDataCollectors() {
		synchronized (dcAndResourcesLock) {
			return registeredDCs;
		}
	}

	public DCDescriptor getRegisteredDC(String id) throws NotFoundException {
		DCDescriptor dc;
		synchronized (dcAndResourcesLock) {
			dc = registeredDCs.get(id);
			if (dc == null) {
				throw new NotFoundException(id);
			}
		}
		return dc;
	}

	// @Monitor(type = "getDCConfigurationByMetric")
	@Override
	public Map<String, Set<DCConfiguration>> getDCConfigurationsByMetric(
			String dcId) throws NotFoundException, SerializationException,
			IOException {
		Map<String, Set<DCConfiguration>> configs = new HashMap<String, Set<DCConfiguration>>();
		synchronized (dcAndResourcesLock) {
			DCDescriptor dc = registeredDCs.get(dcId);
			if (dc == null) {
				throw new NotFoundException(dcId);
			}
			logger.debug(
					"Configuration requested by DC {}, launching keep alive first",
					dcId);
			keepAlive(dcId);
			String metric;
			Set<Resource> resources;
			DCConfiguration conf;
			for (Entry<String, Set<Resource>> entry : dc
					.getMonitoredResourcesByMetric().entrySet()) {
				metric = entry.getKey();
				resources = entry.getValue();
				Set<String> rulesIds = rulesIdsByInputMetric.get(metric);
				if (rulesIds != null) {
					for (String ruleId : rulesIds) {
						conf = dCConfigByRuleId.get(ruleId);
						if (conf != null) {
							for (Resource resource : resources) {
								if (conf.isAboutResource(resource)) {
									Set<DCConfiguration> dcconfigs = configs
											.get(metric);
									if (dcconfigs == null) {
										dcconfigs = new HashSet<DCConfiguration>();
										configs.put(metric, dcconfigs);
									}
									dcconfigs.add(conf);
									break;
								}
							}
						}
					}
				}
			}
		}
		return configs;
	}

	// public class keepAliveChecker implements Runnable {
	//
	// @Override
	// public void run() {
	// dcLock.lock();
	// try {
	// logger.info("Keep alive checker running");
	// for (String dcId : registeredDCs.keySet()) {
	// int newKeepAlive = dcsKeepAlive.get(dcId)
	// - keepAliveCheckPeriod;
	// if (newKeepAlive < 0) {
	// logger.info("DC {} expired, removing it", dcId);
	// dcsKeepAlive.remove(dcId);
	// registeredDCs.remove(dcId);
	// } else {
	// dcsKeepAlive.put(dcId, newKeepAlive);
	// for (Resource resource : registeredDCs.get(dcId)
	// .getMonitoredResources()) {
	// if (!registeredResources.containsKey(resource
	// .getId())) {
	// try {
	// logger.info("Restoring resource {}",
	// resource.getId());
	// knowledgeBase.add(resource,
	// MOVocabulary.idParameterName,
	// ManagerConfig.MODEL_GRAPH_NAME);
	// registeredResources.put(resource.getId(),
	// resource);
	// resourcesKeepAlive.put(resource.getId(),
	// newKeepAlive);
	// } catch (Exception e) {
	// logger.error(
	// "Could not register resource {}",
	// resource.getId(), e);
	// }
	// }
	// }
	// }
	// }
	// Set<String> resourcesIdsToRemove = new HashSet<String>();
	// for (String resourceId : registeredResources.keySet()) {
	// int newValue = resourcesKeepAlive.get(resourceId)
	// - keepAliveCheckPeriod;
	// if (newValue < 0) {
	// resourcesIdsToRemove.add(resourceId);
	// } else {
	// resourcesKeepAlive.put(resourceId, newValue);
	// }
	// }
	// try {
	// if (!resourcesIdsToRemove.isEmpty()) {
	// logger.info("Resources {} expired, removing them",
	// resourcesIdsToRemove);
	// knowledgeBase.deleteEntitiesByPropertyValues(
	// resourcesIdsToRemove,
	// MOVocabulary.idParameterName,
	// ManagerConfig.MODEL_GRAPH_NAME);
	// for (String resourceId : resourcesIdsToRemove) {
	// resourcesKeepAlive.remove(resourceId);
	// registeredResources.remove(resourceId);
	// }
	// }
	// } catch (Exception e) {
	// logger.error("Could not delete expired resources from kb",
	// e);
	// }
	// } finally {
	// dcLock.unlock();
	// }
	// }
	// }

	public class keepAliveChecker implements Runnable {

		@Override
		public void run() {
			Set<String> resourcesIdsToRemove = new HashSet<String>();
			synchronized (dcAndResourcesLock) {
				long timestamp = System.currentTimeMillis();
				logger.info("Checking for expired resources");
				for (String dcId : registeredDCs.keySet()) {
					if (timestamp - dcsKATimestamp.get(dcId) > dcsKeepAlive
							.get(dcId) * 1000) {
						logger.info("DC {} expired, removing it", dcId);
						dcsKeepAlive.remove(dcId);
						dcsKATimestamp.remove(dcId);
						registeredDCs.remove(dcId);
					} else {
						for (Resource resource : registeredDCs.get(dcId)
								.getResources()) {
							if (!registeredResources.containsKey(resource
									.getId())) {
								try {
									logger.info("Restoring resource {}",
											resource.getId());
									knowledgeBase.add(resource,
											MOVocabulary.idParameterName,
											ManagerConfig.MODEL_GRAPH_NAME);
									registeredResources.put(resource.getId(),
											resource);
									resourcesKeepAlive.put(resource.getId(),
											dcsKeepAlive.get(dcId));
									resourcesKATimestamp.put(resource.getId(),
											dcsKATimestamp.get(dcId));
									if (rdfHistoryDB != null) {
										rdfHistoryDB.addResource(resource);
									}
								} catch (Exception e) {
									logger.error(
											"Could not register resource {}",
											resource.getId(), e);
								}
							}
						}
					}
				}
				for (String resourceId : registeredResources.keySet()) {
					if (timestamp - resourcesKATimestamp.get(resourceId) > resourcesKeepAlive
							.get(resourceId) * 1000) {
						resourcesIdsToRemove.add(resourceId);
					}
				}
				try {
					if (!resourcesIdsToRemove.isEmpty()) {
						logger.info("Resources {} expired, removing them",
								resourcesIdsToRemove);
						knowledgeBase.deleteEntitiesByPropertyValues(
								resourcesIdsToRemove,
								MOVocabulary.idParameterName,
								ManagerConfig.MODEL_GRAPH_NAME);
						for (String resourceId : resourcesIdsToRemove) {
							resourcesKeepAlive.remove(resourceId);
							resourcesKATimestamp.remove(resourceId);
							registeredResources.remove(resourceId);
						}
					}
				} catch (Exception e) {
					logger.error("Could not delete expired resources from kb",
							e);
				}
			}
			if (rdfHistoryDB != null && !resourcesIdsToRemove.isEmpty()) {
				try {
					logger.info("Sending udpate to RDF History DB");
					rdfHistoryDB.deleteResources(resourcesIdsToRemove);
				} catch (Exception e) {
					logger.error(
							"Error while sending udpate to RDF History DB: {}",
							e.getMessage());
				}
			}
		}
	}

	// @Monitor(type = "executeAction")
	public void executeAction(String ruleId, String resourceId, String value,
			String timestamp) {
		logger.debug(
				"Action requested for rule {}, with resourceId {}, value {} and timestamp {}",
				ruleId, resourceId, value, timestamp);
		String queryId = queryIdByRuleId.get(ruleId);
		if (queryId == null) {
			logger.error("No query associated to rule {}", ruleId);
			return;
		}
		Query query = queryByQueryId.get(queryId);
		if (query == null) {
			logger.error("Query {} is not installed", queryId);
			return;
		}
		if (query.hasActions()) {
			List<AbstractAction> actions = query.getActions();
			for (AbstractAction action : actions) {
				actionsExecutor.submit(new ActionRequest(action, resourceId,
						value, timestamp));
			}
		}
	}

	public class ActionRequest implements Runnable {

		private AbstractAction action;
		private String value;
		private String timestamp;
		private String resourceId;

		public ActionRequest(AbstractAction action, String resourceId,
				String value, String timestamp) {
			this.action = action;
			this.resourceId = resourceId;
			this.value = value;
			this.timestamp = timestamp;
		}

		@Override
		public void run() {
			action.execute(resourceId, value, timestamp);
		}
	}

	/**
	 * 
	 * @return the metrics required by one or more installed rule that are not
	 *         provided as output metric of other rules
	 */
	@Override
	public Set<String> getRequiredMetrics() {
		HashSet<String> requiredMetrics = new HashSet<String>(
				streamsByMetric.keySet());
		requiredMetrics.removeAll(ruleIdByObservableMetric.keySet());
		return requiredMetrics;
	}

}
