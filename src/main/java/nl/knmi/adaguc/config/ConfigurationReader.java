package nl.knmi.adaguc.config;


import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import nl.knmi.adaguc.tools.Debug;
import nl.knmi.adaguc.tools.ElementNotFoundException;
import nl.knmi.adaguc.tools.MyXMLParser.XMLElement;
import nl.knmi.adaguc.tools.Tools;
import org.springframework.context.annotation.Bean;

/**
 * @author maartenplieger
 * Configuration framework for reading one configuration file with multiple sections for pluggable configurators.
 * New configurators must implement the nl.knmi.adaguc.config.ConfigurationInterface class. 
 * New configurators are automatically found in the project by using Reflection on classes which implement this interface.
 *
 */
public class ConfigurationReader {
	public long readConfigPolInterval = 0;
	private final long readConfigPolIntervalDuration = 10000;
	public String configFileLocationByEnvironment = "ADAGUC_SERVICES_CONFIG";
	public String configFileNameInHomeByDefault = "adaguc-services-config.xml";

//
//	@Value("${value.from.file}")
//	private String stringValue;
//	
//	public void afterPropertiesSet() throws Exception { 
//		Debug.println(stringValue);
//		readConfig();
//	}
	
	public ConfigurationReader(){
	}
	
	private String _getHomePath(){
		return System.getProperty("user.home")+"/";
	}

	private String _getConfigFile(){
		try{
			String configLocation = System.getenv(configFileLocationByEnvironment);
			if(configLocation!=null){
				if(configLocation.length()>0){
					return configLocation;
				}
			}
		}catch(Exception e){
		}
		return _getHomePath()+configFileNameInHomeByDefault;
	}

	public synchronized void reset() {
		readConfigPolInterval = 0;
	}

	/**
	 * 1) Reads the configuration file periodically, re-reads the configuration file every readConfigPolIntervalDuration ms
	 * 2) When read, the doConfig method for all classes which implement ConfiguratorInterface is called
	 * @throws ConfigurationItemNotFoundException 
	 * @throws ElementNotFoundException 
	 * @throws Exception 
	 */
	public synchronized void readConfig() throws  ElementNotFoundException{
		/* Re-read the configuration file every 10 seconds. */
		if(readConfigPolInterval != 0){
			if(System.currentTimeMillis()<readConfigPolInterval+readConfigPolIntervalDuration )return;
		}

		readConfigPolInterval=System.currentTimeMillis(); 
		Debug.println("Reading configfile "+_getConfigFile());
		XMLElement configReader = new XMLElement();
		//try {
			String configFile;
			try {
				configFile = Tools.readFile(_getConfigFile());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
				throw new ElementNotFoundException("Unable to read configuration file " + _getConfigFile());
			}
			
			/* Replace ${ENV.} with environment variable */
			Set<String> foundValues = new HashSet<String>();
			int start = 0, end = 0, index = -1;
			do{
				index = configFile.substring(start).indexOf("{ENV.");
				if(index>=0){
					start += index;
					end = configFile.substring(start).indexOf("}");
					if(end >=0){
						end+=(start+1);
					    foundValues.add(configFile.substring(start, end));
						start=end;
					}else{
						start+=5; /* In case } is missing, jump 5 places forward */
					}
				}
			}while(index !=-1);
			for(String key : foundValues) {
				String envKey = key.substring(5,key.length()-1);
				String envValue = System.getenv(envKey);
				if(envValue == null){
					throw new ElementNotFoundException("Environment variable ["+envKey+"] not set");
				}
				configFile = configFile.replace(key,envValue);
			}
			
	
			try {
				configReader.parseString(configFile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		///}
		Reflections reflections = new Reflections("nl.knmi");
		

        
		Set<Class<? extends ConfiguratorInterface>> allClasses = 
				reflections.getSubTypesOf(ConfiguratorInterface.class);
		
		Iterator<Class<? extends ConfiguratorInterface>> it = allClasses.iterator();
		while(it.hasNext()){
			Class<? extends ConfiguratorInterface> a = it.next();
			try {
				Debug.println("==> Calling get " + a.getName());
				a.newInstance().doConfig(configReader);
//				a.newInstance().setConfigDone();;
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		configReader = null; 
	}

	
	

}

