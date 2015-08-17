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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author davide
 */
public class CsvFileParser {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvFileParser.class);
    
    private String fileUrl;
    
    //stack utilizzato per leggere le righe del file al contrario (dati più recenti
    //in testa allo stack
    private Stack<String> fileLines;
    private String terminationString;
    
    private List<String> itemsArray[];
    
    public CsvFileParser(String fileUrl, String terminationString){
        this.fileUrl = fileUrl;
        this.terminationString = terminationString;
    }
    
    //Metodo che legge il file riga per riga e carica ogni riga in uno stack in modo
    //da permetterne la lettura a partire dai dati più recenti
    private void readFile(){
        
        fileLines = new Stack<String>();
        
        try{
            URL url = new URL(fileUrl);
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while((line=br.readLine())!=null){
                fileLines.push(line);
            }
            br.close();
            is.close();
        }//try
        catch(Exception ex){
            logger.warn("Error while reading remote file: "+fileUrl);
        }
        
    }
    
    //carica le righe del file CSV fino alla stringa di terminazione
    public void readUntilTerminationString(){
        readFile();
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
        }
        catch(EmptyStackException ex){
            logger.warn("Empty stack error: termination string not found");
        }
        catch(ArrayIndexOutOfBoundsException ex){
            logger.warn("Invalid data position");
        }
        
    }
    
    //carica le righe del file CSV fino a che non cambia il contenuto della colonna
    //nella posizione specificata come parametro (solitamente è la colonna del timestamp)
    public void readLastUpdate(int positionColumn){
        readFile();
        try{
            String line = fileLines.pop();
            String[] parts = line.split(",");
            String delimiter = parts[positionColumn];
            itemsArray = new ArrayList[parts.length];
            for(int i = 0; i < itemsArray.length; i++)
                itemsArray[i] = new ArrayList<String>();
            
            while(parts[positionColumn].equals(delimiter)){
                for(int i = 0; i < parts.length; i++)
                    itemsArray[i].add(parts[i]);
                line = fileLines.pop();
                parts = line.split(",");
            }//while
        }
        catch(EmptyStackException ex){
            logger.warn("Empty stack error");
        }
        catch(ArrayIndexOutOfBoundsException ex){
            logger.warn("Invalid data position");
        }
    }
    
    //restituisce una colonna di dati specificata dalla posizione passata come parametro
    //ogni elemento rappresenta il contenuto della colonna in una determinata riga a
    //partire dalla fine del file.
    public List<String> getData(int position){
        try{
            return itemsArray[position];
        }
        catch(ArrayIndexOutOfBoundsException ex){
            logger.warn("Invalid data position in CSV file");
            return null;
        }
        
    }
    
}
