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

import it.polimi.tower4clouds.data_collector_library.DCAgent;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Metric implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(Metric.class);

	private DCAgent dcAgent;

	protected abstract void started(Method method);

	protected abstract void ended(Method method);

	protected abstract void externalCallStarted();

	protected abstract void externalCallEnded();

	protected abstract boolean isApplicationMetric();

	protected abstract boolean isMethodMetric();

	protected String getName() {
		return getClass().getSimpleName();
	}

	protected void send(Number value, Resource resource) {
		if (dcAgent != null) {
			dcAgent.send(resource, getName(), value);
		} else {
			logger.warn("Monitoring is not required, data won't be sent");
		}
	}

	protected abstract void configurationUpdated();

	protected boolean shouldMonitor(Resource resource) {
		if (dcAgent == null) {
			logger.error("{}: DCAgent was null", this.toString());
			return false;
		}
		return dcAgent.shouldMonitor(resource, getName());
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.debug("Sync update called");
		this.dcAgent = (DCAgent) o;
		logger.debug("{}: DCAgent set: {}", this.toString(), dcAgent != null);
		configurationUpdated();
	}

	protected Map<String, String> getParameters() {
		if (this.dcAgent != null)
			return this.dcAgent.getParameters(getName());
		return null;
	}

	protected Set<Method> getMonitoredMethods() {
		return Registry._INSTANCE.getMethods();
	}

	protected InternalComponent getMonitoredApplication() {
		return Registry._INSTANCE.getApplication();
	}

}
