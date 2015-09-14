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
package it.polimi.tower4clouds.rdf_history_db.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;

public class Main {
	
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	@Parameter(names = { "-h", "--help", "-help" }, help = true, description = "Shows this help" , hidden = true )
	private boolean help = false;
	
	@Parameter(names = "-queueip", description = "Queue endpoint IP address")
	public String queueip = Configuration.DEFAULT_QUEUE_IP;
	
	@Parameter(names = "-queueport", description = "Queue endpoint port")
	public String queueport = Configuration.DEFAULT_QUEUE_PORT;
	
	@Parameter(names = "-dbip", description = "DB endpoint IP address")
	public String dbip = Configuration.DEFAULT_FUSEKI_IP;
	
	@Parameter(names = "-dbpath", description = "DB URL path")
	public String dbpath = Configuration.DEFAULT_FUSEKI_PATH;
	
	@Parameter(names = "-dbport", description = "DB endpoint port")
	public String dbport = Configuration.DEFAULT_FUSEKI_PORT;
	
	@Parameter(names = "-listenerport", description = "Listener endpoint port")
	public String listenerport = Integer.toString(Configuration.DEFAULT_PORT);
	
	public static final String APP_TITLE = "\nHistory-DB Manager\n";
	
	public static void main(String[] args) {
		Main m = new Main();
		JCommander jc = new JCommander(m, args);
		
		if (m.help) {
			System.out.println(APP_TITLE);
			jc.usage();
			System.exit(0);
		}
		
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		
		for (ParameterDescription param : jc.getParameters())
			if (param.isAssigned()) {
				String name = param.getLongestName().replaceAll("-", "");
				String value = null;
				try {
					value = Main.class.getField(name).get(m).toString();
				} catch (Exception e) { }
				
				paramsMap.put(name, value);
			}
		
		perform(paramsMap);
	}
	
	public static void perform(Map<String, String> paramsMap) {
		logger.info("HDB Manager starting...");
		
		Configuration.loadFromEnrivonmentVariables();
		Configuration.loadFromSystemProperties();
		Configuration.loadFromArguments(paramsMap);
		
		List<String> errs = Configuration.checkConfiguration();
		if (errs.size() > 0) {
			logger.error("Configuration errors identified:");
			for (String s : errs)
				logger.error("- " + s);
			logger.error("Exiting...");
			
			System.exit(-1);
		}
		
		if (!DataStore.waitUntilUp()) {
			logger.error("The datastore didn't start in time. Aborting.");
			System.exit(-1);
		}
		logger.info("Found the datastore running.");
		if (!Queue.waitUntilUp()) {
			logger.error("The queue didn't start in time. Aborting.");
			System.exit(-1);
		}
		logger.info("Found the queue running.");
		
		DataStore.reset();
		
		Manager.RUNNING_TIME = -1;
		Manager m1 = new Manager(Configuration.QUEUE_RESULTS);
		Manager m2 = new Manager(Configuration.QUEUE_MODELS);
		Manager m3 = new Manager(Configuration.QUEUE_DELTA_MODELS);
		Manager m4 = new Manager(Configuration.QUEUE_MODELS_DELETE);
		
		try {
			Thread.sleep(500);
		} catch (Exception e) { }
		
		m1.start();
		m2.start();
		m3.start();
		m4.start();
		
		logger.debug("Managers started!");
		
		logger.info("HDB Manager started!");
	}
	
}
