package gov.cancer.wcm.images;

import java.util.List;
import java.util.ArrayList;

public class ImageFieldValidator {
	private String imageFieldName;
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	/*
	 * Retrieves the image field name.
	 */
	public String getImageFieldName(){
		return imageFieldName;
	}
	
	/*
	 * Retrieves the constraints
	 */
	public ArrayList<Constraint> getConstraints(){
		return constraints;
	}
	
	public ImageFieldValidator(String fieldName, ArrayList<Constraint> imageConstraints){
		imageFieldName = fieldName;
		constraints = imageConstraints;
	}
	
	public boolean isFieldValid(String dataField, String dataValue) {
		boolean isValid = false;
		
		for(Constraint constraint : constraints) {
			if(dataField.equals(constraint.fieldName)) {
				isValid = constraint.isConstrained(dataValue);
			}
		}
		
		return isValid;
	}
}
