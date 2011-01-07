package gov.cancer.wcm.workflow;

import java.util.List;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionCheck which checks to see if the
 * dependents of a relationship type are allowed to transition and compiles
 * a list of items to transition.
 * @author bpizzillo
 *
 */
public class RelationshipWFTransitionFollowCheck extends
		BaseRelationshipWFTransitionCheck {

	private List<BaseRelationshipWFTransitionStopCondition> stopConditions;
	
	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Follow;
	}
	
	/**
	 * Gets the stop conditions for this follow config.
	 * @return
	 */
	public List<BaseRelationshipWFTransitionStopCondition> getStopConditions() {
		return stopConditions;
	}
	
	@Override
	public RelationshipWFTransitionCheckResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Follow Check (Down) for dependent: " + rel.getDependent().getId() + " in slot " + rel.getConfig().getLabel());
		
		//Get the summary
		PSComponentSummary dependentContentItemSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentContentItemSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Follow Check(down): Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}

		
		RelationshipWFTransitionStopConditionResult lastResult = RelationshipWFTransitionStopConditionResult.Ok;
		for(BaseRelationshipWFTransitionStopCondition stopCond : stopConditions) {
			if (
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Down ||
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Both
			) {
				lastResult = stopCond.validateDown(ownerContentItemSummary, dependentContentItemSummary, rel, wvc);
				if (lastResult != RelationshipWFTransitionStopConditionResult.Ok)
					break;
			}
		}
		
		if (lastResult == RelationshipWFTransitionStopConditionResult.Ok) {
			//Add rel.getDependent() to list of items to transition.
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		}else if (lastResult == RelationshipWFTransitionStopConditionResult.OkStopChecking) {
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		} else {
			return RelationshipWFTransitionCheckResult.StopTransition;
		}
	}
	
	@Override
	public RelationshipWFTransitionCheckResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Follow Check (Up) for dependent: " + rel.getOwner().getId() + " in slot " + rel.getConfig().getLabel());

		//Get the summary
		PSComponentSummary ownerContentItemSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getOwner().getId());
		
		if (ownerContentItemSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Follow Check(up): Could not get Component Summary for id: " + rel.getOwner().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}

		
		RelationshipWFTransitionStopConditionResult lastResult = RelationshipWFTransitionStopConditionResult.Ok;
		for(BaseRelationshipWFTransitionStopCondition stopCond : stopConditions) {
			if (
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Up ||
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Both
			) {
				lastResult = stopCond.validateUp(dependentContentItemSummary, ownerContentItemSummary, rel, wvc);
				if (lastResult != RelationshipWFTransitionStopConditionResult.Ok)
					break;
			}
		}
		
		if (lastResult == RelationshipWFTransitionStopConditionResult.Ok) {
			//Add rel.getDependent() to list of items to transition.
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		}else if (lastResult == RelationshipWFTransitionStopConditionResult.OkStopChecking) {
			//Unlike down, we cannot continue going up if a stop condition has been met.
			return RelationshipWFTransitionCheckResult.StopTransition;
		} else {
			return RelationshipWFTransitionCheckResult.StopTransition;
		}		
	}

	/**
	 * Initializes a new instance of the RelationshipWFTransitionFollowCheck class.
	 * @param relationshipName The name of the relationship to check
	 * @param stopConditions The conditions to check for.
	 */
	public RelationshipWFTransitionFollowCheck(
			String relationshipName,
			List<BaseRelationshipWFTransitionStopCondition> stopConditions
	) {
		super(relationshipName);
		this.stopConditions = stopConditions;
	}

	@Override
	public RelationshipWFTransitionCheckResult archiveValidateDown(
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Follow Check (Down) for dependent: " + rel.getDependent().getId() + " in slot " + rel.getConfig().getLabel());
		
		//Get the summary
		PSComponentSummary dependentContentItemSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentContentItemSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Follow Check(down): Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}

		
		RelationshipWFTransitionStopConditionResult lastResult = RelationshipWFTransitionStopConditionResult.Ok;
		for(BaseRelationshipWFTransitionStopCondition stopCond : stopConditions) {
			if (
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Down ||
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Both
			) {
				lastResult = stopCond.validateDown(ownerContentItemSummary, dependentContentItemSummary, rel, wvc);
				if (lastResult != RelationshipWFTransitionStopConditionResult.Ok)
					break;
			}
		}
		
		if (lastResult == RelationshipWFTransitionStopConditionResult.Ok) {
			//Add rel.getDependent() to list of items to transition.
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		}else if (lastResult == RelationshipWFTransitionStopConditionResult.OkStopChecking) {
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		} else {
			return RelationshipWFTransitionCheckResult.StopTransition;
		}
	}
	
	@Override
	public RelationshipWFTransitionCheckResult archiveValidateUp(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Follow Check (Up) for dependent: " + rel.getOwner().getId() + " in slot " + rel.getConfig().getLabel());

		//Get the summary
		PSComponentSummary ownerContentItemSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getOwner().getId());
		
		if (ownerContentItemSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Follow Check(up): Could not get Component Summary for id: " + rel.getOwner().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}

		
		RelationshipWFTransitionStopConditionResult lastResult = RelationshipWFTransitionStopConditionResult.Ok;
		for(BaseRelationshipWFTransitionStopCondition stopCond : stopConditions) {
			if (
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Up ||
					stopCond.getCheckDirection() == RelationshipWFTransitionStopConditionDirection.Both
			) {
				lastResult = stopCond.validateUp(dependentContentItemSummary, ownerContentItemSummary, rel, wvc);
				if (lastResult != RelationshipWFTransitionStopConditionResult.Ok)
					break;
			}
		}
		
		if (lastResult == RelationshipWFTransitionStopConditionResult.Ok) {
			//Add rel.getDependent() to list of items to transition.
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		}else if (lastResult == RelationshipWFTransitionStopConditionResult.OkStopChecking) {
			//Unlike down, we cannot continue going up if a stop condition has been met.
			return RelationshipWFTransitionCheckResult.StopTransition;
		} else {
			return RelationshipWFTransitionCheckResult.StopTransition;
		}		
	}
}
