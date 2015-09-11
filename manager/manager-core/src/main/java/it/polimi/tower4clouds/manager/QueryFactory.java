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
package it.polimi.tower4clouds.manager;

import it.polimi.csparqool.CSquery;
import it.polimi.csparqool.Function;
import it.polimi.csparqool._graph;
import it.polimi.csparqool._union;
import it.polimi.csparqool.body;
import it.polimi.csparqool.graph;
import it.polimi.tower4clouds.model.ontology.MO;
import it.polimi.tower4clouds.model.ontology.MOVocabulary;
import it.polimi.tower4clouds.rules.AbstractAction;
import it.polimi.tower4clouds.rules.Action;
import it.polimi.tower4clouds.rules.MonitoredTarget;
import it.polimi.tower4clouds.rules.MonitoringRule;
import it.polimi.tower4clouds.rules.actions.OutputMetric;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Strings;
import com.hp.hpl.jena.vocabulary.RDF;

public class QueryFactory {

	public Query prepareQuery(MonitoringRule rule) throws Exception {
		Query query = new Query();
		query.setRuleId(rule.getId());
		query.addTimeStep(rule.getTimeStep() + "s");
		query.addTimeWindow(rule.getTimeWindow() + "s");
		query.addRequiredMetric(rule.getCollectedMetric().getMetricName());

		query.getConstructGraph()
				.add(CSquery.BLANK_NODE, RDF.type, MO.MonitoringDatum)
				.add(MO.resourceId, "?resourceId").add(MO.metric, "?metric")
				.add(MO.value, "?value").add(MO.timestamp, "?timestamp");

		String outputMetricName = UUID.randomUUID().toString()
				.replaceAll("-", "");
		String outputMetricValue = "METRIC";
		String outputResourceId = null;
		for (Action actionInfo : rule.getActions().getActions()) {
			AbstractAction action = AbstractAction
					.getActionInstance(actionInfo);
			action.setRuleId(rule.getId());
			action.setMmPort(ManagerConfig.getInstance().getMmPort());
			if (action instanceof OutputMetric) {
				Map<String, String> parameters = ((OutputMetric) action)
						.getParameters();
				outputMetricName = parameters.get(OutputMetric.metric);
				outputMetricValue = parameters.get(OutputMetric.value);
				outputResourceId = parameters.get(OutputMetric.resourceId);
				query.setOutputMetric(outputMetricName);
			} else {
				query.addAction(action);
			}
		}
		outputMetricValue = outputMetricValue.replaceAll("METRIC", "?value_1");

		String aggregateFunction = null;
		String groupingClass = null;
		String[] aggregateParameters = new String[] { outputMetricValue };
		if (rule.getMetricAggregation() != null) {
			aggregateFunction = rule.getMetricAggregation()
					.getAggregateFunction();
			groupingClass = rule.getMetricAggregation().getGroupingClass();
			if (aggregateFunction != null
					&& aggregateFunction.equals(Function.PERCENTILE)) {
				String thPercentile = rule.getMetricAggregation()
						.getParameters().get(0).getValue();
				aggregateParameters = new String[] { outputMetricValue,
						thPercentile };
			}
		}

		if (outputResourceId == null || outputResourceId.equals("ID")) {
			if (aggregateFunction == null
					&& !containsAggregation(outputMetricValue)) {
				outputResourceId = "?resourceId_1";
			} else if (groupingClass != null) {
				outputResourceId = "?" + groupingClass + "Id_1";
			} else {
				outputResourceId = "\"ALL\"^^xsd:string";
			}
		} else {
			if (groupingClass != null) {
				throw new RuntimeException(
						"Cannot assign custom resourceId name ("
								+ outputResourceId
								+ ") when grouping class is specified");
			}
			outputResourceId = "\"" + outputResourceId + "\"^^xsd:string";
		}

		_union unionOfTargets = new _union();
		String resourceTypeVariable = "?resourceType_1";
		String resourceClassVariable = "?resourceClass_1";
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			String resourceVariable = "?resource_1";
			String resourceIdVariable = "?resourceId_1";
			if (groupingClass != null) {
				resourceVariable = "?" + target.getClazz() + "_1";
				resourceIdVariable = "?" + target.getClazz() + "Id_1";
			}
			String filter;
			if (target.getId() != null) {
				filter = resourceIdVariable + " = \"" + target.getId() + "\"";
			} else if (target.getType() != null) {
				filter = resourceTypeVariable + " = \"" + target.getType()
						+ "\"";
			} else {
				filter = "str(" + resourceClassVariable + ") = \"" + MO.URI
						+ target.getClazz() + "\"";
			}
			_graph unionGraph = graph
					.add(resourceVariable, RDF.type, resourceClassVariable)
					.add(MO.type, resourceTypeVariable)
					.add(MO.id, resourceIdVariable)
					.filter(filter)
					.add(body
							.selectFunction(resourceVariable, null,
									"?input_resource_1")
							.select("?value_1")
							.selectFunction("?timestamp_1", Function.TIMESTAMP,
									"?datum_1",
									"<" + MO.resourceId.getURI() + ">",
									"?resourceId_1")
							.where(graph
									.add("?datum_1", RDF.type,
											MO.MonitoringDatum)
									.add(MO.resourceId, "?resourceId_1")
									.add(MO.value, "?value_1")
									.add("?input_resource_1", MO.id,
											"?resourceId_1")));
			addChainToGraph(unionGraph, target.getClazz(), groupingClass);
			unionOfTargets.add(unionGraph);
		}

