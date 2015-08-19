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
import it.polimi.tower4clouds.model.ontology.Node;
import it.polimi.tower4clouds.model.ontology.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class Registry implements Observer{
    
    private static final Logger logger = LoggerFactory.getLogger(Registry.class);
    
    private Properties dcProperties;
    
    private Map<String, Node> nodesById;
    
    private Set<Metric> metrics;
    
    private DCAgent dcAgent;
    
    private boolean registryInitialized = false;
    private boolean monitoringStarted = false;
    
    protected static final Registry _INSTANCE = new Registry();
    
    public static Integer CONFIG_SYNC_PERIOD = null;
    public static Integer KEEP_ALIVE = null;
    private static final int DEFAULT_CONFIG_SYNC_PERIOD = 30;
    
    private static final String DEFAULT_CONFIG_FILE_LOCATION = "/etc/opt/flexiant-nodes-dc/config";
    
    protected Registry(){}
    
    public synchronized void init(String managerIP, int managerPort, Properties dcProperties){
        
        if (registryInitialized)
            throw new RuntimeException("Registry was already initialized");
        
        //Acquisition of the DC properties
        if(dcProperties == null){
            this.dcProperties = new Properties();
            try{
                this.dcProperties.load(new FileInputStream(DEFAULT_CONFIG_FILE_LOCATION));
            }
            catch(FileNotFoundException ex){
                throw new RuntimeException("Properties file not found");
            }
            catch(IOException ex){
                throw new RuntimeException("Error while parsing properties file");
            }
        }
        else
            this.dcProperties = dcProperties;
        
        //Building of nodes and metrics
        nodesById = buildNodesById();
        this.metrics = buildMetrics();
        
        //Building of the DCAgent
        dcAgent = new DCAgent(new ManagerAPI(managerIP, managerPort));
        dcAgent.addObserver(this);
        
        //Adding observers of the metrics to the DCAgent
        for (Metric metric : metrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Building of the DCDescriptor
        DCDescriptor dcDescriptor = new DCDescriptor();
        dcDescriptor.addMonitoredResources(getMetrics(), getNodes());
        dcDescriptor.addResources(getNodes());
        dcDescriptor.setConfigSyncPeriod(CONFIG_SYNC_PERIOD != null ? CONFIG_SYNC_PERIOD
					: DEFAULT_CONFIG_SYNC_PERIOD);
	dcDescriptor.setKeepAlive(KEEP_ALIVE != null ? KEEP_ALIVE
                                        : (DEFAULT_CONFIG_SYNC_PERIOD + 15));
        
        dcAgent.setDCDescriptor(dcDescriptor);
        registryInitialized = true;
        
    }
    
    private synchronized void start() {
        if (!registryInitialized)
            throw new RuntimeException("Registry was not initialized");
        if (!monitoringStarted) {
            logger.info("Starting monitoring");
            dcAgent.stop();
            dcAgent.start();
            monitoringStarted = true;
        } else {
            logger.warn("Monitoring was already started");
        }
    }

    private synchronized void stop() {
        if (monitoringStarted) {
            logger.info("Stopping monitoring");
            dcAgent.stop();
            monitoringStarted = false;
        } else {
            logger.warn("Monitoring was not running");
        }
    }
    
    //Building of the nodes by the parsing of remote files
    private Map<String, Node> buildNodesById(){
        Map<String, Node> map = new HashMap<String, Node>();
        
        CsvFileParser fileParser = new CsvFileParser((String)this.dcProperties.get(DCProperty.URL_NODES_FILE1),"ID,IP");
        fileParser.readUntilTerminationString();
        List<String> nodes = fileParser.getData(1);
        
        for(String node:nodes){
            logger.info("Node added: "+node);
            map.put(node, new Node("cluster1", node));
        }
        
        fileParser = new CsvFileParser((String)this.dcProperties.get(DCProperty.URL_NODES_FILE2),"ID,IP");
        fileParser.readUntilTerminationString();
        nodes = fileParser.getData(1);
        
        for(String node:nodes){
            logger.info("Node added: "+node);
            map.put(node, new Node("cluster2", node));
        }
        
        return map;
    }
    
    private Set<String> getMetrics() {
        
        Set<String> metricsNames = new HashSet<String>();
	for (Metric metric : metrics) {
            metricsNames.add(metric.getName());
	}
        
        return metricsNames;
    }
    
    private Set<Metric> buildMetrics() {
	Set<Metric> metrics = new HashSet<Metric>();
        
        //add CPUUtilization metric
        Metric cpuMetric = new CPUUtilization();
        cpuMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_CPU_METRIC));
        metrics.add(cpuMetric);
        
        return metrics;
    }
    
    Set<Resource> getNodes() {
        return new HashSet<Resource>(nodesById.values());
    }

    @Override
    public void update(Observable o, Object arg) {
        //Nothing to do
    }
    
    public static void initialize(String managerIP, int managerPort, Properties dcProperties){
        _INSTANCE.init(managerIP, managerPort, dcProperties);
    }
    
    public static void startMonitoring(){
	_INSTANCE.start();
    }
    
    public static void stopMonitoring(){
	_INSTANCE.stop();
    }
    
}
