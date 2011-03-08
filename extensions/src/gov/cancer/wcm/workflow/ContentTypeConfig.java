package gov.cancer.wcm.workflow;

import gov.cancer.wcm.workflow.validators.CTValidatorCollection;

public class ContentTypeConfig {
	private boolean isTopType;
	//private boolean requiresParentNavonsPublic;
	private String name;
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
	
	private ContentTypeConfig(String name, boolean isTopType, CTValidatorCollection validatorCollection) {
		this.name = name;
		this.isTopType = isTopType;
		//this.requiresParentNavonsPublic = requiresParentNavonsPublic;
		this.validatorCollection = validatorCollection;
	}
}
