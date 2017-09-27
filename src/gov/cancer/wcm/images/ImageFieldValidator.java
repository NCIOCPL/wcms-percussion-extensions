package gov.cancer.wcm.images;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageFieldValidator {
	private String imageFieldName;
	private List<Constraint> constraints = new ArrayList<Constraint>();
	private List<String> fileTypes = new ArrayList<String>();
	
	/*
	 * Retrieves the image field name.
	 */
	public String getImageFieldName(){
		return imageFieldName;
	}
	
	/*
	 * Retrieves the constraints
	 */
	public List<Constraint> getConstraints(){
		return constraints;
	}
	
	/*
	 * Retrieves the allowed file types
	 */
	public List<String> getFileTypes(){
		return fileTypes;
	}
	
	/*
	 * Constructs an instance of ImageFieldValidator using a fieldName and a list of constraints.
	 * Constraints may be empty, but must never be null.
	 */
	public ImageFieldValidator(String fieldName, ArrayList<Constraint> imageConstraints){
		imageFieldName = fieldName;
		constraints = imageConstraints;
	}
	
	/*
	 * Given a hashmap of image content item data to validate, determine
	 * if the content item fields are within the defined constraints.
	 */
	public List<ImageValidationError> validateField(HashMap<String, String> dataToValidate) {
		ArrayList<ImageValidationError> validationErrors = new ArrayList<ImageValidationError>();
		
		for(Constraint constraint : this.constraints) {
			String fullFieldName = "";
			
			if(constraint.getFieldName() != null) {
				fullFieldName = this.imageFieldName + "_" + constraint.getFieldName();
			}
			else {
				fullFieldName = this.imageFieldName;
			}
			
			if(dataToValidate.containsKey(fullFieldName)) {
				if(constraint.isConstrained(dataToValidate.get(fullFieldName), this.imageFieldName) != null) {
					validationErrors.add(constraint.isConstrained(dataToValidate.get(fullFieldName), this.imageFieldName));
				}
			}
		}
		
		return validationErrors;
	}
}
