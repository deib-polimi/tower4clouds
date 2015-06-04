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

import it.polimi.deib.rsp_services_csparql.observers.utilities.OutputDataMarshaller;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;

public class DAOutputDataMarshaller implements OutputDataMarshaller {

	private Logger logger = LoggerFactory.getLogger(DAOutputDataMarshaller.class
			.getName());

	@Override
	public String marshal(RDFTable q, String format) {
		try {
		if (format == null)
			format = "RDF/JSON";
		switch (format) {
		case "RDF/JSON":
			return getRDFJsonSerialization(q);
		case "TOWER/JSON":
			return getTowerJsonSerialization(q);
		case "INFLUXDB":
			return getInfluxDBSerialization(q);
		case "GRAPHITE":
			return getGraphiteSerialization(q);
		default:
			return getRDFJsonSerialization(q);
		}
		} catch (NullPointerException e) {
			logger.error("CSPARQL is sending partial results, could not serialize");
			return null;
		}
	}
	
	private static String getInfluxDBSerialization(RDFTable q) {
		if (!q.isGraph()) {
			throw new RuntimeException(
					"Cannot marshal result set to JSON, a graph was was expected");
		}
		JsonArray json = new JsonArray();
		
		Collection<Map<String, Object>> monitoringData = getMonitoringData(q);
		Map<String, List<String>> columnsByMetric = new HashMap<String, List<String>>();
		Map<String,Set<List<Object>>> pointsByMetric = new HashMap<String, Set<List<Object>>>();
		for (Map<String, Object> monitoringDatum : monitoringData) {
			String metric = (String) monitoringDatum.get(MOVocabulary.metric);
			Number timestamp = (Number) monitoringDatum.get(MOVocabulary.timestamp);
			Object value = monitoringDatum.get(MOVocabulary.value);
			String resourceId = (String) monitoringDatum.get(MOVocabulary.resourceId);
			if (!columnsByMetric.containsKey(metric)) {
				ArrayList<String> columns = new ArrayList<String>();
				columns.add("time"); //1 (InfluxDB specific)
				columns.add(MOVocabulary.value); //2
				columns.add(MOVocabulary.resourceId); //3
				columnsByMetric.put(metric, columns);
			}
			Set<List<Object>> points = pointsByMetric.get(metric);
			if (points==null){
				points = new HashSet<List<Object>>();
				pointsByMetric.put(metric, points);
			}
			List<Object> datumPoints = new ArrayList<Object>();
			datumPoints.add(timestamp); //1
			datumPoints.add(value); //2
			datumPoints.add(resourceId); //3
			points.add(datumPoints);
		}
		
		for (String metric : columnsByMetric.keySet()) {
			JsonObject metricJson = new JsonObject();
			metricJson.addProperty("name", metric);
			JsonArray columns = new JsonArray();
			for (String column : columnsByMetric.get(metric)) {
				columns.add(new JsonPrimitive(column));
			}
			metricJson.add("columns", columns);
			JsonArray points = new JsonArray();
			for (List<Object> point : pointsByMetric.get(metric)) {
				JsonArray pointJson = new JsonArray();
				for (Object value : point) {
					pointJson.add(getJsonPrimitive(value));
				}
				points.add(pointJson);
			}
			metricJson.add("points", points);
			json.add(metricJson);
		}
		return json.toString();
	}


	private static Collection<Map<String, Object>> getMonitoringData(RDFTable q) {
		Map<String,Map<String, Object>> monitoringData = new HashMap<String, Map<String,Object>>();
		for (RDFTuple tuple : q) {
			String property = tuple.get(1);
			if (property.equals(RDF.type.getURI()))
				continue;
			String datumId = tuple.get(0);
			Object value = getJavaObject(tuple.get(2));
			Map<String, Object> datum = monitoringData.get(datumId);
			if (datum == null){
				datum = new HashMap<String, Object>();
				monitoringData.put(datumId, datum);
			}
			datum.put(getLocalName(property), value);
		}
		return monitoringData.values();
	}

	private static String getLocalName(String uri) {
		String noPrefix = uri.substring(uri.lastIndexOf(':')+1);
		return noPrefix.substring(noPrefix.lastIndexOf('#')+1);
	}

	private static JsonPrimitive getJsonPrimitive(Object value) {
		if (value instanceof Number) {
			return new JsonPrimitive((Number)value);
		} else if (value instanceof Boolean) {
			return new JsonPrimitive((Boolean)value);
		} else if (value instanceof Character) {
			return new JsonPrimitive((Character) value);
		} else {
			return new JsonPrimitive(value.toString());
		}
	}

	private static String getTowerJsonSerialization(RDFTable q) {
		if (!q.isGraph()) {
			throw new RuntimeException(
					"Cannot marshal result set to JSON, a graph was was expected");
		}
		Collection<Map<String, Object>> monitoringData = getMonitoringData(q);
		JsonArray json = new JsonArray();
		for (Map<String, Object> datum : monitoringData) {
			JsonObject jsonDatum = new JsonObject();
			String metric = (String) datum.get(MOVocabulary.metric);
			Number timestamp = (Number) datum.get(MOVocabulary.timestamp);
			Object value = datum.get(MOVocabulary.value);
			String resourceId = (String) datum.get(MOVocabulary.resourceId);
			jsonDatum.addProperty(MOVocabulary.resourceId, resourceId);
			jsonDatum.addProperty(MOVocabulary.metric, metric);
			jsonDatum.add(MOVocabulary.value, getJsonPrimitive(value));
			jsonDatum.addProperty(MOVocabulary.timestamp, timestamp);
			json.add(jsonDatum);
		}
		return json.toString();
	}
	
	// TODO consider using the pickle protocol
	private static String getGraphiteSerialization(RDFTable q) {
		if (!q.isGraph()) {
			throw new RuntimeException(
					"Cannot marshal result set to JSON, a graph was was expected");
		}
		Collection<Map<String, Object>> monitoringData = getMonitoringData(q);
		String serialization = "";
		for (Map<String, Object> datum : monitoringData) {
			String metric = (String) datum.get(MOVocabulary.metric);
			Number timestamp = (Number) datum.get(MOVocabulary.timestamp);
			Object value = datum.get(MOVocabulary.value);
			String resourceId = (String) datum.get(MOVocabulary.resourceId);
			serialization += resourceId + "." + metric + " " + value + " " + (int)(timestamp.doubleValue()/1000) + "\n";
		}
		return serialization;
	}

	private static Object getJavaObject(String object) {
		Object value = NodeFactoryExtra.parseNode(object
				.replaceAll("\\^\\^null", "\\^\\^xsd\\:string")
				.replaceAll("(\".*\"\\^\\^)(.*\\/\\/.*)", "$1<$2>"))
				.getLiteralValue();
		return value;
	}

	private static String getRDFJsonSerialization(RDFTable q) {
		return q.getJsonSerialization();
	}

}
