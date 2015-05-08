package gov.cancer.wcm.util;

import org.apache.log4j.*;

/**
 * Creates Log4J objects based on Property Configuration files
 * @author whole
 */
public class CGV_Logger {
	
	/**
	 * Retrieves a log4 object set based on parameters
	 * @return an instantiated logger object
	 */
	public static Logger getLogger(Class<?> theClass) {
		return Logger.getLogger(theClass);		
	}
	
	/**
	 * Converts the StackTraceElement array into readable strings.
	 * @param stackTraceElement - e.printStackTrace
	 * @return concatenated string of trace results
	 */
	public String StackTraceToString(StackTraceElement[] stackTraceElement) {
		String stackTraceString = "";
		for(int i=0; i<stackTraceElement.length; i++)
		{
			stackTraceString += ("\n  " + stackTraceElement[i].toString());
		}
		return stackTraceString;
	}
}