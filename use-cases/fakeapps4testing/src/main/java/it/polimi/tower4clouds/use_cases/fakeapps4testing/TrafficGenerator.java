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
package it.polimi.tower4clouds.use_cases.fakeapps4testing;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.data_collector_library.DCAgent;
import it.polimi.tower4clouds.manager.api.Format;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.manager.api.SocketProtocol;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.CloudProvider;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.model.ontology.VM;
import it.polimi.tower4clouds.rules.MonitoringRule;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class TrafficGenerator {

	private static final String MANAGER_HOST = "localhost";
	private static final int MANAGER_PORT = 8170;
	private static final String GRAPHITE_HOST = "localhost";
	private static final int GRAPHITE_PORT = 2003;
	private static final String INFLUXDB_HOST = "localhost";
	private static final int INFLUXDB_PORT = 8086;
	private static final String INFLUXDB_DBNAME = "influxdb";
	private static final String INFLUXDB_PASSWORD = "root";
	private static final String INFLUXDB_USERNAME = "root";
	private static final String DATA2STDOUT_HOST = "localhost";
	private static final int DATA2STDOUT_PORT = 8000;

	public static final boolean RESET_PLATFORM_FIRST = true;
	private static final boolean ATTACH_GRAPHITE = true;
	private static final boolean ATTACH_INFLUXDB = true;
	private static final boolean ATTACH_DATA2STDOUT_SENDING_NEW_FORMAT = true;
	private static final boolean ATTACH_DATA2STDOUT_SENDING_OLD_RDF_JSON = false;

	private static ManagerAPI managerServer = new ManagerAPI(MANAGER_HOST,
			MANAGER_PORT);

	public static void main(String[] args) {
		try {

			if (RESET_PLATFORM_FIRST)
				resetPlatform();

			DCDescriptor dCDescriptor = new DCDescriptor();
			dCDescriptor.setKeepAlive(30);
			dCDescriptor.setConfigSyncPeriod(15);

			CloudProvider amazon = new CloudProvider();
			amazon.setId("amazon");
			amazon.setType("IaaS");
			dCDescriptor.addRelatedResource(amazon);

			Method method1 = new Method();
			method1.setId("register1");
			method1.setType("register");
			dCDescriptor.addMonitoredResource("ResponseTime", method1);

			InternalComponent ic1 = new InternalComponent();
			ic1.addProvidedMethod(method1.getId());
			ic1.setId("app1");
			ic1.setType("app");
			dCDescriptor.addRelatedResource(ic1);

			VM vm1 = new VM();
			vm1.setId("vm1");
			vm1.setType("vm");
			vm1.setCloudProvider(amazon.getId());
			ic1.addRequiredComponent(vm1.getId());
			dCDescriptor.addRelatedResource(vm1);

			Method method2 = new Method();
			method2.setId("register2");
			method2.setType("register");
			dCDescriptor.addMonitoredResource("ResponseTime", method2);

			InternalComponent ic2 = new InternalComponent();
			ic2.addProvidedMethod(method2.getId());
			ic2.setId("app2");
			ic2.setType("app");
			dCDescriptor.addRelatedResource(ic2);

			VM vm2 = new VM();
			vm2.setId("vm2");
			vm2.setType("vm");
			vm2.setCloudProvider(amazon.getId());
			ic2.addRequiredComponent(vm2.getId());
			dCDescriptor.addRelatedResource(vm2);

			dCDescriptor.addRelatedResource(amazon);

			DCAgent dcAgent = new DCAgent("127.0.0.1", "8170");
			dcAgent.setDCDescriptor(dCDescriptor);

			// register rules
			boolean sent = false;
			while (!sent) { // temp fix, sometimes the server fails to respond
				try {
					MonitoringRules rules = XMLHelper.deserialize(
							getResourceAsStream("rules.xml"),
							MonitoringRules.class);
					managerServer.registerRules(rules);
					sent = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out
							.println("Retrying (you should fix this problem if it happens often!)");
				}
			}

			if (ATTACH_GRAPHITE) {
				managerServer.registerSocketObserver("AverageResponseTime",
						GRAPHITE_HOST, GRAPHITE_PORT, SocketProtocol.TCP,
						Format.GRAPHITE);
				managerServer.registerSocketObserver("ViolatedAvgResponseTime",
						GRAPHITE_HOST, GRAPHITE_PORT, SocketProtocol.TCP,
						Format.GRAPHITE);
			}

			if (ATTACH_INFLUXDB) {
				managerServer
						.registerHttpObserver("AverageResponseTime",
								"http://" + INFLUXDB_HOST + ":" + INFLUXDB_PORT
										+ "/db/" + INFLUXDB_DBNAME
										+ "/series?u=" + INFLUXDB_USERNAME
										+ "&p=" + INFLUXDB_PASSWORD,
								Format.INFLUXDB);
				managerServer
						.registerHttpObserver("ViolatedAvgResponseTime",
								"http://" + INFLUXDB_HOST + ":" + INFLUXDB_PORT
										+ "/db/" + INFLUXDB_DBNAME
										+ "/series?u=" + INFLUXDB_USERNAME
										+ "&p=" + INFLUXDB_PASSWORD,
								Format.INFLUXDB);
			}

			if (ATTACH_DATA2STDOUT_SENDING_NEW_FORMAT) {

				managerServer.registerHttpObserver("AverageResponseTime",
						"http://" + DATA2STDOUT_HOST + ":" + DATA2STDOUT_PORT
								+ "/data", Format.TOWER_JSON);
				managerServer.registerHttpObserver("ViolatedAvgResponseTime",
						"http://" + DATA2STDOUT_HOST + ":" + DATA2STDOUT_PORT
								+ "/data", Format.TOWER_JSON);
			}

			if (ATTACH_DATA2STDOUT_SENDING_OLD_RDF_JSON) {

				managerServer.registerHttpObserver("AverageResponseTime",
						"http://" + DATA2STDOUT_HOST + ":" + DATA2STDOUT_PORT
								+ "/data", Format.RDF_JSON);
				managerServer.registerHttpObserver("ViolatedAvgResponseTime",
						"http://" + DATA2STDOUT_HOST + ":" + DATA2STDOUT_PORT
								+ "/data", Format.RDF_JSON);
			}

			while (!(dcAgent.shouldMonitor(method1, "ResponseTime") && dcAgent
					.shouldMonitor(method2, "ResponseTime"))) {
				Thread.sleep(1000);
			}

			dcAgent.send(method1, "ResponseTime", 2000 * Math.random());
			Thread.sleep(2000);

			for (int i = 0; i < 10000
					&& dcAgent.shouldMonitor(method1, "ResponseTime")
					&& dcAgent.shouldMonitor(method2, "ResponseTime"); i++) {
				dcAgent.send(method1, "ResponseTime", 2000 + 500 * Math.random());
				dcAgent.send(method2, "ResponseTime", 3000 + 500 * Math.random());
				Thread.sleep(100);
			}

			// Stops all threads
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static InputStream getResourceAsStream(String fileName) {
		return TrafficGenerator.class.getClassLoader().getResourceAsStream(
				fileName);
	}

	private static void resetPlatform()
			throws UnexpectedAnswerFromServerException, IOException {

		MonitoringRules rules = managerServer.getRules();

		for (MonitoringRule rule : rules.getMonitoringRules()) {
			managerServer.deleteRule(rule.getId());
		}

		Map<String, DCDescriptor> dcs = managerServer.getDataCollectors();

		for (String dcId : dcs.keySet()) {
			managerServer.unregisterDataCollector(dcId);
		}
		Set<Resource> resources = managerServer.getResources();
		for (Resource res : resources) {
			managerServer.deleteResource(res.getId());
		}
	}
}
