package gov.cancer.wcm.workflow;

import gov.cancer.wcm.workflow.properties.BasePropertyContainer;
import gov.cancer.wcm.workflow.properties.PropertyCollection;
import gov.cancer.wcm.workflow.validators.CTValidatorCollection;

public class ContentTypeConfig {
	
	/*
	 * "Well-Known" property names.
	 */
	public final static String PROP_IS_SITE_HOME = "isSiteRoot"; 
	
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
	 * @param propName
	 * @return True if the content type contains a definition for propName.
	 */
	public boolean hasProperty(String propName){
		return propertyCollection.hasProperty(propName);
	}
	
	/**
	 * @param propName
	 * @return The value for the propName property.  Null if propName is not defined.
	 */
	public String getProperty(String propName){
		if(propertyCollection.hasProperty(propName))
			return propertyCollection.getProperty(propName).getValue();
		else
			return null;
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
