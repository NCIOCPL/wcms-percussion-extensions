package gov.cancer.wcm.images;

public class BetweenConstraint extends Constraint {
	private String minValue;
	private String maxValue;
	
	public String getMinValue(){
		return minValue;
	}
	
	public String getMaxValue(){
		return maxValue;
	}
	
	public BetweenConstraint(String fName, String minVal, String maxVal) {
		fieldName = fName;
		minValue = minVal;
		maxValue = maxVal;
	}
	
	boolean isConstrained(String data) {
		try {
			int dataVal = Integer.parseInt(data);
			int constraintMinVal = Integer.parseInt(minValue);
			int constraintMaxVal = Integer.parseInt(maxValue);
			
			if(dataVal >= constraintMinVal && dataVal <= constraintMaxVal) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (NumberFormatException e) {
			// Add error logging here
			return false;
		}
	}
}
