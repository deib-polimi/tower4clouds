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
package it.polimi.tower4clouds.flexiant_nodes_dc.metrics;

import it.polimi.tower4clouds.data_collector_library.DCAgent;
import it.polimi.tower4clouds.flexiant_nodes_dc.Metric;
import it.polimi.tower4clouds.model.ontology.Resource;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class CPUUtilization extends Metric{
    
    private static final Logger logger = LoggerFactory.getLogger(CPUUtilization.class);
    
    private final Map<String, Timer> timerPerNodeId = new ConcurrentHashMap<String, Timer>();
    private final Map<String, Integer> samplingTimePerNodeId = new ConcurrentHashMap<String, Integer>();
    
    private static final int DEFAULT_SAMPLING_TIME = 5;
    
    private static final double TRIAL_SAMPLE_VALUE = 65.0;
    
    private int getSamplingTime(Resource resource) {
	if (getParameters(resource) == null 
                || getParameters(resource).get("samplingTime") == null)
            return DEFAULT_SAMPLING_TIME;
        try {
            return Integer.parseInt(getParameters(resource)
                    .get("samplingTime"));
        }
        catch (Exception e) {
            logger.error("Error while reading the sampling time", e);
            return DEFAULT_SAMPLING_TIME;
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);
        
        Set<Resource> nodes = getNodes();
        for(Resource node : nodes){
            int newSamplingTime = getSamplingTime(node);
            if(timerPerNodeId.containsKey(node.getId())
                    && samplingTimePerNodeId.get(node.getId()) != newSamplingTime){
                timerPerNodeId.remove(node.getId()).cancel();
            }
            if(!timerPerNodeId.containsKey(node.getId())){
                Timer timer = new Timer();
                timerPerNodeId.put(node.getId(), timer);
                samplingTimePerNodeId.put(node.getId(), newSamplingTime);
                timer.scheduleAtFixedRate(new CpuUtilizationSender(node), 
                        0, newSamplingTime * 1000);
            }
        }
    }
    
    private final class CpuUtilizationSender extends TimerTask {
        private Resource node;
        
        public CpuUtilizationSender(Resource node) {
            this.node = node;
        }
        
        @Override
        public void run() {
            send(TRIAL_SAMPLE_VALUE, node);
	}
    }
    
    
}
