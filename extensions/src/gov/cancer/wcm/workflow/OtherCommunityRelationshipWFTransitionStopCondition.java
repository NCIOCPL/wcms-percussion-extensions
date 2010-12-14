package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition for checking if the
 * dependent of the relationship is in another community.
 * @author bpizzillo
 *
 */
public class OtherCommunityRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary, PSRelationship rel) {
		
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
