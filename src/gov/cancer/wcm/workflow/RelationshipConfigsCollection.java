package gov.cancer.wcm.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RelationshipConfigsCollection {
	
	private List<BaseRelationshipWFTransitionConfig> relationshipConfigs;
	private BaseRelationshipWFTransitionConfig defaultConfig;
	
	public BaseRelationshipWFTransitionConfig GetRelationshipWFTransitionConfigOrDefault(String relationshipName) {
		BaseRelationshipWFTransitionConfig rtnConfig = defaultConfig;
		
		for(BaseRelationshipWFTransitionConfig config : relationshipConfigs) {
			if (relationshipName.equals(config.relationshipName)) {
				rtnConfig = config;
				continue;
			}
		}
		
		return defaultConfig;
	}
	
	public RelationshipConfigsCollection(
			List<BaseRelationshipWFTransitionConfig> relationshipConfigs,
			BaseRelationshipWFTransitionConfig defaultConfig
	) {
		this.relationshipConfigs = relationshipConfigs;
		this.defaultConfig = defaultConfig;
	}
}
