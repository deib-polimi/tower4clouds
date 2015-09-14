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
        
	ManagerAPI manager = new ManagerAPI(managerIP, managerPort);
        
        //install rules on manager
	manager.installRules(XMLHelper.deserialize(DCMain.class
			.getResourceAsStream("/rules.xml"),
			MonitoringRules.class));
        
        manager.installRules(XMLHelper.deserialize(DCMain.class
			.getResourceAsStream("/rulesGroupByCluster.xml"),
			MonitoringRules.class));
        
        // Create HTTP observer to monitor sent datas  
        manager.registerHttpObserver("AverageCpuUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageRamUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageLoad", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageTXNetwork", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageRXNetwork", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageStorage", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageRackLoad", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        manager.registerHttpObserver("AverageCpuUtilizationByCluster", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        
        DCMain.main(args);
    }
    
}
