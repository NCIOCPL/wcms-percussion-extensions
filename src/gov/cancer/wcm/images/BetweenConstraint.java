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
	ImageValidationError isConstrained(String data, String imageFieldName) {
		if(data == null) {
			return null;
		}
		else {
			try {
				int dataVal = Integer.parseInt(data);
				int constraintMinVal = Integer.parseInt(minValue);
				int constraintMaxVal = Integer.parseInt(maxValue);
					
				if(dataVal >= constraintMinVal && dataVal <= constraintMaxVal) {
					return null;
				}
				else {
					return new ImageValidationError(imageFieldName, "Error: " + imageFieldName + "_" + this.getFieldName() + " has value " + data 
							+ "which is not between constraint values [" + this.getMinValue()
							+ ", " + this.getMaxValue() + "].");
				}
			}
			catch (NumberFormatException e) {
				// Add error logging here
				return new ImageValidationError(imageFieldName, "Error: " + imageFieldName + "_" + this.getFieldName() + " has an invalid value.");
			}
		}
	}
}
