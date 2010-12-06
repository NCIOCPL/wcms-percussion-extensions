package gov.cancer.wcm.workflow;

public class RelationshipWFTransitionStopConfig extends
		BaseRelationshipWFTransitionConfig {
	
	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Stop;
	}

	public RelationshipWFTransitionStopConfig(String relationshipName) {
		super(relationshipName);
	}
}
