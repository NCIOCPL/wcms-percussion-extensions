package gov.cancer.wcm.images;

public class BetweenConstraint extends Constraint {
	private String minValue;
	private String maxValue;
	
	/*
	 * Retrieves the minimum value.
	 */
	public String getMinValue(){
		return minValue;
	}
	
	/*
	 * Retrieves the maximum value.
	 */
	public String getMaxValue(){
		return maxValue;
	}
	
	/*
	 * Constructs an instance of BetweenConstraint using a minimum and maximum value.
	 * Both minValue and maxValue must never be null.
	 */
	public BetweenConstraint(String fName, String minVal, String maxVal) {
		fieldName = fName;
		minValue = minVal;
		maxValue = maxVal;
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
				int constraintMinVal = Integer.parseInt(minValue);
				int constraintMaxVal = Integer.parseInt(maxValue);
					
				if(dataVal >= constraintMinVal && dataVal <= constraintMaxVal) {
					// Don't return an error if the value is between the constraints
					return null;
				}
				else {
					// Return an error if the value is not between the constraints
					return new ImageValidationError(fullFieldName, String.format(errorMessage, this.getFieldName(), this.getMinValue(), this.getMaxValue()));
				}
			}
			catch (NumberFormatException e) {
				// Add error logging here
				return new ImageValidationError(fullFieldName, fieldDisplayName + this.getFieldName() + " has an invalid value.");
			}
		}
	}
}
