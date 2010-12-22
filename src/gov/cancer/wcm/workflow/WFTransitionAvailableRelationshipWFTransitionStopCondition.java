package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.services.workflow.IPSWorkflowService;

/**
 * Defines a RelationshipWFTransitionStopCondition to check if the dependent of the
 * relationship is in the same workflow as the owner.  Technically, it should check if
 * it can transition with the owner.
 * @author bpizzillo
 *
 */
public class WFTransitionAvailableRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary, 
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Checking OtherWorkflow Stop Condition for dependent(down): " + rel.getDependent().getId());

		
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

	@Override 
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Checking OtherWorkflow Stop Condition for owner(up): " + rel.getOwner().getId());
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

	public WFTransitionAvailableRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
