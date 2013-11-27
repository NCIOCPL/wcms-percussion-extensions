package gov.cancer.wcm.workflow;

import gov.cancer.wcm.workflow.properties.PropertyCollection;
import gov.cancer.wcm.workflow.validators.CTValidatorCollection;

public class ContentTypeConfig {
	
	/*
	 * "Well-Known" property names.
	 */
	public final static String PROP_IS_SITE_HOME = "SiteHomeProperty"; 
	
	private boolean isTopType;
	//private boolean requiresParentNavonsPublic;
	private String name;
	private boolean isPublishable;
	private CTValidatorCollection validatorCollection;
	private PropertyCollection propertyCollection;

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
	
	/**
	 * @param name name of the content type the configuration element is for.
	 * @param isTopType - Is this a top type?
	 * @param isPublishable - Can this type be published?
	 * @param validatorCollection - Collection of workflow validators.
	 * @param propertyCollection - Collection of properties for the content type.
	 */
	private ContentTypeConfig(String name, boolean isTopType, boolean isPublishable, CTValidatorCollection validatorCollection,
			PropertyCollection propertyCollection) {
		this.name = name;
		this.isTopType = isTopType;
		this.isPublishable = isPublishable;
		this.validatorCollection = validatorCollection;
		this.propertyCollection = propertyCollection;
	}
}
