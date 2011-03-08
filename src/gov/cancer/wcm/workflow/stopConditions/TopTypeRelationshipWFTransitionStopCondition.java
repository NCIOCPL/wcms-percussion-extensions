package gov.cancer.wcm.workflow.stopConditions;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

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
	public RelationshipWFTransitionStopConditionResult archiveValidateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Top Type Stop Condition(archive-down): Checking Top Type Stop Condition for dependent: " + rel.getDependent().getId());
		if (ContentItemWFValidatorAndTransitioner.isTopType(dependentContentItemSummary.getContentTypeId(), wvc)) {
			wvc.getLog().debug("Top Type Stop Condition(archive-down): Dependent is top type: " + rel.getDependent().getId());
			return RelationshipWFTransitionStopConditionResult.OkStopChecking;	
		}
		return RelationshipWFTransitionStopConditionResult.Ok;
	}

	@Override 
	public RelationshipWFTransitionStopConditionResult archiveValidateUp(
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		//There is no reason to check the parent to see if it is a top type, if it is, then we would want
		//to go to that item.  The earlier check would get there and say, yes, this item is a top type, stop
		//going up.
		wvc.getLog().error("Top Type Check Stop Condition (archiving): Cannot validate upwards. Check configuration. Called on dependent with id: " + dependentContentItemSummary.getContentId());
		throw new WFValidationException("System Error Occured. Please Check the logs.", true);
	}	
	
	@Override
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	){
		
		wvc.getLog().debug("Top Type Stop Condition(down): Checking Top Type Stop Condition for dependent: " + rel.getDependent().getId());
				
		if (ContentItemWFValidatorAndTransitioner.isTopType(dependentContentItemSummary.getContentTypeId(), wvc)) {
			if (ContentItemWFValidatorAndTransitioner.hasPublicRevisionOrGreater(dependentContentItemSummary, wvc)) {
				wvc.getLog().debug("Top Type Stop Condition(down): Is Top Type, has public revision. dependent: " + rel.getDependent().getId());
				return RelationshipWFTransitionStopConditionResult.OkStopChecking;
			} else {
				wvc.getLog().debug("Top Type Stop Condition(down): Is Top Type, has NO public revision. dependent: " + rel.getDependent().getId());
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.NON_PUBLIC_CHILD_IS_TOP_TYPE,
						new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId()});
				return RelationshipWFTransitionStopConditionResult.StopTransition;
			}
		} else {		
			wvc.getLog().debug("Top Type Stop Condition(down): Is NOT Top Type. dependent: " + rel.getDependent().getId());
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
		//There is no reason to check the parent to see if it is a top type, if it is, then we would want
		//to go to that item.  The earlier check would get there and say, yes, this item is a top type, stop
		//going up.
		wvc.getLog().error("Top Type Check Stop Condition: Cannot validate upwards. Check configuration. Called on dependent with id: " + dependentContentItemSummary.getContentId());
		throw new WFValidationException("System Error Occured. Please Check the logs.", true);
	}

	public TopTypeRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
