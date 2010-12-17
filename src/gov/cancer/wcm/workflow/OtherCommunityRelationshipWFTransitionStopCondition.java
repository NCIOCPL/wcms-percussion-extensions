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
		wvc.getLog().debug("Other Community Stop Condition: Checking dependent: " + rel.getDependent().getId());
		if (contentItemSummary.getCommunityId() == rel.getDependentCommunityId()) {
			wvc.getLog().debug("Other Community Stop Condition: Dependent ID: " + rel.getDependent().getId() + " is in Same Community.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
		else {
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) {
				wvc.getLog().debug("Other Community Stop Condition: Dependent ID: " + rel.getDependent().getId() + " is in Other Community and has public revision.");
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {
				wvc.getLog().debug("Other Community Stop Condition: Dependent ID: " + rel.getDependent().getId() + " is in Other Community and does not have public revision.");
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_OTHER_COMMUNITY,
						new Object[]{contentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}	
		}		
	}

}
