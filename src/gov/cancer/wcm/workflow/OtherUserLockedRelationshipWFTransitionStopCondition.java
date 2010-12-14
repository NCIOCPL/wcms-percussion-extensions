package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition to check if 
 * the dependent of a relationship is locked by another user.
 * @author bpizzillo
 *
 */
public class OtherUserLockedRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(PSComponentSummary contentItemSummary,
			PSRelationship rel) {
		// TODO Auto-generated method stub
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
