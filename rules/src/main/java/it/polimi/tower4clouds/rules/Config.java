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
package it.polimi.tower4clouds.rules;

import it.polimi.modaclouds.qos_models.ConfigurationException;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

public class Config {

	private static String groupingCategoriesFileName = "/monitoring_grouping_categories.xml";
	private static String monitoringAggregateFunctionsFileName = "/monitoring_aggregate_functions.xml";

	private GroupingCategories groupingCategories;
	private AggregateFunctions monitoringAggregateFunctions;

	private static Config _instance = null;

	private Config() throws ConfigurationException {
		try {
			this.groupingCategories = XMLHelper.deserialize(getClass()
					.getResourceAsStream(groupingCategoriesFileName),
					GroupingCategories.class);
			this.monitoringAggregateFunctions = XMLHelper.deserialize(
					getClass().getResourceAsStream(
							monitoringAggregateFunctionsFileName),
					AggregateFunctions.class);
		} catch (Exception e) {
			throw new ConfigurationException(
					"Error while loading configuration files", e);
		}
	}

	public static Config getInstance() throws ConfigurationException {
		if (_instance == null) {
			_instance = new Config();
		}
		return _instance;
	}

	public GroupingCategories getGroupingCategories() {
		return groupingCategories;
	}

	public AggregateFunctions getMonitoringAggregateFunctions() {
		return monitoringAggregateFunctions;
	}

}
