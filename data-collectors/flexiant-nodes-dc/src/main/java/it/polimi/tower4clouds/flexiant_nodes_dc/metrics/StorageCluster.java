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

/**
 *
 * @author davide
 */
public class StorageCluster extends Metric{
    
    public StorageCluster(){
        metricType = MetricsType.CLUSTER_METRIC;
    }

    @Override
    public Number getSample(CsvFileParser fileParser, Resource resource) throws Exception {
        fileParser.setFileUrl(getUrl(resource));
        fileParser.readLastUpdate(0);
        double sample;
        List<String> usedStorage = fileParser.getData(3);
        sample = Double.parseDouble(usedStorage.get(0));
        return sample;
        
    }
    
    //method which build url of the remote file to parse.
    @Override
    protected String getUrl(Resource resource){
        return getUrlFileLocation()+resource.getId()+".csv";
    }
    
}
