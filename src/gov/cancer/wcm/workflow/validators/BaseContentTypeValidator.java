package gov.cancer.wcm.workflow.validators;

import java.util.List;

import gov.cancer.wcm.workflow.PublishingDirection;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a base class for ContentTypeValidators
 * @author wallsjt
 *
 */
public abstract class BaseContentTypeValidator {

	private List<PublishingDirection> _validationDirections;
	

	/**
	 * Gets the publishing directions for which this validator should fire.
	 * @return
	 */
	public List<PublishingDirection> getValidationDirections() {
		return _validationDirections;		
	}
	
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
	public BaseContentTypeValidator(List<PublishingDirection> validationDirections) {
		_validationDirections = validationDirections;
	}
}
