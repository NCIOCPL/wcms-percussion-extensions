package gov.cancer.wcm.images;

public abstract class Constraint {
	protected String fieldName;
	
	/*
	 * Retrieves the constraint fieldName.
	 */
	public String getFieldName(){
		return fieldName;
	}
	
	abstract boolean isConstrained(String data);
}
