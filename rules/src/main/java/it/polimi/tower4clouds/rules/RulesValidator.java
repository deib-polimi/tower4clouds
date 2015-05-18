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
import it.polimi.modaclouds.qos_models.EnumErrorType;
import it.polimi.modaclouds.qos_models.Problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang.NullArgumentException;

public class RulesValidator {

	private Config config;

	public RulesValidator() throws ConfigurationException {
		config = Config.getInstance();
	}

	/**
	 * Validate all rules
	 * 
	 * @param monitoringRules
	 * @return the set of problems found during validation, or an empty set if
	 *         the validation was successful
	 */
	public Set<Problem> validateAllRules(MonitoringRules monitoringRules) {
		if (monitoringRules == null)
			throw new NullArgumentException("monitoringRules");
		Set<Problem> problems = new HashSet<Problem>();
		List<MonitoringRule> rules = monitoringRules.getMonitoringRules();
		List<MonitoringRule> otherRules = new ArrayList<MonitoringRule>(rules);
		MonitoringRule previousRule = null;
		for (MonitoringRule rule : rules) {
			if (previousRule != null)
				otherRules.add(previousRule);
			otherRules.remove(rule);
			problems.addAll(validateRule(rule, otherRules));
			previousRule = rule;
		}
		return problems;
	}

	public Set<Problem> validateRule(MonitoringRule rule,
			List<MonitoringRule> otherRules) {
		Set<Problem> problems = new HashSet<Problem>();
		problems.addAll(validateMissingFields(rule));
		problems.addAll(validateIds(rule, otherRules));
		problems.addAll(validateMonitoredTargets(rule));
		problems.addAll(validateCollectedMetric(rule, otherRules));
		problems.addAll(validateMetricAggregation(rule));
		problems.addAll(validateActions(rule, otherRules));
		problems.addAll(validateCondition(rule));
		return problems;
	}

	private Collection<? extends Problem> validateIds(MonitoringRule rule,
			List<MonitoringRule> otherRules) {
		Set<Problem> problems = new HashSet<Problem>();
		if (otherRules != null) {
			for (MonitoringRule otherRule : otherRules) {
				if (rule.getId() != null && otherRule.getId() != null
						&& rule.getId().equals(otherRule.getId())) {
					problems.add(new Problem(rule.getId(),
							EnumErrorType.ID_ALREADY_EXISTS, "id"));
					return problems;
				}
			}
		}
		return problems;
	}

	private Set<Problem> validateActions(MonitoringRule rule,
			List<MonitoringRule> otherRules) {
		Set<Problem> problems = new HashSet<Problem>();
		if (rule.getActions() == null)
			return problems;
		if (rule.getActions().getActions().size() != 1)
			problems.add(new Problem(rule.getId(),
					EnumErrorType.INVALID_ACTION, "actions",
					"Only one action is allowed at the moment"));
		for (Action action : rule.getActions().getActions()) {
			AbstractAction actionInstance = AbstractAction
					.getActionInstance(action);
			if (actionInstance == null) {
				problems.add(new Problem(rule.getId(),
						EnumErrorType.INVALID_ACTION, action.getName(),
						"There's no such action"));
			} else {
				problems.addAll(actionInstance.validateRule(rule, otherRules));

			}
		}
		return problems;
	}

