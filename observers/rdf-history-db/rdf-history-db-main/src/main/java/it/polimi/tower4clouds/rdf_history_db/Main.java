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
package it.polimi.tower4clouds.rdf_history_db;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	@Parameter(names = { "-h", "--help", "-help" }, help = true, description = "Shows this help", hidden = true)
	private boolean help = false;

	@Parameter(names = "-queueip", description = "Queue endpoint IP address")
	public String queueip = it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_QUEUE_IP;

	@Parameter(names = "-queueport", description = "Queue endpoint port")
	public String queueport = it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_QUEUE_PORT;

	@Parameter(names = "-dbip", description = "DB endpoint IP address")
	public String dbip = it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_FUSEKI_IP;

	@Parameter(names = "-dbpath", description = "DB URL path")
	public String dbpath = it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_FUSEKI_PATH;

	@Parameter(names = "-dbport", description = "DB endpoint port")
	public String dbport = it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_FUSEKI_PORT;

	@Parameter(names = "-listenerport", description = "Listener endpoint port")
	public String listenerport = Integer.toString(it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_PORT);

	@Parameter(names = "-fakemessages", description = "Test the tool by sending a number of fake messages")
	public String fakemessages = Integer.toString(it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_FAKE_MESSAGES);

	@Parameter(names = "-waitfakemessages", description = "The ms to wait between each fake message")
	public String waitfakemessages = Integer.toString(it.polimi.tower4clouds.rdf_history_db.observer.Configuration.DEFAULT_WAIT_FAKE_MESSAGES);

	public static String APP_NAME;
	public static String APP_FILE_NAME;
	public static String APP_VERSION;

	public static void main(String[] args) {
//		args = "-fakemessages 10 -waitfakemessages 1000".split(" ");

		PropertiesConfiguration releaseProperties = null;
		try {
			releaseProperties = new PropertiesConfiguration(
					"release.properties");
		} catch (ConfigurationException e) {
			logger.error("Internal error", e);
			System.exit(1);
		}
		APP_NAME = releaseProperties.getString("application.name");
		APP_FILE_NAME = releaseProperties.getString("dist.file.name");
		APP_VERSION = releaseProperties.getString("release.version");

		Main m = new Main();
		JCommander jc = new JCommander(m, args);

		if (m.help) {
			jc.setProgramName(APP_FILE_NAME);
			jc.usage();
			System.exit(0);
		}

		logger.info("{} {}", APP_NAME, APP_VERSION);

		HashMap<String, String> paramsMap = new HashMap<String, String>();

		for (ParameterDescription param : jc.getParameters())
			if (param.isAssigned()) {
				String name = param.getLongestName().replaceAll("-", "");
				String value = null;
				try {
					value = Main.class.getField(name).get(m).toString();
				} catch (Exception e) {
				}

				paramsMap.put(name, value);
			}

		perform(paramsMap);
	}

	public static void perform(Map<String, String> paramsMap) {
		it.polimi.tower4clouds.rdf_history_db.manager.Main.perform(paramsMap);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) { }

		it.polimi.tower4clouds.rdf_history_db.observer.Main.perform(paramsMap);
	}

}
