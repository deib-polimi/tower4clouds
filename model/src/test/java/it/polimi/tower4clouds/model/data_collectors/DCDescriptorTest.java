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
package it.polimi.tower4clouds.model.data_collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;

import org.junit.Test;

public class DCDescriptorTest {

	@Test
	public void test() {
		Resource method = new Method();
		method.setId("method1");
                method.setType("type1");
		Resource ic = new InternalComponent("type1", "ic1");
		((InternalComponent)ic).addProvidedMethod(method.getId());
		DCDescriptor dc = new DCDescriptor();
		dc.addMonitoredResource("ResponseTime", method);
		dc.addResource(method);
		dc.addResource(ic);
		DCDescriptor fromJsonDC = DCDescriptor.fromJson(dc.toJson());
		assertTrue(fromJsonDC instanceof DCDescriptor);
		assertEquals(((DCDescriptor)fromJsonDC).getResources().size(), 2);
	}

}
