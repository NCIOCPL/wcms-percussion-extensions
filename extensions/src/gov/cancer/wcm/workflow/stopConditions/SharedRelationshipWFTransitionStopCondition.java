package gov.cancer.wcm.workflow.stopConditions;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

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
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel, 
			WorkflowValidationContext wvc
			) throws WFValidationException 
	{
		wvc.getLog().debug("Shared Stop Condition: Checking Shared Stop Condition for dependent: " + rel.getDependent().getId());
		if (ContentItemWFValidatorAndTransitioner.isShared(rel.getDependent(), wvc)) {
			wvc.getLog().debug("Shared Stop Condition: Dependent ID: " + rel.getDependent().getId() + " is Shared.");
			//Since this item is shared, we need to check if it has a public revision or not.
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(dependentContentItemSummary, wvc)) {
				wvc.getLog().debug("Shared Stop Condition: Dependent ID: " + rel.getDependent().getId() + " has public revision.");
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {				
				wvc.getLog().debug("Shared Stop Condition: Dependent ID: " + rel.getDependent().getId() + " has no public revision.");
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_SHARED,
						new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}
		}
		else {
			wvc.getLog().debug("Dependent ID: " + rel.getDependent().getId() + " is NOT Shared.");
			return RelationshipWFTransitionStopConditionResult.Ok;
		}
	}
	
	@Override 
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		//There is no reason to check the parent to see if it is shared, if it is, then we would want
		//to go to that item.  The earlier check would get there and say, yes, this item is shared stop
		//going up.
		wvc.getLog().error("Shared Check Stop Condition: Cannot validate upwards. Check configuration. Called on dependent with id: " + dependentContentItemSummary.getContentId());
		throw new WFValidationException("System Error Occured. Please Check the logs.", true);
	}

	public SharedRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
