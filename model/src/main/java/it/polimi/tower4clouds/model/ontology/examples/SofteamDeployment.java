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
package it.polimi.tower4clouds.model.ontology.examples;

import it.polimi.tower4clouds.model.ontology.CloudProvider;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.model.ontology.VM;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

public class SofteamDeployment {

	public static void main(String[] args) {

		int numberOfAgents = 2;

		Set<Resource> resources = new HashSet<Resource>();

		CloudProvider amazonCloud = new CloudProvider();
		resources.add(amazonCloud);
		amazonCloud.setId("Amazon");

		VM adminServer = new VM();
		resources.add(adminServer);
		adminServer.setId("AdministrationServer1");
		adminServer.setType("AdministrationServer");
		adminServer.setCloudProvider(amazonCloud.getId());

		InternalComponent serverApp = new InternalComponent();
		serverApp.setType("ServerApp");
		serverApp.setId("ServerApp1");
		serverApp.addRequiredComponent(adminServer.getId());
		resources.add(serverApp);

		for (int i = 0; i < numberOfAgents; i++) {
			VM mainAgent = new VM();
			mainAgent.setId("MainAgent" + (i + 1));
			;
			mainAgent.setCloudProvider(amazonCloud.getId());
			mainAgent.setType("MainAgent");
			resources.add(mainAgent);

			InternalComponent agentApp = new InternalComponent();
			agentApp.setId("agentApp" + (i + 1));
			agentApp.setType("AgentApp");
			agentApp.addRequiredComponent(mainAgent.getId());
			resources.add(agentApp);

			serverApp.addRequiredComponent(agentApp.getId());
		}
		Gson gson = new Gson();
		String json = gson.toJson(resources);
		System.out.println(json);
	}

}
