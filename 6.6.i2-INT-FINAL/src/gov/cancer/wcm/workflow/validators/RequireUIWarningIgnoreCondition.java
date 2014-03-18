/**
 * 
 */
package gov.cancer.wcm.workflow.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.server.IPSRequestContext;

import gov.cancer.wcm.privateArchive.PrivateArchiveManager;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

/**
 * Ignore condition for determining whether workflow validation must take place.
 * RequireUIWarningIgnoreCondition verifies that the user has been shown (and
 * chosen to ignore) a warning message about a workflow action.
 * 
 * RequireUIWarningIgnoreCondition checks for the form field identified in UI_WARNING_FIELD
 * to be submitted as part of submitting a UI action.  Valid values are Y, y, T, t and 1.
 * The UI implementor is responsible for submitting the field as part of an HTTP request.
 * @author learnb
 *
 */
public class RequireUIWarningIgnoreCondition extends ValidationIgnoreCondition {

	public static final String UI_WARNING_TRIGGER_FLAG = RequireUIWarningIgnoreCondition.class.getName() + "UI_WARNING_TRIGGER_FLAG"; 

	private static Log log = LogFactory.getLog(PrivateArchiveManager.class);

	/* (non-Javadoc)
	 * @see gov.cancer.wcm.workflow.validators.ValidationIgnoreCondition#ValidationMustFire()
	 */
	@Override
	public boolean ValidationMustFire(WorkflowValidationContext wvc) {
		log.trace("Entering ValidationMustFire(WorkflowValidationContext wvc).");

		// The HTTP request object.
		IPSRequestContext request = wvc.getRequest();
		String warningParam = request.getParameter(UI_WARNING_TRIGGER_FLAG);
		char first;
		
		log.debug("warningParam = " + warningParam);

		boolean parameterNotFound = true;

		// If the warning parameter value exists and has a state,
		// check whether it contains one of the permitted "OK to skip"
		// values.
		if(warningParam != null && warningParam.length() == 1){
			first = warningParam.charAt(0);
			switch(first){
				case 'T':
				case 't':
				case 'Y':
				case 'y':
				case '1':
					parameterNotFound = false;
			}
		}

		log.debug("Final return value is: " + parameterNotFound);
		return parameterNotFound;
	}

}
