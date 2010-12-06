package gov.cancer.wcm.workflow;

public class RelationshipWFTransitionIgnoreConfig extends
		BaseRelationshipWFTransitionConfig {

	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Ignore;
	}
	
	/**
	 * 
	 * @param relationshipName
	 */
	public RelationshipWFTransitionIgnoreConfig(String relationshipName){
		super(relationshipName);
	}
	

}
