package it.polimi.tower4clouds.observers.hdb;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;

public class Main {
	
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	@Parameter(names = { "-h", "--help", "-help" }, help = true, description = "Shows this help" , hidden = true )
	private boolean help = false;
	
	@Parameter(names = "-queueip", description = "Queue endpoint IP address (Default: 127.0.0.1)")
	public String queueip = null;
	
	@Parameter(names = "-queueport", description = "Queue endpoint port (Default: 5672)")
	public String queueport = null;
	
	@Parameter(names = "-dbip", description = "DB endpoint IP address (Default: 127.0.0.1)")
	public String dbip = null;
	
	@Parameter(names = "-dbpath", description = "DB URL path (Default: /ds)")
	public String dbpath = null;
	
	@Parameter(names = "-dbport", description = "DB endpoint port (Default: 3030)")
	public String dbport = null;
	
	@Parameter(names = "-listenerport", description = "Listener endpoint port (Default: 31337)")
	public String listenerport = null;
	
	@Parameter(names = "-fakemessages", description = "Test the tool by sending a number of fake messages (Default: 0)")
	public String fakemessages = null;
	
	@Parameter(names = "-waitfakemessages", description = "The ms to wait between each fake message (Default: 1000 ms == 1 second)")
	public String waitfakemessages = null;
	
	public static final String APP_TITLE = "\nHistory-DB\n";
	public static final String APP_NAME = "historydb";

	public static void main(String[] args) {
		
		args = new String[] {"-fakemessages", "10", "-waitfakemessages", "1000" }; //, "-h", "-queueip", "109.231.121.52"}; // , "-queueport", "55672"});
		
		System.out.println(APP_TITLE);
		
		Main m = new Main();
		JCommander jc = new JCommander(m, args);
		
		if (m.help) {
			jc.setProgramName(APP_NAME);
			jc.usage();
			System.exit(0);
		}
		
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		
		for (ParameterDescription param : jc.getParameters())
			if (param.isAssigned()) {
				String name = param.getLongestName().replaceAll("-", "");
				String value = null;
				try {
					value = Main.class.getField(name).get(m).toString();
				} catch (Exception e) { }
				
				paramsMap.put(name, value);
			}
		
		perform(paramsMap);
	}
	
	public static void perform(Map<String, String> paramsMap) {
		it.polimi.tower4clouds.observers.hdb.manager.Main.perform(paramsMap);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("Error while waiting!", e);
		}
		
		it.polimi.tower4clouds.observers.hdb.metricsobserver.Main.perform(paramsMap);
	}

}
