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
package it.polimi.tower4clouds.flexiant_nodes_dc;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.rules.MonitoringRules;

/**
 *
 * @author davide
 */
public class FlexiantDCTest {
    
    private static final String DEFAULT_GRAPHITE_IP = "localhost";
    private static final int DEFAULT_GRAPHITE_PORT = 8001;
    private static final String DEFAULT_MANAGER_IP = "localhost";
    private static final int DEFAULT_MANAGER_PORT = 8170;
    
    public static void main(String[] args) throws Exception {
        
        String graphiteIP = DEFAULT_GRAPHITE_IP;
        int graphitePort = DEFAULT_GRAPHITE_PORT;

        String managerIP = DEFAULT_MANAGER_IP;
        int managerPort = DEFAULT_MANAGER_PORT;
                
        /* Commented code for debug use only: manual set properties without configuration file 
        flexDCProp.put(DCProperty.URL_NODES, "https://cp.sd1.flexiant.net/nodeid/");
        flexDCProp.put(DCProperty.URL_CPU_METRIC, "https://cp.sd1.flexiant.net/nodecpu10/");
        flexDCProp.put(DCProperty.URL_RAM_METRIC, "https://cp.sd1.flexiant.net/noderam10/");
        flexDCProp.put(DCProperty.URL_NODELOAD_METRIC, "https://cp.sd1.flexiant.net/nodeload10/");
        flexDCProp.put(DCProperty.URL_TXNETWORK_METRIC, "https://cp.sd1.flexiant.net/nodenet10/");
        flexDCProp.put(DCProperty.URL_RXNETWORK_METRIC, "https://cp.sd1.flexiant.net/nodenet10/");
        flexDCProp.put(DCProperty.URL_STORAGE_METRIC, "https://cp.sd1.flexiant.net/storage10/");
        flexDCProp.put(DCProperty.URL_RACKLOAD_METRIC, "https://cp.sd1.flexiant.net/rackload10/upsload.csv");
        flexDCProp.put(DCProperty.URL_VMS, "https://cp.sd1.flexiant.net/VMPlacement/FCOVMPlacement.csv");
        */
        
	ManagerAPI manager = new ManagerAPI(managerIP, managerPort);
        /*
        //install rules on manager
	manager.installRules(XMLHelper.deserialize(DCMain.class
			.getResourceAsStream("/rules.xml"),
			MonitoringRules.class));
        
        manager.installRules(XMLHelper.deserialize(DCMain.class
			.getResourceAsStream("/rulesGroupByCluster.xml"),
			MonitoringRules.class));
        */
        // Create HTTP observer to monitor sent datas  
        manager.registerHttpObserver("CpuUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("RamUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("NodeLoad", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("TXNetwork", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("RXNetwork", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("StorageMetric", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("RackLoadMetric", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        
        DCMain.main(args);
    }
    
}
