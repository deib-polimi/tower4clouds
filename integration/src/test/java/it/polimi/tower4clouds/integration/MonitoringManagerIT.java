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
package it.polimi.tower4clouds.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import it.polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.common.net.NetUtil;
import it.polimi.tower4clouds.data_analyzer.DAServer;
import it.polimi.tower4clouds.manager.api.Observer;
import it.polimi.tower4clouds.manager.server.MMServer;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.MO;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rules.MonitoringRule;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.RestAssured;

public class MonitoringManagerIT {

	private static final int daPort = NetUtil.findFreePort();
	private static final int mmPort = NetUtil.findFreePort();
	private static Thread mmThread;
	private static Thread daThread;
	private static final String daUrl = "http://localhost:" + daPort;
	private static final String mmUrl = "http://localhost:" + mmPort;

	private RSP_services_csparql_API da;

	@BeforeClass
	public static void setUpBeforeClass() {
		startMonitoringManager();
		startDataAnalyzer();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		stopMonitoringManager();
		stopDataAnalyzer();
	}

	private static void startMonitoringManager() {
		mmThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String[] args = new String[] { "-daport",
						Integer.toString(daPort), "-mmport",
						Integer.toString(mmPort) };
				MMServer.main(args);
			}
		});
		mmThread.start();
	}

	private static void startDataAnalyzer() {
		daThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String[] args = new String[] { "-port",
						Integer.toString(daPort) };
				DAServer.main(args);
			}
		});
		daThread.start();
	}

	private static void stopMonitoringManager() {
		mmThread.interrupt();
	}

	private static void stopDataAnalyzer() {
		daThread.interrupt();
	}

	@Before
	public void setUp() throws Exception {
		NetUtil.waitForResponseCode(mmUrl + "/v1/monitoring-rules", 200, 5,
				5000);
		NetUtil.waitForResponseCode(daUrl + "/queries", 200, 5, 5000);
		resetPlatform();
		RestAssured.urlEncodingEnabled = false;
		da = new RSP_services_csparql_API(daUrl);
	}

	@After
	public void tearDown() throws Exception {
		resetPlatform();
	}

	private void resetPlatform() throws JAXBException, SAXException {
		InputStream response = given()
				.get("http://localhost:" + mmPort + "/v1/monitoring-rules")
				.andReturn().asInputStream();
		MonitoringRules rules = XMLHelper.deserialize(response,
				MonitoringRules.class);
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			given().delete(
					"http://localhost:" + mmPort + "/v1/monitoring-rules/"
							+ rule.getId()).then().statusCode(204);
		}

		String dcsJson = given()
				.get("http://localhost:" + mmPort + "/v1/data-collectors")
				.andReturn().asString();
		Map<String, DCDescriptor> dcs = new Gson().fromJson(dcsJson,
				new TypeToken<Map<String, DCConfiguration>>() {
				}.getType());

		for (String dcId : dcs.keySet()) {
			given().delete(
					"http://localhost:" + mmPort + "/v1/data-collectors/"
							+ dcId).then().statusCode(204);
		}

		String resourcesJson = given()
				.get("http://localhost:" + mmPort + "/v1/resources")
				.andReturn().asString();
		Set<Resource> resources = Resource.fromJsonResources(resourcesJson);
		for (Resource res : resources) {
			given().delete(
					"http://localhost:" + mmPort + "/v1/resources/"
							+ res.getId()).then().statusCode(204);
		}
	}

	@Test
	public void daShouldBeClear() throws Exception {
		JSONAssert.assertEquals("[]", da.getStreamsInfo(), false);
		JSONAssert.assertEquals("[]", da.getQueriesInfo(), false);
	}

	@Test
	public void rulesShouldBeInstalledAndUninstalledCorrectlyThroughREST()
			throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		String jsonMetrics = given().get(mmUrl + "/v1/metrics").asString();
		assertEquals(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.size(), 1);
		assertThat(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.get(0), equalToIgnoringCase("AverageResponseTime"));
		given().delete(mmUrl + "/v1/monitoring-rules/AvgResponseTimeRule")
				.then().assertThat().statusCode(204);
		InputStream emptyMonitoringRulesIS = given().get(
				mmUrl + "/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().isEmpty());
		given().get(mmUrl + "/v1/metrics").then().assertThat()
				.body(equalToIgnoringWhiteSpace("{\"metrics\":[]}"));
		JSONAssert.assertEquals("[]", da.getStreamsInfo(), false);
		JSONAssert.assertEquals("[]", da.getQueriesInfo(), false);
	}

	@Test
	public void atosRulesShouldBeInstalledAndUninstalledCorrectlyThroughREST()
			throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("AtosMonitoringRules.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		given().delete(
				mmUrl
						+ "/v1/monitoring-rules/4d1ad207-6b4d-4aed-bee1-cb0fcb1629dc_79127a7e-c2d9-4463-93de-20e422a2d4be_2468ecf1-18e3-42ef-af4b-a94e3d78d823_b780c3f4-298c-4114-8d86-074b81374b4c_seff")
				.then().assertThat().statusCode(204);
		given().delete(
				mmUrl
						+ "/v1/monitoring-rules/4d1ad207-6b4d-4aed-bee1-cb0fcb1629dc_79127a7e-c2d9-4463-93de-20e422a2d4be_2468ecf1-18e3-42ef-af4b-a94e3d78d823_b780c3f4-298c-4114-8d86-074b81374b4c_seff")
				.then().assertThat().statusCode(404);
	}

	@Test
	public void observersShouldBeAddedAndDeletedThroughREST() throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		String callbackUrl = "http://127.0.0.1/null";
		String format = "TOWER/JSON";
		JsonObject jsonBody = new JsonObject();
		jsonBody.addProperty("callbackUrl", callbackUrl);
		jsonBody.addProperty("format", format);
		Observer registeredObserver = given().body(jsonBody.toString())
				.post(mmUrl + "/v1/metrics/AverageResponseTime/observers")
				.andReturn().body().as(Observer.class);
		String jsonObservers = given().get(
				mmUrl + "/v1/metrics/AverageResponseTime/observers").asString();
		List<Observer> observers = getListFromJsonField(jsonObservers,
				Observer.class, "observers");
		assertEquals(observers.get(0).getId(), registeredObserver.getId());
		assertEquals(observers.get(0).getCallbackUrl(), callbackUrl);
		assertEquals(observers.get(0).getFormat(), format);
		assertEquals(observers.size(), 1);
		given().delete(
				mmUrl + "/v1/metrics/AverageResponseTime/observers/"
						+ registeredObserver.getId()).then().assertThat()
				.statusCode(204);
		given().get(mmUrl + "/v1/metrics/AverageResponseTime/observers").then()
				.assertThat()
				.body(equalToIgnoringWhiteSpace("{\"observers\":[]}"));
	}

	@Test
	public void observersShouldBeDeletedWhenUninstallingRule() throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		String callbackUrl = "http://127.0.0.1/null";
		String format = "TOWER/JSON";
		JsonObject jsonBody = new JsonObject();
		jsonBody.addProperty("callbackUrl", callbackUrl);
		jsonBody.addProperty("format", format);
		given().body(jsonBody.toString()).post(
				mmUrl + "/v1/metrics/AverageResponseTime/observers");
		given().delete(mmUrl + "/v1/monitoring-rules/AvgResponseTimeRule")
				.then().assertThat().statusCode(204);
		given().get(mmUrl + "/v1/metrics/AverageResponseTime/observers").then()
				.assertThat().statusCode(404);
	}

	@Test
	public void rulesWithSameIdShouldNotBeInstalled() throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		given().body(
				IOUtils.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(400);
		InputStream emptyMonitoringRulesIS = given().get(
				mmUrl + "/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().size() == 1);
		String jsonMetrics = given().get(mmUrl + "/v1/metrics").asString();
		assertEquals(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.size(), 1);
		assertThat(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.get(0), equalToIgnoringCase("AverageResponseTime"));
	}

	private <T> List<T> getListFromJsonField(String json, Class<T> clazz,
			String field) {
		JsonArray array = new JsonParser().parse(json).getAsJsonObject()
				.get(field).getAsJsonArray();
		List<T> returnedList = new ArrayList<T>();
		for (JsonElement observer : array) {
			returnedList.add(new Gson().fromJson(observer, clazz));
		}
		return returnedList;
	}

	private InputStream getResourceAsStream(String fileName) {
		return getClass().getClassLoader().getResourceAsStream(fileName);
	}

	// @Test
	// public void restCallRuleShouldSelfDestroy() throws Exception {
	// DCDescriptor dc = new DCDescriptor();
	//
	// Method method = new Method();
	// method.setId("register1");
	// method.setType("register");
	// dc.addMonitoredResource("ResponseTime", method);
	// dc.setKeepAlive(0);
	//
	// String dcId = given().body(new Gson().toJson(dc))
	// .post(mmUrl + "/v1/data-collectors").then().statusCode(200)
	// .extract().path("id");
	// given().body(
	// IOUtils.toString(getResourceAsStream("RestCallRule4SelfDestroy.xml")))
	// .post(mmUrl + "/v1/monitoring-rules").then().assertThat()
	// .statusCode(204);
	//
	// Map<String, DCConfiguration> dcConfigByMetric = new Gson().fromJson(
	// given().get(
	// mmUrl + "/v1/data-collectors/" + dcId
	// + "/configuration").asString(),
	// new TypeToken<Map<String, DCConfiguration>>() {
	// }.getType());
	//
	// DCConfiguration dcconfig = dcConfigByMetric.get("ResponseTime");
	//
	// // TODO temp fix: first datum is ignored by csparql engine
	// given().body(
	// IOUtils.toString(getResourceAsStream("MonitoringDataRT.json")))
	// .post(dcconfig.getDaUrl()).then().assertThat().statusCode(200);
	//
	// // TODO temp fix: some time not to have this datum ignored as well
	// Thread.sleep(1000);
	// given().body(
	// IOUtils.toString(getResourceAsStream("MonitoringDataRT.json")))
	// .post(dcconfig.getDaUrl()).then().assertThat().statusCode(200);
	//
	// // The rule have a 5 seconds period
	// Thread.sleep(10000);
	//
	// InputStream emptyMonitoringRulesIS = given().get(
	// mmUrl + "/v1/monitoring-rules").asInputStream();
	// assertTrue(XMLHelper
	// .deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
	// .getMonitoringRules().isEmpty());
	// }

	@Test
	public void registeringWrongObserverUrlShouldFail() throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post(mmUrl + "/v1/monitoring-rules").then().assertThat()
				.statusCode(204);

		given().body(
				"{\"callbackUrl\":\"localhost/null\",\"format\":\"TOWER/JSON\"}")
				.post(mmUrl + "/v1/metrics/AverageResponseTime/observers")
				.then().assertThat().statusCode(400);
	}

	@Test
	public void correctModelShouldBeUploaded() throws Exception {
		given().body(
				IOUtils.toString(getResourceAsStream("mic-deployment.json")))
				.post(mmUrl + "/v1/resources").then().assertThat()
				.statusCode(204);
	}

}
