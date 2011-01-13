package gov.cancer.wcm.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRole;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;


/**
 * Defines a class which holds information which is used for a workflow validation.  This
 * is so we do not have to keep changing the signature of the validate method of 
 * RelationshipWFTransitionChecks and RelationshipWFTransitionStopConditions 
 * @author bpizzillo
 *
 */
public class WorkflowValidationContext {
	 
	private Log _log;
	private Document _errorDoc;
	private IPSRequestContext _request;
	private PSComponentSummary _initiatorContentItem;
	private ArrayList<PSComponentSummary> _transitionItemIds = new ArrayList<PSComponentSummary>();
	private PSState _initiatingItemWorkflowState;
	private PSWorkflow _initiatingItemWorkflowApp;
	private PSTransition _initiatingTransition;
	private List<PSWorkflowRole> _workflowRoles;
	private Map<PSPair<String,String>, List<String>> _workflowTriggers;
	private HashMap<String, PSTransition> _workflowTransitions = new HashMap<String, PSTransition>();
	private boolean _isArchiving = false;
	
	private HashMap<Long, PSState> _wfStates = new HashMap<Long, PSState>();
	
	/**
	 * Gets the log for this validation context
	 * @return
	 */
	public Log getLog() {
		return _log;
	}
	
	/**
	 * Adds an error message to the list of validation errors.
	 * @param submitNames an array of field submit names for which to add a new error message, not null or empty.
	 * @param displayNames an array of field display names for which to add a new error message, not null or empty.
	 * @param pattern the message string pattern, which will be formatted together with the provided arguments, not null or empty.
	 * @param args an array of String objects, containing all arguments which need to be formatted to the string pattern supplied, may be null or empty.
	 * @throws IllegalArgumentException if submitName, displayName, or pattern are null or empty.
	 */
	public void addError(String[] submitNames, String[] displayNames, String pattern, Object[] args)
		throws IllegalArgumentException
	{
		PSItemErrorDoc.addError(_errorDoc, submitNames, displayNames, pattern, args);
	}

	/**
	 * Adds an error message to the list of validation errors.
	 * @param submitName the field submit name for which to add a new error message, not null or empty.
	 * @param displayName the field display name for which to add a new error message, not null or empty.
	 * @param pattern the message string pattern, which will be formatted together with the provided arguments, not null or empty.
	 * @param args an array of String objects, containing all arguments which need to be formatted to the string pattern supplied, may be null or empty.
	 * @throws IllegalArgumentException if submitName, displayName, or pattern are null or empty.
	 */
	public void addError(String submitName, String displayName, String pattern, Object[] args) {
		PSItemErrorDoc.addError(_errorDoc, submitName, displayName, pattern, args);
	}
	
	/**
	 * Gets the item that this workflow validation was fired for.
	 * NOTE: This is not always a top type.
	 * @return
	 */
	public PSComponentSummary getInitiator() {
		return _initiatorContentItem;
	}
	
	/**
	 * Gets the initiating item's workflow app
	 * @return
	 */
	public PSWorkflow getInitiatorWorkflowApp() {
		return _initiatingItemWorkflowApp;
	}
	
	/**
	 * Gets the initiating item's workflow state
	 * @return
	 */
	public PSState getInitiatorWorkflowState() {
		return _initiatingItemWorkflowState;
	}
	
	/**
	 * Adds an id to the list of child items which need to be transitioned.
	 * @param contentId
	 */
	public void addItemToTransition(PSComponentSummary item) {
		_transitionItemIds.add(item);
	}	
	
	/**
	 * Removes an id to the list of child items which does not need to be transitioned.
	 * @param contentId
	 */
	public void removeItemToTransition(PSComponentSummary item) {
		_transitionItemIds.remove(item);
	}	
	
	/**
	 * Gets a list of all of the content ids that would need to be transitioned.
	 * @return
	 */
	public PSComponentSummary[] getItemsToTransition() {
		PSComponentSummary[] items = new PSComponentSummary[_transitionItemIds.size()];
		
		for(int i =0; i<_transitionItemIds.size(); i++)
			items[i] = _transitionItemIds.get(i);
		
		return items;
	}
	
	/**
	 * Gets a workflow state by its ID
	 * @param stateId
	 * @return
	 */
	public PSState getState(long stateId) {	
		
		Long iStateId = new Long(stateId);
		
		if (_wfStates.containsKey(iStateId))
			return _wfStates.get(iStateId);
		
		//We *should* have the state so this is bad. 
		return null;		
	}
	
	/**
	 * Gets the state the transition is trying to get to. 
	 * @return
	 */
	public PSState getDestinationState() {
		
		if (_wfStates.containsKey(_initiatingTransition.getToState()))
			return _wfStates.get(_initiatingTransition.getToState());
		
		//We *should* have the state so this is bad.
		return null;
	}
	
