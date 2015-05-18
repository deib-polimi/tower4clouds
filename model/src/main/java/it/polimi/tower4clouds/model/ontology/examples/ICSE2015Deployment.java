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

import com.google.gson.Gson;

public class ICSE2015Deployment {

	public static void main(String[] args) {

		Set<Resource> resources = new HashSet<Resource>();
		try {

			CloudProvider amazonCloud = new CloudProvider();
			resources.add(amazonCloud);
			amazonCloud.setId("amazon");
			amazonCloud.setType("IaaS");

			VM amazonFrontendVM = new VM();
			resources.add(amazonFrontendVM);
			amazonFrontendVM.setId("frontend1");
			amazonFrontendVM.setType("Frontend");
			amazonFrontendVM.setCloudProvider(amazonCloud.getId());

			InternalComponent amazonMic = new InternalComponent();
			resources.add(amazonMic);
			amazonMic.setId("mic1");
			amazonMic.setType("Mic");
			amazonMic.addRequiredComponent(amazonFrontendVM.getId());

			resources.add(addMethod(amazonMic, "register"));
			resources.add(addMethod(amazonMic, "saveAnswers"));
			resources.add(addMethod(amazonMic, "answerQuestions"));

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Gson gson = new Gson();
		String json = gson.toJson(resources);
		System.out.println(json);
	}

	private static Method addMethod(InternalComponent iComponent,
			String methodType) throws URISyntaxException {
		Method method = new Method(iComponent.getId(), methodType);
		iComponent.addProvidedMethod(method.getId());
		return method;
	}

}
