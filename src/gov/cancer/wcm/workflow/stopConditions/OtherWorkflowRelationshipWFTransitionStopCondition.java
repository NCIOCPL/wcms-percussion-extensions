package gov.cancer.wcm.workflow.stopConditions;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * 
 * @author bpizzillo
 *
 */
public class OtherWorkflowRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary, 
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) throws WFValidationException {
				
		wvc.getLog().debug("Other Workflow Stop Condition (down): Checking dependent: " + rel.getDependent().getId());
		if (ownerContentItemSummary.getWorkflowAppId() == dependentContentItemSummary.getWorkflowAppId()) {
			wvc.getLog().debug("Other Workflow Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Same Community.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
		else {
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(dependentContentItemSummary, wvc)) {
				wvc.getLog().debug("Other Workflow Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Other Community and has public revision.");
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {
				wvc.getLog().debug("Other Workflow Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Other Community and does not have public revision.");
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_OTHER_WORKFLOW,
						new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}	
		}		
	}

	@Override
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) throws WFValidationException {

		if (dependentContentItemSummary.getWorkflowAppId() == ownerContentItemSummary.getWorkflowAppId()) {
			wvc.getLog().debug("Other Workflow Stop Condition (Up): Owner ID: " + rel.getOwner().getId() + " uses Same Workflow.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
		else {
			//Up validation does not check if the item has a public revision since we know that the push must
			//start with the dependentContentItemSummary passed in.
			wvc.getLog().debug("Other Workflow Stop Condition (Up): Owner ID: " + rel.getOwner().getId() + " uses Other Workflow.");
			return RelationshipWFTransitionStopConditionResult.OkStopChecking;
		}		
	}

	public OtherWorkflowRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
