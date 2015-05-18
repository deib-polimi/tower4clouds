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

import it.polimi.deib.rsp_services_csparql.streams.utilities.InputDataUnmarshaller;
import it.polimi.tower4clouds.model.ontology.MO;

import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class DAInputDataUnmarshaller implements InputDataUnmarshaller {

	private static Logger logger = LoggerFactory
			.getLogger(DAInputDataUnmarshaller.class);
	private JsonParser jsonParser = new JsonParser();

	@Override
	public Model unmarshal(String inputData) throws Exception {
//		logger.debug("Unmarshalling data");
//		long startTime = System.currentTimeMillis();
		Model model = ModelFactory.createDefaultModel();

		JsonArray jsonData = jsonParser.parse(inputData).getAsJsonArray();
//		logger.debug("{} monitoring datum json object(s) received",
//				jsonData.size());

		for (JsonElement jsonElement : jsonData) {
			JsonObject jsonDatum = jsonElement.getAsJsonObject();
			Resource resourceDatum = model.createResource(
					UUID.randomUUID().toString()).addProperty(RDF.type,
					MO.MonitoringDatum);
			for (Entry<String, JsonElement> pair : jsonDatum.entrySet()) {
				String property = pair.getKey();
				JsonPrimitive value = pair.getValue().getAsJsonPrimitive();
				if (value.isBoolean()) {
					resourceDatum.addProperty(MO.makeProperty(property), model
							.createTypedLiteral(value.getAsBoolean(),
									XSDDatatype.XSDboolean));
				} else if (value.isString()) {
					resourceDatum.addProperty(MO.makeProperty(property), model
							.createTypedLiteral(value.getAsString(),
									XSDDatatype.XSDstring));
				} else if (value.isNumber()) {
					resourceDatum.addProperty(MO.makeProperty(property), model
							.createTypedLiteral(value.getAsNumber()
									.doubleValue(), XSDDatatype.XSDdouble));
				} else {
					logger.error("Unknown datum property: {}", value);
				}
			}
		}
//		logger.debug("Unmarshalling completed in {} seconds",
//				((double) (System.currentTimeMillis() - startTime)) / 1000);

		return model;
	}

}
