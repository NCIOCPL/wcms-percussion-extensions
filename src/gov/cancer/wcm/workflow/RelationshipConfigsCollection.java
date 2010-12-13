package gov.cancer.wcm.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RelationshipConfigsCollection {
	
	private List<BaseRelationshipWFTransitionConfig> relationshipConfigs;
	private BaseRelationshipWFTransitionConfig defaultConfig;
	
	/**
	 * Gets a WFTransitionConfig based on the name of the relationship.
	 * @param relationshipName
	 * @return
	 */
	public BaseRelationshipWFTransitionConfig GetRelationshipWFTransitionConfigOrDefault(String relationshipName) {
		BaseRelationshipWFTransitionConfig rtnConfig = defaultConfig;
		
		for(BaseRelationshipWFTransitionConfig config : relationshipConfigs) {
			if (relationshipName.equals(config.relationshipName)) {
				rtnConfig = config;
				break;
			}
		}
		
		return rtnConfig;
	}
	
	public RelationshipConfigsCollection(
			List<BaseRelationshipWFTransitionConfig> relationshipConfigs,
			BaseRelationshipWFTransitionConfig defaultConfig
	) {
		this.relationshipConfigs = relationshipConfigs;
		this.defaultConfig = defaultConfig;
	}
}
