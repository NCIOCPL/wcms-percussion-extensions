package gov.cancer.wcm.workflow.properties;

/**
 * Defines a base class for storing pairs of property names and values.
 * @author learnb
 *
 */
public abstract class BasePropertyContainer {

	private String name;
	private String value;
	
	/*
	 * Retrieves the property name.
	 */
	public String getName(){
		return name;
	}
	
	/*
	 * Retrieves the value stored in the property
	 */
	public String getValue(){
		return value;
	}
	
	public BasePropertyContainer(String propertyName, String propertyValue){
		this.name = propertyName;
		this.value = propertyValue;
	}
}
