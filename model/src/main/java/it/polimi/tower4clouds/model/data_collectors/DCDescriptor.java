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
package it.polimi.tower4clouds.model.data_collectors;

import it.polimi.tower4clouds.model.ontology.Resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DCDescriptor {

	private static final Gson gson = new Gson();
	private static final JsonParser jsonParser = new JsonParser();

	private Map<String, Set<Resource>> monitoredResourcesByMetric;
	private Set<Resource> relatedResources;

	private int keepAlive;
	private int configSyncPeriod;

	public static DCDescriptor fromJson(String json) {
		DCDescriptor dcDescriptor = gson.fromJson(json, DCDescriptor.class);
		JsonObject jsonDcDescriptor = jsonParser.parse(json).getAsJsonObject();
		JsonObject monitoredResourcesByMetricJson = jsonDcDescriptor.get(
				"monitoredResourcesByMetric").getAsJsonObject();
		for (Entry<String, JsonElement> entry : monitoredResourcesByMetricJson
				.entrySet()) {
			Set<Resource> resources = Resource.fromJsonResources(entry
					.getValue().toString());
			dcDescriptor.getMonitoredResourcesByMetric().put(entry.getKey(),
					resources);
		}
		JsonElement relatedResourcesJson = jsonDcDescriptor.get("relatedResources");
		if (relatedResourcesJson != null){
			JsonArray relatedResources = relatedResourcesJson.getAsJsonArray();
			dcDescriptor.setRelatedResources(Resource.fromJsonResources(relatedResources.toString()));
		}
		return dcDescriptor;
	}

	public void setRelatedResources(Set<Resource> relatedResources) {
		this.relatedResources = relatedResources;
	}

	public Set<Resource> getRelatedResources() {
		if (relatedResources == null)
			relatedResources = new HashSet<Resource>();
		return relatedResources;
	}

	public int getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
	}

	public int getConfigSyncPeriod() {
		return configSyncPeriod;
	}

	public void setConfigSyncPeriod(int configSyncPeriod) {
		this.configSyncPeriod = configSyncPeriod;
	}

	public Set<Resource> getAllResources() {
		HashSet<Resource> resources = new HashSet<Resource>();
		for (Set<? extends Resource> monResources : getMonitoredResourcesByMetric()
				.values()) {
			resources.addAll(monResources);
		}
		resources.addAll(getRelatedResources());
		return resources;
	}

	public Map<String, Set<Resource>> getMonitoredResourcesByMetric() {
		if (monitoredResourcesByMetric == null)
			monitoredResourcesByMetric = new HashMap<String, Set<Resource>>();
		return monitoredResourcesByMetric;
	}

	public void setMonitoredResourcesByMetric(
			Map<String, Set<Resource>> monitoredResourcesByMetric) {
		this.monitoredResourcesByMetric = monitoredResourcesByMetric;
	}


	@Override
	public String toString() {
		return "DCDescriptor [monitoredResourcesByMetric="
				+ monitoredResourcesByMetric + ", relatedResources="
				+ relatedResources + ", keepAlive=" + keepAlive
				+ ", configSyncPeriod=" + configSyncPeriod + "]";
	}

	public void addMonitoredResources(String metric, Set<Resource> resources) {
		Set<Resource> monResources = getMonitoredResourcesByMetric().get(
				metric);
		if (monResources == null) {
			getMonitoredResourcesByMetric().put(metric, resources);
		} else {
			monResources.addAll(resources);
		}
	}

	public void addMonitoredResource(String metric, Resource resource) {
		Set<Resource> currentResources = getMonitoredResourcesByMetric().get(
				metric);
		if (currentResources == null) {
			currentResources = new HashSet<Resource>();
			getMonitoredResourcesByMetric().put(metric, currentResources);
		}
		currentResources.add(resource);
	}
	
	public void addRelatedResources(Set<Resource> resources) {
		getRelatedResources().addAll(resources);
	}

	public void addRelatedResource(Resource resource) {
		getRelatedResources().add(resource);
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public void addMonitoredResource(Set<String> metrics,
			Resource resource) {
		for (String metric : metrics) {
			addMonitoredResource(metric, resource);
		}
	}

	public void addMonitoredResources(Set<String> metrics,
			Set<Resource> resources) {
		for (String metric : metrics) {
			addMonitoredResources(metric, resources);
		}
	}

}
