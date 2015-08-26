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
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.NodeLoadMetric;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.RXNetworkMetric;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.RackLoad;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.RamUsage;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.StorageCluster;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.TXNetworkMetric;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.Cluster;
import it.polimi.tower4clouds.model.ontology.Node;
import it.polimi.tower4clouds.model.ontology.Rack;
import it.polimi.tower4clouds.model.ontology.Resource;
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
    private Map<String, Cluster> clustersById;
    private Map<String, Rack> racksById;
    
    private Set<Metric> nodeMetrics;
    private Set<Metric> clusterMetrics;
    private Set<Metric> rackMetrics;
    
    private DCAgent dcAgent;
    
    private boolean registryInitialized = false;
    private boolean monitoringStarted = false;
    
    protected static final Registry _INSTANCE = new Registry();
    
    public static Integer CONFIG_SYNC_PERIOD = null;
    public static Integer KEEP_ALIVE = null;
    private static final int DEFAULT_CONFIG_SYNC_PERIOD = 30;
        
    protected Registry(){}
    
    public synchronized void init(String managerIP, int managerPort, Properties dcProperties){
        
        if (registryInitialized)
            throw new RuntimeException("Registry was already initialized");
        
        this.dcProperties = dcProperties;
        
        //Building of nodes and metrics nodes
        nodesById = buildNodesById();
        nodeMetrics = buildNodeMetrics();
        
        //Building of clusters and metrics clusters
        clustersById = buildClustersById();
        clusterMetrics = buildClusterMetrics();
        
        //Building of clusters and metrics clusters
        racksById = buildRacksById();
        rackMetrics = buildRackMetrics();
        
        //Building of the DCAgent
        dcAgent = new DCAgent(new ManagerAPI(managerIP, managerPort));
        dcAgent.addObserver(this);
        
        //Adding observers of nodes metrics to the DCAgent
        for (Metric metric : nodeMetrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Adding observers of cluster metrics to the DCAgent
        for (Metric metric : clusterMetrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Adding observers of rack metrics to the DCAgent
        for (Metric metric : rackMetrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Building of the DCDescriptor
        DCDescriptor dcDescriptor = new DCDescriptor();
        dcDescriptor.addMonitoredResources(getNodeMetrics(), getNodes());
        dcDescriptor.addResources(getNodes());
        dcDescriptor.addMonitoredResources(getClusterMetrics(), getClusters());
        dcDescriptor.addResources(getClusters());
        dcDescriptor.addMonitoredResources(getRackMetrics(), getRacks());
        dcDescriptor.addResources(getRacks());
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
        
        retrieveNodesFromFile(map, (String)this.dcProperties.get(DCProperty.URL_NODES_FILE1), "cluster1");
        retrieveNodesFromFile(map, (String)this.dcProperties.get(DCProperty.URL_NODES_FILE2), "cluster2");
        
        return map;
    }
    
    //Building of the cluster
    private Map<String, Cluster> buildClustersById(){
        Map<String, Cluster> map = new HashMap<String, Cluster>();
        
        map.put("Cluster1", new Cluster("BigCluster", "Cluster1"));
        map.put("Cluster2", new Cluster("BigCluster", "Cluster2"));
        
        return map;
    }
    
    //Building of the cluster
    private Map<String, Rack> buildRacksById(){
        Map<String, Rack> map = new HashMap<String, Rack>();
        
        CsvFileParser fileParser = new CsvFileParser(dcProperties.getProperty(DCProperty.URL_RACKLOAD_METRIC),null);
        fileParser.readLastUpdate(0);
        List<String> racks = fileParser.getData(1);
        
        for(String rack:racks){
            map.put(rack, new Rack("BigRack", rack));
        }
        
        return map;
    }
    
    private void retrieveNodesFromFile(Map<String, Node> map, String url, String type){
        CsvFileParser fileParser = new CsvFileParser(url,"ID,IP");
        fileParser.readUntilTerminationString();
        List<String> nodes = fileParser.getData(1);
        
        for(String node:nodes){
            String nodeId = node.replaceAll("\\.", "_");
            logger.info("Node added: "+nodeId);
            map.put(nodeId, new Node(type, nodeId));
        }
    }
    
    private Set<String> getNodeMetrics() {
        
        Set<String> metricsNames = new HashSet<String>();
	for (Metric metric : nodeMetrics) {
            metricsNames.add(metric.getName());
	}
        
        return metricsNames;
    }
    
    private Set<Metric> buildNodeMetrics() {
	Set<Metric> metrics = new HashSet<Metric>();
        
        //add CPUUtilization metric
        Metric cpuMetric = new CPUUtilization();
        cpuMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_CPU_METRIC));
        metrics.add(cpuMetric);
        
        //add RamUsage metric
        Metric ramMetric = new RamUsage();
        ramMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_RAM_METRIC));
        metrics.add(ramMetric);
        
        //add NodeLoadMetric
        Metric nodeLoadMetric = new NodeLoadMetric();
        nodeLoadMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_NODELOAD_METRIC));
        metrics.add(nodeLoadMetric);
        
        //add TXNetworkMetric
        Metric txNetworkMetric = new TXNetworkMetric();
        txNetworkMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_TXNETWORK_METRIC));
        metrics.add(txNetworkMetric);
        
        //add RXNetworkMetric
        Metric rxNetworkMetric = new RXNetworkMetric();
        rxNetworkMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_RXNETWORK_METRIC));
        metrics.add(rxNetworkMetric);
        
        return metrics;
    }
    
    private Set<String> getClusterMetrics() {
        
        Set<String> metricsNames = new HashSet<String>();
	for (Metric metric : clusterMetrics) {
            metricsNames.add(metric.getName());
	}
        
        return metricsNames;
    }
    
    private Set<Metric> buildClusterMetrics() {
	Set<Metric> metrics = new HashSet<Metric>();
        
        //add Storage metric
        Metric storageMetric = new StorageCluster();
        storageMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_STORAGE_METRIC));
        metrics.add(storageMetric);
        
        return metrics;
    }
    
    private Set<String> getRackMetrics() {
        
        Set<String> metricsNames = new HashSet<String>();
	for (Metric metric : rackMetrics) {
            metricsNames.add(metric.getName());
	}
        
        return metricsNames;
    }
    
    private Set<Metric> buildRackMetrics() {
	Set<Metric> metrics = new HashSet<Metric>();
        
        //add RackLoad metric
        Metric rackLoadMetric = new RackLoad();
        rackLoadMetric.setUrlFileLocation((String)dcProperties.get(DCProperty.URL_RACKLOAD_METRIC));
        metrics.add(rackLoadMetric);
        
        return metrics;
    }
    
    Set<Resource> getNodes() {
        return new HashSet<Resource>(nodesById.values());
    }
    
    Set<Resource> getClusters() {
        return new HashSet<Resource>(clustersById.values());
    }
    
    Set<Resource> getRacks() {
        return new HashSet<Resource>(racksById.values());
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
