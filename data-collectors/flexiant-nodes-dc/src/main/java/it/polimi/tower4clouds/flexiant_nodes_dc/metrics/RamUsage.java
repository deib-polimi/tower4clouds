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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class RamUsage extends Metric{
    
    private static final Logger logger = LoggerFactory.getLogger(RamUsage.class);
    
    public RamUsage(){
        metricType = MetricsType.NODE_METRIC;
    }
    
    @Override
    public Number getSample(CsvFileParser fileParser, Resource resource) {
        fileParser.setFileUrl(getUrl(resource));
        fileParser.readLastUpdate(0);
        int sample;
        List<String> values = fileParser.getData(1);
        sample = Integer.parseInt(values.get(0));
        return sample;
    }
    
}
