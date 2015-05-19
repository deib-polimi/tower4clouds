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
package it.polimi.tower4clouds.rdf_history_db.observer;

import it.polimi.tower4clouds.rdf_history_db.observer.rest.Listener;

import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the project that will initialize everything.
 * 
 * @author Riccardo B. Desantis
 * 
 */
public class MetricsObserver { //extends Application {

	private static final Logger logger = LoggerFactory
			.getLogger(MetricsObserver.class);

	private Listener listener;

	public MetricsObserver() {
		listener = new Listener();
		listener.add(Configuration.DEFAULT_PATH, ResultsServerResource.class);
		listener.add(Configuration.DEFAULT_PATH_MODEL,
				ModelsServerResource.class);
	}

//	/**
//	 * Creates a root Restlet that will receive all incoming calls.
//	 */
//	@Override
//	public synchronized Restlet createInboundRoot() {
//		Router router = new Router(getContext());
//		router.setDefaultMatchingMode(Template.MODE_EQUALS);
//
//		router.attach(Configuration.DEFAULT_PATH, ResultsServerResource.class);
//		router.attach(Configuration.DEFAULT_PATH_MODEL,
//				ModelsServerResource.class);
//
//		return router;
//	}

	public static class ResultsServerResource extends ServerResource {
		@Post
		public void addResult(String message) {
			// Print the requested URI path
			String res = "Resource URI  : " + getReference() + '\n'
					+ "Root URI      : " + getRootRef() + '\n'
					+ "Routed part   : " + getReference().getBaseRef() + '\n'
					+ "Remaining part: " + getReference().getRemainingPart();
			logger.debug("\n{}", res);

			try {
				Queue queue = new Queue(Configuration.QUEUE_RESULTS);
				queue.addMessage(message);

				logger.info("Monitoring data added to the queue.");
				
				getResponse().setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				logger.error(
						"Error while adding the monitoring data to the queue.",
						e);
				
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			} finally {
				getResponse().commit();
				commit();
				release();
			}
		}
	}

	public static class ModelsServerResource extends ServerResource {
		@Post
		public void addDeltaModel(String message) {
			// Print the requested URI path
			String res = "Resource URI  : " + getReference() + '\n'
					+ "Root URI      : " + getRootRef() + '\n'
					+ "Routed part   : " + getReference().getBaseRef() + '\n'
					+ "Remaining part: " + getReference().getRemainingPart();
			logger.debug("\n{}", res);

			try {
				Queue queue = new Queue(Configuration.QUEUE_DELTA_MODELS);
				queue.addMessage(message);

				logger.info("Update to a model added to the queue.");
				
				getResponse().setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				logger.error(
						"Error while adding the update to the model to the queue.",
						e);
				
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			} finally {
				getResponse().commit();
				commit();
				release();
			}
		}

		@Put
		public void addModel(String message) {
			// Print the requested URI path
			String res = "Resource URI  : " + getReference() + '\n'
					+ "Root URI      : " + getRootRef() + '\n'
					+ "Routed part   : " + getReference().getBaseRef() + '\n'
					+ "Remaining part: " + getReference().getRemainingPart();
			logger.debug("\n{}", res);

			try {
				Queue queue = new Queue(Configuration.QUEUE_MODELS);
				queue.addMessage(message);

				logger.info("Model added to the queue.");
				
				getResponse().setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				logger.error("Error while adding the model to the queue.", e);

				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			} finally {
				getResponse().commit();
				commit();
				release();
			}
		}

		@Delete
		public void deleteModel() {
			// Print the requested URI path
			String res = "Resource URI  : " + getReference() + '\n'
					+ "Root URI      : " + getRootRef() + '\n'
					+ "Routed part   : " + getReference().getBaseRef() + '\n'
					+ "Remaining part: " + getReference().getRemainingPart();
			logger.debug("\n{}", res);

			String id = getReference().getRemainingPart();

			if (id.length() == 0 || id.indexOf('/') != 0)
				return;

			id = id.substring(1);

			try {
				Queue queue = new Queue(Configuration.QUEUE_MODELS_DELETE);
				queue.addMessage(id);

				logger.info("Cancellation of a model added to the queue.");
				
				getResponse().setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				logger.error(
						"Error while adding the cancellation of a model to the queue.",
						e);
				
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			} finally {
				getResponse().commit();
				commit();
				release();
			}
		}
	}

	public static int RUNNING_TIME = 100000;

	public void start() {
		if (listener.isStarted())
			return;

		Listener.RUNNING_TIME = RUNNING_TIME;
		listener.start();
	}
}
