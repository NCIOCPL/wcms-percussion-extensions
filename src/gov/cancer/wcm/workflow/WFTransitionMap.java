package gov.cancer.wcm.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.types.PSPair;

/**
 * Defines a mapping of workflow states to other workflow states.
 * @author bpizzillo
 *
 */
public class WFTransitionMap {
	
	private Map<String,Map<String,List<String>>> _contentCreationTransitions;
	private Map<String,Map<String,List<String>>> _contentArchivingTransitions;
	
	/**
	 * Gets a list of transition triggers to be fired from a state to another
	 * state
	 * @param fromState
	 * @param toState
	 * @return
	 */
	public List<String> getContentCreationTransitions(String fromState, String toState){
		List<String> rtnList = Collections.emptyList();
		if (!_contentCreationTransitions.containsKey(fromState)) {
			Map<String, List<String>> toMap = _contentCreationTransitions.get(fromState);
			if (toMap.containsKey(toState))
				rtnList = toMap.get(toState);
		}
		
		return rtnList;
	}

	/**
	 * Gets a collection of transition triggers for this workflow mapping.
	 * @return A Map which represents the transitions.  The key is a PSPair<String, String> which represents where
	 * the first element is the from state and the second element is the to state.  The value is a collection of 
	 * trigger names in the order in which they would be executed.
	 */
	public Map<PSPair<String, String>, List<String>> getContentCreationTransitionTriggers() {
		HashMap<PSPair<String, String>, List<String>> triggers = new HashMap<PSPair<String, String>, List<String>>(); 
		for(String fromKey : _contentCreationTransitions.keySet()) {
			for (String toKey : _contentCreationTransitions.get(fromKey).keySet()) {
				PSPair<String, String> pair = new PSPair<String, String>(fromKey, toKey);
				triggers.put(pair, _contentCreationTransitions.get(fromKey).get(toKey));
			}
		}
		return triggers;
	}
	
	/**
	 * Gets a list of transition triggers to be fired from a state to another
	 * state
	 * @param fromState
	 * @param toState
	 * @return
	 */
	public List<String> getContentArchivingTransitions(String fromState, String toState){
		List<String> rtnList = Collections.emptyList();
		if (!_contentArchivingTransitions.containsKey(fromState)) {
			Map<String, List<String>> toMap = _contentArchivingTransitions.get(fromState);
			if (toMap.containsKey(toState))
				rtnList = toMap.get(toState);
		}
		
		return rtnList;
	}

	/**
	 * Gets a collection of transition triggers for this workflow mapping.
	 * @return A Map which represents the transitions.  The key is a PSPair<String, String> which represents where
	 * the first element is the from state and the second element is the to state.  The value is a collection of 
	 * trigger names in the order in which they would be executed.
	 */
	public Map<PSPair<String, String>, List<String>> getContentArchivingTransitionTriggers() {
		HashMap<PSPair<String, String>, List<String>> triggers = new HashMap<PSPair<String, String>, List<String>>(); 
		for(String fromKey : _contentArchivingTransitions.keySet()) {
			for (String toKey : _contentArchivingTransitions.get(fromKey).keySet()) {
				PSPair<String, String> pair = new PSPair<String, String>(fromKey, toKey);
				triggers.put(pair, _contentArchivingTransitions.get(fromKey).get(toKey));
			}
		}
		return triggers;
	}
	
	/**
	 * Determines whether a state is a destination in the list of content creation transitions.
	 * @param state Workflow state
	 * @return True if state is a known destination for any content creation transition.
	 */
	public boolean isCreationState(PSState state){
		String searchName = state.getName();
		return isStateFound(searchName, _contentCreationTransitions);
	}
	
	/**
	 * Determines whether a state is a destination in the list of content archival transitions.
	 * @param state Workflow state
	 * @return True if state is a known destination for any content archival transition.
	 */
	public boolean isArchiveState(PSState state){
		String searchName = state.getName();
		return isStateFound(searchName, _contentArchivingTransitions);
	}
	
	/**
	 * Internal method to supply the common logic for isCreationState() and isArchiveState().
	 * @param searchName Workflow state
	 * @param transitionList A list of transitions to search for the specified destination.
	 * @return
	 */
	private static boolean isStateFound(String searchName, Map<String,Map<String,List<String>>> transitionList)
	{
		boolean isFound = false;
		
		// Archive states are stored as the destination of an archiving transition.
		// To find them, we need to walk the list of transitions.
		// These are O(n) searches (technically n^2, but the inner loop should only be one
		// destination) and in practice, n is very small, so there's little
		// benefit to adding the complexity of a second map for reverse lookups.
		for(String transition : transitionList.keySet()){
			for(String wfState : transitionList.get(transition).keySet()){
				if(wfState.compareToIgnoreCase(searchName) == 0){
					isFound = true;
					break;
				}
			}
		}
		
		
		return isFound; 
	}
	
	/**
	 * Finds the name of a transition with an Archive state as its destination.
	 * 
	 * Assumes that for any given workflow state, there is only ONE transition
	 * which has an archive state as the destination
	 * @param workflowState The workflow state to check for an archive transition.
	 * @return The trigger name for the archive transition. Returns null if there is
	 * no transition with an archive state as the destination.
	 */
	public String getArchiveTransition(PSState workflowState){

		String transitionName = null;
		
		// Assumption: for any given workflow state, there is only
		// ONE transition which has an archive state as the destination.
		String stateName = workflowState.getName();
		if(_contentArchivingTransitions.containsKey(stateName)){
			// The individual transitions are a mapping of
			// destination state name to transition state name.
			// Assuming there's only one destination for each source on
			// archving list, we can simply loop through the list of transitions
			// and return the name of the "last" one found.
			Map<String,List<String>> transitions = _contentArchivingTransitions.get(stateName);
			for(String key : transitions.keySet()){
				List<String> names = transitions.get(key);
				if( names.iterator().hasNext()){
					transitionName = names.iterator().next();
				}
			}
		}
		
		return transitionName;
	}
	
	public WFTransitionMap(
			Map<String,Map<String,List<String>>> contentCreationTransitions,
			Map<String,Map<String,List<String>>> contentArchivingTransitions
	){
		_contentCreationTransitions = contentCreationTransitions;
		_contentArchivingTransitions = contentArchivingTransitions;
	}

}
