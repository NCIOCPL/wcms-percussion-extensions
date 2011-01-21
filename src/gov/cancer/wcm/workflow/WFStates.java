package gov.cancer.wcm.workflow;

import java.util.Map;

/**
 * Defines a weight mapping of workflow states.
 * @author wallsjt
 *
 */
public class WFStates {
	
	private Map<String, Integer> _workflowStates;
	
	public Map<String, Integer> get_workflowStates() {
		return _workflowStates;
	}

	public void set_workflowStates(Map<String, Integer> _workflowStates) {
		this._workflowStates = _workflowStates;
	}

	public WFStates(
			Map<String, Integer> workflowStates
	){
		_workflowStates = workflowStates;
	}
	
	/**
	 * Returns a bool based off:
	 * left >= right
	 * @param left - left hand side of the equation
	 * @param right - right hand side of the equation
	 * @return if( left >= right ) true. else, false.
	 */
	public boolean greaterThanOrEqual(String left, String right) throws WFValidationException{
		if(!_workflowStates.containsKey(left)){
			WFValidationException e = new WFValidationException("Invalid workflow state "+left+".");
			throw e;
		}
		if(!_workflowStates.containsKey(right)){
			WFValidationException e = new WFValidationException("Invalid workflow state "+right+".");
			throw e;
		}
		
		if( _workflowStates.get(left) >= _workflowStates.get(right) ){
			return true;
		}
		else{
			return false;
		}
	}


}
