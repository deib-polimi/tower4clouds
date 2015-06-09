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
	
	@Parameter(names = "-queueip", description = "Queue endpoint IP address (Default: 127.0.0.1)")
	public String queueip = null;
	
	@Parameter(names = "-queueport", description = "Queue endpoint port (Default: 5672)")
	public String queueport = null;
	
	@Parameter(names = "-dbip", description = "DB endpoint IP address (Default: 127.0.0.1)")
	public String dbip = null;
	
	@Parameter(names = "-dbpath", description = "DB URL path (Default: /ds)")
	public String dbpath = null;
	
	@Parameter(names = "-dbport", description = "DB endpoint port (Default: 3030)")
	public String dbport = null;
	
	@Parameter(names = "-listenerport", description = "Listener endpoint port (Default: 31337)")
	public String listenerport = null;
	
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
		
		DataStore.waitUntilUp();
		Queue.waitUntilUp();
		
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
