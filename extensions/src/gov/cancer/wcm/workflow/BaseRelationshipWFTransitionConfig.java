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
	 * 
	 * @param relationshipName
	 */
	public BaseRelationshipWFTransitionConfig(String relationshipName) {
		this.relationshipName = relationshipName;
	}
}
