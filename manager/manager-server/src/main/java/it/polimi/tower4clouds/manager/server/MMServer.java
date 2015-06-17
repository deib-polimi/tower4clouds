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
package it.polimi.tower4clouds.manager.server;

import it.polimi.deib.csparql_rest_api.exception.ServerErrorException;
import it.polimi.tower4clouds.manager.ConfigurationException;
import it.polimi.tower4clouds.manager.ManagerConfig;
import it.polimi.tower4clouds.manager.MonitoringManager;

import java.io.IOException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jena.atlas.web.HttpException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMServer extends Application {
	public static String APP_NAME;
	public static String APP_FILE_NAME;
	public static String APP_VERSION;
	
	private MonitoringManager manager = null;
	private static final String apiVersion = "v1";

	private static Logger logger = LoggerFactory.getLogger(MMServer.class);

	public MMServer(MonitoringManager manager) {
		this.manager = manager;
	}

	public static void main(String[] args) {
		PropertiesConfiguration releaseProperties = null;
		try {
			releaseProperties = new PropertiesConfiguration(
					"release.properties");
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			logger.error("Internal error", e);
			System.exit(1);
		}
		APP_NAME = releaseProperties.getString("application.name");
		APP_FILE_NAME = releaseProperties.getString("dist.file.name");
		APP_VERSION = releaseProperties.getString("release.version");
		
		try {
			ManagerConfig.init(args, APP_FILE_NAME);

			if (ManagerConfig.getInstance().isHelp()) {
				logger.info(ManagerConfig.usage);
			} else if (ManagerConfig.getInstance().isVersion()) {
				logger.info("Version: {}",
						APP_VERSION);
			} else {
				
				logger.info("{} {}", APP_NAME, APP_VERSION);
				logger.info("Current configuration:\n{}", ManagerConfig
						.getInstance().toString());

				MonitoringManager manager = new MonitoringManager(
						ManagerConfig.getInstance());

				System.setProperty("org.restlet.engine.loggerFacadeClass",
						"org.restlet.ext.slf4j.Slf4jLoggerFacade");
				Component component = new Component();

				Server server = new Server(Protocol.HTTP, ManagerConfig
						.getInstance().getMmPort());
				Context context = new Context();
				context.getParameters().add("maxThreads", "500");
				context.getParameters().add("maxTotalConnections", "500");
				server.setContext(context);
				component.getServers().add(server);
				component.getClients().add(Protocol.CLAP);
				component.getDefaultHost().attach("", new MMServer(manager));

				logger.info("Starting Monitoring Manager server on port "
						+ ManagerConfig.getInstance().getMmPort());
				component.start();
			}
		} catch (ConfigurationException e) {
			logger.error("Configuration problem: " + e.getMessage());
			logger.error("Run \"" + APP_FILE_NAME + " -help\" for help");
			System.exit(1);
		} catch (HttpException | IOException | ServerErrorException e) {
			logger.error("Connection problem: {}", e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			logger.error("Unknown error", e);
			System.exit(1);
		}
	}

	public Restlet createInboundRoot() {

		// String server_address = component.getServers().get(0).getAddress();
		// if (server_address == null) {
		// server_address = "http://localhost";
		// server_address = server_address
		// + ":"
		// + String.valueOf(component.getServers().get(0)
		// .getActualPort());
		// }
		//
		// getContext().getAttributes().put("complete_server_address",
		// server_address);
		getContext().getAttributes().put("manager", manager);

		Router router = new Router(getContext());
		router.setDefaultMatchingMode(Template.MODE_EQUALS);

		router.attach("/" + apiVersion + "/monitoring-rules",
				MultipleRulesDataServer.class);
		router.attach("/" + apiVersion + "/monitoring-rules/{id}",
				SingleRuleDataServer.class);
		router.attach("/" + apiVersion + "/metrics",
				MultipleMetricsDataServer.class);
		router.attach("/" + apiVersion + "/metrics/{metricname}/observers",
				MultipleObserversDataServer.class);
		router.attach(
				"/" + apiVersion + "/metrics/{metricname}/observers/{id}",
				SingleObserverDataServer.class);
		router.attach("/" + apiVersion + "/required-metrics",
				MultipleRequiredMetricsDataServer.class);
		router.attach("/" + apiVersion + "/resources",
				MultipleResourcesDataServer.class);
		router.attach("/" + apiVersion + "/resources/{id}",
				SingleResourceDataServer.class);

		router.attach("/" + apiVersion + "/monitoring-rules/{id}/actions",
				MultipleActionsServer.class);
		router.attach("/" + apiVersion + "/data-collectors",
				MultipleDataCollectorServer.class);
		router.attach("/" + apiVersion + "/data-collectors/{id}",
				SingleDataCollectorServer.class);
		router.attach("/" + apiVersion + "/data-collectors/{id}/keepalive",
				DCKeepAliveServer.class);
		router.attach("/" + apiVersion + "/data-collectors/{id}/configuration",
				DCConfigurationServer.class);

		Redirector redirector = new Redirector(getContext(),
				"/webapp/index.html", Redirector.MODE_CLIENT_PERMANENT);
		router.attach("/webapp", redirector);
		router.attach("/webapp/", redirector);

		final Directory dir = new Directory(getContext(), new LocalReference(
				"clap://class/webapp"));
		dir.setListingAllowed(false);
		dir.setDeeplyAccessible(true);
		dir.setIndexName("index");
		TemplateRoute route = router.attach("/webapp/", dir);
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		return router;
	}
}
