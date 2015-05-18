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

import it.polimi.modaclouds.qos_models.EnumErrorType;
import it.polimi.modaclouds.qos_models.Problem;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAction {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractAction.class);
	private Map<String, String> parameters;

	protected Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass().getName());
	}

	/**
	 * Checks if the action expressed in the rule exists and if all required
	 * parameters were specified. Then calls the action specific validation
	 * check, i.e., {@code validate()}.
	 * 
	 * @param rule
	 * @param otherRules
	 * @return the set of problems found during the validation, an empty set
	 *         otherwise.
	 */
	public final Set<Problem> validateRule(MonitoringRule rule,
			List<MonitoringRule> otherRules) {
		Set<Problem> problems = new HashSet<Problem>();
		for (Action action : getMyActions(rule)) {
			Set<String> requiredPars = getMyRequiredPars();
			Set<String> missingPars = new HashSet<String>();
			missingPars.addAll(requiredPars);
			missingPars.removeAll(getParameters().keySet());
			if (!missingPars.isEmpty()) {
				problems.add(new Problem(rule.getId(),
						EnumErrorType.MISSING_REQUIRED_PARAMETER, action
								.getName(), "Missing required parameters: "
								+ missingPars.toString()));
			}
		}
		Collection<? extends Problem> actionSpecificProblems = validate(rule,
				otherRules);
		if (actionSpecificProblems != null)
			problems.addAll(actionSpecificProblems);
		return problems;
	}

	/**
	 * 
	 * @return the list of required parameters that the user should put in the
	 *         monitoring rule.
	 */
	protected abstract Set<String> getMyRequiredPars();

	/**
	 * Action specific validation checks can be implemented here. Note that
	 * basic validation on the name of the action and on the required parameters
	 * is already done.
	 * 
	 * @param rule
	 *            the rule for which the action should be validated
	 * @param otherRules
	 *            all other rules already installed or about to be installed in
	 *            the platform
	 * @return a Set of problems as a result of the validation. {@code null} or
	 *         an empty set if no problems were detected.
	 */
	protected abstract Collection<? extends Problem> validate(
			MonitoringRule rule, List<MonitoringRule> otherRules);

	public static Map<String, String> extractParameters(Action action) {
		Map<String, String> pars = new HashMap<String, String>();
		for (Parameter par : action.getParameters()) {
			pars.put(par.getName(), par.getValue());
		}
		return pars;
	}

	protected Set<Action> getMyActions(MonitoringRule rule) {
		Set<Action> actions = new HashSet<Action>();
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(getName())) {
				actions.add(action);
			}
		}
		return actions;
	}

	public final String getName() {
		return getClass().getSimpleName();
	}

	public static AbstractAction getActionInstance(Action action) {
		AbstractAction actionInstance = null;
		Reflections.log = null;
		Reflections reflections = new Reflections(
				it.polimi.tower4clouds.rules.actions.OutputMetric.class
						.getPackage().getName());
		Set<Class<? extends AbstractAction>> actionClasses = reflections
				.getSubTypesOf(AbstractAction.class);
		for (Class<? extends AbstractAction> actionClass : actionClasses) {
			if (actionClass.getSimpleName().equals(action.getName())) {
				try {
					actionInstance = actionClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				actionInstance.setParameters(extractParameters(action));
				break;
			}
		}
		if (actionInstance == null) {
			logger.error("Action {} does not exist", action.getName());
		}
		return actionInstance;
	}

	private void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * The concrete action to be executed when the rule is evaluated and the
	 * action is requested.
	 * 
	 * @param resourceId
	 * @param value
	 * @param timestamp
	 */
	public abstract void execute(String resourceId, String value,
			String timestamp);

}