	/**
	 * Gets a list of transitions that must happen in order to move from
	 * a state, fromState to the state we are transitioning to.
	 * @param fromState the state to transition from
	 * @return a List<PSTransitions> which contains the Transitions which must happen in order, or an empty list if the item does not need to transition
	 */
	public List<PSTransition> getTransitions(String fromState) {
		
		ArrayList<PSTransition> transitions = new ArrayList<PSTransition>();

		if (!_wfStates.containsKey(_initiatingTransition.getToState())) {
			//TODO: Log Error
			return transitions;
		}
		
		PSPair<String,String> fromTo = new PSPair<String, String> (fromState, _wfStates.get(_initiatingTransition.getToState()).getName());
		
		if (_workflowTriggers.containsKey(fromTo)) {
			for (String trigger : _workflowTriggers.get(fromTo)) {
				if (_workflowTransitions.containsKey(trigger)) {
					transitions.add(_workflowTransitions.get(trigger));					
				} else {
					//If we cannot find the transition that is bad.
					_log.error("Workflow Validation Context: Could not get transition for trigger: " + trigger);
					throw new WFValidationException("Error getting Transition for trigger: " + trigger, true);
				}
			}
		}
		
		return transitions;
	}
	
	/**
	 * Gets the Request Context for the current validation.
	 * @return
	 */
	public IPSRequestContext getRequest() {
		return _request;
	}
	
	/**
	 * Gets the list of roles defined in the system.
	 * @return
	 */
	public List<PSWorkflowRole> getWorkflowRoles() {
		return _workflowRoles;
	}
	
	public WorkflowValidationContext(
			IPSRequestContext request,
			PSComponentSummary initiatorContentItem, 
			Log log, 
			Document errorDoc,
			PSWorkflow initiatingItemWorkflowApp,
			PSState initiatingItemWorkflowState,
			long initiatingTransitionID
	) {
		_request = request;		
		_initiatorContentItem = initiatorContentItem;		
		_log = log;
		_errorDoc = errorDoc;
		_initiatingItemWorkflowApp = initiatingItemWorkflowApp;
		_initiatingItemWorkflowState = initiatingItemWorkflowState;
		
		_workflowRoles = initiatingItemWorkflowApp.getRoles();		
				
		//Let's find all the workflow transitions we will be using and get them
		//out into a structure before we look through all
		WorkflowConfiguration config = WorkflowConfigurationLocator.getWorkflowConfiguration();
		
		
		//Load up the states and get the initiating transition
		for (PSState state : _initiatingItemWorkflowApp.getStates()) {
			_wfStates.put(state.getStateId(), state);
		
			//Find the transition which is being initiated.
			for (PSTransition transition: state.getTransitions()) {				
				if (transition.getGUID().getUUID() == initiatingTransitionID) {
					_initiatingTransition = transition;
				}				
			}
			
			//TODO: Check to see if _initiatingTransition is not null.
		}

		//Create the to/from to determine if archiving or not.
		//TODO: Fix the to state to make sure exceptions are not thrown out the
		//wazoo.
		if( _wfStates.get(_initiatingTransition.getToState()) == null){
			_log.error("Error: Could not load the destination state of the transition.");
			throw new WFValidationException("Error: Could not load the destination state of the transition.");
		}
		PSPair<String, String> initiatingToFrom = new PSPair<String, String>(
				_initiatingItemWorkflowState.getName(),
				 _wfStates.get(_initiatingTransition.getToState()).getName()
				);
		

		if (config.getTransitionMappings().getContentArchivingTransitionTriggers(initiatingItemWorkflowApp.getName()).containsKey(initiatingToFrom)) {
			_isArchiving = true;
			_workflowTriggers = config.getTransitionMappings().getContentArchivingTransitionTriggers(initiatingItemWorkflowApp.getName());
			_log.debug("Workflow Validation Context, Transition is: Archiving");
		} else {
			_workflowTriggers = config.getTransitionMappings().getContentCreationTransitionTriggers(initiatingItemWorkflowApp.getName());
			_log.debug("Workflow Validation Context, Transition is: Non Archiving");
		}
		
		//if archiving( setup the below things using the archiving lists.... )
		HashSet<String> triggers = new HashSet<String>();
		
		for(List<String> triggerList : _workflowTriggers.values()) {
			triggers.addAll(triggerList);
		}				
		
		//Find all triggers that will be used.
		for (long stateId : _wfStates.keySet()) {
						
			//Find the transition which is being initiated.
			for (PSTransition transition: _wfStates.get(stateId).getTransitions()) {				
				
				//Add the transition to the list of transitions which will be used
				//in this workflow
				if (triggers.contains(transition.getTrigger())) {
					_workflowTransitions.put(transition.getTrigger(), transition);
				}					
			}
		}		
	}
	
	/**
	 * Checks to see if the current transition (in the context) is a transition dealing with archiving.
	 * @return
	 */
	public boolean isArchiveTransition(){
		return _isArchiving;
	}
	
	/**
	 * Checks to see if the current transition (in the context) is a transition dealing with publishing.
	 * @return
	 */
	public boolean isPublicTransition(){
		return !_isArchiving;		
	}
	

}
