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
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Queue handler for the project.
 * 
 * @author Riccardo B. Desantis
 * 
 */
public class Queue {

	private String queueName;
	private String queueHost;

	private Channel channel;
	private Connection connection;
	
	private MessageParser parser;

	private static final Logger logger = LoggerFactory.getLogger(Queue.class);

	private static ExecutorService execService = null;

	public Queue(String queueHost, String queueName) {
		this.queueName = queueName;
		this.queueHost = queueHost;

		if (execService == null)
			execService = Executors.newCachedThreadPool();
	}
	
	public static final String INIT_MSG = "init";
	
	public void init() {
		execService.submit(new AddExecutor(INIT_MSG));
	}

	public void addSubscription(MessageParser pars) throws IOException {
		execService.submit(new AddSubscriptionExecutor(pars));
	}
		
	private void internalAddSubscription(MessageParser pars) throws IOException {
		boolean autoAck = false;
		if (this.parser != null) {
			internalRemoveSubscription();
		}
		this.parser = pars;
		channel.basicConsume(queueName, autoAck, queueName + "@" + queueHost,
				new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag,
							Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						
						long deliveryTag = envelope.getDeliveryTag();

						String message = new String(body);
						logger.debug("Message received:\n{}", message);
						
						parser.parseMessage(message);

						channel.basicAck(deliveryTag, false);
					}
				});
	}
	
	private void internalRemoveSubscription() throws IOException {
		this.parser = null;
		channel.basicCancel(queueName + "@" + queueHost);
	}

	private class AddExecutor extends Thread {
		private String message;
		private Queue queue;

		public AddExecutor(String message) {
			this.message = message;
			queue = new Queue(queueHost, queueName);
		}

		public void run() {
			try {
				queue.connect();
				queue.internalAddMessage(message);
			} catch (Exception e) {
				logger.error("Error while dealing with the queue.", e);
			} finally {
				try {
					queue.close();
				} catch (Exception e) {
					logger.error("Error while dealing with the queue.", e);
				}
			}
		}
	}
	
	private class AddSubscriptionExecutor extends Thread {
		private MessageParser pars;
		private Queue queue;

		public AddSubscriptionExecutor(MessageParser pars) {
			queue = new Queue(queueHost, queueName);
			this.pars = pars;
		}

		public void run() {
			try {
				queue.connect();
				queue.internalAddSubscription(pars);
			} catch (Exception e) {
				logger.error("Error while dealing with the queue.", e);
			}
		}
	}

	private class GetExecutor implements Callable<String> {
		private Queue queue;

		public GetExecutor() {
			queue = new Queue(queueHost, queueName);
		}

		@Override
		public String call() {
			String ret = null;

			try {
				queue.connect();
				ret = queue.internalGetMessage();
			} catch (Exception e) {
				logger.error("Error while dealing with the queue.", e);
			} finally {
				try {
					queue.close();
				} catch (Exception e) {
					logger.error("Error while dealing with the queue.", e);
				}
			}

			return ret;
		}
	}

	private boolean connected = false;

	private void connect() throws IOException {
		if (connected)
			return;

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Configuration.getHost(queueHost));
		
		int port = Configuration.getPort(queueHost);
		if (port > 0)
			factory.setPort(port);
		
		connection = factory.newConnection();
		channel = connection.createChannel();
		logger.debug("Connected to the queue {} on {}.", queueName, queueHost);

		connected = true;
	}

	private void internalAddMessage(String message) throws IOException {
		channel.queueDeclare(queueName, true, false, false, null);

		channel.basicPublish("", queueName,
				MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
		logger.debug("Message added:\n{}", message);
	}

	public void addMessage(String message) {
		execService.submit(new AddExecutor(message));
	}

	public String getMessage() throws IOException, ShutdownSignalException,
			InterruptedException,
			ExecutionException {
		return (String) execService.submit(new GetExecutor()).get();
	}

	private String internalGetMessage() throws IOException,
			ShutdownSignalException,
			InterruptedException {
		channel.queueDeclare(queueName, true, false, false, null);

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, /* true */false, consumer);

		QueueingConsumer.Delivery delivery = null;
		delivery = consumer.nextDelivery();

		if (delivery == null)
			return null;

		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

		String message = new String(delivery.getBody());
		logger.debug("Message received:\n{}", message);

		return message;
	}

	public int count() {
		try {
			connect();

			AMQP.Queue.DeclareOk dok = channel.queueDeclare(queueName, true,
					false, false, null);
			int count = dok.getMessageCount();
			logger.debug("Messages in the queue: {}.", count);

			close();

			return count;
		} catch (Exception e) {
			logger.error(
					"Error while checking the number of messages in the queue!",
					e);
			return -1;
		}
	}

	private void close() throws IOException, TimeoutException {
		if (!connected)
			return;

		channel.close();
		connection.close();
		logger.debug("Connection to the queue closed.");

		connected = false;
	}

	public Queue(String queueName) throws IOException {
		this(Configuration.QUEUE_HOST, queueName);
	}
	
	public static boolean isUp() {
		try {
			String host = Configuration.getHost(Configuration.QUEUE_HOST);
			int port = Configuration.getPort(Configuration.QUEUE_HOST);
			
			Socket s = new Socket(host, port);
			s.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean waitUntilUp() {
		return waitUntilUp(Integer.MAX_VALUE / 1000, 3000);
	}
	
	public static boolean waitUntilUp(int attempts, int sleep) {
		int attempt = 0;
		while (true) {
			boolean res = isUp();
			if (res || attempt > attempts)
				return res;
			attempt++;
			logger.info("Cannot connect to the queue, trying again in " + sleep/1000 + "s...");
			try {
				Thread.sleep(sleep);
			} catch (Exception e) { }
		}
	}
	
	public static void main(String[] args) throws Exception {
		waitUntilUp(); 
	}
}
