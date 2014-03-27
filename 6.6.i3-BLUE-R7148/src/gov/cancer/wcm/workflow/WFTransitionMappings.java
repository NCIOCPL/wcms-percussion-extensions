package gov.cancer.wcm.workflow;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
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
	
	/**
	 * Gets a collection of transition triggers for a workflow.
	 * @param workflowName The name of the workflow.
	 * @return A Map which represents the transitions.  The key is a PSPair<String, String> which represents where
	 * the first element is the from state and the second element is the to state.  The value is a collection of 
	 * trigger names in the order in which they would be executed.
	 */
	public Map<PSPair<String, String>, List<String>> getContentArchivingTransitionTriggers(String workflowName) {
		if (_workflowTransitionMaps.containsKey(workflowName))
			return _workflowTransitionMaps.get(workflowName).getContentArchivingTransitionTriggers();
		
		//Is this an empty map, or is this an error that the workflow was not defined?
		return new HashMap<PSPair<String, String>, List<String>>(); 
	}

	/**
	 * Determines whether a given workflow state is one in which content creation
	 * and publishing takes place.
	 * @param workflow Work flow containing state.
	 * @param state A work flow state.
	 * @return True if state allows for content creation.  False if state does not allow content creation.
	 * If workflow is not known, state is assumed to be a creation state and true is returned.
	 */
	public boolean isCreationState(PSWorkflow workflow, PSState state){
		boolean isCreation = true;
		String workflowName = workflow.getName();

		if(_workflowTransitionMaps.containsKey(workflowName)){
			isCreation = _workflowTransitionMaps.get(workflowName).isCreationState(state);
		}
		return isCreation;
	}

	/**
	 * Determines whether a given workflow state is one in which content is considered to be archived.
	 * @param workflow Work flow containing state.
	 * @param state A work flow state.
	 * @return True if state does not allow content creation and/or publishing. False otherwise.
	 * If workflow is not known, state is assumed to be a creation state and false is returned. 
	 */
	public boolean isArchiveState(PSWorkflow workflow, PSState state){
		boolean isArchive = false;
		String workflowName = workflow.getName();

		if(_workflowTransitionMaps.containsKey(workflowName)){
			isArchive = _workflowTransitionMaps.get(workflowName).isArchiveState(state);
		}
		return isArchive;
	}
	
	/**
	 * Finds the name of an archiving transition associated with a specified workflow state.
	 * 
	 * Assumes that a given workflow state has at most one archival transition.
	 * 
	 * @param workflow The workflow containing the targeted state.
	 * @param workflowState The workflow state for which an archving transition is beign sought.
	 * @return If an archiving transition is found, return its trigger name.
	 * If no archiving transition is found, return null.
	 */
	public String getArchiveTransitionName(PSWorkflow workflow, PSState workflowState){
		String transitionName;
		String workflowName = workflow.getName();
		
		if(_workflowTransitionMaps.containsKey(workflowName)){
			transitionName = _workflowTransitionMaps.get(workflowName).getArchiveTransition(workflowState);
		} else {
			transitionName = null;
		}
		
		return transitionName;
	}
	
	public WFTransitionMappings(Map<String,WFTransitionMap> workflowTransitionMaps) {
		_workflowTransitionMaps = workflowTransitionMaps;
	}
}
