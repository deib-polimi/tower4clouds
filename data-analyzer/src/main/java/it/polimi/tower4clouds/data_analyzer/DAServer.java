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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.deib.rsp_services_csparql.observers.utilities.OutputDataMarshaller;
import it.polimi.deib.rsp_services_csparql.server.rsp_services_csparql_server;
import it.polimi.deib.rsp_services_csparql.streams.utilities.InputDataUnmarshaller;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class DAServer {
	
	public static final Logger logger = LoggerFactory.getLogger(DAServer.class);
	
	public static void main(String[] args) {
		Config config = new Config();
		try {
			new JCommander(config, args);
			String[] rspArgs = new String[1];
			rspArgs[0] = "setup.properties";
			System.setProperty(
					InputDataUnmarshaller.INPUT_DATA_UNMARSHALLER_IMPL_PROPERTY_NAME,
					DAInputDataUnmarshaller.class.getName());
			System.setProperty(
					OutputDataMarshaller.OUTPUT_DATA_MARSHALLER_IMPL_PROPERTY_NAME,
					DAOutputDataMarshaller.class.getName());
			System.setProperty("log4j.configuration", "log4j.properties");
			System.setProperty("rsp_server.static_resources.path",
					config.getKBFolder());
			System.setProperty("csparql_server.port",
					Integer.toString(config.getPort()));
			rsp_services_csparql_server.main(rspArgs);
		} catch (ParameterException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error("Unknown error", e);
		}
	}
}
