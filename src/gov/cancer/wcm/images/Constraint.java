package gov.cancer.wcm.images;

public abstract class Constraint {
	protected String fieldName;
	
	/*
	 * Retrieves the constraint fieldName.
	 */
	public String getFieldName(){
		return fieldName;
	}
	
	/*
	 * Abstract method for isConstrained, which defines how to determine
	 * validation for a passed-in value from a field on an image.
	 */
	abstract String isConstrained(String data, String imageFieldName);
}
