package gov.cancer.wcm.workflow;

import java.util.List;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionCheck which checks to see if the
 * dependents of a relationship type are allowed to transition and compiles
 * a list of items to transition.
 * @author bpizzillo
 *
 */
public class RelationshipWFTransitionFollowCheck extends
		BaseRelationshipWFTransitionCheck {

	private List<BaseRelationshipWFTransitionStopCondition> stopConditions;
	
	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Follow;
	}
	
	/**
	 * Gets the stop conditions for this follow config.
	 * @return
	 */
	public List<BaseRelationshipWFTransitionStopCondition> getStopConditions() {
		return stopConditions;
	}
	
	@Override
	public RelationshipWFTransitionCheckResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {

		
		return RelationshipWFTransitionCheckResult.StopTransition;
	}

	/**
	 * Initializes a new instance of the RelationshipWFTransitionFollowCheck class.
	 * @param relationshipName The name of the relationship to check
	 * @param stopConditions The conditions to check for.
	 */
	public RelationshipWFTransitionFollowCheck(
			String relationshipName,
			List<BaseRelationshipWFTransitionStopCondition> stopConditions
	) {
		super(relationshipName);
		this.stopConditions = stopConditions;
	}
}
