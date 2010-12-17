package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocation;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.webservices.PSErrorException;

/**
 * Defines a RelationshipWFTransitionStopCondition for checking if
 * the dependent of a relationship is shared
 * @author bpizzillo
 *
 */
public class SharedRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel, 
			WorkflowValidationContext wvc
			) throws WFValidationException 
	{
		wvc.getLog().debug("Shared Stop Condition: Checking Shared Stop Condition for dependent: " + rel.getDependent().getId());
		if (ContentItemWFValidatorAndTransitioner.isShared(rel.getDependent(), wvc)) {
			wvc.getLog().debug("Shared Stop Condition: Dependent ID: " + rel.getDependent().getId() + " is Shared.");
			//Since this item is shared, we need to check if it has a public revision or not.
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) {
				wvc.getLog().debug("Shared Stop Condition: Dependent ID: " + rel.getDependent().getId() + " has public revision.");
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {				
				wvc.getLog().debug("Shared Stop Condition: Dependent ID: " + rel.getDependent().getId() + " has no public revision.");
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_SHARED,
						new Object[]{contentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}
		}
		else {
			wvc.getLog().debug("Dependent ID: " + rel.getDependent().getId() + " is NOT Shared.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
	}
}
