package gov.cancer.wcm.images;

public class ExactConstraint extends Constraint {
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
	public ExactConstraint(String fName, String val) {
		fieldName = fName;
		value = val;
	}
	
	/*
	 * Given a value for a field on an image content item, determine
	 * if the value is within the defined constraints.
	 */
	ImageValidationError isConstrained(String data, String fullFieldName, String fieldDisplayName, String errorMessage) {
		if(data == null) {
			// If the data is null for this field, it hasn't been set on the content item and cannot be validated
			return null;
		}
		else {
			try {
				int dataVal = Integer.parseInt(data);
				int constraintVal = Integer.parseInt(value);
				
				if(dataVal == constraintVal) {
					// Don't return an error if the value matches the constraints
					return null;
				}
				else {
					// Return an error if the value does not match the constraints
					if(errorMessage.contains("%s")) {
						return new ImageValidationError(fullFieldName, String.format(errorMessage, this.getFieldName(), this.getValue()));
					}
					else {
						return new ImageValidationError(fullFieldName, errorMessage);
					}
				}
			}
			catch (NumberFormatException e) {
				// Add error logging here
				return new ImageValidationError(fullFieldName, fieldDisplayName + this.getFieldName() + " has an invalid value.");
			}
		}
	}	
}
