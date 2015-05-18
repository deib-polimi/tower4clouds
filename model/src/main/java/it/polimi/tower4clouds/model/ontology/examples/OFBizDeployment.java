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
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.model.ontology.VM;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class OFBizDeployment {

	public static void main(String[] args) {

		Set<Resource> resources = new HashSet<Resource>();
		try {

			CloudProvider amazonCloud = new CloudProvider();
			resources.add(amazonCloud);
			amazonCloud.setId("Amazon");

			VM amazonFrontendVM = new VM();
			resources.add(amazonFrontendVM);
			amazonFrontendVM.setId("FrontendVM1");
			amazonFrontendVM.setType("FrontendVM");
			amazonFrontendVM.setCloudProvider(amazonCloud.getId());

			VM amazonBackendVM = new VM();
			resources.add(amazonBackendVM);
			amazonBackendVM.setId("BackendVM1");
			amazonBackendVM.setType("BackendVM");
			amazonBackendVM.setCloudProvider(amazonCloud.getId());

			InternalComponent amazonJVM = new InternalComponent();
			resources.add(amazonJVM);
			amazonJVM.setId("JVM1");
			amazonJVM.setType("JVM");
			amazonJVM.addRequiredComponent(amazonFrontendVM.getId());

			InternalComponent amazonMySQL = new InternalComponent();
			resources.add(amazonMySQL);
			amazonMySQL.setId("MySQL1");
			amazonMySQL.setType("MySQL");
			amazonJVM.addRequiredComponent(amazonBackendVM.getId());

			InternalComponent amazonFrontend = new InternalComponent();
			resources.add(amazonFrontend);
			amazonFrontend.setId("Frontend1");
			amazonFrontend.setType("Frontend");
			amazonFrontend.addRequiredComponent(amazonJVM.getId());
			amazonFrontend.addRequiredComponent(amazonMySQL.getId());

			resources.add(addMethod(amazonFrontend, "addtocartbulk"));
			resources.add(addMethod(amazonFrontend, "checkLogin"));
			resources.add(addMethod(amazonFrontend, "checkoutoptions"));
			resources.add(addMethod(amazonFrontend, "addtocartbulk"));
			resources.add(addMethod(amazonFrontend, "login"));
			resources.add(addMethod(amazonFrontend, "logout"));
			resources.add(addMethod(amazonFrontend, "main"));
			resources.add(addMethod(amazonFrontend, "orderhistory"));
			resources.add(addMethod(amazonFrontend, "quickadd"));

			resources.add(addMethod(amazonMySQL, "create"));
			resources.add(addMethod(amazonMySQL, "read"));
			resources.add(addMethod(amazonMySQL, "update"));
			resources.add(addMethod(amazonMySQL, "delete"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println(resources);
	}

	private static Method addMethod(InternalComponent iComponent, String methodType)
			throws URISyntaxException {
		Method method = new Method(iComponent.getId(), methodType);
		iComponent.addProvidedMethod(method.getId());
		return method;
	}

}
