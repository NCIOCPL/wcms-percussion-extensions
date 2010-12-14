package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition to check if the dependent of the
 * relationship is in the same workflow as the owner.  Technically, it should check if
 * it can transition with the owner.
 * @author bpizzillo
 *
 */
public class OtherWorkflowRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary, PSRelationship rel) {
		// TODO Auto-generated method stub
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
