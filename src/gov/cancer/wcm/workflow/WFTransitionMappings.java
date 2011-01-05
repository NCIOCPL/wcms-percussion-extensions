package gov.cancer.wcm.workflow;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.percussion.utils.types.PSPair;

/**
 * 
 * @author bpizzillo
 *
 */
public class WFTransitionMappings {
	private Map<String,WFTransitionMap> _workflowTransitionMaps;

	/**
	 * Gets a collection of transition triggers for a workflow.
	 * @param workflowName The name of the workflow.
	 * @return A Map which represents the transitions.  The key is a PSPair<String, String> which represents where
	 * the first element is the from state and the second element is the to state.  The value is a collection of 
	 * trigger names in the order in which they would be executed.
	 */
	public Map<PSPair<String, String>, List<String>> getContentCreationTransitionTriggers(String workflowName) {
		if (_workflowTransitionMaps.containsKey(workflowName))
			return _workflowTransitionMaps.get(workflowName).getContentCreationTransitionTriggers();
		
		//Is this an empty map, or is this an error that the workflow was not defined?
		return new HashMap<PSPair<String, String>, List<String>>(); 
	}
	
	public WFTransitionMappings(Map<String,WFTransitionMap> workflowTransitionMaps) {
		_workflowTransitionMaps = workflowTransitionMaps;
	}
}
