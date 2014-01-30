/**
 * 
 */
package gov.cancer.wcm.logging;

/**
 * @author Learnb
 *
 * Exception thrown when an error happens in the gove.cancer.wcm.logging
 * package.
 */
public class LoggingException extends Exception {

	public LoggingException(String message) {
		super(message);
	}
	
	public LoggingException(String message, Throwable cause){
		super(message, cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
