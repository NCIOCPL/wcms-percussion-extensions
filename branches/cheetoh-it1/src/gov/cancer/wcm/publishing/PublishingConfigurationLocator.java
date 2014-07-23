package gov.cancer.wcm.publishing;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Class to locate/instantiate/load the PublishingConfiguration object.
 * 
 */
public class PublishingConfigurationLocator extends PSBaseServiceLocator {

	   /**
	    * Hide the no-argument constructor as private 
	    */
	   private PublishingConfigurationLocator()
	   {
	   }

	   /**
	    * Returns an instance of the publishing configuration class.
	    * 
	    * @return instance of PublishingConfiguration */
	   public static PublishingConfiguration getPublishingConfiguration(){ 
		   return (PublishingConfiguration) getBean(PUBLISHING_CONFIGURATION_BEAN_NAME);
	   }
	   
	   /**
	    * String representing name of the configuration bean.
	    */
	   private static final String PUBLISHING_CONFIGURATION_BEAN_NAME = "CGV_PublishingConfiguration"; 
}
