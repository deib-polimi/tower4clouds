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

public class ManagerConfig {

	public static final String MODEL_GRAPH_NAME = "http://www.modaclouds.eu/graphs#model";

	@Parameter(names = "-help", help = true, description = "Shows this message")
	private boolean help;

	@Parameter(names = "-daip", description = "DA endpoint IP address")
	private String daIP;

	@Parameter(names = "-daport", description = "DA endpoint port")
	private int daPort;

	@Parameter(names = "-mmip", description = "Monitoring Manager endpoint IP public address")
	private String mmIP;

	@Parameter(names = "-mmport", description = "Monitoring Manager endpoint port")
	private int mmPort;

	private static ManagerConfig _instance = null;
	public static String usage = null;

	public static void init(String[] CLIargs) throws ConfigurationException {
		_instance = new ManagerConfig();
		if (CLIargs != null) {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				JCommander jc = new JCommander(_instance, CLIargs);
				jc.setProgramName("monitoring-manager");
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
					Env.MODACLOUDS_MONITORING_DA_ENDPOINT_PORT, "8175"));
			mmPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_MONITORING_MANAGER_PORT, "8170"));
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
					"The chosen port is not a valid number");
		}

		daIP = getEnvVar(Env.MODACLOUDS_MONITORING_DA_ENDPOINT_IP,
				"127.0.0.1");
		mmIP = getEnvVar(Env.MODACLOUDS_MONITORING_MANAGER_IP, "127.0.0.1");

		if (!validator.isValid(getDaUrl()))
			throw new ConfigurationException(getDaUrl() + " is not a valid URL");

	}

	@Override
	public String toString() {
		return "\tDA URL: " + getDaUrl() + "\n" + "\tMonitoring Manager Port: "
				+ mmPort + "\n" + "\tMonitoring Manager IP: " + mmIP;
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

	private String getEnvVar(String varName, String defaultValue) {
		String var = System.getProperty(varName);
		if (var == null)
			var = System.getenv(varName);
		if (var == null)
			var = defaultValue;
		return var;
	}

}