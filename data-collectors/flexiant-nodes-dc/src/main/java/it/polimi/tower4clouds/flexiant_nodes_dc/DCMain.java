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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.rules.MonitoringRules;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
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
    
    @Parameter(names = "-managerip", description = "Manager IP address")
    public String managerIp = null;
	
    @Parameter(names = "-managerport", description = "Manager port")
    public String managerPort = null;
    
    @Parameter(names = "-config-file", description = "Config file path")
    public String configFilePath = null;
	
    @Parameter(names = "-placement-file", description = "Placement file path")
    public String placementFilePath = null;
    
    @Parameter(names = "-nodes-file", description = "nodes.csv file path")
    public String nodesFilePath = null;
    
    @Parameter(names = "-clusters-file", description = "clusters.csv file path")
    public String clustersFilePath = null;
    
    @Parameter(names = "-racks-file", description = "racks.csv file path")
    public String racksFilePath = null;
    
    private static final String DEFAULT_MANAGER_IP = "localhost";
    private static final String DEFAULT_MANAGER_PORT = "8170";
    private static final String DEFAULT_CONFIG_FILE_PATH = DCMain.class.getResource("/").getPath()+"config.properties";
    private static final String DEFAULT_PLACEMENT_FILE_PATH = DCMain.class.getResource("/").getPath()+"placement.csv";
    private static final String DEFAULT_NODES_FILE_PATH = DCMain.class.getResource("/").getPath()+"nodes.csv";
    private static final String DEFAULT_CLUSTERS_FILE_PATH = DCMain.class.getResource("/").getPath()+"clusters.csv";
    private static final String DEFAULT_RACKS_FILE_PATH = DCMain.class.getResource("/").getPath()+"racks.csv";
    
    private static final String ENV_VAR_MANAGER_IP = "MODACLOUDS_TOWER4CLOUDS_MANAGER_IP";
    private static final String ENV_VAR_MANAGER_PORT = "MODACLOUDS_TOWER4CLOUDS_MANAGER_PORT";
    private static final String ENV_VAR_URL_CONFIG_FILE = "MODACLOUDS_TOWER4CLOUDS_FLEXDC_CONFIG_FILE";
    private static final String ENV_VAR_URL_PLACEMENT_FILE = "MODACLOUDS_TOWER4CLOUDS_FLEXDC_PLACEMENT_FILE";
    private static final String ENV_VAR_URL_NODES_FILE = "MODACLOUDS_TOWER4CLOUDS_FLEXDC_NODES_FILE";
    private static final String ENV_VAR_URL_CLUSTERS_FILE = "MODACLOUDS_TOWER4CLOUDS_FLEXDC_CLUSTERS_FILE";
    private static final String ENV_VAR_URL_RACKS_FILE = "MODACLOUDS_TOWER4CLOUDS_FLEXDC_RACKS_FILE";
    
    public static void main(String[] args) throws Exception {
        
        DCMain mainInstance = new DCMain();
        
        Properties flexDCProp = new Properties();
        
        //load default values
        mainInstance.loadDefaultValues();
        
        //try to load from environment variables
        mainInstance.loadFromEnrivonmentVariables();
        
        //try yo load form arguments
        JCommander jc = new JCommander(mainInstance, args);
        
        HashMap<String, String> paramsMap = new HashMap<String, String>();
		
	for (ParameterDescription param : jc.getParameters())
            if (param.isAssigned()) {
                String name = param.getLongestName().replaceAll("-", "");
		String value = null;
		try {
                    value = DCMain.class.getField(name).get(mainInstance).toString();
		} catch (Exception e) { }
                paramsMap.put(name, value);
            }
        
        mainInstance.loadFromArguments(paramsMap);
        
        //load properties from the config file
        try{
            // Marco temp fix
            flexDCProp.load(new FileInputStream(mainInstance.configFilePath));
        }
        catch(FileNotFoundException ex){
            logger.error("Properties file not found");
            throw new RuntimeException("Properties file not found");
        }
        catch(IOException ex){
            logger.error("Error while parsing properties file");
            throw new RuntimeException("Error while parsing properties file");
        }
        
        int port;
        
        //convert manager port
        try{
            port = Integer.parseInt(mainInstance.managerPort);
        }
        catch(NumberFormatException ex){
            logger.error("Error while converting manager port - must be an integer");
            throw new RuntimeException("Error while parsing properties file");
        }
        
        DCProperty.placementFilePath = mainInstance.placementFilePath;
        DCProperty.nodesFilePath = mainInstance.nodesFilePath;
        DCProperty.clustersFilePath = mainInstance.clustersFilePath;
        DCProperty.racksFilePath = mainInstance.racksFilePath;
        
        Registry.initialize(mainInstance.managerIp, port, flexDCProp);
        Registry.startMonitoring();
    }
    
    private void loadDefaultValues(){
        managerIp = DEFAULT_MANAGER_IP;
        managerPort = DEFAULT_MANAGER_PORT;
        configFilePath = DEFAULT_CONFIG_FILE_PATH;
        placementFilePath = DEFAULT_PLACEMENT_FILE_PATH;
        nodesFilePath = DEFAULT_NODES_FILE_PATH;
        clustersFilePath = DEFAULT_CLUSTERS_FILE_PATH;
        racksFilePath = DEFAULT_RACKS_FILE_PATH;
    }
    
    private void loadFromEnrivonmentVariables(){
        
        if(System.getenv(ENV_VAR_MANAGER_IP) != null)
            managerIp = System.getenv(ENV_VAR_MANAGER_IP);
        
        if(System.getenv(ENV_VAR_MANAGER_PORT) != null)
            managerPort = System.getenv(ENV_VAR_MANAGER_PORT);
        
        if(System.getenv(ENV_VAR_URL_CONFIG_FILE) != null)
            configFilePath = System.getenv(ENV_VAR_URL_CONFIG_FILE);
        
        if(System.getenv(ENV_VAR_URL_PLACEMENT_FILE) != null)
            placementFilePath = System.getenv(ENV_VAR_URL_PLACEMENT_FILE);
        
        if(System.getenv(ENV_VAR_URL_NODES_FILE) != null)
            nodesFilePath = System.getenv(ENV_VAR_URL_NODES_FILE);
        
        if(System.getenv(ENV_VAR_URL_CLUSTERS_FILE) != null)
            clustersFilePath = System.getenv(ENV_VAR_URL_CLUSTERS_FILE);
        
        if(System.getenv(ENV_VAR_URL_RACKS_FILE) != null)
            racksFilePath = System.getenv(ENV_VAR_URL_RACKS_FILE);
        
    }
    
    private void loadFromArguments(Map<String, String> paramsMap) {
	if (paramsMap == null)
            return;
		
	if (paramsMap.get("managerip") != null)
            managerIp = paramsMap.get("managerip");
        if (paramsMap.get("managerport") != null)
            managerPort = paramsMap.get("managerport");
        if (paramsMap.get("configfile") != null)
            configFilePath = paramsMap.get("configfile");
        if (paramsMap.get("placementfile") != null)
            placementFilePath = paramsMap.get("placementfile");
        if (paramsMap.get("nodesfile") != null)
            nodesFilePath = paramsMap.get("nodesfile");
        if (paramsMap.get("clustersfile") != null)
            clustersFilePath = paramsMap.get("clustersfile");
        if (paramsMap.get("racksfile") != null)
            racksFilePath = paramsMap.get("racksfile");
    }
    
}