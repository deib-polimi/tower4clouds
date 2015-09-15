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
package it.polimi.tower4clouds.flexiant_nodes_dc;

/**
 *
 * @author davide
 * Class DCProperty: contains the names of the properties which must be set to
 * allow the DC to link the remote files.
 */
public final class DCProperty {
    
    public static final String URL_CPU_METRIC = "URL_CPU_METRIC";
    public static final String URL_RAM_METRIC = "URL_RAM_METRIC";
    public static final String URL_NODELOAD_METRIC = "URL_NODELOAD_METRIC";
    public static final String URL_TXNETWORK_METRIC = "URL_TXNETWORK_METRIC";
    public static final String URL_RXNETWORK_METRIC = "URL_RXNETWORK_METRIC";
    public static final String URL_STORAGE_METRIC = "URL_STORAGE_METRIC";
    public static final String URL_RACKLOAD_METRIC = "URL_RACKLOAD_METRIC";
    public static final String URL_VMS = "URL_VMS";
        
    //Resrources files path
    public static String placementFilePath;
    public static String nodesFilePath;
    public static String clustersFilePath;
    public static String racksFilePath;
}
