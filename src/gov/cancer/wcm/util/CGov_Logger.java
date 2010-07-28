package gov.cancer.wcm.util;

import org.apache.log4j.*;

/**
 * Creates Log4J objects based on Property Configuration files
 * @author whole
 */
public class CGov_Logger {
	
	/**
	 * Retrieves a log4 object set based on parameters
	 * @return an instantiated logger object
	 */
	public static Logger getLogger(Class<?> theClass) {
		return Logger.getLogger(theClass);		
	}
}
