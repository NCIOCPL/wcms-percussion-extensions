package gov.cancer.wcm.workflow;

import java.util.List;

public class RelationshipWFTransitionFollowConfig extends
		BaseRelationshipWFTransitionConfig {

	List<RelationshipWFTransitionStopConditions> stopConditions;
	
	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Follow;
	}
	
	public RelationshipWFTransitionFollowConfig(
			String relationshipName,
			List<RelationshipWFTransitionStopConditions> stopConditions
	) {
		super(relationshipName);
		this.stopConditions = stopConditions;
	}
}
