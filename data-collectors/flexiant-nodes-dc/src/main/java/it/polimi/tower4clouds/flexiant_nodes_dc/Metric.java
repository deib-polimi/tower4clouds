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
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Metric implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(Metric.class);

    private DCAgent dcAgent;

    private String urlFileLocation;
    
    private final Map<String, Timer> timerPerNodeId = new ConcurrentHashMap<String, Timer>();
    private final Map<String, Integer> samplingTimePerNodeId = new ConcurrentHashMap<String, Integer>();
    
    private static final int DEFAULT_SAMPLING_TIME = 5;
    
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
    
    private boolean shouldMonitor(Resource resource) {
        if (dcAgent == null) {
                logger.error("{}: DCAgent was null", this.toString());
                return false;
        }
        return dcAgent.shouldMonitor(resource, getName());
    }

    public void update(Observable o, Object arg) {
        this.dcAgent = (DCAgent) o;
        Set<Resource> nodes = getNodes();
        for(Resource node : nodes){
            if(shouldMonitor(node)){
                int newSamplingTime = getSamplingTime(node);
                if(timerPerNodeId.containsKey(node.getId())
                        && samplingTimePerNodeId.get(node.getId()) != newSamplingTime){
                    timerPerNodeId.remove(node.getId()).cancel();
                }
                if(!timerPerNodeId.containsKey(node.getId())){
                    Timer timer = new Timer();
                    timerPerNodeId.put(node.getId(), timer);
                    samplingTimePerNodeId.put(node.getId(), newSamplingTime);
                    createTask(timer, node, newSamplingTime);
                }
            }
            else{
                Timer timer = timerPerNodeId.remove(node.getId());
                if (timer != null)
                    timer.cancel();
                samplingTimePerNodeId.remove(node.getId());
            }
        }
    }
    
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
      
    //Abstract method which create a task (one for each node) that will acquire and 
    //send the sample periodically.
    protected abstract void createTask(Timer timer, Resource node, int samplingTime);

    protected Map<String, String> getParameters(Resource resource) {
        if (this.dcAgent != null)
            return this.dcAgent.getParameters(resource, getName());
        return null;
    }

    protected Set<Resource> getNodes() {
        return Registry._INSTANCE.getNodes();
    }

    public String getUrlFileLocation() {
        return urlFileLocation;
    }

    public void setUrlFileLocation(String urlFileLocation) {
        this.urlFileLocation = urlFileLocation;
    }    

}
