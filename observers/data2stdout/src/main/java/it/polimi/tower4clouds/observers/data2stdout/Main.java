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
package it.polimi.tower4clouds.observers.data2stdout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

@Path("/")
public class Main {
	
	@POST
    @Path("/data")
    public Response receiveData(InputStream incomingData) {
        StringBuilder dataBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                dataBuilder.append(line+"\n");
            }
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.print(dataBuilder.toString());
 
        return Response.status(204).build();
    }

    public static void startServer(String port) throws IOException {

        final ResourceConfig rc = new ResourceConfig().packages(Main.class.getPackage().getName());
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://0.0.0.0:"+port), rc, false);
        NetworkListener listener = httpServer.getListeners().iterator().next(); 
        ThreadPoolConfig thx=listener.getTransport().getWorkerThreadPoolConfig(); 
        thx.setQueueLimit(500);
        thx.setMaxPoolSize(500);
		httpServer.start();
    }

    public static void main(String[] args) throws IOException {
    	String port = (args.length > 0) ? args[0] : "8001";
        startServer(port);
    }
}
