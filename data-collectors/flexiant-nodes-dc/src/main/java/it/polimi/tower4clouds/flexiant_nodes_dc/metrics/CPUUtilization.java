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
import it.polimi.tower4clouds.flexiant_nodes_dc.MetricsType;
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
    
    public CPUUtilization(){
        metricType = MetricsType.NODE_METRIC;
    }
    
    @Override
    public Number getSample(CsvFileParser fileParser, Resource resource) {
        
        fileParser.setFileUrl(getUrl(resource));
        if(!fileParser.readLastUpdate(0)){
            //if the .csv file doesn't exists then search for the compressed file
            fileParser.setFileUrl(getUrl(resource)+".1.gz");
            fileParser.readLastUpdate(0);
        }
        
        double sample = 0.0;
        int count = 0;
        List<String> values = fileParser.getData(2);
        for(String value:values){
            sample += Double.parseDouble(value);
            count++;
        }
        return sample/count;
        
    }
    
}
