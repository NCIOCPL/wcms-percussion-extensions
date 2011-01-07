package gov.cancer.wcm.workflow;

import gov.cancer.wcm.workflow.checks.RelationshipWFTransitionChecksCollection;

/**
 * Defines the workflow configuration for the WCM Project
 * @author bpizzillo
 *
 */
public class WorkflowConfiguration {
	private ContentTypesConfigCollection contentTypes;
	private RelationshipWFTransitionChecksCollection relationshipChecks;	
	private WFTransitionMappings transitionMappings;
	private ValidatorIgnoreConfig validatorIgnore;
	
	public ValidatorIgnoreConfig getValidatorIgnore() {
		return validatorIgnore;
	}

	public void setValidatorIgnore(ValidatorIgnoreConfig validatorIgnore) {
		this.validatorIgnore = validatorIgnore;
	}

	/**
	 * Gets the configurations for content types.  These are extra metadata about
	 * a content type since it is not possible to add extra metadata to a content
	 * type in percussion.
	 * @return
	 */
	public ContentTypesConfigCollection getContentTypes(){
		return contentTypes;
	}
	
	/**
	 * Gets a collection of RelationshipWFTransitionChecks used to check dependents
	 * in a relationship for an owner that is transitioning through the workflow.
	 * @return the relationshipConfigs
	 */
	public RelationshipWFTransitionChecksCollection getRelationshipConfigs() {
		return relationshipChecks;
	}
	
	/**
	 * Gets the transition mappings for the workflow system.
	 * @return
	 */
	public WFTransitionMappings getTransitionMappings() {
		return this.transitionMappings;
	}

	/**
	 * Initializes a new instance of the WorkflowConfiguration class.
	 * @param contentTypes a collection of content type configurations.
	 * @param relationshipChecks a collection of relationship checks.
	 */
	public WorkflowConfiguration(
			ContentTypesConfigCollection contentTypes,
			RelationshipWFTransitionChecksCollection relationshipChecks,
			WFTransitionMappings transitionMappings, 
			ValidatorIgnoreConfig validatorIgnore
	) {
		this.contentTypes = contentTypes;
		this.relationshipChecks = relationshipChecks;
		this.transitionMappings = transitionMappings;
		this.validatorIgnore = validatorIgnore;
	}

}
