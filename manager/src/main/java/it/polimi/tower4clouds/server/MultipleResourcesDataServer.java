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
package it.polimi.tower4clouds.server;

import it.polimi.tower4clouds.manager.MonitoringManager;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.util.Collection;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MultipleResourcesDataServer extends ServerResource {

	private static final Logger logger = LoggerFactory
			.getLogger(MultipleResourcesDataServer.class);

	@Get
	public void getResources() {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			Collection<Resource> resources = manager.getResources();
			this.getResponse().setStatus(Status.SUCCESS_OK);
			this.getResponse().setEntity(new Gson().toJson(resources),
					MediaType.APPLICATION_JSON);
		} catch (Exception e) {
			logger.error("Error while getting current model", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while getting current model: " + e.toString(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Post
	public void addResources(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String payload = rep.getText();
			Set<Resource> resources = Resource.fromJsonResources(payload);

			manager.addResources(resources);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

		} catch (JsonSyntaxException e) {
			logger.error("Error while adding components: {}", e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
			this.getResponse().setEntity(
					"Error while adding components: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while adding components", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
			this.getResponse().setEntity(
					"Error while adding components: "
							+ Throwables.getStackTraceAsString(e),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Put
	public void replaceResources(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String payload = rep.getText();
			Set<Resource> resources = Resource.fromJsonResources(payload);

			manager.replaceResources(resources);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

		} catch (JsonSyntaxException e) {
			logger.error("Error while replacing resources: {}", e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
			this.getResponse().setEntity(
					"Error while uploading the model: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while replacing resources", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while replacing resources: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

}