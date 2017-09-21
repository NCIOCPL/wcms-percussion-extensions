package gov.cancer.wcm.images;

public class EqualConstraint extends Constraint {
	private String value;
	
	public String getValue(){
		return value;
	}
	
	public EqualConstraint(String fName, String val) {
		fieldName = fName;
		value = val;
	}
	
	boolean isConstrained(String data) {
		try {
			int dataVal = Integer.parseInt(data);
			int constraintVal = Integer.parseInt(value);
			
			if(dataVal == constraintVal) {
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
