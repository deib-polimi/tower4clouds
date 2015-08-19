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
package it.polimi.tower4clouds.use_cases.fakeapps4testing.flexiantnodesdc;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.flexiant_nodes_dc.DCProperty;
import it.polimi.tower4clouds.flexiant_nodes_dc.Registry;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.rules.MonitoringRules;
import java.util.Properties;

/**
 *
 * @author davide
 */
public class FlexiantNodesDCTester {
    
    private static final String graphiteIP = "localhost";
    private static final int graphitePort = 8001;
    private static String managerIP = "localhost";
    private static int managerPort = 8170;
    
    public static void main(String[] args) throws Exception {
        
        Properties flexDCProp = new Properties();
        flexDCProp.put(DCProperty.URL_NODES_FILE1, "https://cp.sd1.flexiant.net/nodeid/Cluster1.csv");
        flexDCProp.put(DCProperty.URL_NODES_FILE2, "https://cp.sd1.flexiant.net/nodeid/Cluster2.csv");
        flexDCProp.put(DCProperty.URL_CPU_METRIC, "https://cp.sd1.flexiant.net/nodecpu10/");

	ManagerAPI manager = new ManagerAPI(managerIP, managerPort);
	manager.installRules(XMLHelper.deserialize(FlexiantNodesDCTester.class
			.getResourceAsStream("/rules4FlexiantNodesDCTester.xml"),
			MonitoringRules.class));
        manager.registerHttpObserver("CpuUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        Registry.initialize(managerIP, managerPort, flexDCProp);
        Registry.startMonitoring();
        
        Thread.sleep(5 * 60 * 1000);
        //Close the application after 5 minutes
        Registry.stopMonitoring();
        System.exit(0);
        
    }
        
    
}
