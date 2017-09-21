package gov.cancer.wcm.images;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageValidationConfiguration {
	private static Log log = LogFactory.getLog(ImageValidationConfiguration.class);
	private List<ImageCTValidator> imageCTValidators = new ArrayList<ImageCTValidator>(); 
	
	/*
	 
	public List<ImageCTValidator> getImageCTValidatorS(){
		return imageCTValidators;
	}
	
	 * Returns ImageCTValidator for a specific content type name.
	public ImageCTValidator getImageCTValidator(String contentTypeName){
		for(ImageCTValidator ctValidator : imageCTValidators) { 
			if(ctValidator.getContentTypeName().equal(contentTypeName)) {
				return ctValidator;
			}
		}
	}
	*/
	
	public ImageValidationConfiguration() {
		/*
		log.debug("Creating new instance of ImageValidationConfiguration");
		
		** Variable passed in: List<ImageCTValidator> imgCTValidators
		imageCTValidators = imgCTValidators;
		
		*/
	}
}
