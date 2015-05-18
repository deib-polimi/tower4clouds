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
package it.polimi.tower4clouds.data_analyzer;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

public class KBTest {
	
	private CsparqlEngineImpl engine;

	@Before
	public void setUp() throws Exception{
		engine = new CsparqlEngineImpl();
		engine.initialize();
	}
	
	@Test
	public void clearAllTest1() throws Exception {
		engine.execUpdateQueryOverDatasource("INSERT DATA { GRAPH <http://example.org/graph> "
				+ "{ <http://example.org#s> <http://example.org#p> <http://example.org#o> } }");
		engine.execUpdateQueryOverDatasource("CLEAR ALL");
		RDFTable rdfTable = engine.evaluateGeneralQueryOverDatasource("SELECT * FROM <http://example.org/graph> WHERE { ?s ?p ?o }");
		assertTrue(rdfTable.isEmpty());
	}
	
	@Test
	public void clearAllTest2() throws Exception {
		engine.execUpdateQueryOverDatasource("INSERT DATA { GRAPH <http://example.org/graph> "
				+ "{ <http://example.org#s> <http://example.org#p> <http://example.org#o> } }");
		RDFTable rdfTable1 = engine.evaluateGeneralQueryOverDatasource("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <http://example.org/graph> { ?s ?p ?o } }");
		assertTrue(!rdfTable1.isEmpty());
		engine.execUpdateQueryOverDatasource("CLEAR ALL");
		RDFTable rdfTable2 = engine.evaluateGeneralQueryOverDatasource("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <http://example.org/graph> { ?s ?p ?o } }");
		assertTrue(rdfTable2.isEmpty());
	}

}
