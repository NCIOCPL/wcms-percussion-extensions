package gov.cancer.wcm.workflow;

import java.util.Map;

/**
 * 
 * @author bpizzillo
 *
 */
public class WFTransitionMappings {
	private Map<String,WFTransitionMap> _workflowTransitionMaps;
	
	public WFTransitionMappings(Map<String,WFTransitionMap> workflowTransitionMaps) {
		_workflowTransitionMaps = workflowTransitionMaps;
	}
}
