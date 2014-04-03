package gov.cancer.wcm.workflow;

/**
 * Defines a class for throwing workflow validation Exceptions
 * @author bpizzillo
 *
 */
public class WFValidationException extends RuntimeException {
	
	private boolean _hasBeenLogged = false;
	
	/**
	 * Checks to see if the error has already been logged.
	 * @return
	 */
	public boolean hasBeenLogged() {
		return _hasBeenLogged;
	}
	
	public WFValidationException(String message) {
		super(message);
	}

	public WFValidationException(String message, boolean hasBeenLogged) {
		super(message);
		_hasBeenLogged = hasBeenLogged;
	}

	public WFValidationException(String message, Throwable innerException) {
		super(message, innerException);
	}

	public WFValidationException(String message, Throwable innerException, boolean hasBeenLogged) {
		super(message, innerException);
		_hasBeenLogged = hasBeenLogged;
	}

}
