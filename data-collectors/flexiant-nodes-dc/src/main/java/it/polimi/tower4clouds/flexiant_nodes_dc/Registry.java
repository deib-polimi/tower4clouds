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
public class Registry {
    
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
    
    public void init(String managerIP, int managerPort, Properties dcProperties){
        
        if (registryInitialized)
            throw new RuntimeException("Registry was already initialized");
        
        this.dcProperties = dcProperties;
        
        //create clusters map
        clustersById = new HashMap<String, Cluster>();
                
        //Build nodes and metrics nodes
        nodesById = buildNodesById();
        nodeMetrics = buildNodeMetrics();
        
        //add VMs to nodes
        retrieveVmsFromFile();   

        //Build metrics clusters
        clusterMetrics = buildClusterMetrics();
        
        //Build clusters and metrics clusters
        racksById = buildRacksById();
        rackMetrics = buildRackMetrics();
        
        //Build the DCAgent
        dcAgent = new DCAgent(new ManagerAPI(managerIP, managerPort));
        
        //Add observers of nodes metrics to the DCAgent
        for (Metric metric : nodeMetrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Add observers of cluster metrics to the DCAgent
        for (Metric metric : clusterMetrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Add observers of rack metrics to the DCAgent
        for (Metric metric : rackMetrics) {
            logger.debug("Added metric {} as observer of dcagent", metric.getName());
            dcAgent.addObserver(metric);
        }
        
        //Build the DCDescriptor
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
    
    private void start() {
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

    private void stop() {
        if (monitoringStarted) {
            logger.info("Stopping monitoring");
            dcAgent.stop();
            monitoringStarted = false;
        } else {
            logger.warn("Monitoring was not running");
        }
    }
    
    //Build nodes by parsing of remote files
    private Map<String, Node> buildNodesById(){
        Map<String, Node> map = new HashMap<String, Node>();
        
        int i = 1;
        
        while(retrieveNodesFromFile(map, (String)this.dcProperties.get(DCProperty.URL_NODES)+"Cluster"+i+".csv", "Cluster"+i)){
            i++;
        }
        
        return map;
    }
    
    //Build racks
    private Map<String, Rack> buildRacksById(){
        Map<String, Rack> map = new HashMap<String, Rack>();
        
        //retrieve nodes of every rack
        CsvFileParser nodesFileParser = new CsvFileParser("file://"+Registry.class.getResource("/").getPath()+"RackNodes.csv", "Rack,Node,Cluster");
        nodesFileParser.readUntilTerminationString();
        
        CsvFileParser fileParser = new CsvFileParser(dcProperties.getProperty(DCProperty.URL_RACKLOAD_METRIC),null);
        fileParser.readLastUpdate(0);
        List<String> racks = fileParser.getData(1);
        List<String> nodes = nodesFileParser.getData(1);
        List<String> racksId = nodesFileParser.getData(0);
        
        for(String rack:racks){
            Rack newRack = new Rack("BigRack", rack);
            map.put(rack, newRack);
            Set<String> nodesRack = new HashSet<String>();
            for(int i = 0; i < racksId.size(); i++){
                if(racksId.get(i).equals(rack))
                    nodesRack.add(nodes.get(i).replaceAll("\\.", "_"));
            }
            newRack.addNodes(nodesRack);
        }
        
        return map;
    }
    
    private boolean retrieveNodesFromFile(Map<String, Node> map, String url, String type){
        CsvFileParser fileParser = new CsvFileParser(url,"ID,IP");
        if(!fileParser.readUntilTerminationString())
            return false; //return false if cluster doesn't exist
        
        Set<String> clusterNodes = new HashSet<String>();
        Cluster cluster = new Cluster("BigCluster", type);
        
        //add cluster
        clustersById.put(type, cluster);
        
        //add nodes
        List<String> nodes = fileParser.getData(1);
        
        for(String node:nodes){
            String nodeId = node.replaceAll("\\.", "_");
            logger.info("Node added: "+nodeId);
            map.put(nodeId, new Node(type, nodeId));
            clusterNodes.add(nodeId);
        }
        
        //add nodes to the cluster
        cluster.addNodes(clusterNodes);
        
        return true;
    }
    
    private void retrieveVmsFromFile(){
        
        Map<String, Set<String>> vmsByNodeId;
        
        //Initialize map of vms
        vmsByNodeId = new HashMap<String, Set<String>>();
        for(String nodeId:nodesById.keySet()){
            vmsByNodeId.put(nodeId, new HashSet<String>());
        }
        
        //retrieve vms from remote file
        CsvFileParser fileParser = new CsvFileParser(dcProperties.getProperty(DCProperty.URL_VMS), "Date,Server UUID,Server IP,Number of CPUs,RAM,Custer,Node UUID,Node IP,Server status,All key data,");
        fileParser.readLastUpdate(0);
        List<String> nodesId = fileParser.getData(7);
        List<String> vmsId = fileParser.getData(2);
        
        for(int i = 0; i < vmsId.size(); i++){
            String vmId = vmsId.get(i).replaceAll("\\.", "_");
            String nodeId = nodesId.get(i).replaceAll("\\.", "_");
            vmsByNodeId.get(nodeId).add(vmId);
            logger.info("Added VM: "+vmId+" to node: "+nodeId);
        }
        
        //add vms to the nodes
        for(Node node:nodesById.values()){
            node.addVms(vmsByNodeId.get(node.getId()));
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
