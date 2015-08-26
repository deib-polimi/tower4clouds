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
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Metric implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(Metric.class);

    private DCAgent dcAgent;

    private String urlFileLocation;
    
    private final Map<String, Timer> timerPerResourceId = new ConcurrentHashMap<String, Timer>();
    private final Map<String, Integer> samplingTimePerResourceId = new ConcurrentHashMap<String, Integer>();
    
    private static final int DEFAULT_SAMPLING_TIME = 60;
    
    protected MetricsType metricType;
    
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
        Set<Resource> resources;
        
        //Determine which type of target.
        switch(metricType){
            case CLUSTER_METRIC:
                resources = Registry._INSTANCE.getClusters();
                break;
            case RACK_METRIC:
                resources = Registry._INSTANCE.getRacks();
                break;
            default:
                resources = Registry._INSTANCE.getNodes();
        }
        
        for(Resource resource : resources){
            if(shouldMonitor(resource)){
                int newSamplingTime = getSamplingTime(resource);
                if(timerPerResourceId.containsKey(resource.getId())
                        && samplingTimePerResourceId.get(resource.getId()) != newSamplingTime){
                    timerPerResourceId.remove(resource.getId()).cancel();
                }
                if(!timerPerResourceId.containsKey(resource.getId())){
                    Timer timer = new Timer();
                    timerPerResourceId.put(resource.getId(), timer);
                    samplingTimePerResourceId.put(resource.getId(), newSamplingTime);
                    createTask(timer, resource, newSamplingTime);
                }
            }
            else{
                Timer timer = timerPerResourceId.remove(resource.getId());
                if (timer != null)
                    timer.cancel();
                samplingTimePerResourceId.remove(resource.getId());
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
    private void createTask(Timer timer, Resource node, int samplingTime){
        timer.scheduleAtFixedRate(new MetricSender(node), 
                        0, samplingTime * 1000);
    }
    
    private class MetricSender extends TimerTask{
        private Resource node;
        private CsvFileParser fileParser;
        
        //constructor: initialize a CsvFileParser object which handle the reading
        //of the remote file
        public MetricSender(Resource node) {
            this.node = node;
            String url = getUrl(node);
            logger.info("URL: "+url);
            fileParser = new CsvFileParser(url, null);  
        }
        
        @Override
        public void run() {
            logger.info("Getting Sample...");
            long first = System.currentTimeMillis();
            send(getSample(fileParser, node), node);
            logger.info("Sample retrieved and sent in: "+(System.currentTimeMillis()-first)+ "ms");
	}
        
    }
    
    //method which build url of the remote file to parse.
    protected String getUrl(Resource resource){
        String url;
        String nodeIp = resource.getId().replaceAll("_", "\\.");
        url = getUrlFileLocation();
        if(resource.getType().equals("cluster2"))
            url += resource.getType()+"-"+nodeIp+".csv";
        else
            url += nodeIp+".csv";
        return url;
    }
    
    protected abstract Number getSample(CsvFileParser fileParser, Resource resource);
    
    protected Map<String, String> getParameters(Resource resource) {
        if (this.dcAgent != null)
            return this.dcAgent.getParameters(resource, getName());
        return null;
    }

    public String getUrlFileLocation() {
        return urlFileLocation;
    }

    public void setUrlFileLocation(String urlFileLocation) {
        this.urlFileLocation = urlFileLocation;
    }    

}
