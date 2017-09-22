package gov.cancer.wcm.images;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageValidationConfiguration {
	private static Log log = LogFactory.getLog(ImageValidationConfiguration.class);
	private List<ImageCTValidator> imageCTValidators = new ArrayList<ImageCTValidator>(); 
	
	public List<ImageCTValidator> getImageCTValidatorS(){
		return imageCTValidators;
	}
	
	/*
	 * Returns true if the image validation configuration contains an image CT validator
	 * for a specific content type.
	 */
	public boolean hasImageCTValidator(String contentTypeName) {
		for(ImageCTValidator ctValidator : imageCTValidators) {
			if(ctValidator.getContentTypeName().equals(contentTypeName)) {
				return true;
			}
		}
		return false;
	}
	
	/* 
	 * Returns ImageCTValidator for a specific content type name.
	 */
	public ImageCTValidator getImageCTValidator(String contentTypeName){
		for(ImageCTValidator ctValidator : imageCTValidators) { 
			if(ctValidator.getContentTypeName().equals(contentTypeName)) {
				return ctValidator;
			}
		}
		return null;
	}
	
	public ImageValidationConfiguration(List<ImageCTValidator> imgCTValidators) {
		imageCTValidators = imgCTValidators;
		
		log.debug("Creating new instance of ImageValidationConfiguration");
	}
}
