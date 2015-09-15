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

/**
 *
 * @author davide
 */
public class Rack extends Resource{
    
    //set that contains all id of nodes inside the rack
    private Set<String> nodes;
    
    public Rack(String type, String id) {
        super(type, id);
    }
    
    public Rack() {
    }
    
     //add nodes to the rack
    public void addNodes(Set<String> nodes){
        if(this.nodes == null)
            setNodes(nodes);
        else
            this.nodes.addAll(nodes);
    }
    
    public void addNode(String node){
        if(this.nodes == null)
            nodes = new HashSet<String>();
        nodes.add(node);
    }


    public Set<String> getNodes() {
        return nodes;
    }

    public void setNodes(Set<String> nodes) {
        this.nodes = nodes;
    }

	@Override
	public String toString() {
		return "Rack [nodes=" + nodes + ", type=" + getType()
				+ ", id=" + getId() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
		Rack other = (Rack) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
	
	
    
    
    
}

