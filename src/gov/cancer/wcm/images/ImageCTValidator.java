package gov.cancer.wcm.images;

import java.util.*;

public class ImageCTValidator {
	private String contentTypeName;
	private HashMap<String, ImageFieldValidator> imageFieldValidators = new HashMap<String, ImageFieldValidator>();
	
	/*
	 * Retrieves the content type name.
	 */
	public String getContentTypeName(){
		return contentTypeName;
	}
	
	/*
	 * Returns true if the image CT validator contains an image field validator.
	 */
	public Boolean hasImageFieldValidator(String fieldValidatorName){
		return imageFieldValidators.containsKey(fieldValidatorName);
	}

	/*
	 * Retrieves the image field validator named fieldValidatorName.
	 * Returns null if fieldValidatorName does not exist.
	 */
	public ImageFieldValidator getImageFieldValidator(String fieldValidatorName){
		if(imageFieldValidators.containsKey(fieldValidatorName)){
			return imageFieldValidators.get(fieldValidatorName);
		}
		else
			return null;
	}
	
	/*
	 * Constructs an instance of ImageCTValidator using a List of image field validators.
	 * imageFieldValidators may be empty, but must never be null.
	 */
	public ImageCTValidator(String ctName, List<ImageFieldValidator> imgValidators){
		contentTypeName = ctName;
		
		// Convert image field validators list to a map for faster lookups.
		for(ImageFieldValidator validator : imgValidators){
			String key = validator.getImageFieldName();
			
			if(!imageFieldValidators.containsKey(key)) {
				imageFieldValidators.put(key, validator);
			}
		}
	}
	
	/*
	 * Using the list of image field validators, determine which fields
	 * are validated by this CT Validator.
	 */
	public ArrayList<String> getFieldsToValidate() {
		ArrayList<String> fieldsToValidate = new ArrayList<String>();
		
		for(String key : this.imageFieldValidators.keySet()) {
			for(Constraint constraint : this.imageFieldValidators.get(key).getConstraints()) {
				if(!fieldsToValidate.contains(key + "_" + constraint.fieldName)) {
					fieldsToValidate.add(key + "_" + constraint.fieldName);
				}
			}
		}
		
		return fieldsToValidate;
	}
	
	/*
	 * Using the list of image field validators, determine which fields
	 * are constrained by this CT Validator.
	 
	public ArrayList<String> getConstraintFields() {
		ArrayList<String> constraintFields = new ArrayList<String>();
		
		for(String key : this.imageFieldValidators.keySet()) {
			for(Constraint constraint : this.imageFieldValidators.get(key).getConstraints()) {
				constraintFields.add("_" + constraint.fieldName);
			}
		}
		
		return constraintFields;
	}*/
	
	/*
	 * Given a hashmap of image content item data to validate, determine
	 * if the content item fields are within the defined constraints.
	 */
	public ArrayList<ImageValidationError> validateItems(HashMap<String, String> dataToValidate) {
		ArrayList<ImageValidationError> validationErrors = new ArrayList<ImageValidationError>();
		
		for(String key : this.imageFieldValidators.keySet()) {
			if(!this.imageFieldValidators.get(key).validateField(dataToValidate).isEmpty()) {
				validationErrors.addAll(this.imageFieldValidators.get(key).validateField(dataToValidate));
			}
		}
		
		return validationErrors;
	}
}
