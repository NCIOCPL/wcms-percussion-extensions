package gov.cancer.wcm.images;

public class EqualConstraint extends Constraint {
	private String value;
	
	/*
	 * Retrieves the value.
	 */
	public String getValue(){
		return value;
	}
	
	/*
	 * Constructs an instance of EqualConstraint using an exact value.
	 * Value must never be null.
	 */
	public EqualConstraint(String fName, String val) {
		fieldName = fName;
		value = val;
	}
	
	/*
	 * Given a value for a field on an image content item, determine
	 * if the value is within the defined constraints.
	 */
	String isConstrained(String data, String imageFieldName) {
		try {
			int dataVal = Integer.parseInt(data);
			int constraintVal = Integer.parseInt(value);
			
			if(dataVal == constraintVal) {
				return "Validated " + imageFieldName + "_" + this.getFieldName();
			}
			else {
				return "Error: " + imageFieldName + "_" + this.getFieldName() + " has value " + data 
						+ "which does not equal constraint value " + this.getValue() + ".";
			}
		}
		catch (NumberFormatException e) {
			// Add error logging here
			return "Error: " + imageFieldName + "_" + this.getFieldName() + " has an invalid value.";
		}
	}	
}
