package gov.cancer.wcm.workflow.validators;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.cancer.wcm.privateArchive.PrivateArchiveManager;
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

	private static Log log = LogFactory.getLog(PrivateArchiveManager.class);

	private List<PublishingDirection> _validationDirections;
	private ValidationIgnoreConditionCollection _ignoreConditions;
	

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
	public abstract boolean isValid(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			);

	/**
	 * @return true if validation is required to fire, false if validation may be skipped.
	 */
	public boolean MustFire(WorkflowValidationContext wvc){
		log.trace("Enter MustFire()");
		boolean mustFire = _ignoreConditions.validationMustFire(wvc);
		log.debug("Must fire: " + mustFire);
		return mustFire;
	}
	
	/**
	 * 
	 * @param relationshipName
	 */
	public BaseContentTypeValidator(List<PublishingDirection> validationDirections,
			ValidationIgnoreConditionCollection ignoreConditions) {
		log.trace("Enter BaseContentTypeValidator constructor." );
		_validationDirections = validationDirections;
		_ignoreConditions = ignoreConditions;
	}
}
