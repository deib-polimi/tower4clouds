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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class CsvFileParser {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvFileParser.class);
    
    private String fileUrl;
    
    private Stack<String> fileLines;
    private String terminationString;
    
    private List<String> itemsArray[];
    
    public CsvFileParser(String fileUrl, String terminationString){
        this.fileUrl = fileUrl;
        this.terminationString = terminationString;
    }
    
    //Method which read the file line by line and load every line in a stack to allow
    //the read of data from the most recent.
    private boolean readFile(){
        
        try{
            URL url = new URL(fileUrl);
            InputStream is = url.openStream();
            if(fileUrl.endsWith(".gz"))
                is = new GZIPInputStream(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            fileLines = new Stack<String>();
            String line;
            while((line=br.readLine())!=null){
                fileLines.push(line);
            }
            br.close();
            is.close();
            return true;
        }//try
        catch(Exception ex){
            logger.warn("Error while reading remote file: "+fileUrl);
            return false;
        }
        
    }
    
    //load lines of the CSV file untile the termination string
    public boolean readUntilTerminationString(){
        if(!readFile())
            return false;
        try{
            String line = fileLines.pop();
            String[] parts = line.split(",");
            itemsArray = new ArrayList[parts.length];
            for(int i = 0; i < itemsArray.length; i++)
                itemsArray[i] = new ArrayList<String>();
            
            while(!line.equals(this.terminationString)){
                for(int i = 0; i < parts.length; i++)
                    itemsArray[i].add(parts[i]);
                line = fileLines.pop();
                parts = line.split(",");
            }//while
            return true;
        }
        catch(EmptyStackException ex){
            logger.warn("Empty stack error: termination string not found");
            return false;
        }
        catch(ArrayIndexOutOfBoundsException ex){
            logger.warn("Invalid data position");
            return false;
        }
        
    }
    
    //load lines of the CSV file untile the content of the selected column changes
    //usually the selected column content is the timestamp
    public boolean readLastUpdate(int positionColumn){
        if(!readFile())
            return false;
        try{
            String line = fileLines.pop();
            String[] parts = line.split(",");
            String delimiter = parts[positionColumn];
            itemsArray = new ArrayList[parts.length];
            for(int i = 0; i < itemsArray.length; i++)
                itemsArray[i] = new ArrayList<String>();
            
            while(parts[positionColumn].equals(delimiter)){
                for(int i = 0; i < itemsArray.length; i++){
                    if(i > parts.length-1)
                        break;
                    itemsArray[i].add(parts[i]);
                }//for
                if(fileLines.empty())
                    break;
                line = fileLines.pop();
                parts = line.split(",");
                
            }//while
            return true;
        }
        catch(EmptyStackException ex){
            logger.warn("Empty stack error");
            return false;
        }
        catch(ArrayIndexOutOfBoundsException ex){
            logger.warn("Invalid data position");
            return false;
        }
    }
    
    //return a column of data specifice by the position parameter, every element represent
    //the content of the columns in a row.
    public List<String> getData(int position){
        try{
            return itemsArray[position];
        }
        catch(ArrayIndexOutOfBoundsException ex){
            logger.warn("Invalid data position in CSV file");
            return null;
        }
        
    }
    
    public void setFileUrl(String fileUrl){
        this.fileUrl = fileUrl;
    }
    
}
