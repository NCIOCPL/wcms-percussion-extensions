package gov.cancer.wcm.workflow.stopConditions;


import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowValidationContext;
import gov.cancer.wcm.workflow.checks.RelationshipWFTransitionCheckResult;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * Defines a RelationshipWFTransitionStopCondition to check the dependent
 * of a relationship's dependents.  This will continue following relationships
 * until it has no more relationships to follow.
 * 
 * This should always be the last Stop Condition of a Check.  This is because it
 * will actually push the dependent's ID into the list of items that should be
 * transitioned.  This is because we know that by the time we get to this point
 * the item has been validated as being ok for transitioning, we just need to
 * check its relationships.
 * 
 * @author bpizzillo
 *
 */
public class DependentsCheckRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult archiveValidateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		PSComponentSummary dependentSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Dependents Check Stop Condition: Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}
		
		//Check the dependent's relationships
		if (ContentItemWFValidatorAndTransitioner.validateChildRelationships(dependentSummary, wvc) == RelationshipWFTransitionCheckResult.ContinueTransition) {
			wvc.getLog().debug("Dependents Check Stop Condition: Adding dependent id: " + rel.getDependent().getId() + " to list of items to transition");
			//PSLocator loc = rel.getDependent();
			
			IPSCmsContentSummaries contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
			//        wvc.addItemToTransition(contentSummariesService.loadComponentSummary(rel.getDependent().getId()));
			
			//The below return is different from the rest of the stop conditions and that is because there is
			//no difference for this stop condition between Ok and OkStopChecking.  We either cannot transition
			//this child because of a child relationship stops or it can transition.
			return RelationshipWFTransitionStopConditionResult.Ok;
		} else {
			wvc.getLog().debug("Dependents Check Stop Condition: Cannot transition dependent id: " + rel.getDependent().getId());
			//There is no reason to add a PSError message since it would have already been added in the one of the
			//child checks.
			return RelationshipWFTransitionStopConditionResult.StopTransition;
		}
	}

	@Override 
	public RelationshipWFTransitionStopConditionResult archiveValidateUp(
			PSComponentSummary dependentContentItemSummary, 
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().error("Dependents Check Stop Condition: Cannot archive validate upwards. Check configuration. Called on dependent with id: " + dependentContentItemSummary.getContentId());
		throw new WFValidationException("System Error Occured. Please Check the logs.", true);
	}	

	
	@Override 
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary, 
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		//Get the summary
		PSComponentSummary dependentSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Dependents Check Stop Condition: Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}
		
		//Check the dependent's relationships
		if (ContentItemWFValidatorAndTransitioner.validateChildRelationships(dependentSummary, wvc) == RelationshipWFTransitionCheckResult.ContinueTransition) {
			wvc.getLog().debug("Dependents Check Stop Condition: Adding dependent id: " + rel.getDependent().getId() + " to list of items to transition");
			//PSLocator loc = rel.getDependent();
			
			IPSCmsContentSummaries contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
			wvc.addItemToTransition(contentSummariesService.loadComponentSummary(rel.getDependent().getId()));
			
			//The below return is different from the rest of the stop conditions and that is because there is
			//no difference for this stop condition between Ok and OkStopChecking.  We either cannot transition
			//this child because of a child relationship stops or it can transition.
			return RelationshipWFTransitionStopConditionResult.Ok;
		} else {
			wvc.getLog().debug("Dependents Check Stop Condition: Cannot transition dependent id: " + rel.getDependent().getId());
			//There is no reason to add a PSError message since it would have already been added in the one of the
			//child checks.
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
		wvc.getLog().error("Dependents Check Stop Condition: Cannot validate upwards. Check configuration. Called on dependent with id: " + dependentContentItemSummary.getContentId());
		throw new WFValidationException("System Error Occured. Please Check the logs.", true);
	}
	
	/**
	 * Initializes a new instance of the DependentsCheckRelationshipWFTransitionStopCondition class.
	 * @param checkDirection the direction when this check should be validated.
	 */
	public DependentsCheckRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}

}
