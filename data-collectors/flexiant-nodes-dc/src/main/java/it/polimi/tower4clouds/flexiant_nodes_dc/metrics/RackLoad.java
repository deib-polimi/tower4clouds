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
package it.polimi.tower4clouds.flexiant_nodes_dc.metrics;

import it.polimi.tower4clouds.flexiant_nodes_dc.CsvFileParser;
import it.polimi.tower4clouds.flexiant_nodes_dc.Metric;
import it.polimi.tower4clouds.flexiant_nodes_dc.MetricsType;
import it.polimi.tower4clouds.model.ontology.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class RackLoad extends Metric{
    
    private static final Logger logger = LoggerFactory.getLogger(RackLoad.class);
    private static Map<String, Integer> lastSamples;
    
    public RackLoad(){
        metricType = MetricsType.RACK_METRIC;
        lastSamples = new HashMap<String, Integer>();
    }

    @Override
    protected Number getSample(CsvFileParser fileParser, Resource resource) {
        return retrieveSample(fileParser, getUrl(resource), resource);
    }
    
    private static synchronized Integer retrieveSample(CsvFileParser fileParser, String url, Resource resource){
        if(lastSamples.containsKey(resource.getId()))
            return lastSamples.remove(resource.getId());
        
        fileParser.setFileUrl(url);
        fileParser.readLastUpdate(0);
        List<String> rackIds = fileParser.getData(1);
        List<String> samples = fileParser.getData(2);
        for(int i = 0; i < rackIds.size(); i++){
            lastSamples.put(rackIds.get(i), Integer.parseInt(samples.get(i)));
        }
        
        return lastSamples.remove(resource.getId());
    }
    
    //method which build url of the remote file to parse.
    @Override
    protected String getUrl(Resource resource){
        return getUrlFileLocation();
    }
    
}
