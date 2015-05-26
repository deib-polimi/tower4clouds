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

public class InternalComponent extends Component {

	public InternalComponent(String type, String id) {
		super(type, id);
	}

	public InternalComponent() {
	}

	// TODO required components is ambiguous,
	private Set<String> requiredComponents = new HashSet<String>();
	private Set<String> providedMethods = new HashSet<String>();

	public void addRequiredComponent(String component) {
		requiredComponents.add(component);
	}

	public void addProvidedMethod(String method) {
		providedMethods.add(method);
	}

	public Set<String> getRequiredComponents() {
		return requiredComponents;
	}

	public void setRequiredComponents(Set<String> requiredComponents) {
		this.requiredComponents = requiredComponents;
	}

	public Set<String> getProvidedMethods() {
		return providedMethods;
	}

	public void setProvidedMethods(Set<String> providedMethods) {
		this.providedMethods = providedMethods;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((providedMethods == null) ? 0 : providedMethods.hashCode());
		result = prime
				* result
				+ ((requiredComponents == null) ? 0 : requiredComponents
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InternalComponent other = (InternalComponent) obj;
		if (providedMethods == null) {
			if (other.providedMethods != null)
				return false;
		} else if (!providedMethods.equals(other.providedMethods))
			return false;
		if (requiredComponents == null) {
			if (other.requiredComponents != null)
				return false;
		} else if (!requiredComponents.equals(other.requiredComponents))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InternalComponent [requiredComponents=" + requiredComponents
				+ ", providedMethods=" + providedMethods + ", clazz="
				+ getClazz() + ", type=" + getType() + ", id=" + getId() + "]";
	}

}
