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
package it.polimi.tower4clouds.rdf_history_db.observer;

import it.polimi.tower4clouds.rdf_history_db.observer.rest.Producer;

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
	
	@Parameter(names = "-fakemessages", description = "Test the tool by sending a number of fake messages (Default: 0)")
	public String fakemessages = null;
	
	@Parameter(names = "-waitfakemessages", description = "The ms to wait between each fake message (Default: 1000 ms == 1 second)")
	public String waitfakemessages = null;
	
	public static final String APP_TITLE = "\nHistory-DB Metrics Observer\n";
	
	public static void main(String[] args) {
		args = new String[] {"-fakemessages", "30", "-waitfakemessages", "1000"};
		
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
		logger.info("HDB Metrics Observer starting...");
		
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
		
		Queue.waitUntilUp();
		
		MetricsObserver.RUNNING_TIME = -1;
		MetricsObserver mo = new MetricsObserver();
		mo.start();
		logger.debug("MetricsObserver started!");
		
		logger.info("HDB Metrics Observer started!");
		
		int fakeMessages = -1;
		int waitFakeMessages = -1;
		
		if (paramsMap != null) {
			if (paramsMap.get("fakemessages") != null)
				fakeMessages = Integer.parseInt(paramsMap.get("fakemessages"));
			if (paramsMap.get("waitfakemessages") != null)
				waitFakeMessages = Integer.parseInt(paramsMap.get("waitfakemessages"));
		}
        
		if (fakeMessages > 0) {
	        try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				logger.error("Error while waiting.", e);
			}
	        
	        logger.debug("Starting the producer...");
	        if (waitFakeMessages >= 0)
	        	Producer.test(fakeMessages, waitFakeMessages);
	        else 
	        	Producer.test(fakeMessages, 1000);
		}
        
	}
}
