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
package it.polimi.tower4clouds.manager.api;

import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IManagerAPI {

	public abstract MonitoringRules getRules()
			throws UnexpectedAnswerFromServerException, IOException;

	public abstract void uninstallRule(String id)
			throws NotFoundException, IOException;

	public abstract Map<String, DCDescriptor> getRegisteredDataCollectors()
			throws IOException;

	public abstract void unregisterDataCollector(String id)
			throws NotFoundException, IOException;

	public abstract Collection<Resource> getResources()
			throws Exception;

	public abstract void deleteResource(String id)
			throws NotFoundException, IOException;

	public abstract void installRules(MonitoringRules rules)
			throws Exception;

	public abstract Observer registerHttpObserver(String metric,
			String callbackUrl, String format)
			throws NotFoundException, IOException;

	public abstract Observer registerSocketObserver(String metric,
			String observerHost, int observerPort, SocketProtocol protocol,
			String format) throws NotFoundException,
			IOException;

	public abstract void registerDataCollector(String id,
			DCDescriptor dCDescriptor)
			throws Exception;

	public abstract String registerDataCollector(DCDescriptor dCDescriptor)
			throws Exception;

	public abstract void keepAlive(String dataCollectorId)
			throws Exception;

	public abstract Map<String, Set<DCConfiguration>> getDCConfigurationsByMetric(
			String dataCollectorId) throws Exception;

	public abstract Set<String> getRequiredMetrics()
			throws UnexpectedAnswerFromServerException, IOException;

	public abstract Set<String> getObservableMetrics()
			throws UnexpectedAnswerFromServerException, IOException;

	public abstract void enableRule(String id) throws NotFoundException, IOException;

	public abstract void disableRule(String id) throws NotFoundException, IOException;

}