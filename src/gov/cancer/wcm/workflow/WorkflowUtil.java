/**
 * Utility class for working with Worflows.
 * It is *not* safe to share an instance of this class between threads,
 * however, re-using a single instance is otherwise encouraged.
 */
package gov.cancer.wcm.workflow;

import java.util.HashMap;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.types.PSPair;

/**
 * @author learnb
 *
 */
public class WorkflowUtil {
	
	private static IPSWorkflowService workflowService;
	
	static {
		workflowService = PSWorkflowServiceLocator.getWorkflowService();
	}
	
	// Caches to avoid repeatedly looking up the same workflows.
	// NOTE: These objects are explicitly *NOT* declared as static. This avoids potential threading problems.
	// This will still prevent redundant workflow lookups during a single validator instance's lifetime.
	private HashMap<Integer, PSWorkflow> _workflowCache;	// Map workflow IDs to workflow objects.
	private HashMap<PSPair<Integer,Integer>, PSState> _workflowStateCache;	// Map (workflow ID, state ID) pairs to PSState objects.  

	/**
	 * Convenience method for retrieving a content item's workflow. 
	 * @param item ComponentSummary object of the object
	 * @return Object representing the item's workflow.
	 */
	public PSWorkflow getWorkflow(PSComponentSummary item){
		int wfId = item.getWorkflowAppId();

		// Guarantee workflow object is in cache.
		if(!_workflowCache.containsKey(wfId)){
			_workflowCache.put(wfId, workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, wfId)));
		}
		return _workflowCache.get(wfId);
	}
	
	/**
	 * Convenience method for retrieving a content item's workflow state.
	 * @param item ComponentSummary object of the object
	 * @return Object representing the item's workflow state.
	 */
	public PSState getWorkflowState(PSComponentSummary item){
		int wfState = item.getContentStateId();
		int wfId = item.getWorkflowAppId();

		// Guarantee workflow state object is in cache.
		PSPair<Integer,Integer> wfStatePair = new PSPair<Integer,Integer>(wfId, wfState);
		if(!_workflowStateCache.containsKey(wfStatePair)){
			_workflowStateCache.put(wfStatePair, 
				 workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfState),
							new PSGuid(PSTypeEnum.WORKFLOW,wfId)));
		}
		return _workflowStateCache.get(wfStatePair);
	}

	public String getWorkflowStateName(PSComponentSummary item){
		PSState state = getWorkflowState(item);
		return state.getName();
	}
	
	public WorkflowUtil(){
		_workflowCache = new HashMap<Integer, PSWorkflow>();
		_workflowStateCache = new HashMap<PSPair<Integer,Integer>, PSState>(); 
	}
}
