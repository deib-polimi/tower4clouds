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
import it.polimi.tower4clouds.manager.api.NotFoundException;
import it.polimi.tower4clouds.manager.api.Observer;
import it.polimi.tower4clouds.manager.api.SocketProtocol;

import java.net.MalformedURLException;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MultipleObserversDataServer extends ServerResource {

	private Logger logger = LoggerFactory
			.getLogger(MultipleObserversDataServer.class.getName());

	@Post
	public void addObserver(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String metricname = (String) this.getRequest().getAttributes()
					.get("metricname");
			String json = rep.getText();
			JsonObject jsonObject = new JsonParser().parse(json)
					.getAsJsonObject();
			String format = "RDF/JSON";
			String protocol = "HTTP";
			if (jsonObject.has("format")) {
				format = jsonObject.get("format").getAsString();
			}
			if (jsonObject.has("protocol")) {
				protocol = jsonObject.get("protocol").getAsString();
			}
			Observer observer;
			if (protocol.equalsIgnoreCase("HTTP")) {
				String callbackUrl = jsonObject.get("callbackUrl").getAsString();
				observer = manager.registerHttpObserver(metricname,
						callbackUrl, format);
			} else {
				String observerHost = jsonObject.get("observerHost").getAsString();
				int observerPort = jsonObject.get("observerPort").getAsInt();
				observer = manager.registerSocketObserver(metricname, observerHost, observerPort, SocketProtocol.valueOf(protocol), format);
			}
			String jsonResponse = new Gson().toJson(observer);
			this.getResponse().setStatus(Status.SUCCESS_CREATED);
			this.getResponse().setEntity(jsonResponse,
					MediaType.APPLICATION_JSON);
		} catch (NullPointerException e) { //TODO do a better check
			logger.error(e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					e.getMessage());
		} catch (NotFoundException e) {
			logger.error(e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					e.getMessage());
			this.getResponse().setEntity(e.getMessage(), MediaType.TEXT_PLAIN);
		} catch (MalformedURLException e) {
			String message = "Callbackurl is not a valid url: "
					+ e.getMessage();
			logger.error(message);
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					e.getMessage());
			this.getResponse().setEntity(message, MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while registering observer", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while registering observer: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Get
	public void getObservers() {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String metricname = (String) this.getRequest().getAttributes()
					.get("metricname");

			Set<Observer> observers = manager.getObservers(metricname);
			this.getResponse().setStatus(Status.SUCCESS_OK);
			this.getResponse().setEntity(new Gson().toJson(observers),
					MediaType.APPLICATION_JSON);
		} catch (NotFoundException e) {
			String message = "The metric does not exist: " + e.getMessage();
			logger.error(message);
			this.getResponse()
					.setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
			this.getResponse().setEntity(message, MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while getting observers", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Error while getting observers: " + e.getMessage());
			this.getResponse().setEntity(
					"Error while getting observers: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

}
