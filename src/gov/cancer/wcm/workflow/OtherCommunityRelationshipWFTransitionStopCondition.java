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
			PSComponentSummary contentItemSummary, 
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Checking OtherCommunity Stop Condition for dependent: " + rel.getDependent().getId());
		if (contentItemSummary.getCommunityId() == rel.getDependentCommunityId())
			wvc.getLog().debug("Dependent ID: " + rel.getDependent().getId() + " is in Same Community.");
		else
			wvc.getLog().debug("Dependent ID: " + rel.getDependent().getId() + " is in Other Community.");

		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

}
