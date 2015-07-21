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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DCConfiguration {
	
	private static final int CLASS = 0;
	private static final int TYPE = 1;
	private static final int ID = 2;

	private Map<String, String> parameters;
	private Set<List<String>> targetResources;
	private String daUrl;
	private String dataFormat;
	
	public void addTargetResource(String clazz, String type, String id) {
		ArrayList<String> resource = new ArrayList<String>();
		resource.add(clazz);
		resource.add(type);
		resource.add(id);
		getTargetResources().add(resource);
	}
	
	public Set<List<String>> getTargetResources() {
		if (targetResources == null) {
			targetResources = new HashSet<List<String>>();
		}
		return targetResources;
	}
	

	

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((daUrl == null) ? 0 : daUrl.hashCode());
		result = prime * result
				+ ((dataFormat == null) ? 0 : dataFormat.hashCode());
		result = prime
				* result
				+ ((targetResources == null) ? 0 : targetResources
						.hashCode());
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
		if (targetResources == null) {
			if (other.targetResources != null)
				return false;
		} else if (!targetResources.equals(other.targetResources))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DCConfiguration [parameters=" + parameters
				+ ", targetResources=" + targetResources + ", daUrl="
				+ daUrl + ", dataFormat=" + dataFormat + "]";
	}
	
	public boolean isAboutResource(Resource resource) {
		try {
			for (List<String> targetResource : getTargetResources()) {
				if (targetResource.get(CLASS) != null) {
					if (!resource.getClass().isAssignableFrom(
							Class.forName(Resource.class.getPackage()
									.getName()
									+ "."
									+ targetResource.get(CLASS))))
						continue;
				}
				if (targetResource.get(TYPE) != null) {
					if (resource.getType() != null) {
						if (!resource.getType().equals(targetResource.get(TYPE)))
							continue;
					}
				}
				if (resource.getId() != null) {
					if (targetResource.get(ID) != null) {
						if (resource.getId().equals(targetResource.get(ID)))
							return true;
					} else if (targetResource.get(TYPE) != null) {
						if (resource.getType() != null) {
							if (resource.getType().equals(targetResource.get(TYPE)))
								return true;
						} else {
							if (targetResource.get(CLASS) != null) {
								if (resource.getClass().isAssignableFrom(
										Class.forName(Resource.class
												.getPackage().getName()
												+ "."
												+ targetResource.get(CLASS))))
									return true;
							}
						}
					} else {
						if (targetResource.get(CLASS) != null) {
							if (resource.getClass().isAssignableFrom(
									Class.forName(Resource.class
											.getPackage().getName()
											+ "."
											+ targetResource.get(CLASS))))
								return true;
						}
					}
				} else if (resource.getType() != null) {
					if (targetResource.get(TYPE) != null) {
						if (resource.getType() != null) {
							if (resource.getType().equals(targetResource.get(TYPE)))
								return true;
						} else {
							if (targetResource.get(CLASS) != null) {
								if (resource.getClass().isAssignableFrom(
										Class.forName(Resource.class
												.getPackage().getName()
												+ "."
												+ targetResource.get(CLASS))))
									return true;
							}
						}
					}
				} else {
					if (targetResource.get(CLASS) != null) {
						if (resource.getClass().isAssignableFrom(
								Class.forName(Resource.class.getPackage()
										.getName()
										+ "."
										+ targetResource.get(CLASS))))
							return true;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return false;
	}
	
}
