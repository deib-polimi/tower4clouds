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
package it.polimi.tower4clouds.evaluation.scalability.rules;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import it.polimi.tower4clouds.data_collector_library.DCAgent;
import it.polimi.tower4clouds.manager.api.ManagerAPI;
import it.polimi.tower4clouds.model.data_collectors.DCDescriptor;
import it.polimi.tower4clouds.model.ontology.VM;

public class Main {

	

	@Parameter(names = "-manager-addr", description = "Manager address")
	private String serverAddress = "127.0.0.1";

	@Parameter(names = "-manager-port", description = "Manager port")
	private int serverPort = 8170;
	
	@Parameter(names = "-process", description = "Process number")
	private int process = 0;
	
	@Parameter(names = "-threads", description = "Number of clients to spawn")
	private int threads = 1;

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		Main main = new Main();
		new JCommander(main, args);
		main.run();
	}

	private void run() {
		for (int i = 0; i < threads; i++) {
			new MockDC(Integer.toString(process*threads + i));
		}
	}

	
	public class MockDC implements Observer {

		private final String id;
		private final VM vm;
		private final ScheduledExecutorService executor;
		private final DCAgent dcAgent;
		
		public MockDC(String id) {
			this.id = id;
			dcAgent = new DCAgent(new ManagerAPI(serverAddress, serverPort));
			DCDescriptor dCDescriptor = new DCDescriptor();
			vm = new VM("TestVM", id);
			dCDescriptor.addResource(vm);
			dCDescriptor.addMonitoredResource("CPUUtilization", vm);
			dCDescriptor.setConfigSyncPeriod(60);
			dCDescriptor.setKeepAlive(90);
			dcAgent.setDCDescriptor(dCDescriptor);
			dcAgent.start();
			LOG.info("DC for {} started", id);
			dcAgent.addObserver(this);
			
			executor = Executors.newSingleThreadScheduledExecutor();
		}

		@Override
		public void update(Observable o, Object arg) {
			
			if (!dcAgent.getRequiredMetrics().isEmpty()) {
				LOG.info("DC {} starts sending metrics", id);
				executor.scheduleAtFixedRate(new Runnable() {
					
					@Override
					public void run() {
						dcAgent.send(vm, "CPUUtilization", 0.3);
					}
				}, 0, 10, TimeUnit.SECONDS);
				
			} else {
				LOG.info("DC {} stops sending metrics", id);
				executor.shutdownNow();
			};
		}

	}
}
