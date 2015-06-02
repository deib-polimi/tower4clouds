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
package it.polimi.tower4clouds.manager.api;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.common.net.DefaultRestClient;
import it.polimi.tower4clouds.common.net.RestClient;
import it.polimi.tower4clouds.common.net.RestMethod;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

// TODO create common api interface with core
public class ManagerAPI {

	private static final String MONITORING_RULES_PATH = "/monitoring-rules";
	private static final String DATA_COLLECTORS_PATH = "/data-collectors";
	private static final String RESOURCES_PATH = "/resources";
	private static final String METRICS_PATH = "/metrics";
	private static final String OBSERVERS_PATH = "/observers";
	private static final String REQUIRED_METRICS_PATH = "/required-metrics";

	private final RestClient client;
	private final String managerUrl;
	private int timeout = 10000;
	private final Gson gson = new Gson();
	private final JsonParser jsonParser = new JsonParser();

	public ManagerAPI(String managerHost, int managerPort) {
		client = new DefaultRestClient();
		managerUrl = "http://" + managerHost + ":" + managerPort + "/v1";
	}

	public void setDefaultTimeout(int defaultTimeout) {
		this.timeout = defaultTimeout;
	}

	public MonitoringRules getRules()
			throws UnexpectedAnswerFromServerException, IOException {
		String xmlRules = client.execute(RestMethod.GET, managerUrl
				+ MONITORING_RULES_PATH, null, 200, timeout);
		try {
			MonitoringRules rules = XMLHelper.deserialize(
					IOUtils.toInputStream(xmlRules), MonitoringRules.class);
			return rules;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteRule(String id)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.DELETE, managerUrl + MONITORING_RULES_PATH
				+ "/" + id, null, 204, timeout);
	}

	public Map<String, DCDescriptor> getDataCollectors()
			throws UnexpectedAnswerFromServerException, IOException {
		String dcsJson = client.execute(RestMethod.GET, managerUrl
				+ DATA_COLLECTORS_PATH, null, 200, timeout);

		Map<String, DCDescriptor> dcs = gson.fromJson(dcsJson,
				new TypeToken<Map<String, DCConfiguration>>() {
				}.getType());
		return dcs;
	}

	public void unregisterDataCollector(String id)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.DELETE, managerUrl + DATA_COLLECTORS_PATH
				+ "/" + id, null, 204, timeout);
	}

	public Set<Resource> getResources()
			throws UnexpectedAnswerFromServerException, IOException {
		String jsonResources = client.execute(RestMethod.GET, managerUrl
				+ RESOURCES_PATH, null, 200, timeout);
		return Resource.fromJsonResources(jsonResources);
	}

	public void deleteResource(String id)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.DELETE, managerUrl + RESOURCES_PATH + "/"
				+ id, null, 204, timeout);
	}

	public void registerRules(MonitoringRules rules)
			throws UnexpectedAnswerFromServerException, IOException {
		try {
			client.execute(RestMethod.POST, managerUrl + MONITORING_RULES_PATH,
					XMLHelper.serialize(rules), 204, timeout);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public Observer registerHttpObserver(String metric, String callbackUrl,
			String format) throws UnexpectedAnswerFromServerException,
			IOException {
		Observer observer = new Observer();
		observer.setCallbackUrl(callbackUrl);
		observer.setFormat(format);
		observer.setProtocol("HTTP");
		String observerJson = client.execute(RestMethod.POST, managerUrl
				+ METRICS_PATH + "/" + metric + OBSERVERS_PATH,
				gson.toJson(observer), 201, timeout);
		observer = gson.fromJson(observerJson, Observer.class);
		return observer;
	}

	public Observer registerSocketObserver(String metric, String observerHost,
			int observerPort, SocketProtocol protocol, String format)
			throws UnexpectedAnswerFromServerException, IOException {
		Observer observer = new Observer();
		observer.setFormat(format);
		observer.setObserverHost(observerHost);
		observer.setObserverPort(observerPort);
		observer.setProtocol(protocol.name());
		String observerJson = client.execute(RestMethod.POST, managerUrl
				+ METRICS_PATH + "/" + metric + OBSERVERS_PATH,
				gson.toJson(observer), 201, timeout);
		observer = gson.fromJson(observerJson, Observer.class);
		return observer;
	}

	public void registerDataCollector(String id, DCDescriptor dCDescriptor)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.PUT, managerUrl + DATA_COLLECTORS_PATH + "/"
				+ id, dCDescriptor.toJson(), 201, timeout);
	}

	public String registerDataCollector(DCDescriptor dCDescriptor)
			throws UnexpectedAnswerFromServerException, IOException {
		String json = client.execute(RestMethod.POST, managerUrl
				+ DATA_COLLECTORS_PATH, dCDescriptor.toJson(), 201, timeout);
		return jsonParser.parse(json).getAsJsonObject().get("id").getAsString();
	}

	public void keepAlive(String dataCollectorId)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.GET, managerUrl + DATA_COLLECTORS_PATH + "/"
				+ dataCollectorId + "/keepalive", null, 204, timeout);
	}

	public Map<String, DCConfiguration> getDCConfigurationByMetric(
			String dataCollectorId) throws UnexpectedAnswerFromServerException,
			IOException {
		String json = client.execute(RestMethod.GET, managerUrl
				+ DATA_COLLECTORS_PATH + "/" + dataCollectorId
				+ "/configuration", null, 200, timeout);
		try {
			return new Gson().fromJson(json,
					new TypeToken<Map<String, DCConfiguration>>() {
					}.getType());
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String, DCConfiguration>();
		}
	}

	public Set<String> getRequiredMetrics()
			throws UnexpectedAnswerFromServerException, IOException {
		String jsonRequiredMetrics = client.execute(RestMethod.GET, managerUrl
				+ REQUIRED_METRICS_PATH, null, 200, timeout);
		return gson.fromJson(jsonRequiredMetrics, new TypeToken<Set<String>>() {
		}.getType());
	}

	public Set<String> getObservableMetrics()
			throws UnexpectedAnswerFromServerException, IOException {
		String jsonRequiredMetrics = client.execute(RestMethod.GET, managerUrl
				+ METRICS_PATH, null, 200, timeout);
		return gson.fromJson(jsonRequiredMetrics, new TypeToken<Set<String>>() {
		}.getType());
	}
}
