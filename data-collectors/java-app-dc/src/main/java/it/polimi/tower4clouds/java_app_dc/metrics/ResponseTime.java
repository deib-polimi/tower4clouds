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

	private double samplingProbability = 1;

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
		if (shouldMonitor(method) && samplingProbability > Math.random()) {
			send(String.valueOf(responseTime), method);
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
		String value = getParameters().get("samplingProbability");
		if (value != null) {
			try {
				samplingProbability = Double.parseDouble(value);
			} catch (Exception e) {
				logger.error("Error while reading the sampling probability", e);
			}
		}
	}

}
