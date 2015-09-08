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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class DCMain {
    
    private static final Logger logger = LoggerFactory.getLogger(DCMain.class);
    
    private static final String DEFAULT_GRAPHITE_IP = "localhost";
    private static final int DEFAULT_GRAPHITE_PORT = 8001;
    private static final String DEFAULT_MANAGER_IP = "localhost";
    private static final int DEFAULT_MANAGER_PORT = 8170;
    private static final String DEFAULT_CONFIG_FILE_LOCATION = "/etc/opt/flexiant-nodes-dc/config";
    
    private static final String ENV_VAR_MANAGER_IP = "MODACLOUDS_TOWER4CLOUDS_MANAGER_IP";
    private static final String ENV_VAR_MANAGER_PORT = "MODACLOUDS_TOWER4CLOUDS_MANAGER_PORT";
    private static final String ENV_VAR_URL_CONFIG_FILE = "MODACLOUDS_TOWER4CLOUDS_FLEXDC_CONFIG_FILE";
    
    public static void main(String[] args) throws Exception {
        
        Properties flexDCProp = new Properties();
                
        //retrieve env variables
        Map<String, String> env = System.getenv();
        
        String managerIP = DEFAULT_MANAGER_IP;
        int managerPort = DEFAULT_MANAGER_PORT;
        String urlFileProperties = DEFAULT_CONFIG_FILE_LOCATION;
        
        String graphiteIP = DEFAULT_GRAPHITE_IP;
        int graphitePort = DEFAULT_GRAPHITE_PORT;
        
        //set the manager IP
        if(env.containsKey(ENV_VAR_MANAGER_IP)){
            managerIP = env.get(ENV_VAR_MANAGER_IP);
        }
        
        //set the manager port
        if(env.containsKey(ENV_VAR_MANAGER_PORT)){
            try{
                managerPort = Integer.parseInt(env.get(ENV_VAR_MANAGER_PORT));
            }
            catch(NumberFormatException ex){
                logger.warn("The property "+ENV_VAR_MANAGER_PORT+" must be integer");
            }
        }
        
        //set url of the configuration file
        if(env.containsKey(ENV_VAR_URL_CONFIG_FILE)){
            urlFileProperties = env.get(ENV_VAR_URL_CONFIG_FILE);
        }
        
        //load properties from the config file
        try{
//            flexDCProp.load(new FileInputStream(urlFileProperties));
        	
        	// Marco temp fix
        	flexDCProp.load(DCMain.class.getResourceAsStream("/config.properties"));
        }
        catch(FileNotFoundException ex){
            logger.error("Properties file not found");
            throw new RuntimeException("Properties file not found");
        }
        catch(IOException ex){
            logger.error("Error while parsing properties file");
            throw new RuntimeException("Error while parsing properties file");
        }
        
        //check if there is relations file path in the arguments
        if(args.length == 2 && args[0].equals("--relations-file")){
            flexDCProp.put(DCProperty.RELATIONS_FILE_PATH, args[1]);
        }
        
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
        
//	ManagerAPI manager = new ManagerAPI(managerIP, managerPort);

        // Commented code for debug use only: use the rules.xml file inside the resources folder to install rules
//	manager.installRules(XMLHelper.deserialize(DCMain.class
//			.getResourceAsStream("/rules.xml"),
//			MonitoringRules.class));
        
//        manager.installRules(XMLHelper.deserialize(DCMain.class
//			.getResourceAsStream("/rulesGroupByCluster.xml"),
//			MonitoringRules.class));
        
        // Commented code for debug use only: create HTTP observer to monitor sent datas  
//        manager.registerHttpObserver("CpuUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
//        manager.registerHttpObserver("RamUtilization", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
//        manager.registerHttpObserver("NodeLoad", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
//        manager.registerHttpObserver("TXNetwork", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
//        manager.registerHttpObserver("RXNetwork", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
//        manager.registerHttpObserver("StorageMetric", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
//        manager.registerHttpObserver("RackLoadMetric", "http://" + graphiteIP + ":" + graphitePort + "/data", "GRAPHITE");
        
        
        
        Registry.initialize(managerIP, managerPort, flexDCProp);
        Registry.startMonitoring();
    }
    
}
