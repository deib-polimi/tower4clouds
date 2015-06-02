package it.polimi.tower4clouds.use_cases.fakeapps4testing.sda;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.common.net.UnexpectedAnswerFromServerException;
import it.polimi.tower4clouds.data_collector_library.DCAgent;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

public class SDAExample {
	private static final String sdaUrl = "http://127.0.0.1/data";
	private static final String managerHost = "localhost";
	private static final int managerPort = 8170;

	public static void main(String[] args)
			throws UnexpectedAnswerFromServerException, IOException,
			JAXBException {
		ManagerAPI manager = new ManagerAPI(managerHost, managerPort);
		manager.registerRules(XMLHelper.deserialize(
				SDAExample.class.getResourceAsStream("/rules4SDAExample.xml"),
				MonitoringRules.class));

		DCAgent dcAgent = new DCAgent(manager);
		DCDescriptor dcDescriptor = new DCDescriptor();
		dcDescriptor.setConfigSyncPeriod(60);
		dcAgent.setDCDescriptor(dcDescriptor);
		dcAgent.start();

		// The following should be checked periodically (e.g., every 60 seconds)
		// by the SDA
		Set<String> requiredMetrics = manager.getRequiredMetrics();
		for (String requiredMetric : requiredMetrics) {
			if (requiredMetric.startsWith("Forecast_")) {
				dcDescriptor.addMonitoredResource(requiredMetric,
						new Resource()); // registering a data collector for
											// metric
											// Forecast_AverageFrontendCPU and
											// for all resources
				dcAgent.refresh(); // required to be called when dcDescriptor is
									// updated

				String metricToBeForecast = requiredMetric.substring(9);
				System.out.println("Forecast required for metric "
						+ metricToBeForecast);
				manager.registerHttpObserver(metricToBeForecast, sdaUrl,
						"TOWER/JSON"); // An observer can
										// be attached asking for different
										// formats: RDF/JSON, TOWER/JSON,
										// GRAPHITE,... TOWER/JSON is the new
										// serialization format for sending
										// monitoring data through the network
										// replacing RDF/JSON. Example of json
										// datum:
										// {"resourceId": "frontend1", "metric":
										// "AverageFrontendCPU", "value": 0.14,
										// "timestamp": 123131231 }
			}
		}

		// When data is received at the SDA url for resource frontend1:
		if (dcAgent.shouldMonitor(new Resource(null, "frontend1"),
				"Forecast_AverageFrontendCPU")) {
			Map<String, String> parameters = dcAgent
					.getParameters("Forecast_AverageFrontendCPU");
			dcAgent.send(new Resource(null, "frontend1"),
					"Forecast_AverageFrontendCPU", computeForecast(parameters));
		}
	}

	private static double computeForecast(Map<String, String> parameters) {
		return 0.34;
	}

}
