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
package it.polimi.tower4clouds.data_analyzer;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class DAConfig {

	@Parameter(names = "-port", description = "Data Analyzer endpoint port")
	private int port = 8175;

	@Parameter(names = "-kbloc", description = "Knowledge Base folder location")
	private String kBFolder = "/tmp/tower4clouds/kb";

	@Parameter(names = "-help", help = true, description = "Shows this message")
	private boolean help;
	
	@Parameter(names = "-version", description = "Shows the version number")
	private boolean version = false;

	public String usage = null;
	
	public DAConfig(String[] args, String programName) throws ConfigurationException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			JCommander jc = new JCommander(this, args);
			jc.setProgramName(programName);
			jc.usage(stringBuilder);
		} catch (ParameterException e) {
			throw new ConfigurationException(e.getMessage());
		}
		usage = stringBuilder.toString();
	}
	
	public String getUsage() {
		return usage;
	}
	
	public boolean isVersion() {
		return version;
	}
	
	public boolean isHelp() {
		return help;
	}

	public int getPort() {
		return port;
	}

	public String getKBFolder() {
		return kBFolder;
	}
}
