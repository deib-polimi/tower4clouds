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

	public abstract Map<String, DCConfiguration> getDCConfigurationByMetric(
			String dataCollectorId) throws Exception;

	public abstract Set<String> getRequiredMetrics()
			throws UnexpectedAnswerFromServerException, IOException;

	public abstract Set<String> getObservableMetrics()
			throws UnexpectedAnswerFromServerException, IOException;

	public abstract void enableRule(String id) throws NotFoundException, IOException;

	public abstract void disableRule(String id) throws NotFoundException, IOException;

}