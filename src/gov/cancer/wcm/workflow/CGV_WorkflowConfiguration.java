package gov.cancer.wcm.workflow;

/**
 * 
 * @author bpizzillo
 *
 */
public class CGV_WorkflowConfiguration {
	private CGV_ContentTypesConfigCollection contentTypes;
	private CGV_RelationshipConfigsCollection relationshipConfigs;
	
	
	public CGV_WorkflowConfiguration(
			CGV_ContentTypesConfigCollection contentTypes,
			CGV_RelationshipConfigsCollection relationshipConfigs
	) {
		this.contentTypes = contentTypes;
		this.relationshipConfigs = relationshipConfigs;		
	}
	
	
	
	public CGV_ContentTypesConfigCollection getContentTypes(){
		return contentTypes;
	}
	
	/**
	 * @return the relationshipConfigs
	 */
	public CGV_RelationshipConfigsCollection getRelationshipConfigs() {
		return relationshipConfigs;
	}
	
	
}
