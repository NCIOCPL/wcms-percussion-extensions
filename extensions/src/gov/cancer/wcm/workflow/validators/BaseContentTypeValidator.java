package gov.cancer.wcm.workflow.validators;

import gov.cancer.wcm.workflow.WorkflowValidationContext;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a base class for ContentTypeValidators
 * @author wallsjt
 *
 */
public abstract class BaseContentTypeValidator {

	/**
	 * Validates whether or not the item can be allowed to transition or not.
	 * @param dependentContentItemSummary 
	 * @param rel
	 * @return If the object is valid to move, true.  Else, false.
	 */
	public abstract boolean validate(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			);

	/**
	 * 
	 * @param relationshipName
	 */
	public BaseContentTypeValidator() {
	}
}
