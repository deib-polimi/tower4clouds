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
package it.polimi.tower4clouds.use_cases.fakeapps4testing.javaappdc;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.java_app_dc.ExternalCall;
import it.polimi.tower4clouds.java_app_dc.Monitor;
import it.polimi.tower4clouds.java_app_dc.Property;
import it.polimi.tower4clouds.java_app_dc.Registry;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;

public class JavaEEAppDCTester {

	private static final String graphiteIP = "localhost";
	private static final int graphitePort = 8001;
	private static String managerIP = "localhost";
	private static int managerPort = 8170;

	public static void main(String[] args) throws Exception {
		ManagerAPI manager = new ManagerAPI(managerIP, managerPort);
		manager.installRules(XMLHelper.deserialize(JavaEEAppDCTester.class
				.getResourceAsStream("/rules4JavaAppDCTester.xml"),
				MonitoringRules.class));
		manager.registerHttpObserver("AverageResponseTime", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
		manager.registerHttpObserver("AverageEffectiveResponseTime", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
		manager.registerHttpObserver("AverageThroughput", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
		Map<Property, String> applicationProperties = new HashMap<Property, String>();
		applicationProperties.put(Property.ID, "App1");
		applicationProperties.put(Property.TYPE, "App");
		applicationProperties.put(Property.VM_ID, "Frontend1");
		applicationProperties.put(Property.VM_TYPE, "Frontend");
		applicationProperties.put(Property.CLOUD_PROVIDER_ID, "AWS");
		applicationProperties.put(Property.CLOUD_PROVIDER_TYPE, "IaaS");
		Registry.initialize(managerIP, managerPort, applicationProperties,
				JavaEEAppDCTester.class.getPackage().getName(), false, true);
		Registry.startMonitoring();
		JavaEEAppDCTester app = new JavaEEAppDCTester();
		for (int i = 0; i < 100000; i++) {
			app.login();
		}
	}

	@GET
	public void login() throws InterruptedException {
		Thread.sleep((long) (Math.random() * 1000));
	}
}
