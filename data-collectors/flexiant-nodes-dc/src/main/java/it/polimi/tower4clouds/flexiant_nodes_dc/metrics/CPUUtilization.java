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
package it.polimi.tower4clouds.flexiant_nodes_dc.metrics;

import it.polimi.tower4clouds.flexiant_nodes_dc.CsvFileParser;
import it.polimi.tower4clouds.flexiant_nodes_dc.Metric;
import it.polimi.tower4clouds.model.ontology.Resource;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class CPUUtilization extends Metric{
    
    private static final Logger logger = LoggerFactory.getLogger(CPUUtilization.class);
        
    @Override
    protected void createTask(Timer timer, Resource node, int samplingTime){
        timer.scheduleAtFixedRate(new CpuUtilizationSender(node), 
                        0, samplingTime * 1000);
    }
    
    /*
        Private class which handle acquisition and sending of a sample.
    */
    private final class CpuUtilizationSender extends TimerTask {
        private Resource node;
        private CsvFileParser fileParser;
        
        //constructor: initialize a CsvFileParser object which handle the reading
        //of the remote file
        public CpuUtilizationSender(Resource node) {
            this.node = node;
            String fileName = node.getId()+".csv";
            if(node.getType().equals("cluster2"))
                fileName = node.getType()+"-"+fileName;
            
            logger.info("URL: "+getUrlFileLocation()+fileName);
            fileParser = new CsvFileParser(getUrlFileLocation()+fileName, null);
            
        }
        
        @Override
        public void run() {
            send(getSample(), node);
	}
        
        //method which acquire the sample from the remote file
        private double getSample(){
            logger.info("Getting Sample...");
            long first = System.currentTimeMillis();
            double sample = 0.0;
            int count = 0;
            fileParser.setFileUrl(getUrl());
            if(!fileParser.readLastUpdate(0)){
                //if the .csv file doesn't exists then search for the compressed file
                fileParser.setFileUrl(getUrl()+".1.gz");
                fileParser.readLastUpdate(0);
            }
            List<String> values = fileParser.getData(2);
            for(String value:values){
                sample += Double.parseDouble(value);
                count++;
            }
            logger.info("Sample calculated in: "+(System.currentTimeMillis()-first)+ "ms");
            return sample/count;
            
        }
        
        //method which build url of the remote file to parse.
        private String getUrl(){
            String url;
            url = getUrlFileLocation();
            if(node.getType().equals("cluster2"))
                url += node.getType()+"-"+node.getId()+".csv";
            else
                url += node.getId()+".csv";
            return url;
        }
    }
    
    
}
