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

import com.beust.jcommander.Parameter;

public class Config {

	@Parameter(names = "-port", description = "Data Analyzer endpoint port")
	private int port = 8175;
	
	@Parameter(names = "-kbloc", description = "Knowledge Base folder location", required = true)
	private String kBFolder;
	
	@Parameter(names = "-help", help = true, description = "Shows this message")
	private boolean help;

	public int getPort() {
		return port;
	}

	public String getKBFolder() {
		return kBFolder;
	}
}
