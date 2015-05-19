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
package it.polimi.tower4clouds.rdf_history_db.manager.data;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * An object of this class represent the monitoring data sent as a RDF/JSON file.
 * 
 * @author Riccardo B. Desantis
 *
 */
public class MonitoringData {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitoringData.class);
	
	private com.hp.hpl.jena.rdf.model.Model model;
	private long timestamp;
	private String metric;
	private String value;
	private String resourceId;

	public String getMetric() {
		return metric;
	}
	public void setMetric(String metric) {
		this.metric = metric;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	private MonitoringData() { }
	
	public com.hp.hpl.jena.rdf.model.Model getModel() {
		return model;
	}

	public void setModel(com.hp.hpl.jena.rdf.model.Model model) {
		this.model = model;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public static MonitoringData resultFromRdfJson(String jsonRdfDatum) {
		if (jsonRdfDatum == null)
			return null;
		
		MonitoringData r = new MonitoringData();
		
		r.model = ModelFactory.createDefaultModel();
		com.hp.hpl.jena.rdf.model.Model model = ModelFactory.createDefaultModel();
		/*r.*/model.read(new StringReader(jsonRdfDatum), null, "RDF/JSON");
		
		r.timestamp = -1;
		
		Resource subject = null;
		{
			String subjectUri = "mo:";
			int i = jsonRdfDatum.indexOf('"');
			i = jsonRdfDatum.indexOf(':', i+1);
			int j = jsonRdfDatum.indexOf('"', i+1);
			subjectUri += jsonRdfDatum.substring(i+1, j);
			
			subject = r.model.createResource(subjectUri);
		}
		
		for (StmtIterator iter = /*r.*/model.listStatements(); iter.hasNext();) {
			Statement stmt      = iter.nextStatement();
		    Property  predicate = stmt.getPredicate();
		    RDFNode   object    = stmt.getObject();
		    
		    String name = predicate.toString();
		    int i = 0;
		    if (( i = name.indexOf('#') ) > 0)
		    	name = name.substring(i + 1);
		    else if (( i = name.lastIndexOf('/') ) > 0) {
		    	name = name.substring(i + 1);
		    	predicate = r.model.createProperty("http://www.modaclouds.eu/rdfs/1.0/monitoringdata#" + name);
		    }
		    else
		    	continue;
		    
		    r.model.add(subject, predicate, object);
		    
		    
		    String value = "";
		    
		    if (!object.isLiteral())
		    	continue;
		    value = object.asLiteral().getValue().toString();

		    logger.debug("{}: {}", name, value);
		    
		    if (name.equalsIgnoreCase("timestamp"))
		    	try {
		    		r.timestamp = Long.valueOf(value);
		    	} catch (NumberFormatException e) {
		    		logger.error("Argh!", e);
		    	}
		    else if (name.equalsIgnoreCase("metric"))
		    	r.metric = value;
		    else if (name.equalsIgnoreCase("value")) {
		    	r.value = value;
		    } else if (name.equalsIgnoreCase("resourceId"))
		    	r.resourceId = value;
		}
		
		if (r.timestamp > -1)
			return r;
		
		return null;
	}
	
	public static com.hp.hpl.jena.rdf.model.Model defaultGraphStatement(String graphUrl, long timestamp) {
		com.hp.hpl.jena.rdf.model.Model m = ModelFactory.createDefaultModel();
		
		com.hp.hpl.jena.rdf.model.Resource   subject   = null;
		com.hp.hpl.jena.rdf.model.Property   property  = null;
		com.hp.hpl.jena.rdf.model.Statement  statement = null;
		
		subject = m.createResource(graphUrl);
		
		property = m.createProperty("mo:timestamp");
		
		statement = m.createLiteralStatement(subject, property, timestamp);
		m.add(statement);
		
		return m;
	}
}
