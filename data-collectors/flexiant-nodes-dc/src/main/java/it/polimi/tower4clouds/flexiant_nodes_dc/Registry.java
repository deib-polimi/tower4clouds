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
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.CPUUtilization;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Node;
import it.polimi.tower4clouds.model.ontology.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class Registry implements Observer{
    
    private static final Logger logger = LoggerFactory.getLogger(Registry.class);
    
    private Map<String, Node> nodesById;
    //private Map<String, InternalComponent> nodesById;
    
    private Set<Metric> metrics;
    
    private DCAgent dcAgent;
    
    protected static final Registry _INSTANCE = new Registry();
    
    public static Integer CONFIG_SYNC_PERIOD = null;
    public static Integer KEEP_ALIVE = null;
    private static final int DEFAULT_CONFIG_SYNC_PERIOD = 30;
    
    protected Registry(){
        
    }
    
    public void init(String managerIP, int managerPort, String[] nodeIds){
        
        nodesById = buildNodesById(nodeIds);
        this.metrics = getMetrics();
        
        dcAgent = new DCAgent(new ManagerAPI(managerIP, managerPort));
        dcAgent.addObserver(this);
        
        for (Metric metric : metrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        DCDescriptor dcDescriptor = new DCDescriptor();
        dcDescriptor.addMonitoredResources(getNodeMetrics(), getNodes());
        dcDescriptor.addResources(getNodes());
        dcDescriptor.setConfigSyncPeriod(CONFIG_SYNC_PERIOD != null ? CONFIG_SYNC_PERIOD
					: DEFAULT_CONFIG_SYNC_PERIOD);
	dcDescriptor.setKeepAlive(KEEP_ALIVE != null ? KEEP_ALIVE
                                        : (DEFAULT_CONFIG_SYNC_PERIOD + 15));
        
        dcAgent.setDCDescriptor(dcDescriptor);
        dcAgent.start();
        
    }
        
    private static Map<String, Node> buildNodesById(String[] nodeIds){
        Map<String, Node> map = new HashMap<String, Node>();
        
        for(String id:nodeIds){
            map.put(id, new Node("bigNode", id));
        }
        
        return map;
    }
    
    private Set<String> getNodeMetrics() {
        
        Set<String> metricsNames = new HashSet<String>();
	for (Metric metric : metrics) {
            metricsNames.add(metric.getName());
	}
        
        return metricsNames;
    }
    
    private static Set<Metric> getMetrics() {
	Set<Metric> metrics = new HashSet<Metric>();
        metrics.add(new CPUUtilization());
        return metrics;
    }
    
    Set<Resource> getNodes() {
        return new HashSet<Resource>(nodesById.values());
    }

    @Override
    public void update(Observable o, Object arg) {
        //Nothing to do
    }
    
    /* Main di prova temporaneo */
    private static final String graphiteIP = "localhost";
    private static final int graphitePort = 8001;
    private static String managerIP = "localhost";
    private static int managerPort = 8170;
    
    public static void main(String[] args) throws Exception{
        
        //nodes
        String[] ids = new String[1];
        ids[0] = "c57308e7-defd-32ac-80a5-ddb85db05b6a";
        
        ManagerAPI manager = new ManagerAPI(managerIP, managerPort);
        manager.registerHttpObserver("CpuUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        _INSTANCE.init(managerIP, managerPort, ids);
    }
    
}
