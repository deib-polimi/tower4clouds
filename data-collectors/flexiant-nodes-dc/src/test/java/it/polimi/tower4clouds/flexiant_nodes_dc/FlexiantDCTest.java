/*
 * Copyright 2015 Politecnico di Milano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.polimi.tower4clouds.flexiant_nodes_dc;

import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.CPUUtilization;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.NodeLoadMetric;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.RXNetworkMetric;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.RackLoad;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.RamUsage;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.StorageCluster;
import it.polimi.tower4clouds.flexiant_nodes_dc.metrics.TXNetworkMetric;
import it.polimi.tower4clouds.model.ontology.Node;
import it.polimi.tower4clouds.model.ontology.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class FlexiantDCTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FlexiantDCTest.class);
    private static final String TEST_URL = "file://"+FlexiantDCTest.class.getResource("/").getPath();
    
    private static final double EXPECTED_CPU_SAMPLE = 1.0;
    private static final double EXPECTED_NODE_LOAD_SAMPLE = 1.620;
    private static final double EXPECTED_RX_NET_SAMPLE = 2.68;
    private static final double EXPECTED_TX_NET_SAMPLE = 4.39;
    private static final double EXPECTED_RAM_SAMPLE = 1.0;
    private static final double EXPECTED_STORAGE_SAMPLE = 1.0;
    private static final int EXPECTED_RACK_LOAD_SAMPLE = 8;
    
    @Test
    public void testCPUUtilization() {
        
        CPUUtilization metric = new CPUUtilization();
        metric.setUrlFileLocation(TEST_URL);
        Resource testNode = new Node("type1", "testIdNodeCPU");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL , null);
        
        double sample = (double)metric.getSample(fileParser, testNode);
        
        Assert.assertTrue(sample == EXPECTED_CPU_SAMPLE);
        
    }
    
    @Test
    public void testNodeLoadMetric() {
        
        NodeLoadMetric metric = new NodeLoadMetric();
        metric.setUrlFileLocation(TEST_URL);
        Resource testNode = new Node("type1", "testIdNodeLOAD");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL , null);
        
        double sample = (double)metric.getSample(fileParser, testNode);
        
        Assert.assertTrue(sample == EXPECTED_NODE_LOAD_SAMPLE);
        
    }
    
    @Test
    public void testRXNetworkMetric() {
        
        RXNetworkMetric metric = new RXNetworkMetric();
        metric.setUrlFileLocation(TEST_URL);
        Resource testNode = new Node("type1", "testIdNodeNET");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL , null);
        
        double sample = (double)metric.getSample(fileParser, testNode);
        
        Assert.assertTrue(sample == EXPECTED_RX_NET_SAMPLE);
        
    }
    
    @Test
    public void testTXNetworkMetric() {
        
        TXNetworkMetric metric = new TXNetworkMetric();
        metric.setUrlFileLocation(TEST_URL);
        Resource testNode = new Node("type1", "testIdNodeNET");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL , null);
        
        double sample = (double)metric.getSample(fileParser, testNode);
        
        Assert.assertTrue(sample == EXPECTED_TX_NET_SAMPLE);
        
    }
    
    @Test
    public void testRamUsage() {
        
        RamUsage metric = new RamUsage();
        metric.setUrlFileLocation(TEST_URL);
        Resource testNode = new Node("type1", "testIdNodeRAM");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL , null);
        
        double sample = (double)metric.getSample(fileParser, testNode);
        
        Assert.assertTrue(sample == EXPECTED_RAM_SAMPLE);
        
    }
    
    @Test
    public void testStorageCluster() {
        
        StorageCluster metric = new StorageCluster();
        metric.setUrlFileLocation(TEST_URL);
        Resource testCluster = new Node("type1", "testCluster");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL , null);
        
        double sample = (double)metric.getSample(fileParser, testCluster);

        Assert.assertTrue(sample == EXPECTED_STORAGE_SAMPLE);
        
    }
    
    @Test
    public void testRackLoad() {
        
        RackLoad metric = new RackLoad();
        metric.setUrlFileLocation(TEST_URL + "upsload.csv");
        Resource testRack = new Node("type1", "A4");
        CsvFileParser fileParser = new CsvFileParser(TEST_URL + "upsload.csv" , null);
        
        int sample = (int)metric.getSample(fileParser, testRack);

        Assert.assertTrue(sample == EXPECTED_RACK_LOAD_SAMPLE);
        
    }
    
}
