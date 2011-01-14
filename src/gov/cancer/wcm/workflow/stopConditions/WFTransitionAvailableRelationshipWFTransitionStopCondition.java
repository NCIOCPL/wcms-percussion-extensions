package gov.cancer.wcm.workflow.stopConditions;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

import java.util.List;
import java.util.Set;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRole;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;

/**
 * Defines a RelationshipWFTransitionStopCondition to check if the dependent of the
 * relationship is in the same workflow as the owner.  Technically, it should check if
 * it can transition with the owner.
 * <p>
 * Note: This only determines if the dependent would cause the transition to fail.  Thus,
 * if an item is somehow in public that is now a child and we are transitioning the parent
 * to say the review state, while there is no path to go from public to review, this function
 * would return an Ok RelationshipWFTransitionStopConditionResult since we would not actually
 * transition the public child an therefore would no have the possibility of causing an error
 * in the transition.
 * </p>
 * @author bpizzillo
 *
 */
public class WFTransitionAvailableRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult archiveValidateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Checking Transition Available Stop Condition for dependent(archive down): " + rel.getDependent().getId());
		
		//Get the state of the Dependent.
		PSState dependentState = wvc.getState(dependentContentItemSummary.getContentStateId());
		
		if (dependentState == null) {
			wvc.getLog().error("Transition Available Stop Condition for dependent(archive down): " + dependentContentItemSummary.getContentId() + " could not get workflow state.");
			throw new WFValidationException("Could not get workflow state for dependent: " + dependentContentItemSummary.getContentId(), true);
		}	

		if (!canTransition(dependentState, wvc)) {
			//This is a hard error because we must be able to transition a child in order
			//to transition the parent.
			wvc.getLog().debug("Workflow Transition Available Stop Condition (archive down): Dependent ID: " + rel.getDependent().getId() + " cannot be transitioned because user does not have rights to transition.");
			wvc.addError(
					ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
					ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
					ContentItemWFValidatorAndTransitioner.NO_TRANSITION_AVAILABLE,
					new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId()});
			return RelationshipWFTransitionStopConditionResult.StopTransition;
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
		wvc.getLog().debug("Checking Transition Available Stop Condition for owner(archive up): " + rel.getOwner().getId());
		
		//Get the state of the Dependent.
		PSState ownerState = wvc.getState(ownerContentItemSummary.getContentStateId());
		
		if (ownerState == null) {
			wvc.getLog().error("Transition Available Stop Condition for dependent(archive up): " + ownerContentItemSummary.getContentId() + " could not get workflow state.");
			throw new WFValidationException("Could not get workflow state for dependent: " + ownerContentItemSummary.getContentId(), true);
		}	

		if (!canTransition(ownerState, wvc)) {
			//This is a hard error because we must be able to transition a child in order
			//to transition the parent.
			throw new WFValidationException(
					String.format(
							ContentItemWFValidatorAndTransitioner.PARENT_HAS_NO_AVAILABLE_TRANSITION, 
							dependentContentItemSummary.getContentId(),
							ownerContentItemSummary.getContentId()
					)
			);
		}
		
		return RelationshipWFTransitionStopConditionResult.Ok;
	}
	
	
	@Override
	public RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary, 
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Checking Transition Available Stop Condition for dependent(down): " + rel.getDependent().getId());
		
		//Get the state of the Dependent.
		PSState dependentState = wvc.getState(dependentContentItemSummary.getContentStateId());
		
		if (dependentState == null) {
			wvc.getLog().error("Transition Available Stop Condition for dependent(down): " + dependentContentItemSummary.getContentId() + " could not get workflow state.");
			throw new WFValidationException("Could not get workflow state for dependent: " + dependentContentItemSummary.getContentId(), true);
		}	

		if (!canTransition(dependentState, wvc)) {
			//This is a hard error because we must be able to transition a child in order
			//to transition the parent.
			wvc.getLog().debug("Workflow Transition Available Stop Condition (down): Dependent ID: " + rel.getDependent().getId() + " cannot be transitioned because user does not have rights to transition.");
			wvc.addError(
					ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
					ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
					ContentItemWFValidatorAndTransitioner.NO_TRANSITION_AVAILABLE,
					new Object[]{ownerContentItemSummary.getContentId(), rel.getDependent().getId()});
			return RelationshipWFTransitionStopConditionResult.StopTransition;
		}
		
		return RelationshipWFTransitionStopConditionResult.Ok;
	}
	
	@Override 
	public RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Checking Transition Available Stop Condition for owner(up): " + rel.getOwner().getId());
		
		//Get the state of the Dependent.
		PSState ownerState = wvc.getState(ownerContentItemSummary.getContentStateId());
		
		if (ownerState == null) {
			wvc.getLog().error("Transition Available Stop Condition for dependent(up): " + ownerContentItemSummary.getContentId() + " could not get workflow state.");
			throw new WFValidationException("Could not get workflow state for dependent: " + ownerContentItemSummary.getContentId(), true);
		}	

		if (!canTransition(ownerState, wvc)) {
			//This is a hard error because we must be able to transition a child in order
			//to transition the parent.
			throw new WFValidationException(
					String.format(
							ContentItemWFValidatorAndTransitioner.PARENT_HAS_NO_AVAILABLE_TRANSITION, 
							dependentContentItemSummary.getContentId(),
							ownerContentItemSummary.getContentId()
					)
			);
		}
		
		return RelationshipWFTransitionStopConditionResult.Ok;
	}
	
	/**
	 * Checks to see if the current user can transition an item to the destination
	 * state.
	 * @param checkItemState the PSState to check
	 * @param wvc the WorkflowValidationContext of this 
	 * @return
	 */
	private boolean canTransition(PSState checkItemState, WorkflowValidationContext wvc) {
		PSWorkflow workflow = wvc.getInitiatorWorkflowApp();
		
		//Basically, find the transition path if there is one, and check to see if the user
		//would have the rights to make the transitions.
		List<PSTransition> transitionsToBePerformed = wvc.getTransitions(checkItemState.getName());

		//Now we check to see if the user would have permissions to transition
		//The way it seems that this stuff works is that if a user is in the list of Assigned roles
		//they can transition an item. If not, they cannot really do anything.
		//Then we have to check to see if the user is an allowed role for the transition.  A
		//transition may be allowed for all roles, but if the user is not in a role which is
		//assignable they would not be allowed to make that transition happen.
		
		List<String> userRoles = wvc.getRequest().getSubjectRoles();
		Set<Integer> validRoles = workflow.getRoleIds(userRoles); //List of workflow roles user has
		
		PSState currState = checkItemState;
		for (PSTransition transition : transitionsToBePerformed) {
			if (
				!hasAssignableRole(validRoles, currState.getAssignedRoles()) ||
				!hasTransitionRole(validRoles, transition)
			) {
				return false;
			}
			
			//Gets the to state of the transition.
			currState = wvc.getState(transition.getToState());
		}

		//We did not have an error so we must be ok to transition
		return true;
	}
	
	/**
	 * Loops through the list of assigned roles for a state to determine if the
	 * user is in one of them.
	 * @param validRoles the list of workflow roles for the user.
	 * @param assignedRoles the list of assignable roles for the workflow state
	 * @return
	 */
	private boolean hasAssignableRole(Set<Integer> validRoles, List<PSAssignedRole> assignedRoles) {

		for(PSAssignedRole assignedRole : assignedRoles) {
			if (
				(assignedRole.getAssignmentType() == PSAssignmentTypeEnum.ADMIN || 
				assignedRole.getAssignmentType() == PSAssignmentTypeEnum.ASSIGNEE) &&
				validRoles.contains(assignedRole.getGUID().getUUID())
			) {
				//This user has assignee permissions to this role.
				return true;
			}
		}
		
		return false;		
	}
	
	/**
	 * Loops through the list of transition roles to determine if the
	 * user is in one of them.
	 * @param validRoles the list of workflow roles for the user.
	 * @param assignedRoles the PSTransition to check.
	 * @return
	 */	
	private boolean hasTransitionRole(Set<Integer> validRoles, PSTransition transition) {
		
		//Check if the transition is allowed for all roles, if so the user has a 
		//transition role
		if (transition.isAllowAllRoles())
			return true;
		
		//Not for all roles?  Then check the roles of the transition
		for(PSTransitionRole transitionRole : transition.getTransitionRoles()) {
			if (validRoles.contains(transitionRole.getGUID().getUUID())) {
				//This user has permissions to this role.
				return true;
			}
		}
		
		//No roles
		return false;				
	}

	public WFTransitionAvailableRelationshipWFTransitionStopCondition(
			RelationshipWFTransitionStopConditionDirection checkDirection
	) {
		super(checkDirection);
	}
}
