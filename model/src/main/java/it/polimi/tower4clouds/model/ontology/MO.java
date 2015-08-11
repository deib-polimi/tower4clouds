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
package it.polimi.tower4clouds.model.ontology;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MO {
	
	public static final String URI = "http://www.modaclouds.eu/model#";
    public static String prefix = "mo";
    
	public static OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
	
	public static OntClass Component = makeClass(MOVocabulary.Component);
	public static OntClass CloudProvider = makeClass(MOVocabulary.CloudProvider);
	public static OntClass VM = makeClass(MOVocabulary.VM);
	public static OntClass PaaSService = makeClass(MOVocabulary.PaaSService);
	public static OntClass Location = makeClass(MOVocabulary.Location);
	public static OntClass InternalComponent = makeClass(MOVocabulary.InternalComponent);
	public static OntClass ExternalComponent = makeClass(MOVocabulary.ExternalComponent);
	public static OntClass Method = makeClass(MOVocabulary.Method);
	public static OntClass Node = makeClass(MOVocabulary.Node);

	public static Property IDRef = makeProperty(MOVocabulary.IDRef);
	public static Property cloudProvider = makeProperty(MOVocabulary.cloudProvider);
	public static Property requiredComponents = makeProperty(MOVocabulary.requiredComponents);
	public static Property location = makeProperty(MOVocabulary.location);
	public static Property providedMethods = makeProperty(MOVocabulary.providedMethods);
	public static Property type = makeProperty(MOVocabulary.type);
	public static Property id = makeProperty(MOVocabulary.id);
//	public static Property numberOfCPUs = makeProperty(MOVocabulary.numberOfCPUs);

	public static OntClass Resource = makeClass(MOVocabulary.Resource);
	
	public static OntClass MonitoringDatum = makeClass(MOVocabulary.MonitoringDatum);
	public static Property resourceId = makeProperty(MOVocabulary.resourceId);
	public static Property metric = makeProperty(MOVocabulary.metric);
	public static Property value = makeProperty(MOVocabulary.value);
	public static Property timestamp = makeProperty(MOVocabulary.timestamp);
	

	static {
		model.setNsPrefix(prefix, URI);
		
		ExternalComponent.addProperty(RDFS.subClassOf, Component);
		VM.addProperty(RDFS.subClassOf, ExternalComponent);
		PaaSService.addProperty(RDFS.subClassOf, ExternalComponent);
		Component.addProperty(RDFS.subClassOf, Resource);
		InternalComponent.addProperty(RDFS.subClassOf, Component);
		Method.addProperty(RDFS.subClassOf, Resource);
		CloudProvider.addProperty(RDFS.subClassOf, Resource);
		Location.addProperty(RDFS.subClassOf, Resource);
		Node.addProperty(RDFS.subClassOf, Resource);
		
		IDRef.addProperty(RDFS.domain, Resource);
		IDRef.addProperty(RDFS.range, RDFS.Literal);
		cloudProvider.addProperty(RDFS.domain, ExternalComponent);
		cloudProvider.addProperty(RDFS.range, CloudProvider);
		requiredComponents.addProperty(RDFS.domain, InternalComponent);
		requiredComponents.addProperty(RDFS.range, Component);
		location.addProperty(RDFS.domain, VM);
		location.addProperty(RDFS.range, Location);
		providedMethods.addProperty(RDFS.domain, InternalComponent);
		providedMethods.addProperty(RDFS.range, Method);
		type.addProperty(RDFS.domain, Resource);
		type.addProperty(RDFS.range, RDFS.Literal);
		id.addProperty(RDFS.domain, Resource);
		id.addProperty(RDFS.range, RDFS.Literal);
//		numberOfCPUs.addProperty(RDFS.domain, VM);
//		numberOfCPUs.addProperty(RDFS.range, RDFS.Literal);
		
		cloudProvider.addProperty(RDFS.subPropertyOf, IDRef);
		requiredComponents.addProperty(RDFS.subPropertyOf, IDRef);
		location.addProperty(RDFS.subPropertyOf, IDRef);
		providedMethods.addProperty(RDFS.subPropertyOf, IDRef);
	}

	public static Property makeProperty(String string) {
		return model.createProperty(URI + string);
	}

	private static OntClass makeClass(String string) {
		return model.createClass(URI + string);
	}

    
	public static String shortForm(Property property) {
		return prefix+":"+property.getLocalName();
	}
	
	public static String shortForm(OntClass ontClass) {
		return prefix+":"+ontClass.getLocalName();
	}
	
	public static void main(String[] args) {
		// System.out.println("<?xml version=\"1.0\"?>");
		// m.write(System.out, "RDF/XML-ABBREV");
		MO.model.write(System.out, "TURTLE");
	}
   

}
