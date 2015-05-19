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
package it.polimi.tower4clouds.common.net;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRestClient implements RestClient {

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultRestClient.class);

	private CloseableHttpClient client;
	private HttpRequestBase request;
	private PoolingHttpClientConnectionManager connManager;
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private String response = null;
	private UnexpectedAnswerFromServerException unexpectedExc = null;
	private IOException ioExc = null;

	private final int maxThreads = 500;

	public DefaultRestClient() {
		connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(maxThreads);
		connManager.setMaxTotal(maxThreads);
		client = HttpClients.custom().setConnectionManager(connManager).build();
	}

	// public static void main(String[] args) throws InterruptedException {
	// ExecutorService exec = Executors.newCachedThreadPool();
	// final RestClient restClient = new DefaultRestClient();
	// for (int i = 0; i < 50; i++) {
	// exec.execute(new Runnable() {
	//
	// @Override
	// public void run() {
	// try {
	// long ts = System.currentTimeMillis();
	// restClient.execute(RestMethod.GET,
	// // "http://localhost:8000/testing/sleep?time=1", null, 204, 5000);
	// "http://localhost:8170/testing", null, 204, 10000);
	// System.out.println(((double) (System
	// .currentTimeMillis() - ts) / 1000 + " seconds"));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//
	// }
	// exec.shutdown();
	// }

	@Override
	public String execute(RestMethod method, String url, String jsonEntity,
			int expectedCode, int timeout)
			throws UnexpectedAnswerFromServerException, IOException {
		return execute(method, url, jsonEntity, expectedCode, timeout, false);
	}

	// TODO should be Future<String>
	@Override
	public String execute(RestMethod method, String url, String jsonEntity,
			int expectedCode, int timeout, boolean async)
			throws UnexpectedAnswerFromServerException, IOException {
		Runnable command = new RequestExecutor(method, url, jsonEntity,
				expectedCode, timeout);
		if (async) {
			executor.execute(command);
			return null;
		} else {
			synchronized (response) {
				ioExc = null;
				unexpectedExc = null;
				response = null;
				command.run();
				if (unexpectedExc != null) {
					throw unexpectedExc;
				} else if (ioExc != null) {
					throw ioExc;
				} else {
					return response;
				}
			}
		}
	}

	private final class RequestExecutor implements Runnable {
		private RestMethod method;
		private String url;
		private String jsonEntity;
		private int expectedCode;
		private int timeout;

		public RequestExecutor(RestMethod method, String url,
				String jsonEntity, int expectedCode, int timeout) {
			this.method = method;
			this.url = url;
			this.jsonEntity = jsonEntity;
			this.expectedCode = expectedCode;
			this.timeout = timeout;
		}

		@Override
		public void run() {
			switch (method) {
			case POST:
				request = new HttpPost(url);
				((HttpEntityEnclosingRequest) request)
						.setEntity(new StringEntity(jsonEntity,
								ContentType.APPLICATION_JSON));
				break;
			case PUT:
				request = new HttpPut(url);
				((HttpEntityEnclosingRequest) request)
						.setEntity(new StringEntity(jsonEntity,
								ContentType.APPLICATION_JSON));
				break;
			case GET:
				request = new HttpGet(url);
				break;
			case DELETE:
				request = new HttpDelete(url);
				break;
			default:
				throw new RuntimeException("Unknown method");
			}
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setSocketTimeout(timeout).setConnectTimeout(timeout)
					.setConnectionRequestTimeout(timeout).build();
			request.setConfig(defaultRequestConfig);

			CloseableHttpResponse response = null;
			try {
				response = client.execute(request);
				if (response.getStatusLine().getStatusCode() != expectedCode) {
					synchronized (DefaultRestClient.this.response) {
						unexpectedExc = new UnexpectedAnswerFromServerException(
								response.getStatusLine().getStatusCode());
						logger.error(unexpectedExc.getMessage());
					}
					return;
				}
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					synchronized (DefaultRestClient.this.response) {
						DefaultRestClient.this.response = null;
					}
					return;
				}
				String content = EntityUtils.toString(entity);
				synchronized (DefaultRestClient.this.response) {
					DefaultRestClient.this.response = content;
				}
			} catch (SocketTimeoutException e) {
				synchronized (DefaultRestClient.this.response) {
					ioExc = new IOException("Request timed out");
					logger.error(ioExc.getMessage());
				}
			} catch (IOException e) {
				synchronized (DefaultRestClient.this.response) {
					ioExc = e;
					logger.error(ioExc.getMessage());
				}
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}
}
