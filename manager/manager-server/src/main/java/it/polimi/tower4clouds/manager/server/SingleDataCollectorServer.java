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
import it.polimi.tower4clouds.manager.NotFoundException;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;

import org.apache.jena.atlas.json.JsonObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class SingleDataCollectorServer extends ServerResource {

	private static final Logger logger = LoggerFactory
			.getLogger(SingleDataCollectorServer.class);

	@Get
	public void getDataCollectorDescriptor() {
		String id = (String) this.getRequest().getAttributes().get("id");
		try {
			Gson gson = new Gson();
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");

			DCDescriptor dc = manager.getRegisteredDC(id);

			this.getResponse().setStatus(Status.SUCCESS_OK,
					"DC successfully retrieved");
			this.getResponse().setEntity(gson.toJson(dc),
					MediaType.APPLICATION_JSON);
		} catch (NotFoundException e) {
			logger.error("DC {} does not exist", id);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"DC " + id + " does not exist");
			this.getResponse().setEntity("DC " + id + " does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while getting dc", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while getting dc: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}
	
	@Delete
	public void deleteDataCollectorDescriptor() {
		String id = (String) this.getRequest().getAttributes().get("id");
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");

			manager.unregisterDC(id);

			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT,
					"DC successfully retrieved");
		} catch (NotFoundException e) {
			logger.error("DC {} does not exist", id);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"DC " + id + " does not exist");
			this.getResponse().setEntity("DC " + id + " does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while unregistering dc", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while unregistering dc: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Put
	public void replaceDC(Representation rep) {
		String id = (String) this.getRequest().getAttributes().get("id");
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String payload = rep.getText();

			DCDescriptor dcDescriptor = DCDescriptor.fromJson(payload);

			manager.registerDataCollector(id, dcDescriptor);

			this.getResponse().setStatus(Status.SUCCESS_CREATED);
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
}
