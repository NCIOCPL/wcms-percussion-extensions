package gov.cancer.wcm.images;

import java.util.HashMap;
import java.util.List;

public class ImageCTValidator {
	private String contentTypeName;
	private HashMap<String, ImageFieldValidator> imageFieldValidators;
	
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
	public ImageCTValidator(List<ImageFieldValidator> imgValidators){
		// Convert image field validators list to a map for faster lookups.
		for(ImageFieldValidator validator : imgValidators){
			String key = validator.getImageField();
			imageFieldValidators.put(key, validator);
		}
	}
}
