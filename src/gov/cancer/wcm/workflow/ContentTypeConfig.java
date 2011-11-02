package gov.cancer.wcm.workflow;

import gov.cancer.wcm.workflow.validators.CTValidatorCollection;

public class ContentTypeConfig {
	private boolean isTopType;
	//private boolean requiresParentNavonsPublic;
	private String name;
	private boolean isPublishable;
	private CTValidatorCollection validatorCollection;

	public CTValidatorCollection getValidatorCollection() {
		return validatorCollection;
	}

	public String getName(){
		return this.name;
	}
	
	/**
	 * @return the isTopType
	 */
	public boolean getIsTopType() {
		return this.isTopType;
	}
	
	/**
	 * @return the isPublishable
	 */
	public boolean getIsPublishable() {
		return this.isPublishable;
	}
	
	private ContentTypeConfig(String name, boolean isTopType, boolean isPublishable, CTValidatorCollection validatorCollection) {
		this.name = name;
		this.isTopType = isTopType;
		this.isPublishable = isPublishable;
		//this.requiresParentNavonsPublic = requiresParentNavonsPublic;
		this.validatorCollection = validatorCollection;
	}
}
