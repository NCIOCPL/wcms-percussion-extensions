package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.webservices.PSErrorException;

/**
 * Defines a base class for all RelationshipWFTransitionStopConditions.
 * @author bpizzillo
 *
 */
public abstract class BaseRelationshipWFTransitionStopCondition {

	private RelationshipWFTransitionStopConditionDirection _checkDirection;
	
	/**
	 * Gets the RelationshipWFTransitionStopConditionDirection which defines the 
	 * direction when this StopCondition should occur.
	 * @return
	 */
	public RelationshipWFTransitionStopConditionDirection getCheckDirection() {
		return _checkDirection;
	}
	
	/**
	 * Base class for checking stop conditions when going down. 
	 * @param contentItemSummary The owner PSComponentSummary
	 * @param rel The relationship to test
	 * @return
	 */
	public abstract RelationshipWFTransitionStopConditionResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			)  throws WFValidationException;

	/**
	 * Base class for checking stop conditions when going up. 
	 * @param contentItemSummary The owner PSComponentSummary
	 * @param rel The relationship to test
	 * @return
	 */
	public abstract RelationshipWFTransitionStopConditionResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			)  throws WFValidationException;

	public BaseRelationshipWFTransitionStopCondition(RelationshipWFTransitionStopConditionDirection checkDirection){
		_checkDirection = checkDirection;
	}
}
