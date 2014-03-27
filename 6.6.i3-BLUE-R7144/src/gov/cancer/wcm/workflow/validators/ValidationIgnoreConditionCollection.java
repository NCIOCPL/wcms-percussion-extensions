/**
 * Object to encapsulate the logic for determining whether workflow
 * validation must be executed or may be skipped.
 */
package gov.cancer.wcm.workflow.validators;

import gov.cancer.wcm.workflow.WorkflowValidationContext;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author learnb
 *
 */
public class ValidationIgnoreConditionCollection {
	private static Log log = LogFactory.getLog(ValidationIgnoreConditionCollection.class);

	private List<ValidationIgnoreCondition> _ignoreConditions;

	/**
	 * Determine whether validation must fire.  
	 * @return If any of the ignore conditions is not satisfied, then validation
	 * must fire.  If all conditions allow validation to be bypassed, then return false.
	 * If no ignore conditions exist, then returns true.
	 */
	public boolean validationMustFire(WorkflowValidationContext wvc){
		log.trace("Enter validationMustFire().");
		
		boolean conditionsExist = _ignoreConditions.size() > 0;
		boolean allConditionsAreSatisfied = true;
		boolean mustFire;
		

		// Check whether all ignore conditions are satisfied.
		// If any given condition is not satisfied, additional conditions
		// will not be tested.
		for(ValidationIgnoreCondition condition : _ignoreConditions){
			if(condition.ValidationMustFire(wvc)){
				if(log.isDebugEnabled()){
					log.debug(condition.getClass().getName() + " is not satisfied.");
				}
				allConditionsAreSatisfied = false;
				break;
			}
		}
		
		// If there are ignore conditions, AND all of them are satifired
		// then, validation may be skipped.
		// Otherwise, validation must fire.
		log.debug("conditionsExist = " + conditionsExist);
		log.debug("allConditionsAreSatisfied = " + allConditionsAreSatisfied);
		if(conditionsExist && allConditionsAreSatisfied){
			mustFire = false;
		} else {
			mustFire = true;
		}
		
		log.debug("mustFire = " + mustFire);
		return mustFire;
	}
	
	public ValidationIgnoreConditionCollection(List<ValidationIgnoreCondition> ignoreConditions){
		log.trace("Enter ValidationIgnoreConditionCollection constructor).");
		_ignoreConditions = ignoreConditions;
	}
}
