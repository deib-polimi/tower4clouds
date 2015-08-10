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
package it.polimi.tower4clouds.flexiant_nodes_dc;


import it.polimi.tower4clouds.data_collector_library.DCAgent;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Metric implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(Metric.class);

	private DCAgent dcAgent;

	protected String getName() {
            return getClass().getSimpleName();
	}

	protected void send(Number value, Resource resource) {
            if (dcAgent != null) {
		dcAgent.send(resource, getName(), value);
            } else {
		logger.warn("Monitoring is not required, data won't be sent");
            }
	}
        
        public void update(Observable o, Object arg) {
            this.dcAgent = (DCAgent) o;
        }
        
        protected Map<String, String> getParameters(Resource resource) {
            if (this.dcAgent != null)
		return this.dcAgent.getParameters(resource, getName());
            return null;
	}
        
        protected Set<Resource> getNodes() {
            return Registry._INSTANCE.getNodes();
	}

}
