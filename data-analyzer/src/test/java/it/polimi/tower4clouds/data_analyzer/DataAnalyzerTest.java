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
package it.polimi.tower4clouds.data_analyzer;

import static org.junit.Assert.*;
import it.polimi.csparqool.CSquery;
import it.polimi.csparqool.graph;
import it.polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import it.polimi.deib.csparql_rest_api.exception.QueryErrorException;
import it.polimi.deib.csparql_rest_api.exception.ServerErrorException;
import it.polimi.deib.csparql_rest_api.exception.StreamErrorException;
import it.polimi.deib.rsp_services_csparql.queries.utilities.CsparqlQueryDescriptionForGet;
import it.polimi.deib.rsp_services_csparql.server.rsp_services_csparql_server;
import it.polimi.deib.rsp_services_csparql.streams.utilities.CsparqlStreamDescriptionForGet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataAnalyzerTest {

	private static int daport;
	private static String daurl;
	private static RSP_services_csparql_API csparqlAPI;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		daport = findFreePort();
		daurl = "http://localhost:" + daport;
		csparqlAPI = new RSP_services_csparql_API(daurl);

		String[] rspArgs = new String[1];
		rspArgs[0] = "test-setup.properties";
		System.setProperty("log4j.configuration", "test-log4j.properties");
		System.setProperty("rsp_server.static_resources.path", new File(
				"target/kb").getAbsolutePath());
		System.setProperty("csparql_server.port", Integer.toString(daport));
		rsp_services_csparql_server.main(rspArgs);
	}

	@Before
	public void setUp() throws Exception {

		List<CsparqlQueryDescriptionForGet> queries = getQueries();
		for (CsparqlQueryDescriptionForGet query : queries) {
			csparqlAPI.unregisterQuery(daurl + "/queries/" + query.getId());
		}

		List<CsparqlStreamDescriptionForGet> streams = getStreams();
		for (CsparqlStreamDescriptionForGet stream : streams) {
			csparqlAPI.unregisterStream(stream.getStream());
		}
	}

	@Test
	public void engineShouldBeEmpty() throws Exception {
		assertTrue(getQueries().isEmpty());
		assertTrue(getStreams().isEmpty());
	}

	@Test
	public void test() throws Exception {
		String queryName = "query1";
		String baseURI = "http://www.example.org#";
		String streamURI = baseURI + "road";
		String graphURI = baseURI + "animals";
		String queryURI = getQueryURI(queryName);

		csparqlAPI.launchUpdateQuery("CREATE SILENT GRAPH <" + graphURI + ">");
		csparqlAPI.registerStream(streamURI);
		String query = CSquery
				.createDefaultQuery(queryName)
				.setNsPrefix("rdf",
						"http://www.w3.org/1999/02/22-rdf-syntax-ns#")
				.setNsPrefix("ex", baseURI)
				.select("?tag")
				.fromStream(streamURI, "5s", "5s")
				.from(graphURI)
				.where(graph.add("?animal", "rdf:type", "ex:Chicken").add(
						"ex:tag", "?tag")).getCSPARQL();
		csparqlAPI.registerQuery(queryName, query);
		csparqlAPI.addHttpObserver(queryURI, "http://localhost:8000/simpleobserver/data", "RDF/JSON");
	}

	private String getQueryURI(String queryName) {
		return daurl + "/queries/" + queryName;
	}

	private static int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			int port = socket.getLocalPort();
			try {
				socket.close();
			} catch (IOException e) {
				// Ignore IOException on close()
			}
			return port;
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		throw new IllegalStateException(
				"Could not find a free TCP/IP port to start the data analyzer on");
	}

	private List<CsparqlQueryDescriptionForGet> getQueries()
			throws ServerErrorException, QueryErrorException {
		Gson jsonParser = new Gson();
		Type listOfQueriesType = new TypeToken<List<CsparqlQueryDescriptionForGet>>() {
		}.getType();
		List<CsparqlQueryDescriptionForGet> queries = jsonParser.fromJson(
				csparqlAPI.getQueriesInfo(), listOfQueriesType);
		return queries;
	}

	private List<CsparqlStreamDescriptionForGet> getStreams()
			throws ServerErrorException, StreamErrorException {
		Gson jsonParser = new Gson();
		Type listOfStremsType = new TypeToken<List<CsparqlStreamDescriptionForGet>>() {
		}.getType();
		List<CsparqlStreamDescriptionForGet> streams = jsonParser.fromJson(
				csparqlAPI.getStreamsInfo(), listOfStremsType);
		return streams;
	}

}
