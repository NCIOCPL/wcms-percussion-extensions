package gov.cancer.wcm.images;

public abstract class Constraint {
	private int fieldName;
	
	/*
	 * Retrieves the constraint width.
	 */
	public int getFieldName(){
		return fieldName;
	}
	
	abstract boolean isConstrained();
}
