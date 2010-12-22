package gov.cancer.wcm.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	
	
	public WFTransitionMap(
			Map<String,Map<String,List<String>>> contentCreationTransitions,
			Map<String,Map<String,List<String>>> contentArchivingTransitions
	){
		_contentCreationTransitions = contentCreationTransitions;
		_contentArchivingTransitions = contentArchivingTransitions;
	}
}
