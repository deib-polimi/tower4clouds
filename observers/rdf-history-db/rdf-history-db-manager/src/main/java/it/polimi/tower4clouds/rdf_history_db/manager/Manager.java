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
package it.polimi.tower4clouds.rdf_history_db.manager;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A manager keeps polling the queue for new packets, then read them and saves them correctly in the datastore.
 * 
 * @author Riccardo B. Desantis
 *
 */
public class Manager extends Thread implements MessageParser {
	
	private static final Logger logger = LoggerFactory.getLogger(Manager.class);
	
	private Queue queue;
	private DataStore dataStore;
	
	private String queueName;
	
	public Manager(String queueName) {
		this.queueName = queueName;
		try {
			queue = new Queue(queueName);
			queue.init();
			dataStore = new DataStore();
			logger.debug("Manager initialized.");
		} catch (Exception e) {
			logger.error("Error while initializing the manager!", e);
		}
	}
	
	public static int RUNNING_TIME = 100000;
	
	@Override
	public void run() {
		logger.debug("Manager started.");
		
		try {
			queue.addSubscription(this);
		} catch (IOException e1) {
			logger.error("Error while subscribing to the queue!", e1);
		}
		
		
//		long x = System.currentTimeMillis();
//		long y = x;
//		int runningTime = RUNNING_TIME;
//		if (runningTime < 0)
//			runningTime = Integer.MAX_VALUE;
//		while (y - x < runningTime) {
//			String msg = null;
//			try {
//				msg = queue.getMessage();
//			} catch (Exception e) {
//				logger.error("Error while getting the message from the queue!", e);
//			}
//			if (msg == null)
//				continue;
//			logger.debug("Message received:\n{}", msg);
//			parseMessage(msg);
//			
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				logger.error("Error while waiting between messages.", e);
//			}
//			
//			y = System.currentTimeMillis();
//		}
	}
	
	public void parseMessage(String msg) {
		logger.debug("Message to be parsed:\n{}", msg);
		
		if (msg.equals(Queue.INIT_MSG))
			return;
		
		if (queueName.equals(Configuration.QUEUE_RESULTS))
			dataStore.addMonitoringData(msg);
		else if (queueName.equals(Configuration.QUEUE_MODELS))
			dataStore.addModel(msg);
		else if (queueName.equals(Configuration.QUEUE_DELTA_MODELS))
			dataStore.addDeltaModel(msg);
		else if (queueName.equals(Configuration.QUEUE_MODELS_DELETE))
			dataStore.deleteModel(msg);
	}
	
	public static void main(String[] args) {
		Manager.RUNNING_TIME = -1;
		new Manager(Configuration.QUEUE_RESULTS).start();
	}
}
