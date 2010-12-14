package gov.cancer.wcm.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RelationshipWFTransitionChecksCollection {
	
	private List<BaseRelationshipWFTransitionCheck> relationshipConfigs;
	private BaseRelationshipWFTransitionCheck defaultConfig;
	
	/**
	 * Gets a WFTransitionConfig based on the name of the relationship.
	 * @param relationshipName
	 * @return
	 */
	public BaseRelationshipWFTransitionCheck GetRelationshipWFTransitionConfigOrDefault(String relationshipName) {
		BaseRelationshipWFTransitionCheck rtnConfig = defaultConfig;
		
		for(BaseRelationshipWFTransitionCheck config : relationshipConfigs) {
			if (relationshipName.equals(config.relationshipName)) {
				rtnConfig = config;
				break;
			}
		}
		
		return rtnConfig;
	}
	
	public RelationshipWFTransitionChecksCollection(
			List<BaseRelationshipWFTransitionCheck> relationshipConfigs,
			BaseRelationshipWFTransitionCheck defaultConfig
	) {
		this.relationshipConfigs = relationshipConfigs;
		this.defaultConfig = defaultConfig;
	}
}
