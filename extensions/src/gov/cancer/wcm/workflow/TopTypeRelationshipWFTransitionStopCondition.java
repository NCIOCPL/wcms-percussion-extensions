package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition for checking if the
 * dependent of a relationship is a top type.
 * @author bpizzillo
 *
 */
public class TopTypeRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		
		wvc.getLog().debug("Checking Top Type Stop Condition for dependent: " + rel.getDependent().getId());
		
		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
