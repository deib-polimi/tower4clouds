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
package it.polimi.modaclouds.monitoring.monitoring_manager;

import static org.junit.Assert.fail;
import it.polimi.modaclouds.qos_models.Problem;
import it.polimi.tower4clouds.manager.Query;
import it.polimi.tower4clouds.manager.QueryFactory;
import it.polimi.tower4clouds.rules.Action;
import it.polimi.tower4clouds.rules.Actions;
import it.polimi.tower4clouds.rules.CollectedMetric;
import it.polimi.tower4clouds.rules.MonitoredTarget;
import it.polimi.tower4clouds.rules.MonitoredTargets;
import it.polimi.tower4clouds.rules.MonitoringMetricAggregation;
import it.polimi.tower4clouds.rules.MonitoringRule;
import it.polimi.tower4clouds.rules.Parameter;
import it.polimi.tower4clouds.rules.RulesValidator;

import java.util.Set;

import org.junit.Test;

public class Rule2CSPARQLTest {

	@Test
	public void test1() {
		MonitoringRule rule = new MonitoringRule();

		rule.setId("queryTest1");

		Actions actions = new Actions();
		Action action = new Action();
		action.setName("OutputMetric");
		Parameter actionParameter1 = new Parameter();
		actionParameter1.setName("metric");
		actionParameter1.setValue("myMetric");
		action.getParameters().add(actionParameter1);
		Parameter actionParameter2 = new Parameter();
		actionParameter2.setName("resourceId");
		actionParameter2.setValue("ID");
		action.getParameters().add(actionParameter2);
		Parameter actionParameter3 = new Parameter();
		actionParameter3.setName("value");
		actionParameter3.setValue("METRIC");
		action.getParameters().add(actionParameter3);
		actions.getActions().add(action);
		rule.setActions(actions);

		rule.setTimeStep("60");
		rule.setTimeWindow("60");

		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName("ResponseTime");
		Parameter metricParameter = new Parameter();
		metricParameter.setName("samplingProbability");
		metricParameter.setValue("1.0");
		collectedMetric.getParameters().add(metricParameter);
		rule.setCollectedMetric(collectedMetric);

		MonitoredTarget target = new MonitoredTarget();
		target.setClazz("Method");
		target.setType("Login");
		MonitoredTargets targets = new MonitoredTargets();
		targets.getMonitoredTargets().add(target);
		rule.setMonitoredTargets(targets);

		try {
			RulesValidator validator = new RulesValidator();
			Set<Problem> problems = validator.validateRule(rule, null);
			if (problems.isEmpty()) {
				Query query = new QueryFactory().prepareQuery(rule);
				query.addInputStreamURI("http://www.modaclouds.eu/streams/ResponseTime");
				System.out.println(query.build());
			} else {
				for (Problem p : problems) {
					System.out.println(p.toString());
				}
				fail();
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() {
		MonitoringRule rule = new MonitoringRule();

		rule.setId("queryTest2");

		Actions actions = new Actions();
		Action action = new Action();
		action.setName("OutputMetric");
		Parameter actionParameter1 = new Parameter();
		actionParameter1.setName("metric");
		actionParameter1.setValue("myMetric");
		action.getParameters().add(actionParameter1);
		Parameter actionParameter2 = new Parameter();
		actionParameter2.setName("resourceId");
		actionParameter2.setValue("allLogins");
		action.getParameters().add(actionParameter2);
		Parameter actionParameter3 = new Parameter();
		actionParameter3.setName("value");
		actionParameter3.setValue("METRIC");
		action.getParameters().add(actionParameter3);
		actions.getActions().add(action);
		rule.setActions(actions);

		rule.setTimeStep("60");
		rule.setTimeWindow("60");

		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName("ResponseTime");
		Parameter metricParameter = new Parameter();
		metricParameter.setName("samplingProbability");
		metricParameter.setValue("1.0");
		collectedMetric.getParameters().add(metricParameter);
		rule.setCollectedMetric(collectedMetric);

		MonitoredTarget target = new MonitoredTarget();
		target.setClazz("Method");
		target.setType("Login");
		MonitoredTargets targets = new MonitoredTargets();
		targets.getMonitoredTargets().add(target);
		rule.setMonitoredTargets(targets);

		MonitoringMetricAggregation aggregation = new MonitoringMetricAggregation();
		aggregation.setAggregateFunction("Average");
		rule.setMetricAggregation(aggregation);

		try {
			RulesValidator validator = new RulesValidator();
			Set<Problem> problems = validator.validateRule(rule, null);
			if (problems.isEmpty()) {
				Query query = new QueryFactory().prepareQuery(rule);
				query.addInputStreamURI("http://www.modaclouds.eu/streams/ResponseTime");
				System.out.println(query.build());
			} else {
				for (Problem p : problems) {
					System.out.println(p.toString());
				}
				fail();
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3() {
		MonitoringRule rule = new MonitoringRule();

		rule.setId("queryTest3");

		Actions actions = new Actions();
		Action action = new Action();
		action.setName("OutputMetric");
		Parameter actionParameter1 = new Parameter();
		actionParameter1.setName("metric");
		actionParameter1.setValue("myMetric");
		action.getParameters().add(actionParameter1);
		Parameter actionParameter2 = new Parameter();
		actionParameter2.setName("resourceId");
		actionParameter2.setValue("ID");
		action.getParameters().add(actionParameter2);
		Parameter actionParameter3 = new Parameter();
		actionParameter3.setName("value");
		actionParameter3.setValue("METRIC");
		action.getParameters().add(actionParameter3);
		actions.getActions().add(action);
		rule.setActions(actions);

		rule.setTimeStep("60");
		rule.setTimeWindow("60");

		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName("ResponseTime");
		Parameter metricParameter = new Parameter();
		metricParameter.setName("samplingProbability");
		metricParameter.setValue("1.0");
		collectedMetric.getParameters().add(metricParameter);
		rule.setCollectedMetric(collectedMetric);

		MonitoredTarget target = new MonitoredTarget();
		target.setClazz("Method");
		target.setType("Login");
		MonitoredTargets targets = new MonitoredTargets();
		targets.getMonitoredTargets().add(target);
		rule.setMonitoredTargets(targets);

		MonitoringMetricAggregation aggregation = new MonitoringMetricAggregation();
		aggregation.setAggregateFunction("Average");
		aggregation.setGroupingClass("CloudProvider");
		rule.setMetricAggregation(aggregation);

		try {
			RulesValidator validator = new RulesValidator();
			Set<Problem> problems = validator.validateRule(rule, null);
			if (problems.isEmpty()) {
				Query query = new QueryFactory().prepareQuery(rule);
				query.addInputStreamURI("http://www.modaclouds.eu/streams/ResponseTime");
				System.out.println(query.build());
			} else {
				for (Problem p : problems) {
					System.out.println(p.toString());
				}
				fail();
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
