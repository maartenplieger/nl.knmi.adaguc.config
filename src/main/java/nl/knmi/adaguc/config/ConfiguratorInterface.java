package nl.knmi.adaguc.config;

import nl.knmi.adaguc.tools.ElementNotFoundException;
import nl.knmi.adaguc.tools.MyXMLParser.XMLElement;

/**
 * @author maartenplieger
 * New configurators must implement this class. New configurators are automatically found by using Reflection on classes which implement this interface.
 *
 */
public interface ConfiguratorInterface {
//	static class ConfiguratorHelper {
//		static boolean configDone = false;
//	
//		public void setConfigDone() {
//			configDone = true;
//		}
//		
//		public static boolean getConfigDone() {
//			return configDone;
//		}
//	}
//	
//	ConfiguratorHelper configurationHelper = null;
	
//	public void createConfigurationHelper ();
		
	/**
	 * This method is called when the configuration is read.
	 * @param configReader The configuration file object
	 * @throws Exception 
	 */
	public void doConfig(XMLElement configReader) throws ElementNotFoundException;
	
	
//	public void setConfigDone ();

}

