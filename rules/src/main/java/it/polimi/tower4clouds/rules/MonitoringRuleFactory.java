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
package it.polimi.tower4clouds.rules;

import it.polimi.modaclouds.qos_models.ConfigurationException;
import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.tower4clouds.rules.actions.OutputMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringRuleFactory {

	private Logger logger = LoggerFactory.getLogger(MonitoringRuleFactory.class
			.getName());
	private Config config;

	public MonitoringRuleFactory() throws ConfigurationException {
		config = Config.getInstance();
	}

	/**
	 * 
	 * @param qosConstraints
	 * @return all monitoring rules that can be built from the constraints
	 */
	public MonitoringRules makeRulesFromQoSConstraints(
			Constraints qosConstraints) {
		MonitoringRules rules = new MonitoringRules();
		for (Constraint c : qosConstraints.getConstraints()) {
			MonitoringRule rule = makeRuleFromConstraint(c);
			if (rule != null)
				rules.getMonitoringRules().add(rule);
		}
		return rules;
	}

	/**
	 * 
	 * @param qosConstraint
	 * @param ruleID
	 * @return the monitoring rule built from the constraint, {@code null} if no
	 *         monitoring rule can be constructed from the constraint
	 */
	public MonitoringRule makeRuleFromConstraint(Constraint qosConstraint,
			String ruleID) {
		MonitoringRule monitoringRule = new MonitoringRule();
		monitoringRule.setId(ruleID);
		monitoringRule.setRelatedQosConstraintId(qosConstraint.getId());
		monitoringRule.setTimeStep("60");
		monitoringRule.setTimeWindow("60");

		monitoringRule.setCollectedMetric(makeCollectedMetric(qosConstraint));

		MonitoringMetricAggregation monitoringMetricAggregation = makeMetricAggregation(qosConstraint);
		if (monitoringMetricAggregation == null) {
			logger.warn(
					"Constraint {} could not be translated to a monitoring rule: "
							+ "{} is not a valid aggregate function",
					qosConstraint.getId(), qosConstraint.getMetricAggregation()
							.getAggregateFunction());
			return null;
		}
		monitoringRule.setMetricAggregation(monitoringMetricAggregation);

		MonitoredTargets targets = new MonitoredTargets();
		MonitoredTarget target = new MonitoredTarget();
		target.setType(qosConstraint.getTargetResourceIDRef());
		target.setClazz(qosConstraint.getTargetClass());
		targets.getMonitoredTargets().add(target);
		monitoringRule.setMonitoredTargets(targets);

		Float maxValue = qosConstraint.getRange().getHasMaxValue();
		Float minValue = qosConstraint.getRange().getHasMinValue();
		String conditionValue = "";
		if (maxValue != null) {
			conditionValue += "METRIC > " + maxValue;
		}

		if (minValue != null) {
			conditionValue += (maxValue != null ? " && " : "") + "METRIC < "
					+ minValue;
		}
		Condition condition = new Condition();
		condition.setValue(conditionValue);
		// condition.setInherited(false);
		monitoringRule.setCondition(condition);

		monitoringRule.setStartEnabled(true);

		Action action = new Action();
		action.setName(OutputMetric.class.getSimpleName());
		Parameter p1 = new Parameter();
		p1.setName("metric");
		p1.setValue("qosConstraint_" + qosConstraint.getId() + "_Violated");
		action.getParameters().add(p1);
		Parameter p2 = new Parameter();
		p2.setName("value");
		p2.setValue("METRIC");
		action.getParameters().add(p2);
		Parameter p3 = new Parameter();
		p3.setName("resourceId");
		p3.setValue("ID");
		action.getParameters().add(p3);
		Actions actions = new Actions();
		actions.getActions().add(action);

		monitoringRule.setActions(actions);

		return monitoringRule;
	}

	private CollectedMetric makeCollectedMetric(Constraint qosConstraint) {
		CollectedMetric collectedMetric = buildCollectedMetric(
				qosConstraint.getMetric(), null);
		return collectedMetric;
	}

	private MonitoringMetricAggregation makeMetricAggregation(
			Constraint qosConstraint) {
		List<AggregateFunction> availableAggregateFunctions = config
				.getMonitoringAggregateFunctions().getAggregateFunctions();
		MonitoringMetricAggregation monitoringMetricAggregation = new MonitoringMetricAggregation();
		monitoringMetricAggregation.setAggregateFunction(qosConstraint
				.getMetricAggregation().getAggregateFunction());
		List<Parameter> defaultParameters = toParam(qosConstraint
				.getMetricAggregation().getParameters());
		for (AggregateFunction af : availableAggregateFunctions) {
			if (qosConstraint.getMetricAggregation().getAggregateFunction()
					.equals(af.getName())) {
				defaultParameters = mergeParameters(qosConstraint
						.getMetricAggregation().getParameters(),
						getDefaultParameters(af));
				break;
			}
		}
		monitoringMetricAggregation.getParameters().addAll(defaultParameters);
		return monitoringMetricAggregation;
	}

	private List<Parameter> toParam(
			List<it.polimi.modaclouds.qos_models.schema.Parameter> parameters) {
		List<Parameter> params = new ArrayList<Parameter>();
		for (it.polimi.modaclouds.qos_models.schema.Parameter parameter : parameters) {
			Parameter param = new Parameter();
			param.setName(parameter.getName());
			param.setValue(parameter.getValue());
			params.add(param);
		}
		return params;
	}

	private CollectedMetric buildCollectedMetric(String metricName,
			List<Parameter> parameters) {
		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName(metricName);
		if (parameters != null)
			collectedMetric.getParameters().addAll(parameters);
		return collectedMetric;
	}

	private List<Parameter> mergeParameters(
			List<it.polimi.modaclouds.qos_models.schema.Parameter> parameters,
			List<Parameter> params) {
		List<Parameter> mergedParameters = toParam(parameters);
		for (Parameter p2 : params) {
			boolean found = false;
			for (it.polimi.modaclouds.qos_models.schema.Parameter p1 : parameters) {
				if (p2.getName().equals(p1.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				mergedParameters.add(p2);
			}
		}
		return mergedParameters;
	}

	private List<Parameter> getDefaultParameters(AggregateFunction af) {
		List<Parameter> parameters = new ArrayList<Parameter>();
		for (AggregateFunction.RequiredParameter rPar : af
				.getRequiredParameters()) {
			if (rPar.getDefaultValue() != null) {
				Parameter par = new Parameter();
				par.setName(rPar.getValue());
				par.setValue(rPar.getDefaultValue());
				parameters.add(par);
			}
		}
		return parameters;
	}

	// private List<Parameter> getDefaultParameters(Metric m) {
	// List<Parameter> parameters = new ArrayList<Parameter>();
	// for (Metric.RequiredParameter rPar : m.getRequiredParameters()) {
	// if (rPar.getDefaultValue() != null) {
	// Parameter par = new Parameter();
	// par.setName(rPar.getValue());
	// par.setValue(rPar.getDefaultValue());
	// parameters.add(par);
	// }
	// }
	// return parameters;
	// }

	/**
	 * 
	 * @param relatedConstraint
	 * @return the monitoring rule built from the constraint, {@code null} if no
	 *         monitoring rule can be constructed from the constraint
	 */
	public MonitoringRule makeRuleFromConstraint(Constraint relatedConstraint) {
		return makeRuleFromConstraint(relatedConstraint, UUID.randomUUID()
				.toString());
	}

}
