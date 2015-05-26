package it.polimi.tower4clouds.java_app_dc.metrics;

import it.polimi.tower4clouds.java_app_dc.Metric;
import it.polimi.tower4clouds.model.ontology.Method;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectiveResponseTime extends Metric {

	private Map<Long, Map<String, Long>> startTimesPerMethodPerThreadId = new ConcurrentHashMap<Long, Map<String, Long>>();
	private Map<Long, Long> externalStartTimes = new ConcurrentHashMap<Long, Long>();

	private static final Logger logger = LoggerFactory
			.getLogger(EffectiveResponseTime.class);

	private double samplingProbability = 1;

	@Override
	protected void started(Method method) {
		Long threadId = Thread.currentThread().getId();
		Map<String, Long> startTimePerMethodId = startTimesPerMethodPerThreadId
				.get(threadId);
		if (startTimePerMethodId == null) {
			startTimePerMethodId = new ConcurrentHashMap<String, Long>();
			startTimesPerMethodPerThreadId.put(threadId, startTimePerMethodId);
		}
		startTimePerMethodId.put(method.getId(), System.currentTimeMillis());
	}

	@Override
	protected void ended(Method method) {
		long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		long effectiveResponseTime = endTime
				- startTimesPerMethodPerThreadId.get(threadId).remove(
						method.getId());
		if (startTimesPerMethodPerThreadId.get(threadId).isEmpty()) {
			startTimesPerMethodPerThreadId.remove(threadId);
		}

		logger.debug("Effective Response Time for method {}: {}",
				method.getId(), effectiveResponseTime);

		if (shouldMonitor(method) && samplingProbability > Math.random()) {
			send(String.valueOf(effectiveResponseTime), method);
		}
	}

	@Override
	protected void externalCallStarted() {
		long threadId = Thread.currentThread().getId();
		Long externalCallStartTime = externalStartTimes.get(threadId);
		if (externalCallStartTime != null) {
			logger.warn("The beginning of an external call was declared inside the "
					+ "scope of an external call. Effective Response Time may be inaccurate");
		} else {
			externalStartTimes.put(threadId, System.currentTimeMillis());
		}
	}

	@Override
	protected void externalCallEnded() {
		Long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		Long externalCallStartTime = externalStartTimes.remove(threadId);
		if (externalCallStartTime == null) {
			logger.error("Declaring the end of an external call outside the scope of an external call."
					+ " Effective Response Time may be inaccurate");
		} else {
			long externalTime = endTime - externalCallStartTime;
			Map<String, Long> startTimesPerMethod = startTimesPerMethodPerThreadId
					.get(threadId);
			if (startTimesPerMethod == null) {
				return;
			}
			for (Entry<String, Long> entry : startTimesPerMethod.entrySet()) {
				entry.setValue(entry.getValue() + externalTime);
			}
		}
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
