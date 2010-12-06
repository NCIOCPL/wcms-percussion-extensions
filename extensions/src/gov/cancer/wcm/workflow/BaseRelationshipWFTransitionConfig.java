package gov.cancer.wcm.workflow;

public abstract class BaseRelationshipWFTransitionConfig {

	protected String relationshipName;
	
	/**
	 * 
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
	 * 
	 * @param relationshipName
	 */
	public BaseRelationshipWFTransitionConfig(String relationshipName) {
		this.relationshipName = relationshipName;
	}
}
