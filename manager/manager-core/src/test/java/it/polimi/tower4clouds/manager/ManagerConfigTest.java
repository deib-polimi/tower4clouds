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

import static org.junit.Assert.assertEquals;
import it.polimi.tower4clouds.manager.ConfigurationException;
import it.polimi.tower4clouds.manager.Env;
import it.polimi.tower4clouds.manager.ManagerConfig;

import org.junit.Before;
import org.junit.Test;

public class ManagerConfigTest {

	@Before
	public void clearProperties() {
		System.clearProperty(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP);
		System.clearProperty(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT);
	}

	@Test
	public void daIPFromSystemPropertyShouldOverrideDefault()
			throws ConfigurationException {
		String expected = "100.100.100.100";
		System.setProperty(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP, expected);
		ManagerConfig.init();
		assertEquals(ManagerConfig.getInstance().getDaIP(), expected);
	}

	@Test
	public void daIPFromArgsShouldOverrideSystemProperty()
			throws ConfigurationException {
		String expected = "100.100.100.100";
		System.setProperty(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP,
				"99.99.99.99");
		ManagerConfig.init(new String[] { "-daip", expected },"");
		assertEquals(ManagerConfig.getInstance().getDaIP(), expected);
	}

	@Test(expected = ConfigurationException.class)
	public void daPortShouldBeANumber() throws ConfigurationException {
		System.setProperty(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT,
				"notaport");
		ManagerConfig.init();
	}

	@Test(expected = ConfigurationException.class)
	public void daPortShouldBeAValidPort() throws ConfigurationException {
		System.setProperty(Env.MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT,
				"9999999");
		ManagerConfig.init();
	}

}
