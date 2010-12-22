package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.utils.request.PSRequestInfo;

/**
 * Defines a RelationshipWFTransitionStopCondition to check if 
 * the dependent of a relationship is locked by another user.
 *
 * Note: There is NO check for public revision since an item which is checked out would still be
 * considered a component of the page. (vs. a shared component which is not "owned" by the page.
 * In a future release maybe we should check out all dependent items to a single user
 * whenever they check anything out.  (In essence locking the entire page not just a portion.) 
 * @author bpizzillo
 *
 */
public class OtherUserLockedRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("OtherUserCheckedOut Stop Condition(down): Checking dependent: " + rel.getDependent().getId());

		String checkedOutUser = ContentItemWFValidatorAndTransitioner.isCheckedOutToOtherUser(dependentContentItemSummary, wvc);
		if (checkedOutUser == null) {
			wvc.getLog().debug("OtherUserCheckedOut Stop Condition(down): Not checked out or checked out by this user.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		} else {
			wvc.getLog().debug("OtherUserCheckedOut Stop Condition(down): checked out to user, " + checkedOutUser);
			wvc.addError(
					ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
					ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
					ContentItemWFValidatorAndTransitioner.CHILD_IS_CHECKED_OUT,
					new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId(), checkedOutUser});
			return RelationshipWFTransitionStopConditionResult.StopTransition;
		}
	}

	@Override 
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("OtherUserCheckedOut Stop Condition(up): Checking dependent: " + rel.getOwner().getId());

		String checkedOutUser = ContentItemWFValidatorAndTransitioner.isCheckedOutToOtherUser(ownerContentItemSummary, wvc);
		if (checkedOutUser == null) {
			wvc.getLog().debug("OtherUserCheckedOut Stop Condition(up): Not checked out or checked out by this user.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		} else {
			wvc.getLog().debug("OtherUserCheckedOut Stop Condition(up): checked out to user, " + checkedOutUser);
			
			//Since this item is checked out to another user, then we cannot go to that item to start the transition
			//however, we need to get information back to the initial call to say, hey stop.  I think an exception
			//will make this behave the way that we want.
			
			throw new WFValidationException(
					String.format(
							ContentItemWFValidatorAndTransitioner.PARENT_IS_CHECKED_OUT, 
							dependentContentItemSummary.getContentId(),
							ownerContentItemSummary.getContentId(),
							checkedOutUser
					)
			);
		}
	}

	public OtherUserLockedRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
