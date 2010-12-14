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
	 * Validates whether or not this relationship should stop the transition or not.
	 * Follow types should be expected to include a list of dependents which need to be included
	 * in transitions.
	 * @param contentItemSummary 
	 * @param rel
	 * @return
	 */
	public abstract RelationshipWFTransitionCheckResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel);
	
	/**
	 * 
	 * @param relationshipName
	 */
	public BaseRelationshipWFTransitionCheck(String relationshipName) {
		this.relationshipName = relationshipName;
	}
}
