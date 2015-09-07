/*
 * Copyright 2015 Politecnico di Milano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.polimi.tower4clouds.model.ontology;

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
    
    @Override
    public String toString() {
        return "Method [clazz=" + getClazz() + ", type=" + getType() + ", id="
                + getId() + "]";
    }
    
     //add nodes to the rack
    public void addNodes(Set<String> nodes){
        if(this.nodes == null)
            setNodes(nodes);
        else
            this.nodes.addAll(nodes);
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public void setNodes(Set<String> nodes) {
        this.nodes = nodes;
    }
    
}

