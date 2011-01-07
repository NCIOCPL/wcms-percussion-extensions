package gov.cancer.wcm.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.percussion.services.workflow.data.PSTransition;
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
	
	public WFTransitionMap(
			Map<String,Map<String,List<String>>> contentCreationTransitions,
			Map<String,Map<String,List<String>>> contentArchivingTransitions
	){
		_contentCreationTransitions = contentCreationTransitions;
		_contentArchivingTransitions = contentArchivingTransitions;
	}
	
	
	////////////////////////// TODO: FILL IN THE BELOW METHODS.
	
	/**
	 * Check to see if the trigger name is in the archiving transitions map.
	 * @param transitionMap - Map of the transitions to check against
	 * @return
	 */
	public boolean isArchiving(HashMap<String, PSTransition> transitionMap ){
		
		HashMap<PSPair<String, String>, List<String>> triggers = new HashMap<PSPair<String, String>, List<String>>(); 
		
		for(String fromKey : _contentCreationTransitions.keySet()) {
			for (String toKey : _contentCreationTransitions.get(fromKey).keySet()) {
				PSPair<String, String> pair = new PSPair<String, String>(fromKey, toKey);
				triggers.put(pair, _contentCreationTransitions.get(fromKey).get(toKey));
			}
		}
		return false;
	}
	
	/**
	 * Check to see if the trigger name is in the public transitions map.
	 * @param fromState, name of the state the item is coming from.
	 * @param toState, name of the state the item is transitioning to.
	 * @return
	 */
	public boolean isPublic(){
		return false;
	}
}
