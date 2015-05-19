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
package it.polimi.tower4clouds.rdf_history_db.observer.rest;

import it.polimi.tower4clouds.rdf_history_db.observer.Configuration;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for generating (fake?) messages that are then read by the Listener.
 * 
 * @author Riccardo B. Desantis
 *
 */
public class Producer {
	
	private int port;
	
	private static final Logger logger = LoggerFactory.getLogger(Producer.class);
	
	private static final Random r = new Random(UUID.randomUUID().getMostSignificantBits());
	
	public Producer(int port) {
		this.port = port;
	}
	
	public Producer() {
		this(Configuration.DEFAULT_PORT);
	}
	
	private void sendMessage(String path, Method method) {
		sendMessage(path, "", method);
	}
	
	private void sendMessage(String path, String body, Method method) {
		Client client = new Client(new Context(), Protocol.HTTP);
		
		ClientResource request = new ClientResource(Configuration.DEFAULT_BASEPATH + ":" + port + path);
		
		Representation representation = null;
		if (method == Method.POST)
			representation = request.post(body);
		else if (method == Method.PUT)
			representation = request.put(body);
		else if (method == Method.DELETE)
			representation = request.delete();
		
		if (representation != null) {
			logger.info("Message sent!");
			try {
				logger.debug("Answer:\n{}", representation.getText());
			} catch (IOException e) {
				logger.error("Argh!", e);
			}
		} else {
			logger.error("Error while sending the message! Method not recognized.");
		}
		
		try {
			client.stop();
		} catch (Exception e) {
			logger.error("Error while stopping the REST client!", e);
		}
	}
	
	public void sendMonitoringData() {
		String body = "";
		
		Scanner sc = new Scanner(this.getClass().getResourceAsStream("/" + Configuration.EXAMPLE_RESULT_FILE));
		
		while (sc.hasNextLine())
			body += sc.nextLine() + "\n";
		
		sc.close();
		
		body = String.format(body, UUID.randomUUID().toString(), System.currentTimeMillis());
		
		logger.info("Trying sending the monitoring data...");
		
		this.sendMessage(Configuration.DEFAULT_PATH, body, Method.POST);
	}
	
	public void sendModel() {
		String body = "";
		
		Scanner sc = new Scanner(this.getClass().getResourceAsStream("/" + Configuration.EXAMPLE_MODEL_FILE));
		
		while (sc.hasNextLine())
			body += sc.nextLine() + "\n";
		
		sc.close();
		
//		body = String.format(body, UUID.randomUUID().toString(), System.currentTimeMillis());

		logger.info("Trying sending the model...");
		
		this.sendMessage(Configuration.DEFAULT_PATH_MODEL, body, Method.PUT);
	}
	
	public void sendDeltaModel() {
		String body = "";
		
		Scanner sc = new Scanner(this.getClass().getResourceAsStream("/" + Configuration.EXAMPLE_MODEL_FILE));
		
		while (sc.hasNextLine())
			body += sc.nextLine() + "\n";
		
		sc.close();
		
//		body = String.format(body, UUID.randomUUID().toString(), System.currentTimeMillis());

		logger.info("Trying sending the update to a model...");
		
		this.sendMessage(Configuration.DEFAULT_PATH_MODEL, body, Method.POST);
	}
	
	public void sendDeleteModel() {
		String id = "id" + System.currentTimeMillis();

		logger.info("Trying sending the cancellation of a model...");
		
		this.sendMessage(Configuration.DEFAULT_PATH_MODEL + "/" + id, Method.DELETE);
	}
	
	public void randomMessage() {
		
//		sendMonitoringData();
		
//		sendModel();
		
//		sendDeleteModel();
		
//		sendDeltaModel();
		
		
		int rnd = r.nextInt(4);
		
		switch (rnd) {
		case 0:
			sendModel();
			break;
		case 1:
			sendDeltaModel();
			break;
		case 2:
			sendDeleteModel();
			break;
		default:
			sendMonitoringData();
		}
	}
	
	public static void test(int msgs, int wait) {
		Producer p = new Producer();
		for (int i = 1; i <= msgs; ++i) {
			p.randomMessage();
			
			if (wait > 0 && i < msgs)
				try {
					Thread.sleep(wait);
				} catch (Exception e) {}
		}
	}
	
	public static void test(int msgs) {
		test(msgs, 1000);
	}
	
}
