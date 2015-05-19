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

import it.polimi.tower4clouds.common.net.DefaultRestClient;
import it.polimi.tower4clouds.common.net.RestClient;
import it.polimi.tower4clouds.common.net.RestMethod;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class RdfHistoryDBAPI {

	private final static int timeout = 10000;
	private boolean async = false;
	private final int rdfHistoryDbPort;
	private final String rdfHistoryDbIP;
	private final RestClient client;

	public RdfHistoryDBAPI(String rdfHistoryDbIP, int rdfHistoryDbPort) {
		this.rdfHistoryDbIP = rdfHistoryDbIP;
		this.rdfHistoryDbPort = rdfHistoryDbPort;
		client = new DefaultRestClient();
	}

	public void deleteResource(String resourceId)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.DELETE, "http://" + rdfHistoryDbIP + ":"
				+ rdfHistoryDbPort + "/resources/" + resourceId, null, 204,
				timeout, async);
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public void replaceResources(Set<Resource> resources)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.PUT, "http://" + rdfHistoryDbIP + ":"
				+ rdfHistoryDbPort + "/resources",
				new Gson().toJson(resources), 204, timeout, async);
	}

	public void addResources(Set<Resource> resources)
			throws UnexpectedAnswerFromServerException, IOException {
		client.execute(RestMethod.POST, "http://" + rdfHistoryDbIP + ":"
				+ rdfHistoryDbPort + "/resources",
				new Gson().toJson(resources), 204, timeout, async);
	}

	public void addResource(Resource resource)
			throws UnexpectedAnswerFromServerException, IOException {
		Set<Resource> resources = new HashSet<Resource>();
		resources.add(resource);
		client.execute(RestMethod.PUT, "http://" + rdfHistoryDbIP + ":"
				+ rdfHistoryDbPort + "/resources",
				new Gson().toJson(resources), 204, timeout, async);
	}

	public void deleteResources(Set<String> resourcesIdsToRemove)
			throws UnexpectedAnswerFromServerException, IOException {
		for (String resourceId : resourcesIdsToRemove) {
			deleteResource(resourceId);
		}
	}

}
