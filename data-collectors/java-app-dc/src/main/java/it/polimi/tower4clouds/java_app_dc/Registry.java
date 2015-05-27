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
import it.polimi.tower4clouds.java_app_dc.metrics.ResponseTime;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.CloudProvider;
import it.polimi.tower4clouds.model.ontology.ExternalComponent;
import it.polimi.tower4clouds.model.ontology.InternalComponent;
import it.polimi.tower4clouds.model.ontology.Location;
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.model.ontology.VM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry implements Observer {

	private static final Logger logger = LoggerFactory
			.getLogger(Registry.class);

	private InternalComponent application;
	private Map<String, Method> methodsById;
	private Map<String, Method> methodsByType;

	private DCAgent dcAgent;

	private Set<Metric> metrics;

	private boolean registryInitialized = false;
	private boolean monitoringStarted = false;

	protected static final Registry _INSTANCE = new Registry();

	private static final int CONFIG_SYNC_PERIOD = 30;
	private static final int KEEP_ALIVE = 60;
	
	protected Registry() {
	}

	private void ended(String methodType) {
		logger.debug("Method {} ended", methodType);
		if (monitoringStarted) {
			for (Metric metric : metrics) {
				metric.ended(methodsByType.get(methodType));
			}
		} else {
			logger.warn("Monitoring was not started");
		}
	}

	private void started(String methodType) {
		logger.debug("Method {} started", methodType);
		if (monitoringStarted) {
			for (Metric metric : metrics) {
				metric.started(methodsByType.get(methodType));
			}
		} else {
			logger.warn("Monitoring was not started");
		}
	}

	private void externalCallStarted() {
		logger.debug("External call started");
		if (monitoringStarted) {
			for (Metric metric : metrics) {
				metric.externalCallStarted();
			}
		} else {
			logger.warn("Monitoring was not started");
		}
	}

	private void externalCallEnded() {
		logger.debug("External call ended");
		if (monitoringStarted) {
			for (Metric metric : metrics) {
				metric.externalCallEnded();
			}
		} else {
			logger.warn("Monitoring was not started");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

	// TODO validate parameters
	private synchronized void init(String managerIP, int managerPort,
			Map<Property, String> applicationProperties,
			String monitoredClassesPackage) {
		if (registryInitialized)
			throw new RuntimeException("Registry was already initialized");
		this.methodsById = buildMethodsById(monitoredClassesPackage,
				applicationProperties);
		this.methodsByType = buildMethodsByType(methodsById);
		this.application = buildInternalComponent(methodsById,
				applicationProperties);
		Set<Resource> relatedResources = buildRelatedResources(applicationProperties);
		this.metrics = parseMetrics();
		if (dcAgent != null)
			dcAgent.stop();
		dcAgent = new DCAgent(new ManagerAPI(managerIP, managerPort));
		dcAgent.addObserver(this);
		for (Metric metric : metrics) {
			dcAgent.addObserver(metric);
		}
		dcAgent.setDCDescriptor(buildDCDescriptor(methodsById, application,
				relatedResources, metrics));
		registryInitialized = true;
	}

	private synchronized void start() {
		if (!registryInitialized)
			throw new RuntimeException("Registry was not initialized");
		if (!monitoringStarted) {
			logger.info("Starting monitoring");
			dcAgent.stop();
			dcAgent.start();
			monitoringStarted = true;
		} else {
			logger.warn("Monitoring was already started");
		}
	}

	private synchronized void stop() {
		if (monitoringStarted) {
			logger.info("Stopping monitoring");
			dcAgent.stop();
			monitoringStarted = false;
		} else {
			logger.warn("Monitoring was not running");
		}
	}

	private Map<String, Method> buildMethodsByType(
			Map<String, Method> methodsById) {
		Map<String, Method> methodsByType = new HashMap<String, Method>();
		for (Method method : methodsById.values()) {
			methodsByType.put(method.getType(), method);
		}
		return methodsByType;
	}

	private static Set<Metric> parseMetrics() {
		logger.debug("Parsing available metrics");
		Set<Metric> metrics = new HashSet<Metric>();
		Reflections.log = null;
		Reflections reflections = new Reflections(ResponseTime.class
				.getPackage().getName());
		Set<Class<? extends Metric>> metricsClasses = reflections
				.getSubTypesOf(Metric.class);
		for (Class<? extends Metric> metricClass : metricsClasses) {
			try {
				logger.debug("Metric {} found", metricClass.getSimpleName());
				metrics.add(metricClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return metrics;
	}

	private static DCDescriptor buildDCDescriptor(Map<String, Method> methods,
			InternalComponent application, Set<Resource> relatedResources,
			Set<Metric> metrics) {
		DCDescriptor dcDescriptor = new DCDescriptor();
		dcDescriptor.addMonitoredResources(getMethodMetrics(metrics),
				new HashSet<Resource>(methods.values()));
		dcDescriptor.addMonitoredResource(getApplicationMetrics(metrics),
				application);
		dcDescriptor.setConfigSyncPeriod(CONFIG_SYNC_PERIOD);
		dcDescriptor.setKeepAlive(KEEP_ALIVE);
		return dcDescriptor;
	}

	private Set<Resource> buildRelatedResources(
			Map<Property, String> applicationProperties) {
		Set<Resource> resources = new HashSet<Resource>();
		String cloudProviderId = applicationProperties
				.get(Property.CLOUD_PROVIDER_ID);
		String cloudProviderType = applicationProperties
				.get(Property.CLOUD_PROVIDER_TYPE);
		String vmId = applicationProperties.get(Property.VM_ID);
		String vmType = applicationProperties.get(Property.VM_TYPE);
		String locationId = applicationProperties.get(Property.LOCATION_ID);
		String locationType = applicationProperties.get(Property.LOCATION_TYPE);
		String paasServiceId = applicationProperties
				.get(Property.PAAS_SERVICE_ID);
		String paasServiceType = applicationProperties
				.get(Property.PAAS_SERVICE_TYPE);

		CloudProvider cloudProvider = null;
		if (cloudProviderId != null) {
			cloudProvider = new CloudProvider(cloudProviderType,
					cloudProviderId);
			resources.add(cloudProvider);
		}

		Location location = null;
		if (locationId != null) {
			location = new Location(locationType, locationId);
			resources.add(location);
		}

		VM vm = null;
		if (vmId != null) {
			vm = new VM(vmType, vmId);
			if (cloudProviderId != null)
				vm.setCloudProvider(cloudProviderId);
			if (locationId != null)
				vm.setLocation(locationId);
			resources.add(vm);
		}

		ExternalComponent paasService = null;
		if (paasServiceId != null) {
			paasService = new ExternalComponent(paasServiceType, paasServiceId);
			if (cloudProviderId != null)
				paasService.setCloudProvider(cloudProviderId);
			if (locationId != null)
				paasService.setLocation(locationId);
			resources.add(paasService);
		}

		return resources;
	}

	private InternalComponent buildInternalComponent(
			Map<String, Method> methods,
			Map<Property, String> applicationProperties) {
		String internalComponentId = applicationProperties.get(Property.ID);
		String internalComponentType = applicationProperties.get(Property.TYPE);
		String requiredComponentId = applicationProperties.get(Property.VM_ID);
		if (requiredComponentId == null)
			requiredComponentId = applicationProperties
					.get(Property.PAAS_SERVICE_ID);
		InternalComponent internalComponent = new InternalComponent(
				internalComponentType, internalComponentId);
		internalComponent.setProvidedMethods(methods.keySet());
		internalComponent.addRequiredComponent(requiredComponentId);
		return internalComponent;
	}

	private static Set<String> getApplicationMetrics(Set<Metric> metrics) {
		Set<String> metricsNames = new HashSet<String>();
		for (Metric metric : metrics) {
			if (metric.isApplicationMetric())
				metricsNames.add(metric.getName());
		}
		return metricsNames;
	}

	private static Set<String> getMethodMetrics(Set<Metric> metrics) {
		Set<String> metricsNames = new HashSet<String>();
		for (Metric metric : metrics) {
			if (metric.isMethodMetric())
				metricsNames.add(metric.getName());
		}
		return metricsNames;
	}

	private static Map<String, Method> buildMethodsById(
			String monitoredClassesPackage,
			Map<Property, String> applicationProperties) {
		Reflections.log = null;
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage(monitoredClassesPackage))
				.setScanners(new MethodAnnotationsScanner()));
		Set<java.lang.reflect.Method> annotatedMethods = reflections
				.getMethodsAnnotatedWith(Monitor.class);
		Map<String, Method> monitoredMethods = new HashMap<String, Method>();
		for (java.lang.reflect.Method annotatedMethod : annotatedMethods) {
			String methodType = annotatedMethod.getAnnotation(Monitor.class)
					.type();
			Method method = buildMethod(methodType,
					applicationProperties.get(Property.ID));
			monitoredMethods.put(method.getId(), method);
			logger.info("Monitored method found: type={}, id={}",
					method.getType(), method.getId());
		}
		return monitoredMethods;
	}

	private static Method buildMethod(String methodType, String applicationId) {
		return new Method(methodType, methodType + "_"
				+ applicationId.hashCode());
	}

	public static void notifyStart(String methodType) {
		_INSTANCE.started(methodType);
	}

	public static void notifyEnd(String methodType) {
		_INSTANCE.ended(methodType);
	}

	public static void notifyExternalCallStart() {
		_INSTANCE.externalCallStarted();
	}

	public static void notifyExternalCallEnd() {
		_INSTANCE.externalCallEnded();
	}

	public static void initialize(String managerIP, int managerPort,
			Map<Property, String> applicationProperties,
			String monitoredClassesPackage) {
		_INSTANCE.init(managerIP, managerPort, applicationProperties,
				monitoredClassesPackage);
	}

	public static void startMonitoring() {
		_INSTANCE.start();
	}

	Set<Method> getMethods() {
		return new HashSet<Method>(methodsById.values());
	}
}
