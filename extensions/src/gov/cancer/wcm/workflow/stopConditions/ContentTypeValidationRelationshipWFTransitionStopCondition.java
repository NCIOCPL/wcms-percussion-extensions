package gov.cancer.wcm.workflow.stopConditions;


import gov.cancer.wcm.util.CGV_TypeNames;
import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowConfigurationLocator;
import gov.cancer.wcm.workflow.WorkflowValidationContext;
import gov.cancer.wcm.workflow.checks.RelationshipWFTransitionCheckResult;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Checks a list of BaseContentTypeValidator based off the content type, setup in the workflow config.
 * Runs the entire list of the validators, catching any errors.
 * @author wallsjt
 *
 */
public class ContentTypeValidationRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override 
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary, 
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Content Type Validation Stop Condition (down): Checking item: " + dependentContentItemSummary.getContentId());
		String contentTypeName = "";
		try {
			contentTypeName = CGV_TypeNames.getTypeName(dependentContentItemSummary.getContentTypeGUID().getUUID());
		} catch (Exception ex) {
			wvc.getLog().error("Content Type Validation: Could not get content type name for id: " + dependentContentItemSummary.getContentTypeGUID().getUUID(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);
		}
		if(	!WorkflowConfigurationLocator.
				getWorkflowConfiguration().
				getContentTypes().
				getContentTypeOrDefault(contentTypeName).
				getValidatorCollection().
				validate(dependentContentItemSummary, null, wvc) ){
			//Failed the validation
			wvc.getLog().debug("Failed the validation check for item with content id: " + 
					dependentContentItemSummary.getContentId() + ", see log for errors.", null);
			return RelationshipWFTransitionStopConditionResult.StopTransition;
		}
		wvc.getLog().debug("Content Type Validation Stop Condition (down): Item: " + dependentContentItemSummary.getContentId() + " is valid.");
		return RelationshipWFTransitionStopConditionResult.Ok;
	}

	@Override 
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().error("Content Type Validation Stop Condition: Cannot validate upwards. Check configuration. Called on dependent with id: " + dependentContentItemSummary.getContentId());
		throw new WFValidationException("System Error Occured. Please Check the logs.", true);
	}
	
	/**
	 * Initializes a new instance of the DependentsCheckRelationshipWFTransitionStopCondition class.
	 * @param checkDirection the direction when this check should be validated.
	 */
	public ContentTypeValidationRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}

}
