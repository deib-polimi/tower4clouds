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
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;

import java.util.Map;

import org.apache.jena.atlas.json.JsonObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MultipleDataCollectorServer extends ServerResource {

	private static final Logger logger = LoggerFactory
			.getLogger(MultipleDataCollectorServer.class);

	@Post
	public void registerDC(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String payload = rep.getText();

			DCDescriptor dcDescriptor = DCDescriptor.fromJson(payload);

			String id = manager.registerDataCollector(dcDescriptor);
			JsonObject responseJson = new JsonObject();
			responseJson.put("id", id);

			this.getResponse().setStatus(Status.SUCCESS_OK);
			this.getResponse().setEntity(responseJson.toString(),
					MediaType.APPLICATION_JSON);

		} catch (JsonSyntaxException e) {
			logger.error("Error while registering data collector: {}",
					e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
			this.getResponse()
					.setEntity(
							"Error while registering data collector: "
									+ e.getMessage(), MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while registering data collector", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
			this.getResponse()
					.setEntity(
							"Error while registering data collector: "
									+ e.getMessage(), MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Get
	public void retrieveDCs() {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");

			Map<String, DCDescriptor> dcs = manager.getRegisteredDCs();

			this.getResponse().setEntity(new Gson().toJson(dcs),
					MediaType.APPLICATION_JSON);
			this.getResponse().setStatus(Status.SUCCESS_OK);

		} catch (Exception e) {
			logger.error("Error while getting data collectors", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
			this.getResponse().setEntity(
					"Error while getting data collector: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}
}
