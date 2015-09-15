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
package it.polimi.tower4clouds.rdf_history_db.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains a list of all the default strings and variables.
 * 
 * @author Riccardo B. Desantis
 *
 */
public abstract class Configuration {
	
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	
	public static final int DEFAULT_PORT = 31337;
	public static int PORT = DEFAULT_PORT;
	
	public static final String DEFAULT_PATH = "/monitoringdata";
	public static final String DEFAULT_PATH_MODEL = "/resources";
	
	public static final String DEFAULT_BASEPATH = "http://localhost";
	
	public static final String DEFAULT_QUEUE_IP = "localhost";
	public static final String DEFAULT_QUEUE_PORT = "5672";
	public static String QUEUE_HOST = String.format("%s:%s", DEFAULT_QUEUE_IP, DEFAULT_QUEUE_PORT);
	
	public static final String QUEUE_RESULTS = "hdb_results";
	public static final String QUEUE_MODELS = "hdb_models";
	public static final String QUEUE_DELTA_MODELS = "hdb_delta_models";
	public static final String QUEUE_MODELS_DELETE = "hdb_models_delete";
	
	public static final String DEFAULT_FUSEKI_IP = "localhost";
	public static final String DEFAULT_FUSEKI_PORT = "3030";
	public static final String DEFAULT_FUSEKI_PATH = "/ds";
	public static String FUSEKI_HOST = String.format("http://%s:%s%s", DEFAULT_FUSEKI_IP, DEFAULT_FUSEKI_PORT, DEFAULT_FUSEKI_PATH);
	
	public static final String FUSEKI_BASEURI = "http://www.modaclouds.eu/historydb/";
	public static final String FUSEKI_MONITORING = FUSEKI_BASEURI + "monitoring-data/";
	public static final String FUSEKI_MODEL = FUSEKI_BASEURI + "model/";
	public static final String FUSEKI_MODEL_DAILY = FUSEKI_BASEURI + "meta/model/";
	public static final String FUSEKI_DELTAS_MODEL = FUSEKI_BASEURI + "model/updates/";
	public static final String FUSEKI_DELTAS_MODEL_DAILY = FUSEKI_BASEURI + "meta/model/updates/";
	public static final String FUSEKI_MODELS_DELETE = FUSEKI_BASEURI + "model/cancellations/";
	public static final String FUSEKI_MODELS_DELETE_DAILY = FUSEKI_BASEURI + "meta/model/cancellations/";
	
	public static final String EXAMPLE_RESULT_FILE = "example-result.rdf";
	public static final String EXAMPLE_MODEL_FILE = "example-model.json";
	
	public static void saveConfiguration(String filePath) throws IOException {
		FileOutputStream fos = new FileOutputStream(filePath);
		Properties prop = new Properties();
		
		prop.put("PORT", Integer.toString(PORT));
		prop.put("QUEUE_HOST", QUEUE_HOST);
		prop.put("FUSEKI_HOST", FUSEKI_HOST);

		prop.store(fos, "HDB configuration properties");
		fos.flush();
	}
	
	public static void loadConfiguration(String filePath) throws IOException {
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(filePath);
		prop.load(fis);
		
		QUEUE_HOST = prop.getProperty("QUEUE_HOST", QUEUE_HOST);
		FUSEKI_HOST = prop.getProperty("FUSEKI_HOST", FUSEKI_HOST);
		try {
			PORT = Integer.parseInt(prop.getProperty("PORT", Integer.toString(PORT)));
		} catch (NumberFormatException e) {
			logger.error("Error while parsing the default port value!", e);
		}
	}
	
	public static void loadFromEnrivonmentVariables() {
		String queueIp = System.getenv("MODACLOUDS_HDB_QUEUE_ENDPOINT_IP");
		String queuePort = System.getenv("MODACLOUDS_HDB_QUEUE_ENDPOINT_PORT");
		setQueueEndpoint(queueIp, queuePort);
		
		String kbUrl = System.getenv("MODACLOUDS_HDB_DB_ENDPOINT_IP");
		String kbPort = System.getenv("MODACLOUDS_HDB_DB_ENDPOINT_PORT");
		String dataset = System.getenv("MODACLOUDS_HDB_DB_DATASET_PATH");
		setDBEndpoint(kbUrl, kbPort, dataset);
		
		String port = System.getenv("MODACLOUDS_HDB_LISTENER_PORT");
		setPort(port);
	}
	
