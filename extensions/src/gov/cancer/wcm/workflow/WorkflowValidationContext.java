package gov.cancer.wcm.workflow;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;


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
	private PSComponentSummary _initiatorContentItem;
	private ArrayList<Integer> _transitionItemIds = new ArrayList<Integer>();
	private PSState _initiatingItemWorkflowState;
	private PSWorkflow _initiatingItemWorkflowApp;
	private int _initiatingTransitionID = -1;
	
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
	public void addItemToTransition(int contentId) {
		_transitionItemIds.add(contentId);
	}	
	
	/**
	 * Gets a list of all of the content ids that would need to be transitioned.
	 * @return
	 */
	public int[] getItemsToTransition() {
		int[] items = new int[_transitionItemIds.size()];
		
		for(int i =0; i<_transitionItemIds.size(); i++)
			items[i] = _transitionItemIds.get(i).intValue();
		
		return items;
	}
	
	public WorkflowValidationContext(
			PSComponentSummary initiatorContentItem, 
			Log log, 
			Document errorDoc,
			PSWorkflow initiatingItemWorkflowApp,
			PSState initiatingItemWorkflowState,
			int initiatingTransitionID
	) {
		_initiatorContentItem = initiatorContentItem;
		_log = log;
		_errorDoc = errorDoc;
		_initiatingItemWorkflowApp = initiatingItemWorkflowApp;
		_initiatingItemWorkflowState = initiatingItemWorkflowState;
		_initiatingTransitionID = initiatingTransitionID;
	}
}
