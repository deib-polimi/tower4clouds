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
package it.polimi.tower4clouds.manager;

import org.apache.commons.validator.routines.UrlValidator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

// TODO requires refactoring, parameters should probably be in the server project
public class ManagerConfig {

	public static final String MODEL_GRAPH_NAME = "http://www.modaclouds.eu/graphs#model";

	@Parameter(names = "-help", help = true, description = "Shows this message")
	private boolean help;

	@Parameter(names = "-daip", description = "DA endpoint IP address")
	private String daIP;

	@Parameter(names = "-daport", description = "DA endpoint port")
	private int daPort;

	@Parameter(names = "-mmip", description = "Monitoring Manager endpoint IP address")
	private String mmIP;

	@Parameter(names = "-mmport", description = "Monitoring Manager endpoint port")
	private int mmPort;

	@Parameter(names = "-rdf-history-db-port", description = "RDF History DB endpoint port")
	private int rdfHistoryDbPort;

	@Parameter(names = "-rdf-history-db-ip", description = "RDF History DB endpoint IP address")
	private String rdfHistoryDbIP;

	@Parameter(names = "-version", description = "Shows the version number")
	private boolean version = false;

	@Override
	public String toString() {
		String toString = "\tDA URL: " + getDaUrl() + "\n"
				+ "\tMonitoring Manager Port: " + mmPort + "\n"
				+ "\tMonitoring Manager IP: " + mmIP;
		if (rdfHistoryDbIP != null) {
			toString += "\n\tRDF History DB Port: " + rdfHistoryDbPort + "\n"
					+ "\tRDF History DB IP: " + rdfHistoryDbIP;
		}
		return toString;
	}

	private static ManagerConfig _instance = null;
	public static String usage = null;

	public static void init(String[] CLIargs, String programName)
			throws ConfigurationException {
		_instance = new ManagerConfig();
		if (CLIargs != null) {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				JCommander jc = new JCommander(_instance, CLIargs);
				jc.setProgramName(programName);
				jc.usage(stringBuilder);
			} catch (ParameterException e) {
				throw new ConfigurationException(e.getMessage());
			}
			usage = stringBuilder.toString();
		}
	}

	public static void init() throws ConfigurationException {
		_instance = new ManagerConfig();
	}

	public static ManagerConfig getInstance() {
		return _instance;
	}

	private ManagerConfig() throws ConfigurationException {
		UrlValidator validator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

		try {
			daPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT, "8175"));
			mmPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_PORT, "8170"));
			rdfHistoryDbPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT, "31337"));
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
					"The chosen port is not a valid number");
		}

		daIP = getEnvVar(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP, "127.0.0.1");
		mmIP = getEnvVar(Env.MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_IP, "127.0.0.1");
		rdfHistoryDbIP = getEnvVar(Env.MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_IP, null);

		if (!validator.isValid(getDaUrl()))
			throw new ConfigurationException(getDaUrl() + " is not a valid URL");

	}

	public boolean isHelp() {
		return help;
	}

	public String getDaIP() {
		return daIP;
	}

	public void setDaIP(String daIP) {
		this.daIP = daIP;
	}

	public boolean isVersion() {
		return version;
	}

	public int getDaPort() {
		return daPort;
	}

	public void setDaPort(int daPort) {
		this.daPort = daPort;
	}

	public String getDaUrl() {
		return "http://" + daIP + ":" + daPort;
	}

	public String getMmIP() {
		return mmIP;
	}

	public void setMmIP(String mmIP) {
		this.mmIP = mmIP;
	}

	public int getMmPort() {
		return mmPort;
	}

	public void setMmPort(int mmPort) {
		this.mmPort = mmPort;
	}
	
	public String getRdfHistoryDbIP() {
		return rdfHistoryDbIP;
	}
	
	public int getRdfHistoryDbPort() {
		return rdfHistoryDbPort;
	}
	
	public void setRdfHistoryDbIP(String rdfHistoryDbIP) {
		this.rdfHistoryDbIP = rdfHistoryDbIP;
	}
	
	public void setRdfHistoryDbPort(int rdfHistoryDbPort) {
		this.rdfHistoryDbPort = rdfHistoryDbPort;
	}

	private String getEnvVar(String varName, String defaultValue) {
		String var = System.getProperty(varName);
		if (var == null)
			var = System.getenv(varName);
		if (var == null)
			var = defaultValue;
		return var;
	}

}
