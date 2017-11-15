package gov.cancer.wcm.images;

import com.percussion.services.PSBaseServiceLocator;

public class ImageValidationConfigurationLocator extends PSBaseServiceLocator {
	/**
	    * Private constructor 
	    */
	   private ImageValidationConfigurationLocator(){ }

	   /**
	    * Returns an instance of Service class for image validation.
	    * 
	   
	    * @return instance of CGV_ImageValidationConfiguration */
	   public static ImageValidationConfiguration getImageValidationConfiguration(){ 
		   return (ImageValidationConfiguration) getBean(IMAGE_VALIDATION_CONFIGURATION_BEAN_NAME);
	   }
	   
	   /**
	    * String representing name of the Service class
	    */
	   private static final String IMAGE_VALIDATION_CONFIGURATION_BEAN_NAME = "CGV_ImageValidationConfiguration"; 
}
