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
import it.polimi.csparqool.MalformedQueryException;
import it.polimi.csparqool._body;
import it.polimi.csparqool._graph;
import it.polimi.tower4clouds.model.ontology.MO;
import it.polimi.tower4clouds.rules.AbstractAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Query {

	private String id;
	private Set<String> requiredMetrics;
	private String timeStep;
	private String timeWindow;
	private Set<String> inputStreamsURIs;
	private _body body;
	private _graph constructGraph;
	private List<AbstractAction> actions;
	private String outputMetric;
	private String ruleId;

	public Query() {
		this.id = UUID.randomUUID().toString().replaceAll("-", "");
	}

	public Set<String> getRequiredMetrics() {
		if (requiredMetrics == null)
			requiredMetrics = new HashSet<String>();
		return requiredMetrics;
	}

	public void addInputStreamURI(String streamURI) {
		getInputStreamsURIs().add(streamURI);
	}

	private Set<String> getInputStreamsURIs() {
		if (inputStreamsURIs == null)
			inputStreamsURIs = new HashSet<String>();
		return inputStreamsURIs;
	}

	public String getOutputMetric() {
		return outputMetric;
	}

	public void setOutputMetric(String outputMetric) {
		this.outputMetric = outputMetric;
	}

	public String getId() {
		return id;
	}

	public boolean hasOutputMetric() {
		return getOutputMetric() != null;
	}

	public String build() throws MalformedQueryException {
		if (getInputStreamsURIs().isEmpty())
			throw new MalformedQueryException(
					"No input streams have been specified");

		CSquery csparqlQuery = CSquery.createDefaultQuery(id);

		csparqlQuery.setNsPrefix("xsd", XSD.getURI())
				.setNsPrefix("rdf", RDF.getURI())
				.setNsPrefix("rdfs", RDFS.getURI())
				.setNsPrefix(MO.prefix, MO.URI)
				.setNsPrefix("f", "http://larkc.eu/csparql/sparql/jena/ext#")
				.setNsPrefix("afn", "http://jena.hpl.hp.com/ARQ/function#");

		csparqlQuery.from(ManagerConfig.MODEL_GRAPH_NAME);

		for (String streamURI : getInputStreamsURIs()) {
			csparqlQuery.fromStream(streamURI, timeWindow, timeStep);
		}

		csparqlQuery.where(body);

		csparqlQuery.construct(constructGraph);

		return csparqlQuery.getCSPARQL();
	}

	public _graph getConstructGraph() {
		if (constructGraph == null)
			constructGraph = new _graph();
		return constructGraph;
	}

	public _body getBody() {
		if (body == null)
			body = new _body();
		return body;
	}

	public boolean hasActions() {
		return !getActions().isEmpty();
	}

	public void addRequiredMetric(String metric) {
		getRequiredMetrics().add(metric);
	}

	public void addTimeStep(String timeStep) {
		this.timeStep = timeStep;
	}

	public void addTimeWindow(String timeWindow) {
		this.timeWindow = timeWindow;
	}

	public void addAction(AbstractAction action) {
		getActions().add(action);
	}

	public void setActions(List<AbstractAction> actions) {
		this.actions = actions;
	}

	public List<AbstractAction> getActions() {
		if (actions == null)
			actions = new ArrayList<AbstractAction>();
		return actions;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

}