		query.getBody()
				.selectFunction("?resourceId", null, outputResourceId)
				.selectFunction("?metric", null,
						"\"" + outputMetricName + "\"^^xsd:string")
				.selectFunction("?value", aggregateFunction,
						aggregateParameters)
				.selectFunction(
						"?timestamp",
						aggregateFunction != null
								|| containsAggregation(outputMetricValue) ? Function.MAX
								: null, "?timestamp_1")
				.where(graph.add(unionOfTargets));

		if (rule.getCondition() != null
				&& !Strings.isNullOrEmpty(rule.getCondition().getValue())) {
			String condition = rule.getCondition().getValue();
			condition = condition.replaceAll("METRIC", "?value");
			query.getBody().having(condition);
		}

		if (groupingClass != null) {
			query.getBody().groupby(outputResourceId);
		}

		return query;
	}

	private boolean containsAggregation(String outputMetricValue) {
		return outputMetricValue.contains("AVG(")
				|| outputMetricValue.contains("SUM(")
				|| outputMetricValue.contains("PERCENTILE(")
				|| outputMetricValue.contains("MAX(")
				|| outputMetricValue.contains("MIN(")
				|| outputMetricValue.contains("COUNT(");
	}

	private void addChainToGraph(_graph graph, String fromClass, String toClass) {
		if (fromClass == null || toClass == null || fromClass.equals(toClass))
			return;
		switch (fromClass) {
		case MOVocabulary.CloudProvider:
		case MOVocabulary.Location:
			throw new RuntimeException("Cannot group by " + toClass
					+ " when monitoring " + fromClass);
		case MOVocabulary.Method:
			graph.add("?" + MOVocabulary.InternalComponent + "_1",
					MO.providedMethods, "?" + MOVocabulary.Method + "_1");
			switch (toClass) {
			case MOVocabulary.InternalComponent:
				graph.add("?" + MOVocabulary.InternalComponent + "_1", MO.id,
						"?" + MOVocabulary.InternalComponent + "Id_1");
				break;
			case MOVocabulary.VM:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents, "?" + MOVocabulary.VM + "_1")
						.add("?" + MOVocabulary.VM + "_1", RDF.type, MO.VM)
						.add(MO.id, "?" + MOVocabulary.VM + "Id_1");
				break;
			case MOVocabulary.PaaSService:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.PaaSService + "_1")
						.add("?" + MOVocabulary.PaaSService + "_1", RDF.type,
								MO.VM)
						.add(MO.id, "?" + MOVocabulary.PaaSService + "Id_1");
				break;
			case MOVocabulary.CloudProvider:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.ExternalComponent + "_1",
								MO.cloudProvider,
								"?" + MOVocabulary.CloudProvider + "_1")
						.add("?" + MOVocabulary.CloudProvider + "_1", MO.id,
								"?" + MOVocabulary.CloudProvider + "Id_1");
				break;
			case MOVocabulary.Location:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.ExternalComponent + "_1",
								MO.location, "?" + MOVocabulary.Location + "_1")
						.add("?" + MOVocabulary.Location + "_1", MO.id,
								"?" + MOVocabulary.Location + "Id_1");
				break;
			case MOVocabulary.Node:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.vms,
								"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.id,
								"?" + MOVocabulary.Node + "Id_1");
				break;
			case MOVocabulary.Cluster:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.vms,
								"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.nodes,
								"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.id,
								"?" + MOVocabulary.Cluster + "Id_1")
						.add("?" + MOVocabulary.Cluster + "_1", RDF.type,
								MO.Cluster);
				break;
			case MOVocabulary.Rack:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.vms,
								"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.nodes,
								"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.id,
								"?" + MOVocabulary.Rack + "Id_1")
						.add("?" + MOVocabulary.Rack + "_1", RDF.type, MO.Rack);
				break;
			default:
				throw new RuntimeException("Unknown class " + toClass);
			}
			break;
		case MOVocabulary.InternalComponent:
			switch (toClass) {
			case MOVocabulary.Method:
				throw new RuntimeException("Cannot group by " + toClass
						+ " when monitoring " + fromClass);
			case MOVocabulary.VM:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents, "?" + MOVocabulary.VM + "_1")
						.add("?" + MOVocabulary.VM + "_1", RDF.type, MO.VM)
						.add(MO.id, "?" + MOVocabulary.VM + "Id_1");
				break;
			case MOVocabulary.PaaSService:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.PaaSService + "_1")
						.add("?" + MOVocabulary.PaaSService + "_1", RDF.type,
								MO.VM)
						.add(MO.id, "?" + MOVocabulary.PaaSService + "Id_1");
				break;
			case MOVocabulary.CloudProvider:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.ExternalComponent + "_1",
								MO.cloudProvider,
								"?" + MOVocabulary.CloudProvider + "_1")
						.add("?" + MOVocabulary.CloudProvider + "_1", MO.id,
								"?" + MOVocabulary.CloudProvider + "Id_1");
				break;
			case MOVocabulary.Location:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.ExternalComponent + "_1",
								MO.location, "?" + MOVocabulary.Location + "_1")
						.add("?" + MOVocabulary.Location + "_1", MO.id,
								"?" + MOVocabulary.Location + "Id_1");
				break;
			case MOVocabulary.Node:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.vms,
								"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.id,
								"?" + MOVocabulary.Node + "Id_1");
				break;
			case MOVocabulary.Cluster:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.vms,
								"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.nodes,
								"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.id,
								"?" + MOVocabulary.Cluster + "Id_1")
						.add("?" + MOVocabulary.Cluster + "_1", RDF.type,
								MO.Cluster);
				break;
			case MOVocabulary.Rack:
				graph.addTransitive(
						"?" + MOVocabulary.InternalComponent + "_1",
						MO.requiredComponents,
						"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Node + "_1", MO.vms,
								"?" + MOVocabulary.ExternalComponent + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.nodes,
								"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.id,
								"?" + MOVocabulary.Rack + "Id_1")
						.add("?" + MOVocabulary.Rack + "_1", RDF.type, MO.Rack);
				break;
			default:
				throw new RuntimeException("Unknown class " + toClass);
			}
			break;
		case MOVocabulary.PaaSService:
			switch (toClass) {
			case MOVocabulary.Method:
			case MOVocabulary.InternalComponent:
			case MOVocabulary.VM:
				throw new RuntimeException("Cannot group by " + toClass
						+ " when monitoring " + fromClass);
			case MOVocabulary.CloudProvider:
				graph.add("?" + MOVocabulary.PaaSService + "_1",
						MO.cloudProvider,
						"?" + MOVocabulary.CloudProvider + "_1").add(
						"?" + MOVocabulary.CloudProvider + "_1", MO.id,
						"?" + MOVocabulary.CloudProvider + "Id_1");
				break;
			case MOVocabulary.Location:
				graph.add("?" + MOVocabulary.PaaSService + "_1", MO.location,
						"?" + MOVocabulary.Location + "_1").add(
						"?" + MOVocabulary.Location + "_1", MO.id,
						"?" + MOVocabulary.Location + "Id_1");
				break;
			default:
				throw new RuntimeException("Unknown class " + toClass);
			}
			break;
		case MOVocabulary.VM:
			switch (toClass) {
			case MOVocabulary.Method:
			case MOVocabulary.InternalComponent:
			case MOVocabulary.PaaSService:
				throw new RuntimeException("Cannot group by " + toClass
						+ " when monitoring " + fromClass);
			case MOVocabulary.CloudProvider:
				graph.add("?" + MOVocabulary.VM + "_1", MO.cloudProvider,
						"?" + MOVocabulary.CloudProvider + "_1").add(
						"?" + MOVocabulary.CloudProvider + "_1", MO.id,
						"?" + MOVocabulary.CloudProvider + "Id_1");
				break;
			case MOVocabulary.Location:
				graph.add("?" + MOVocabulary.VM + "_1", MO.location,
						"?" + MOVocabulary.Location + "_1").add(
						"?" + MOVocabulary.Location + "_1", MO.id,
						"?" + MOVocabulary.Location + "Id_1");
				break;
			case MOVocabulary.Node:
				graph.add("?" + MOVocabulary.Node + "_1", MO.vms,
						"?" + MOVocabulary.VM + "_1").add(
						"?" + MOVocabulary.Node + "_1", MO.id,
						"?" + MOVocabulary.Node + "Id_1");
				break;
			case MOVocabulary.Cluster:
				graph.add("?" + MOVocabulary.Node + "_1", MO.vms,
						"?" + MOVocabulary.VM + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.nodes,
								"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.id,
								"?" + MOVocabulary.Cluster + "Id_1")
						.add("?" + MOVocabulary.Cluster + "_1", RDF.type,
								MO.Cluster);
				break;
			case MOVocabulary.Rack:
				graph.add("?" + MOVocabulary.Node + "_1", MO.vms,
						"?" + MOVocabulary.VM + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.nodes,
								"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.id,
								"?" + MOVocabulary.Rack + "Id_1")
						.add("?" + MOVocabulary.Rack + "_1", RDF.type, MO.Rack);
				break;
			default:
				throw new RuntimeException("Unknown class " + toClass);
			}
			break;
		case MOVocabulary.Node:
			switch (toClass) {
			case MOVocabulary.Method:
			case MOVocabulary.InternalComponent:
			case MOVocabulary.PaaSService:
			case MOVocabulary.VM:
			case MOVocabulary.CloudProvider:
			case MOVocabulary.Location:
				throw new RuntimeException("Cannot group by " + toClass
						+ " when monitoring " + fromClass);
			case MOVocabulary.Cluster:
				graph.add("?" + MOVocabulary.Cluster + "_1", MO.nodes,
						"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Cluster + "_1", MO.id,
								"?" + MOVocabulary.Cluster + "Id_1")
						.add("?" + MOVocabulary.Cluster + "_1", RDF.type,
								MO.Cluster);
				break;
			case MOVocabulary.Rack:
				graph.add("?" + MOVocabulary.Rack + "_1", MO.nodes,
						"?" + MOVocabulary.Node + "_1")
						.add("?" + MOVocabulary.Rack + "_1", MO.id,
								"?" + MOVocabulary.Rack + "Id_1")
						.add("?" + MOVocabulary.Rack + "_1", RDF.type, MO.Rack);
				break;
			default:
				throw new RuntimeException("Cannot group by " + toClass
						+ " when monitoring " + fromClass);
			}
			break;
		default:
			throw new RuntimeException("Cannot group by " + toClass
					+ " when monitoring " + fromClass);
		}
	}

}
