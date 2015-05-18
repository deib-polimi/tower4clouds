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

public class BOCDeployment  {
	
	public static void main(String[] args) {
		
		Set<Resource> resources = new HashSet<Resource>();
		
		int nBLTiers = 3;
	
		CloudProvider flexiantCloud = new CloudProvider();
		resources.add(flexiantCloud);
		flexiantCloud.setId("Flexiant");

		VM winVM = new VM();
		resources.add(winVM);
		winVM.setId("WinVM1");
		winVM.setType("WinVM");
		winVM.setCloudProvider(flexiantCloud.getId());

		InternalComponent tomcat = new InternalComponent();
		resources.add(tomcat);
		tomcat.setId("Tomcat1");
		tomcat.setType("Tomcat");
		tomcat.addRequiredComponent(winVM.getId());

		InternalComponent war = new InternalComponent();
		resources.add(war);
		war.setId("War1");
		war.setType("War");
		war.addRequiredComponent(tomcat.getId());

		InternalComponent sqlDB = new InternalComponent();
		resources.add(sqlDB);
		sqlDB.setId("SQLDB1");
		sqlDB.setType("SQLDB");
		sqlDB.addRequiredComponent(winVM.getId());

		for (int i = 0; i < nBLTiers; i++) {
			InternalComponent bLTier = new InternalComponent();
			resources.add(bLTier);
			bLTier.setId("BLTier" + (i + 1));
			bLTier.setType("BLTier");
			bLTier.addRequiredComponent(sqlDB.getId());
			bLTier.addRequiredComponent(winVM.getId());

			war.addRequiredComponent(bLTier.getId());
		}
		Gson gson = new Gson();
		String json = gson.toJson(resources);
		System.out.println(json);
	}

}
