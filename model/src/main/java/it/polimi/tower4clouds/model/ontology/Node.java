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

import java.util.Set;

public class Node extends Resource{
    
    //set that contains all id of vms inside the node
    private Set<String> vms;
    
    public Node(String type, String id) {
        super(type, id);
    }
    
    public Node() {
    }
    
    //add Virtual Machines to the node
    public void addVms(Set<String> vms){
        if(this.vms == null)
            setVms(vms);
        else
            this.vms.addAll(vms);
    }

    public Set<String> getVms() {
        return vms;
    }

    public void setVms(Set<String> vms) {
        this.vms = vms;
    }
    
    

	@Override
	public String toString() {
		return "Node [vms=" + vms + ", type=" + getType() + ", id="
				+ getId() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((vms == null) ? 0 : vms.hashCode());
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
		Node other = (Node) obj;
		if (vms == null) {
			if (other.vms != null)
				return false;
		} else if (!vms.equals(other.vms))
			return false;
		return true;
	}
    
    
    
}
