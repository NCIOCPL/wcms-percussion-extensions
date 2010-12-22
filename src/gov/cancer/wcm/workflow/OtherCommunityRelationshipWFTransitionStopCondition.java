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
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary, 
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Other Community Stop Condition (down): Checking dependent: " + rel.getDependent().getId());
		if (ownerContentItemSummary.getCommunityId() == rel.getDependentCommunityId()) {
			wvc.getLog().debug("Other Community Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Same Community.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
		else {
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) {
				wvc.getLog().debug("Other Community Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Other Community and has public revision.");
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {
				wvc.getLog().debug("Other Community Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " is in Other Community and does not have public revision.");
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
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Other Community Stop Condition (Up): Checking owner: " + rel.getOwner().getId());
		
		//Get the summary
		PSComponentSummary ownerSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getOwner().getId());
		
		if (ownerSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("OtherUserCheckedOut Stop Condition (Up): Could not get Component Summary for id: " + rel.getOwner().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}
		
		if (dependentContentItemSummary.getCommunityId() == ownerSummary.getCommunityId()) {
			wvc.getLog().debug("Other Community Stop Condition (Up): Owner ID: " + rel.getOwner().getId() + " is in Same Community.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
		else {
			//Up validation does not check if the item has a public revision since we know that the push must
			//start with the dependentContentItemSummary passed in.
			wvc.getLog().debug("Other Community Stop Condition (Up): Owner ID: " + rel.getOwner().getId() + " is in Other Community.");
			return RelationshipWFTransitionStopConditionResult.OkStopChecking;
		}		
	}

	public OtherCommunityRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
