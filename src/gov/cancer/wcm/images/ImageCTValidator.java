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
	public Boolean hasValidator(String fieldValidatorName){
		return imageFieldValidators.containsKey(fieldValidatorName);
	}

	/*
	 * Retrieves the image field validator named fieldValidatorName.
	 * Returns null if fieldValidatorName does not exist.
	 */
	public ImageFieldValidator getFieldValidator(String fieldValidatorName){
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
			imageFieldValidators.put(key, validator);
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
				fieldsToValidate.add(key + "_" + constraint.fieldName);
			}
		}
		
		return fieldsToValidate;
	}
	
	/*
	 * Given a hashmap of image content item data to validate, determine
	 * if the content item fields are within the defined constraints.
	 */
	public ArrayList<String> validateItems(HashMap<String, String> dataToValidate) {
		ArrayList<String> validationErrors = new ArrayList<String>();
		
		for(String key : this.imageFieldValidators.keySet()) {
			validationErrors.addAll(this.imageFieldValidators.get(key).validateField(dataToValidate));	
		}
		
		return validationErrors;
	}
}
