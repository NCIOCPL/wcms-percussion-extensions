/**
 * 
 */
package gov.cancer.wcm.workflow.validators;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.server.IPSRequestContext;

import gov.cancer.wcm.workflow.WorkflowValidationContext;

/**
 * @author learnb
 *
 */
public class ExemptGroupIgnoreCondition extends ValidationIgnoreCondition {

	private static Log log = LogFactory.getLog(ExemptGroupIgnoreCondition.class);

	// For fast lookups, we'll store the exempt groups in a HashSet.
	private HashSet<String> _exemptRoles;
	
	/* (non-Javadoc)
	 * @see gov.cancer.wcm.workflow.validators.ValidationIgnoreCondition#ValidationMustFire(gov.cancer.wcm.workflow.WorkflowValidationContext)
	 */
	@Override
	public boolean ValidationMustFire(WorkflowValidationContext wvc) {
		log.trace("Entering ValidationMustFire(WorkflowValidationContext wvc)");
		
		// Get the list of roles the user belongs to.
		List<String> roles = getUserRoles(wvc.getRequest());
		
		// Check each role to find if is one of the exempt ones.
		boolean userIsExempt = false;
		for(String role : roles){
			if(_exemptRoles.contains(role)){
				userIsExempt = true;
				break;
			}
		}

		// Note the negation.  If the user is exempt, validation does not need to fire.
		// If the user is not exempt, then validation does need to fire.
		return !userIsExempt;
	}
	
	/**
	 * Get the list of roles for the user who made the initial request.
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getUserRoles(IPSRequestContext request){
		// Get the list of roles
		PSSubject user = request.getOriginalSubject();
		
		// Per the Rhythmyx documentation, getSubjectRoles() returns a List which is
		// nothing but strings.  Under the hood, Java treats a List<String> identically
		// to a List which happens to contain strings, but this causes a type-safety
		// warning.  Because we know that the List really does contain strings, we
		// can safely add the @SuppressWarnings("unchecked") annotation.
		return request.getSubjectRoles(user);
	}

	/**
	 * 
	 */
	public ExemptGroupIgnoreCondition(List<String> exemptRoles) {
		
		_exemptRoles = new HashSet<String>(exemptRoles);
	}

}
