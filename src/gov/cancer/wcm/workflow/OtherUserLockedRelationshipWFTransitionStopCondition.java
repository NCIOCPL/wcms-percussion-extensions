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
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("OtherUserCheckedOut Stop Condition: Checking dependent: " + rel.getDependent().getId());

		//Get the summary
		PSComponentSummary dependentSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("OtherUserCheckedOut Stop Condition: Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}

		String checkedOutUser = ContentItemWFValidatorAndTransitioner.isCheckedOutToOtherUser(contentItemSummary, wvc);
		if (checkedOutUser == null) {
			wvc.getLog().debug("OtherUserCheckedOut Stop Condition: Not checked out or checked out by this user.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		} else {
			wvc.getLog().debug("OtherUserCheckedOut Stop Condition: checked out to user, " + checkedOutUser);
			wvc.addError(
					ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
					ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
					ContentItemWFValidatorAndTransitioner.CHILD_IS_CHECKED_OUT,
					new Object[]{contentItemSummary.getContentId(), rel.getDependent().getId(), checkedOutUser});
			return RelationshipWFTransitionStopConditionResult.StopTransition;
		}
	}

}
