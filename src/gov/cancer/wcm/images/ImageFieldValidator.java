package gov.cancer.wcm.images;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageFieldValidator {
	private String imageFieldName;
	private String imageFieldDisplayName;
	private String errorMessage;
	private List<Constraint> constraints = new ArrayList<Constraint>();
	private List<String> fileTypes = new ArrayList<String>();
	
	/*
	 * Retrieves the image field name.
	 */
	public String getImageFieldName(){
		return imageFieldName;
	}
	
	/*
	 * Retrieves the image field name.
	 */
	public String getImageFieldDisplayName(){
		return imageFieldDisplayName;
	}
	
	/*
	 * Retrieves the error message for this image field.
	 */
	public String getErrorMessage(){
		return errorMessage;
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
	public ImageFieldValidator(String fieldName, String fieldDisplayName, String errMsg, ArrayList<Constraint> imageConstraints){
		imageFieldName = fieldName;
		imageFieldDisplayName = fieldDisplayName;
		errorMessage = errMsg;
		constraints = imageConstraints;
	}
	
	/*
	 * Given a hashmap of image content item data to validate, determine
	 * if the content item fields are within the defined constraints.
	 */
	public ArrayList<ImageValidationError> validateField(HashMap<String, String> dataToValidate) {
		ArrayList<ImageValidationError> validationErrors = new ArrayList<ImageValidationError>();
		
		for(Constraint constraint : this.constraints) {
			String fullFieldName = "";
			
			if(constraint.getFieldName() != null) {
				// There is a field name associated with this image field constraint
				fullFieldName = this.imageFieldName + "_" + constraint.getFieldName();
			}
			else {
				// The image field itself is being validated
				fullFieldName = this.imageFieldName;
			}
			
			if(dataToValidate.containsKey(fullFieldName)) {
				if(constraint.isConstrained(dataToValidate.get(fullFieldName), fullFieldName, this.imageFieldDisplayName,this.errorMessage) != null) {
					// Validate the values against the constraints and add any potential errors to the list
					validationErrors.add(constraint.isConstrained(dataToValidate.get(fullFieldName), fullFieldName, this.imageFieldDisplayName, this.errorMessage));
				}
			}
		}
		
		return validationErrors;
	}
}
