package gov.cancer.wcm.workflow;

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
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) {
				wvc.getLog().debug("Other Workflow Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Other Community and has public revision.");
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {
				//TODO: Check if public revision
				wvc.getLog().debug("Other Workflow Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Other Community and does not have public revision.");
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_OTHER_COMMUNITY,
						new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}	
		}		
	}

	@Override
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary contentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) throws WFValidationException {

		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

	public OtherWorkflowRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
