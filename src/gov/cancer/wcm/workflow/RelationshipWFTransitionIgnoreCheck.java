package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionCheck which ignores the state of
 * the dependent.  (Does not need a public revision, will not transition)
 * @author bpizzillo
 *
 */
public class RelationshipWFTransitionIgnoreCheck extends
		BaseRelationshipWFTransitionCheck {

	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Ignore;
	}

	@Override
	public RelationshipWFTransitionCheckResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {

		
		return RelationshipWFTransitionCheckResult.ContinueTransition;
	}

	@Override
	public RelationshipWFTransitionCheckResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {		
		return RelationshipWFTransitionCheckResult.ContinueTransition;
	}
	
	/**
	 * Initializes a new instance of the RelationshipWFTransitionIgnoreConfig class.
	 * @param relationshipName the name of the relationship this config is for.
	 */
	public RelationshipWFTransitionIgnoreCheck(String relationshipName){
		super(relationshipName);
	}
	

}
