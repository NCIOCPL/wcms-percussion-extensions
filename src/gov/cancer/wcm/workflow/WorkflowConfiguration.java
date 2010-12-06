package gov.cancer.wcm.workflow;

/**
 * 
 * @author bpizzillo
 *
 */
public class WorkflowConfiguration {
	private ContentTypesConfigCollection contentTypes;
	private RelationshipConfigsCollection relationshipConfigs;
	
	
	public WorkflowConfiguration(
			ContentTypesConfigCollection contentTypes,
			RelationshipConfigsCollection relationshipConfigs
	) {
		this.contentTypes = contentTypes;
		this.relationshipConfigs = relationshipConfigs;		
	}
	
	
	
	public ContentTypesConfigCollection getContentTypes(){
		return contentTypes;
	}
	
	/**
	 * @return the relationshipConfigs
	 */
	public RelationshipConfigsCollection getRelationshipConfigs() {
		return relationshipConfigs;
	}
	
	
}
