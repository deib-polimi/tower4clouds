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
package it.polimi.tower4clouds.model.ontology;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Resource {

	private static final JsonParser jsonParser = new JsonParser();
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory
			.getLogger(Resource.class);

	private String clazz;
	private String type;
	private String id;

	public Resource(String id) {
		this();
		this.id = id;
	}

	public Resource() {
		this.clazz = getClass().getSimpleName();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClazz() {
		return clazz;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Resource other = (Resource) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Resource [clazz=" + clazz + ", type=" + type + ", id=" + id
				+ "]";
	}

	public static Resource fromJsonResource(String json) {
		Resource resource = null;
		try {
			String clazz = Resource.class.getPackage().getName()
					+ "."
					+ jsonParser.parse(json).getAsJsonObject().get("clazz")
							.getAsString();
			resource = (Resource) gson.fromJson(json, Resource.class
					.getClassLoader().loadClass(clazz));
		} catch (Exception e) {
			logger.error("Cannot deserialized json to a valid resource", e);
		}
		return resource;
	}

	public static Set<Resource> fromJsonResources(String json) {
		Set<Resource> resources = new HashSet<Resource>();
		JsonArray jsonResources = jsonParser.parse(json).getAsJsonArray();
		for (JsonElement jsonElement : jsonResources) {
			resources.add(fromJsonResource(jsonElement.toString()));
		}
		return resources;
	}

	public String toJson() {
		return gson.toJson(this);
	}
	
	public String toJson(Set<Resource> resources) {
		return gson.toJson(resources);
	}

}
