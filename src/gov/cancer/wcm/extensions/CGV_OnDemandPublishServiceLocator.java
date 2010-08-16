package gov.cancer.wcm.extensions;
import com.percussion.services.PSBaseServiceLocator;

/**
 *	 
 *	This class represents the factory design pattern.  It returns an instantiated instance of 
 *	CGV_OnDemandPublishService.
 *
 * 	@author DavidBenua
 *
 */
public class CGV_OnDemandPublishServiceLocator extends PSBaseServiceLocator
{
   /**
    * Private constructor 
    */
   private CGV_OnDemandPublishServiceLocator()
   {
      
   }

   /**
    * Returns an instance of Service class for publishing.
    * 
    * @return instance of CGV_OnDemandPublishService
    */
   public static CGV_OnDemandPublishService getCGV_OnDemandPublishService(){ 
	   
	   System.out.println("DEBUG: Getting the bean...");
	   return (CGV_OnDemandPublishService) getBean(CGVONDEMENDPUBLISHSERVICEBEAN);
   }
   
   /**
    * String representing name of the Service class
    */
   private static final String CGVONDEMENDPUBLISHSERVICEBEAN = "CGV_OnDemandPublishService"; 
}
