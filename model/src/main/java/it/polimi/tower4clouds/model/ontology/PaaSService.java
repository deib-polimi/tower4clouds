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

public class PaaSService extends ExternalComponent {
	
	public PaaSService(String type, String id) {
		super(type, id);
	}

	public PaaSService() {
	}

	@Override
	public String toString() {
		return "PaaSService [cloudProvider=" + getCloudProvider()
				+ ", location=" + getLocation() + ", clazz=" + getClazz()
				+ ", type=" + getType() + ", id=" + getId() + "]";
	}

}
