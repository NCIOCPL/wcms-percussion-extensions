package gov.cancer.wcm.images;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public void getFieldsToValidate() {
		// returns all individual items to validate? i.e. [ img1, width ] ?
	}
	
	public boolean validateItems(HashMap<String, String> fieldsToValidate) {
		boolean itemsAreValid = false;
		
		for(Map.Entry<String, String> entry : fieldsToValidate.entrySet()) {
			String[] parts = entry.getKey().split("_");
			String itemImageName = parts[0];
			String itemField = parts[1];
			String itemValue = entry.getValue();
			
			for(Map.Entry<String, ImageFieldValidator> validator : imageFieldValidators.entrySet()) {
				if(validator.getKey().equals(itemImageName)) {
					itemsAreValid = validator.getValue().isFieldValid(itemField, itemValue);
				}
			}
		}
		
		return itemsAreValid;
	}
}
