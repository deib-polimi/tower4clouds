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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.common.net.DefaultRestClient;
import it.polimi.tower4clouds.common.net.RestClient;
import it.polimi.tower4clouds.common.net.RestMethod;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rules.MonitoringRules;

public class ManagerAPI {

	private static final String MONITORING_RULES_PATH = "/monitoring-rules";
	private static final String DATA_COLLECTORS_PATH = "/data-collectors";
	private static final String RESOURCES_PATH = "/resources";
	private static final String METRICS_PATH = "/metrics";
	private static final String OBSERVERS_PATH = "/observers";

	private final RestClient client;
	private final String managerUrl;
	private int defaultTimeout = 10000;
	private final Gson gson = new Gson();

	public ManagerAPI(String managerHost, int managerPort) {
		client = new DefaultRestClient();
		managerUrl = "http://" + managerHost + ":" + managerPort + "/v1";
	}

	public void setDefaultTimeout(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	public MonitoringRules getRules()
			throws UnexpectedAnswerFromServerException, IOException {
		String xmlRules = client.execute(RestMethod.GET, managerUrl
				+ MONITORING_RULES_PATH, null, 200, defaultTimeout);
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
				+ "/" + id, null, 204, defaultTimeout);
	}

	public Map<String, DCDescriptor> getDataCollectors()
			throws UnexpectedAnswerFromServerException, IOException {
		String dcsJson = client.execute(RestMethod.GET, managerUrl
				+ DATA_COLLECTORS_PATH, null, 200, defaultTimeout);

		Map<String, DCDescriptor> dcs = gson.fromJson(dcsJson,
				new TypeToken<Map<String, DCConfiguration>>() {
				}.getType());
		return dcs;
	}

	public void unregisterDataCollector(String id)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.DELETE, managerUrl + DATA_COLLECTORS_PATH
				+ "/" + id, null, 204, defaultTimeout);
	}

	public Set<Resource> getResources()
			throws UnexpectedAnswerFromServerException, IOException {
		String jsonResources = client.execute(RestMethod.GET, managerUrl
				+ RESOURCES_PATH, null, 200, defaultTimeout);
		return Resource.fromJsonResources(jsonResources);
	}

	public void deleteResource(String id)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.DELETE, managerUrl + RESOURCES_PATH + "/"
				+ id, null, 204, defaultTimeout);
	}

	public void registerRules(MonitoringRules rules)
			throws UnexpectedAnswerFromServerException, IOException {
		try {
			client.execute(RestMethod.POST, managerUrl + MONITORING_RULES_PATH,
					XMLHelper.serialize(rules), 204, defaultTimeout);
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
				gson.toJson(observer), 201, defaultTimeout);
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
				gson.toJson(observer), 201, defaultTimeout);
		observer = gson.fromJson(observerJson, Observer.class);
		return observer;
	}

}
