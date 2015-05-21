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
package it.polimi.tower4clouds.manager.server;

import it.polimi.tower4clouds.manager.MonitoringManager;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MultipleActionsServer extends ServerResource {

	private static Logger logger = LoggerFactory
			.getLogger(MultipleActionsServer.class);

	@Post
	public void executeAction(Representation rep) {
		try {
			String jsonString = rep.getText();
			String ruleId = (String) this.getRequest().getAttributes()
					.get("id");
			logger.debug(
					"Execute action for rule {}, received json object: {}",
					ruleId, jsonString);
			JsonArray jsonMonitoringData = new JsonParser().parse(jsonString)
					.getAsJsonArray();
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			for (JsonElement datum : jsonMonitoringData) {
				JsonObject datumJson = datum.getAsJsonObject();
				manager.executeAction(ruleId,
						datumJson.get(MOVocabulary.resourceId).getAsString(),
						datumJson.get(MOVocabulary.value).getAsString(),
						datumJson.get(MOVocabulary.timestamp).getAsString());
			}
		} catch (Exception e) {
			logger.error("Error while trying to execute action", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while trying to execute action: " + e.toString(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}
}
