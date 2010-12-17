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

	/**
	 * Base class for checking stop conditions. 
	 * @param contentItemSummary The owner PSComponentSummary
	 * @param rel The relationship to test
	 * @return
	 */
	public abstract RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary, 
			PSRelationship rel,
			WorkflowValidationContext wvc
			)  throws WFValidationException;
}