	private Set<Problem> validateMissingFields(MonitoringRule rule) {
		Set<Problem> problems = new HashSet<Problem>();
		if (rule.getTimeStep() == null)
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"timeStep"));
		if (rule.getTimeWindow() == null)
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"timeWindow"));
		if (rule.getActions() == null)
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"actions"));
		if (rule.getCollectedMetric() == null)
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"collectedMetric"));
		else if (rule.getCollectedMetric().getMetricName() == null) {
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"metricName",
					"collected metric requires the field metricName"));
		}
		if (rule.getId() == null)
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"id"));
		if (rule.getMonitoredTargets() == null)
			problems.add(new Problem(rule.getId(), EnumErrorType.MISSING_FIELD,
					"monitoredTargets"));
		return problems;
	}

	private Set<Problem> validateMetricAggregation(MonitoringRule rule) {
		Set<Problem> problems = new HashSet<Problem>();
		if (rule.getMetricAggregation() == null)
			return problems;
		boolean found = false;

		for (AggregateFunction aggregateF : config
				.getMonitoringAggregateFunctions().getAggregateFunctions()) {
			if (nullableEquals(aggregateF.getName(), rule
					.getMetricAggregation().getAggregateFunction())) {
				found = true;
				break;
			}
		}
		if (!found) {
			problems.add(new Problem(rule.getId(),
					EnumErrorType.INVALID_AGGREGATE_FUNCTION,
					"aggregateFunction"));
			return problems;
		}
		found = false;
		if (rule.getMetricAggregation().getGroupingClass() != null) {
			for (GroupingCategory clazz : config.getGroupingCategories()
					.getGroupingCategories()) {
				if (nullableEquals(rule.getMetricAggregation()
						.getGroupingClass(), clazz.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				problems.add(new Problem(rule.getId(),
						EnumErrorType.INVALID_CLASS, "groupingClass"));
			}
		}
		List<AggregateFunction.RequiredParameter> requiredParameters = null;
		if (rule.getMetricAggregation().getAggregateFunction() != null) {
			for (AggregateFunction function : config
					.getMonitoringAggregateFunctions().getAggregateFunctions()) {
				if (nullableEquals(rule.getMetricAggregation()
						.getAggregateFunction(), function.getName())) {
					requiredParameters = function.getRequiredParameters();
					found = true;
					break;
				}
			}
			if (!found) {
				problems.add(new Problem(rule.getId(),
						EnumErrorType.INVALID_AGGREGATE_FUNCTION,
						"metricAggregation"));
			} else {
				if (requiredParameters != null) {
					for (AggregateFunction.RequiredParameter reqP : requiredParameters) {
						found = false;
						for (Parameter ruleP : rule.getMetricAggregation()
								.getParameters()) {
							if (nullableEquals(reqP.getValue(), ruleP.getName())) {
								found = true;
								break;
							}
						}
						if (!found) {
							problems.add(new Problem(rule.getId(),
									EnumErrorType.MISSING_REQUIRED_PARAMETER,
									"metricAggregation", "Parameter \""
											+ reqP.getValue()
											+ "\" is missing."));
						}
					}
				}
			}
		}
		return problems;
	}

	private Set<Problem> validateMonitoredTargets(MonitoringRule rule) {
		Set<Problem> problems = new HashSet<Problem>();
		if (rule.getMonitoredTargets() == null)
			return problems;
		boolean found;
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			if (target.getClazz() == null) {
				problems.add(new Problem(rule.getId(),
						EnumErrorType.MISSING_FIELD, "monitoredTargets",
						"target class is required"));
				break;
			}
			found = false;
			for (GroupingCategory clazz : config.getGroupingCategories()
					.getGroupingCategories()) {
				if (nullableEquals(target.getClazz(), clazz.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				problems.add(new Problem(rule.getId(),
						EnumErrorType.INVALID_CLASS, "monitoredTargets"));
			}
		}
		return problems;
	}

	private Set<Problem> validateCollectedMetric(MonitoringRule rule,
			List<MonitoringRule> otherRules) {
		Set<Problem> problems = new HashSet<Problem>();
		// TODO anything to check??
		return problems;
	}

	private Set<Problem> validateCondition(MonitoringRule rule) {
		Set<Problem> problems = new HashSet<Problem>();
		if (rule.getCondition() == null)
			return problems;
		String condition = rule.getCondition().getValue();
		if (condition != null) {
			ANTLRInputStream input = new ANTLRInputStream(condition);
			ConditionLexer lexer = new ConditionLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ConditionParser parser = new ConditionParser(tokens);
			parser.setErrorHandler(new ErrorStrategy());
			parser.removeErrorListeners();
			lexer.removeErrorListeners();

			try {
				parser.expression();
			} catch (Exception e) {
				Problem problem = new Problem();
				problem.setElementId(rule.getId());
				problem.setTagName("condition");
				problem.setError(EnumErrorType.CONDITION_LEXICAL_ERROR);
				if (e.getCause().toString().contains("NoViableAltException")) {
					Token token = ((NoViableAltException) (e.getCause()))
							.getOffendingToken();
					problem.setDescription("Lexical error: '"
							+ condition.substring(0,
									token.getCharPositionInLine())
							+ " »"
							+ condition.substring(token.getCharPositionInLine())
							+ "'");
				} else if (e instanceof InputMismatchException) {
					problem.setDescription("Input Mismatch Exception");
				} else if (e instanceof FailedPredicateException) {
					problem.setDescription("Failed Predicate Exception");
				} else {
					problem.setDescription("Unknown recognition error! Exception: "
							+ e.getClass().getName());
				}
				problems.add(problem);
			}

		}
		return problems;
	}

	private boolean nullableEquals(String name1, String name2) {
		if (name1 == null && name2 == null)
			return true;
		if (name1 == null || name2 == null)
			return false;
		return name1.equals(name2);
	}

}
