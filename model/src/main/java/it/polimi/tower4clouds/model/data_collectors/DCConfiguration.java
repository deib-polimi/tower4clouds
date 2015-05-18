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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DCConfiguration {

	private Map<String, String> parameters = new HashMap<String, String>();
	private Set<String> monitoredResourcesClasses = new HashSet<String>();
	private Set<String> monitoredResourcesTypes = new HashSet<String>();
	private Set<String> monitoredResourcesIds = new HashSet<String>();
	private String daUrl;
	private String dataFormat;

	public Map<String, String> getParameters() {
		if (parameters == null)
			parameters = new HashMap<String, String>();
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(String key, String value) {
		getParameters().put(key, value);
	}

	public Set<String> getMonitoredResourcesIds() {
		if (monitoredResourcesIds == null)
			monitoredResourcesIds = new HashSet<String>();
		return monitoredResourcesIds;
	}

	public void setMonitoredResourcesIds(Set<String> monitoredResourcesIds) {
		this.monitoredResourcesIds = monitoredResourcesIds;
	}

	public void addMonitoredResourceId(String monitoredResourceId) {
		getMonitoredResourcesIds().add(monitoredResourceId);
	}

	public void addMonitoredResourceType(String monitoredResourceType) {
		getMonitoredResourcesTypes().add(monitoredResourceType);
	}

	public void addMonitoredResourceClass(String monitoredResourceClass) {
		getMonitoredResourcesClasses().add(monitoredResourceClass);
	}

	@Override
	public String toString() {
		return "DCConfig [parameters=" + parameters
				+ ", monitoredResourcesClasses=" + monitoredResourcesClasses
				+ ", monitoredResourcesTypes=" + monitoredResourcesTypes
				+ ", monitoredResourcesIds=" + monitoredResourcesIds + "]";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((daUrl == null) ? 0 : daUrl.hashCode());
		result = prime * result
				+ ((dataFormat == null) ? 0 : dataFormat.hashCode());
		result = prime
				* result
				+ ((monitoredResourcesClasses == null) ? 0
						: monitoredResourcesClasses.hashCode());
		result = prime
				* result
				+ ((monitoredResourcesIds == null) ? 0 : monitoredResourcesIds
						.hashCode());
		result = prime
				* result
				+ ((monitoredResourcesTypes == null) ? 0
						: monitoredResourcesTypes.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DCConfiguration other = (DCConfiguration) obj;
		if (daUrl == null) {
			if (other.daUrl != null)
				return false;
		} else if (!daUrl.equals(other.daUrl))
			return false;
		if (dataFormat == null) {
			if (other.dataFormat != null)
				return false;
		} else if (!dataFormat.equals(other.dataFormat))
			return false;
		if (monitoredResourcesClasses == null) {
			if (other.monitoredResourcesClasses != null)
				return false;
		} else if (!monitoredResourcesClasses
				.equals(other.monitoredResourcesClasses))
			return false;
		if (monitoredResourcesIds == null) {
			if (other.monitoredResourcesIds != null)
				return false;
		} else if (!monitoredResourcesIds.equals(other.monitoredResourcesIds))
			return false;
		if (monitoredResourcesTypes == null) {
			if (other.monitoredResourcesTypes != null)
				return false;
		} else if (!monitoredResourcesTypes
				.equals(other.monitoredResourcesTypes))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	public Set<String> getMonitoredResourcesClasses() {
		if (monitoredResourcesClasses == null)
			monitoredResourcesClasses = new HashSet<String>();
		return monitoredResourcesClasses;
	}

	public void setMonitoredResourcesClasses(
			Set<String> monitoredResourcesClasses) {
		this.monitoredResourcesClasses = monitoredResourcesClasses;
	}

	public Set<String> getMonitoredResourcesTypes() {
		if (monitoredResourcesTypes == null)
			monitoredResourcesTypes = new HashSet<String>();
		return monitoredResourcesTypes;
	}

	public void setMonitoredResourcesTypes(Set<String> monitoredResourcesTypes) {
		this.monitoredResourcesTypes = monitoredResourcesTypes;
	}

	public String getDaUrl() {
		return daUrl;
	}

	public void setDaUrl(String daUrl) {
		this.daUrl = daUrl;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getDataFormat() {
		return dataFormat;
	}
}
