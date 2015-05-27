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
package it.polimi.tower4clouds.java_app_dc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public aspect MonitorAspect {

	private pointcut monitoredMethod(Monitor annotation) : execution(@Monitor * *(..)) && @annotation(annotation);

	before(Monitor annotation) : monitoredMethod(annotation) {
		Registry.notifyStart(annotation.type());
	}

	after(Monitor annotation): monitoredMethod(annotation){
		Registry.notifyEnd(annotation.type());
	}

}
