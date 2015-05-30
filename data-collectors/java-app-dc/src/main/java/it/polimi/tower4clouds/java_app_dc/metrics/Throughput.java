package it.polimi.tower4clouds.java_app_dc.metrics;

import it.polimi.tower4clouds.java_app_dc.Metric;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Throughput extends Metric {
	
	private static final Logger logger = LoggerFactory
			.getLogger(Throughput.class);

	private final Map<String, Integer> counterPerMethodId4Methods = new ConcurrentHashMap<String, Integer>();
	private int counter4App;
	private final Map<String, Timer> timerPerMethodId = new ConcurrentHashMap<String, Timer>();
	private final Map<String, Integer> samplingTimePerMethodId = new ConcurrentHashMap<String, Integer>();
	private Timer appTimer;
	private int appSamplingTime;
	private static final int DEFAULT_SAMPLING_TIME = 60;

	@Override
	protected void started(Method method) {
		// Nothing to do
	}

	@Override
	protected void ended(Method method) {
		Integer count = counterPerMethodId4Methods.get(method.getId());
		if (count != null) {
			counterPerMethodId4Methods.put(method.getId(), count + 1);
		}
		counter4App++;
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
		return true;
	}

	@Override
	protected boolean isMethodMetric() {
		return true;
	}

	@Override
	protected void configurationUpdated() {
		Set<Method> methods = getMonitoredMethods();
		for (Method method : methods) {
			if (shouldMonitor(method)) {
				int newSamplingTime = getSamplingTime();
				if (timerPerMethodId.containsKey(method.getId())
						&& samplingTimePerMethodId.get(method.getId()) != newSamplingTime) {
					timerPerMethodId.remove(method.getId()).cancel();
				}
				if (!timerPerMethodId.containsKey(method.getId())) {
					counterPerMethodId4Methods.put(method.getId(), 0);
					Timer timer = new Timer();
					timerPerMethodId.put(method.getId(), timer);
					samplingTimePerMethodId
							.put(method.getId(), newSamplingTime);
					timer.scheduleAtFixedRate(new ThroughputSender4Methods(
							method), 0, newSamplingTime * 1000);
				}
			} else {
				Timer timer = timerPerMethodId.remove(method.getId());
				if (timer != null)
					timer.cancel();
				counterPerMethodId4Methods.remove(method.getId());
			}
		}
		InternalComponent app = getMonitoredApplication();
		if (app != null && shouldMonitor(app)) {
			int newSamplingTime = getSamplingTime();
			if (appTimer != null && appSamplingTime != newSamplingTime) {
				appTimer.cancel();
				appTimer = null;
			}
			if (appTimer == null) {
				counter4App = 0;
				appTimer = new Timer();
				appSamplingTime = newSamplingTime;
				appTimer.scheduleAtFixedRate(new ThroughputSender4App(
						getMonitoredApplication()), 0, newSamplingTime * 1000);
			}
		} else {
			if (appTimer != null)
				appTimer.cancel();
			appTimer = null;
		}
	}
	
	private int getSamplingTime() {
		if (getParameters() == null
				|| getParameters().get("samplingTime") == null)
			return DEFAULT_SAMPLING_TIME;
		try {
			return Integer.parseInt(getParameters()
					.get("samplingTime"));
		} catch (Exception e) {
			logger.error("Error while reading the sampling time", e);
			return DEFAULT_SAMPLING_TIME;
		}
	}
	
	private final class ThroughputSender4Methods extends TimerTask {
		private Resource resource;

		public ThroughputSender4Methods(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void run() {
			send((double) counterPerMethodId4Methods
					.get(resource.getId())
					/ samplingTimePerMethodId.get(resource.getId()), resource);
			counterPerMethodId4Methods.put(resource.getId(), 0);
		}
	}

	private final class ThroughputSender4App extends TimerTask {
		private Resource app;

		public ThroughputSender4App(Resource app) {
			this.app = app;
		}

		@Override
		public void run() {
			send((double) counter4App / appSamplingTime, app);
			counter4App = 0;
		}
	}

}