	public static void loadFromSystemProperties() {
		String queueIp = System.getProperty("MODACLOUDS_HDB_QUEUE_ENDPOINT_IP");
		String queuePort = System.getProperty("MODACLOUDS_HDB_QUEUE_ENDPOINT_PORT");
		setQueueEndpoint(queueIp, queuePort);
		
		String kbUrl = System.getProperty("MODACLOUDS_HDB_DB_ENDPOINT_IP");
		String kbPort = System.getProperty("MODACLOUDS_HDB_DB_ENDPOINT_PORT");
		String dataset = System.getProperty("MODACLOUDS_HDB_DB_DATASET_PATH");
		setDBEndpoint(kbUrl, kbPort, dataset);
		
		String port = System.getProperty("MODACLOUDS_HDB_LISTENER_PORT");
		setPort(port);
	}
	
	public static void loadFromArguments(Map<String, String> paramsMap) {
		if (paramsMap == null)
			return;
		
		if (paramsMap.get("queueip") != null)
			setQueueEndpoint(paramsMap.get("queueip"), null);
		if (paramsMap.get("queueport") != null)
			setQueueEndpoint(null, paramsMap.get("queueport"));
		if (paramsMap.get("dbip") != null)
			setDBEndpoint(paramsMap.get("dbip"), null, null);
		if (paramsMap.get("dbport") != null)
			setDBEndpoint(null, paramsMap.get("dbport"), null);
		if (paramsMap.get("dbpath") != null)
			setDBEndpoint(null, null, paramsMap.get("dbpath"));
		if (paramsMap.get("listenerport") != null)
			setPort(paramsMap.get("listenerport"));
	}
	
	private static void setQueueEndpoint(String queueIp, String queuePort) {
		if (queueIp != null) {
			int i = QUEUE_HOST.indexOf(':');
			if (i == -1)
				QUEUE_HOST = queueIp;
			else
				QUEUE_HOST = queueIp + ":" + QUEUE_HOST.substring(i+1);
		}
		if (queuePort != null) {
			int i = QUEUE_HOST.indexOf(':');
			if (i == -1)
				QUEUE_HOST = QUEUE_HOST + ":" + queuePort;
			else
				QUEUE_HOST = QUEUE_HOST.substring(0, i) + ":" + queuePort;
		}
	}
	
	private static void setDBEndpoint(String kbUrl, String kbPort, String dataset) {
		if (kbUrl != null) {
			int i = FUSEKI_HOST.indexOf(":", "http://".length());
			FUSEKI_HOST = "http://" + kbUrl + FUSEKI_HOST.substring(i);
		}
		if (kbPort != null) {
			int i = FUSEKI_HOST.indexOf(":", "http://".length());
			int j = FUSEKI_HOST.indexOf("/", "http://".length());
			FUSEKI_HOST = FUSEKI_HOST.substring(0, i) + ":" + kbPort + FUSEKI_HOST.substring(j);
		}
		if (dataset != null) {
			int i = FUSEKI_HOST.indexOf("/", "http://".length());
			FUSEKI_HOST = FUSEKI_HOST.substring(0, i) + dataset;
		}
	}
	
	private static void setPort(String port) {
		if (port != null)
			try {
				PORT = Integer.parseInt(port);
			} catch (Exception e) { }
	}
	
	public static int getPort(String url) {
		url = url.replaceAll("http://", "");
		url = url.replaceAll("https://", "");
		
		int i = url.indexOf('/');
		if (i > -1)
			url = url.substring(0, i);
		
		i = url.indexOf(':');
		if (i > -1)
			url = url.substring(i+1);
		
		try {
			return Integer.parseInt(url);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String getHost(String url) {
		url = url.replaceAll("http://", "");
		url = url.replaceAll("https://", "");
		
		int i = url.indexOf('/');
		if (i > -1)
			url = url.substring(0, i);
		
		i = url.indexOf(':');
		if (i > -1)
			url = url.substring(0, i);
		
		return url;
	}
	
	public static List<String> checkConfiguration() {
		List<String> res = new ArrayList<String>();
		
		// TODO: add any check to the configuration
		
		return res;
	}
}