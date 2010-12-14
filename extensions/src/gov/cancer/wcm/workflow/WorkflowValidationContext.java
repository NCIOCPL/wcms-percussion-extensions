package gov.cancer.wcm.workflow;

import org.apache.commons.logging.Log;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a class which holds information which is used for a workflow validation.  This
 * is so we do not have to keep changing the signature of the validate method of 
 * RelationshipWFTransitionChecks and RelationshipWFTransitionStopConditions 
 * @author bpizzillo
 *
 */
public class WorkflowValidationContext {
	
	private Log _log;
	
	public Log getLog() {
		return _log;
	}
	
	public WorkflowValidationContext(Log log) {
		_log = log;
	}
}
