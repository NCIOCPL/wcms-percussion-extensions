package gov.cancer.wcm.workflow;

import java.util.List;

public class RelationshipWFTransitionFollowConfig extends
		BaseRelationshipWFTransitionConfig {

	private List<RelationshipWFTransitionStopConditions> stopConditions;
	
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
	public List<RelationshipWFTransitionStopConditions> getStopConditions() {
		return stopConditions;
	}
	
	public RelationshipWFTransitionFollowConfig(
			String relationshipName,
			List<RelationshipWFTransitionStopConditions> stopConditions
	) {
		super(relationshipName);
		this.stopConditions = stopConditions;
	}
}
