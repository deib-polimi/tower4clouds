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

import it.polimi.modaclouds.monitoring.kb.api.FusekiKbAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.tower4clouds.model.ontology.MO;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rdf_history_db.manager.data.Model;
import it.polimi.tower4clouds.rdf_history_db.manager.data.MonitoringData;

import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;

/**
 * 
 * This class hides the datastore used behind. It is now implemented for working with Fuseki. 
 * 
 * @author Riccardo B. Desantis
 *
 */
public class DataStore {
	
	private static final Logger logger = LoggerFactory.getLogger(DataStore.class);
	
	@SuppressWarnings("unused")
	private String host;
	
	private static FusekiKbAPI knowledgeBaseModels = null;
	
	private static DatasetAccessor datasetAccessor = null;
	
	private static ExecutorService execService = null;
	
	public static final String DEFAULT_GRAPH = "default";
	
	public DataStore(String host) {
		this.host = host;
		
		if (knowledgeBaseModels == null) {
			knowledgeBaseModels = new FusekiKbAPI(host);
			try {
				knowledgeBaseModels.putModel(MO.model, MO.URI + "models");
			} catch (IOException e) {
				logger.error("Error while putting the model in the datastore. Maybe it's already there?");
			}
		}
		
		if (datasetAccessor == null)
			datasetAccessor = DatasetAccessorFactory.createHTTP(host + "/data");
		
		if (execService == null)
			execService = Executors.newCachedThreadPool();
		
		logger.debug("Connection created to the fuseki store at {}.", host);
	}
	
	public DataStore() {
		this(Configuration.FUSEKI_HOST);
	}
	
	private boolean add(String graphUri, com.hp.hpl.jena.rdf.model.Model model) {
		execService.submit(new AddExecutor(graphUri, model));
		return true;
	}
	
	public boolean addMonitoringData(String jsonRdfDatum) {
		MonitoringData r = MonitoringData.resultFromRdfJson(jsonRdfDatum);
		
		if (r == null)
			return false;
		
		long hourTimestamp = hourTimestamp(r.getTimestamp());
		
		String graphUri = Configuration.FUSEKI_MONITORING + hourTimestamp;
		
		boolean res1 = add(graphUri, r.getModel());
		
		boolean res2 = add(DEFAULT_GRAPH, MonitoringData.defaultGraphStatement(graphUri, hourTimestamp));
		
		if (res1)
			logger.info("Monitoring data added to the datastore.");
		else
			logger.error("Error while adding the monitoring data added to the datastore.");
		
		return res1 && res2;
		
	}
	
	private static Set<Resource> getAsResourceSet(String jsonDatum) {
		HashSet<Resource> res = new HashSet<Resource>();
		
		if (jsonDatum == null || jsonDatum.trim().length() == 0)
			return res;
		
//		Model m = Model.modelFromJson(jsonDatum);
//		if (m != null)
//			res.addAll(m.getResources());

		try {
			res.addAll(Resource.fromJsonResources(jsonDatum));
		} catch (Exception e) {
			logger.debug(jsonDatum);
			logger.error("Error while getting the resource set.", e);
		}
		
		res.remove(null);
		return res;
	}
	
	public boolean addModel(String jsonDatum) {
		Set<Resource> resources = getAsResourceSet(jsonDatum);
		if (resources.size() == 0)
			return false;
		
		long timestamp = System.currentTimeMillis();
		
		String graphUri = Configuration.FUSEKI_MODEL + timestamp;
	
		boolean res1 = true;
		try {
			knowledgeBaseModels.addMany(resources, MOVocabulary.idParameterName, graphUri);
			logger.info("New model added to the datastore.");
		} catch (SerializationException e) {
			logger.error("Error while adding the model to the datastore.", e);
			return false;
		}
		
		String dailyGraphUri = Configuration.FUSEKI_MODEL_DAILY + dayTimestamp(timestamp);
		boolean res2 = add(dailyGraphUri, Model.getNameModel(graphUri, timestamp));
		
		boolean res3 = add(DEFAULT_GRAPH, Model.defaultGraphStatementAdd(graphUri, hourTimestamp(timestamp)));
		
		if (!res2)
			logger.error("Error while adding the info on the model in the daily graph in the datastore.");
		
		if (!res3)
			logger.error("Error while adding the info on the model in the default graph in the datastore.");
		
		return res1 && res2 && res3;
		
	}
	
