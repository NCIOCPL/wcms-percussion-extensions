package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a base class for RelationshipWFTransitionChecks
 * @author bpizzillo
 *
 */
public abstract class BaseRelationshipWFTransitionCheck {

	protected String relationshipName;
	
	/**
	 * Gets the name of the relationship.
	 * @return
	 */
	public String getRelationshipName() {
		return relationshipName;
	}
	
	/**
	 * Gets the WF transition type for this relationship 
	 * @return
	 */
	public abstract RelationshipWFTransitionTypes getTransitionType();
	
	/**
	 * Validates whether or not this relationship should stop the transition or not when the check is going down.
	 * Follow types should be expected to include a list of dependents which need to be included
	 * in transitions.
	 * @param ownerContentItemSummary 
	 * @param rel
	 * @return
	 */
	public abstract RelationshipWFTransitionCheckResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			);
	
	/**
	 * Validates whether or not this relationship should stop the transition or not when the check is going up.
	 * Follow types should be expected to include a list of dependents which need to be included
	 * in transitions.
	 * @param dependentContentItemSummary 
	 * @param rel
	 * @return
	 */
	public abstract RelationshipWFTransitionCheckResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			);
	
	/**
	 * Validates whether or not this relationship should stop the transition or not when the check is going down.
	 * Follow types should be expected to include a list of dependents which need to be included
	 * in transitions.
	 * This is run when an archiving transition is called.
	 * @param ownerContentItemSummary 
	 * @param rel
	 * @return
	 */
	public abstract RelationshipWFTransitionCheckResult archiveValidateDown(
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			);
	
	/**
	 * Validates whether or not this relationship should stop the transition or not when the check is going up.
	 * Follow types should be expected to include a list of dependents which need to be included
	 * in transitions.
	 * This is run when an archiving transition is called.
	 * @param dependentContentItemSummary 
	 * @param rel
	 * @return
	 */
	public abstract RelationshipWFTransitionCheckResult archiveValidateUp(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
			);

	/**
	 * 
	 * @param relationshipName
	 */
	public BaseRelationshipWFTransitionCheck(String relationshipName) {
		this.relationshipName = relationshipName;
	}
}
