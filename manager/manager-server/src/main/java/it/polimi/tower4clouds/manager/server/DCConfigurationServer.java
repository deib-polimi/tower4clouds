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
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class DCConfigurationServer extends ServerResource {

	private static final Logger logger = LoggerFactory
			.getLogger(DCConfigurationServer.class);

	@Get
	public void getDCConfiguration() {
		String id = (String) this.getRequest().getAttributes().get("id");
		try {
			Gson gson = new Gson();
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");

			Map<String, DCConfiguration> dcconfigs = manager
					.getDCConfigurationByMetric(id);
			this.getResponse().setStatus(Status.SUCCESS_OK,
					"DC configuration successfully retrieved");
			this.getResponse().setEntity(gson.toJson(dcconfigs),
					MediaType.APPLICATION_JSON);
		} catch (NotFoundException e) {
			logger.error("DC {} is not register", id);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"DC " + id + " is not register");
			this.getResponse().setEntity("DC " + id + " is not register",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while getting the dc configuration", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while getting the dc configuration: "
							+ e.getMessage(), MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

}