	public boolean deleteModel(String id) {
		if (id == null || id.length() == 0)
			return false;
		
		long timestamp = System.currentTimeMillis();
		
		String graphUri = Configuration.FUSEKI_MODELS_DELETE + timestamp;
		
		boolean res1 = add(graphUri, Model.getDeleteModel(id, timestamp));
		
		if (res1)
			logger.info("Cancellation of a model added to the datastore.");
		else
			logger.error("Error while adding the info on the cancellation of a model in the datastore.");
		
		String dailyGraphUri = Configuration.FUSEKI_MODELS_DELETE_DAILY + dayTimestamp(timestamp);
		boolean res2 = add(dailyGraphUri, Model.getNameModel(Configuration.FUSEKI_MODELS_DELETE + timestamp, timestamp));
		
		boolean res3 = add(DEFAULT_GRAPH, Model.defaultGraphStatementDelete(graphUri, hourTimestamp(timestamp)));
		
		if (!res2)
			logger.error("Error while adding the info on the cancellation of a model in the daily graph in the datastore.");
		
		if (!res3)
			logger.error("Error while adding the info on the cancellation of a model in the default graph in the datastore.");
		
		return res1 && res2 && res3;
	}
	
	public boolean addDeltaModel(String jsonDatum) {
		Set<Resource> resources = getAsResourceSet(jsonDatum);
		if (resources.size() == 0)
			return false;
		
		long timestamp = System.currentTimeMillis();
		
		String graphUri = Configuration.FUSEKI_DELTAS_MODEL + timestamp;
		
		boolean res1 = true;
		try {
			knowledgeBaseModels.addMany(resources, MOVocabulary.idParameterName, graphUri);
			logger.info("Updated model added to the datastore.");
		} catch (SerializationException e) {
			logger.error("Error while adding the update to the model to the datastore.", e);
			return false;
		}
		
		String dailyGraphUri = Configuration.FUSEKI_DELTAS_MODEL_DAILY + dayTimestamp(timestamp);
		boolean res2 = add(dailyGraphUri, Model.getNameModel(graphUri, timestamp));
		
		boolean res3 = add(DEFAULT_GRAPH, Model.defaultGraphStatementUpdate(graphUri, hourTimestamp(timestamp)));
		
		if (!res2)
			logger.error("Error while adding the info on the update of a model in the daily graph in the datastore.");
		
		if (!res3)
			logger.error("Error while adding the info on the update of a model in the default graph in the datastore.");
		
		return res1 && res2 && res3;
	}
	
	private static long hourTimestamp(long timestamp) {
		Date d = null;
		try {
			d = new Date(timestamp);
		} catch (Exception e) {
			logger.error("Argh!", e);
			d = new Date();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		return c.getTimeInMillis();
	}
	
	private static long dayTimestamp(long timestamp) {
		Date d = null;
		try {
			d = new Date(timestamp);
		} catch (Exception e) {
			logger.error("Argh!", e);
			d = new Date();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		return c.getTimeInMillis();
	}
	
	private class AddExecutor extends Thread {
		private String graphUri;
		private com.hp.hpl.jena.rdf.model.Model model;
		
		public AddExecutor(String graphUri, com.hp.hpl.jena.rdf.model.Model model) {
			this.graphUri = graphUri;
			this.model = model;
		}
		
		public void run() {
			if (graphUri.equals(DEFAULT_GRAPH))
				datasetAccessor.add(model);
			else
				datasetAccessor.add(graphUri, model);
			logger.debug("Model added to the datastore.");
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error("Error while waiting.", e);
			}
		}

	}
	
	public static void reset(String host) {
//		UpdateRequest query = UpdateFactory.create("DROP all", Syntax.syntaxSPARQL_11);
//		UpdateProcessor execUpdate = UpdateExecutionFactory.createRemote(query, host + "/update");
//		execUpdate.execute();
	}
	
	public static void reset() {
		reset(Configuration.FUSEKI_HOST);
	}
	
	public static String encodeURL(String URL) {
		try {
			return URLEncoder.encode(URL, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}
	
	public static boolean isUp() {
		try {
			String host = Configuration.getHost(Configuration.FUSEKI_HOST);
			int port = Configuration.getPort(Configuration.FUSEKI_HOST);
			
			Socket s = new Socket(host, port);
			s.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean waitUntilUp() {
		return waitUntilUp(Integer.MAX_VALUE / 1000, 3000);
	}
	
	public static boolean waitUntilUp(int attempts, int sleep) {
		int attempt = 0;
		while (true) {
			boolean res = isUp();
			if (res || attempt > attempts)
				return res;
			attempt++;
			try {
				Thread.sleep(sleep);
			} catch (Exception e) { }
		}
	}
	
	public static void main(String[] args) throws Exception {
		waitUntilUp(); 
	}
}
