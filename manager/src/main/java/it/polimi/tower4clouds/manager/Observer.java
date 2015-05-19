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
package it.polimi.tower4clouds.manager;

public class Observer {

	private String id;
	private String callbackUrl;
	private transient String queryUri;
	private String format;
	private String observerHost;
	private int observerPort;
	private String protocol;

	public Observer() {
	}

	public Observer(String id, String queryUri, String callbackUrl,
			String protocol, String format) {
		setId(id);
		setCallbackUrl(callbackUrl);
		setQueryUri(queryUri);
		setProtocol(protocol);
		setFormat(format);
	}

	public Observer(String id, String queryUri, String observerHost,
			int observerPort, String protocol, String format) {
		setId(id);
		setQueryUri(queryUri);
		setObserverHost(observerHost);
		setObserverPort(observerPort);
		setProtocol(protocol);
		setFormat(format);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}

	public String getId() {
		return id;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public String getQueryUri() {
		return queryUri;
	}

	public void setQueryUri(String queryUri) {
		this.queryUri = queryUri;
	}

	public String getUri() {
		return queryUri + "/observers/" + id;
	}

	public String getObserverHost() {
		return observerHost;
	}

	public void setObserverHost(String observerHost) {
		this.observerHost = observerHost;
	}

	public int getObserverPort() {
		return observerPort;
	}

	public void setObserverPort(int observerPort) {
		this.observerPort = observerPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}
