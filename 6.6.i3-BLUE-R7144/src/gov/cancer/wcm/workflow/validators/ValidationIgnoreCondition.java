/**
 * Base class for 
 */
package gov.cancer.wcm.workflow.validators;

import gov.cancer.wcm.workflow.WorkflowValidationContext;

/**
 * @author learnb
 *
 */
public abstract class ValidationIgnoreCondition {

	/**
	 * Performs a test to dtermine whether validation steps are required
	 * to run.
	 * @param wvc The workflow validation context.
	 * @return Returns true if validation is required, false if it may be skipped.
	 */
	public abstract boolean ValidationMustFire(WorkflowValidationContext wvc);
	
	public ValidationIgnoreCondition(){
		
	}
}
