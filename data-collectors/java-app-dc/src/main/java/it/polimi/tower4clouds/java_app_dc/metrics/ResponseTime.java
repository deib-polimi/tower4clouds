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
package it.polimi.tower4clouds.java_app_dc.metrics;

import it.polimi.tower4clouds.java_app_dc.Metric;
import it.polimi.tower4clouds.model.ontology.Method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseTime extends Metric {

	private static final Logger logger = LoggerFactory
			.getLogger(ResponseTime.class);

	private Map<Long, Map<String, Long>> startTimesPerMethodIdPerThreadId = new ConcurrentHashMap<Long, Map<String, Long>>();

	private static final double DEFAULT_SAMPLING_PROBABILITY = 1;

	@Override
	protected void started(Method method) {
		Long threadId = Thread.currentThread().getId();
		Map<String, Long> startTimePerMethodId = startTimesPerMethodIdPerThreadId
				.get(threadId);
		if (startTimePerMethodId == null) {
			startTimePerMethodId = new ConcurrentHashMap<String, Long>();
			startTimesPerMethodIdPerThreadId
					.put(threadId, startTimePerMethodId);
		}
		startTimePerMethodId.put(method.getId(), System.currentTimeMillis());
	}

	@Override
	protected void ended(Method method) {
		long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		long responseTime = endTime
				- startTimesPerMethodIdPerThreadId.get(threadId).remove(
						method.getId());
		if (startTimesPerMethodIdPerThreadId.get(threadId).isEmpty()) {
			startTimesPerMethodIdPerThreadId.remove(threadId);
		}

		logger.debug("Response Time for method {}: {}", method.getId(),
				responseTime);
		if (shouldMonitor(method) && getSamplingProbability(method) > Math.random()) {
			send(responseTime, method);
		}
	}

	private double getSamplingProbability(Method method) {
		if (getParameters(method) == null
				|| getParameters(method).get("samplingProbability") == null)
			return DEFAULT_SAMPLING_PROBABILITY;
		try {
			return Double.parseDouble(getParameters(method)
					.get("samplingProbability"));
		} catch (Exception e) {
			logger.error("Error while reading the sampling probability", e);
			return DEFAULT_SAMPLING_PROBABILITY;
		}
	}

	@Override
	protected void externalCallStarted() {
		// Nothing to do
	}

	@Override
	protected void externalCallEnded() {
		// Nothing to do
	}

	@Override
	protected boolean isApplicationMetric() {
		return false;
	}

	@Override
	protected boolean isMethodMetric() {
		return true;
	}

	@Override
	protected void configurationUpdated() {
		// Nothing to do
	}

}
