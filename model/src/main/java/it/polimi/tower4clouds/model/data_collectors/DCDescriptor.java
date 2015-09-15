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
	private Set<Resource> resources;

	private int keepAlive;
	private int configSyncPeriod;

	public static DCDescriptor fromJson(String json) {
		DCDescriptor dcDescriptor = gson.fromJson(json, DCDescriptor.class);
		JsonObject jsonDcDescriptor = jsonParser.parse(json).getAsJsonObject();
		JsonElement monitoredResourcesByMetricJson = jsonDcDescriptor
				.get("monitoredResourcesByMetric");
		if (monitoredResourcesByMetricJson != null) {
			for (Entry<String, JsonElement> entry : monitoredResourcesByMetricJson
					.getAsJsonObject().entrySet()) {
				Set<Resource> resources = Resource.fromJsonResources(entry
						.getValue().toString());
				dcDescriptor.getMonitoredResourcesByMetric().put(
						entry.getKey(), resources);
			}
		}
		JsonElement resourcesJson = jsonDcDescriptor.get("resources");
		if (resourcesJson != null) {
			JsonArray resources = resourcesJson.getAsJsonArray();
			dcDescriptor.setResources(Resource.fromJsonResources(resources
					.toString()));
		}
		return dcDescriptor;
	}

	public void setResources(Set<Resource> resources) {
		this.resources = resources;
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

	public Set<Resource> getResources() {
		if (resources == null)
			resources = new HashSet<Resource>();
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
				+ monitoredResourcesByMetric + ", resources=" + resources
				+ ", keepAlive=" + keepAlive + ", configSyncPeriod="
				+ configSyncPeriod + "]";
	}

	public void addMonitoredResources(String metric, Set<Resource> resources) {
		Set<Resource> monResources = getMonitoredResourcesByMetric()
				.get(metric);
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

	public void addResources(Set<Resource> resources) {
		validate(resources);
		getResources().addAll(resources);
	}

	private void validate(Set<Resource> resources) {
		for (Resource resource : resources) {
			validate(resource);
		}
	}

	private void validate(Resource resource) {
		if (resource.getType() == null || resource.getId() == null)
			throw new NullPointerException(
					"Resources in the model must have an Id and a Type");
	}

	public void addResource(Resource resource) {
		validate(resource);
		getResources().add(resource);
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public void addMonitoredResource(Set<String> metrics, Resource resource) {
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
