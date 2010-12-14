package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition for checking if
 * the dependent of a relationship is shared
 * @author bpizzillo
 *
 */
public class SharedRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(PSComponentSummary contentItemSummary,
			PSRelationship rel) {
		// TODO Auto-generated method stub
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
