/**
 * 
 */
package gov.cancer.wcm.workflow.properties;

import java.util.HashMap;
import java.util.List;

/**
 * Container for a content type's list of properties.
 */
public class PropertyCollection {

	// Store property list as a HashMap for faster access.
	private HashMap<String, BasePropertyContainer> propertyMap = new HashMap<String, BasePropertyContainer>();

	/*
	 * Returns true if the collection contains property name.
	 */
	public Boolean hasProperty(String propertyName){
		return propertyMap.containsKey(propertyName);
	}

	/*
	 * Retrieves the property named propertyName.
	 * Returns null if propertyName does not exist.
	 */
	public BasePropertyContainer getProperty(String propertyName){
		if(propertyMap.containsKey(propertyName)){
			return propertyMap.get(propertyName);
		}
		else
			return null;
	}

	/*
	 * Constructs an instance of PropertyCollection using a List of properties.
	 * propertyCollection may be empty, but must never be null.
	 */
	public PropertyCollection(List<BasePropertyContainer> propertyCollection){
		// Convert property collection to a map for faster lookups.
		for(BasePropertyContainer property : propertyCollection){
			String key = property.getName();
			propertyMap.put(key, property);
		}
	}
	
}
