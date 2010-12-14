package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition to check the dependent
 * of a relationship's dependents.  This will continue following relationships
 * until it has no more relationships to follow.
 * @author bpizzillo
 *
 */
public class DependentsCheckRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary, 
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		// TODO Auto-generated method stub
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
