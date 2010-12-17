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
		
		wvc.getLog().debug("Top Type Stop Condition: Checking Top Type Stop Condition for dependent: " + rel.getDependent().getId());
		
		//Get the summary
		PSComponentSummary dependentSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Top Type Stop Condition: Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}

		
		if (ContentItemWFValidatorAndTransitioner.isTopType(dependentSummary.getContentTypeId(), wvc)) {
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) {
				wvc.getLog().debug("Top Type Stop Condition: Is Top Type, has public revision. dependent: " + rel.getDependent().getId());
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {
				wvc.getLog().debug("Top Type Stop Condition: Is Top Type, has NO public revision. dependent: " + rel.getDependent().getId());
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_TOP_TYPE,
						new Object[]{contentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}
		} else {		
			wvc.getLog().debug("Top Type Stop Condition: Is NOT Top Type. dependent: " + rel.getDependent().getId());
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
	}

}
