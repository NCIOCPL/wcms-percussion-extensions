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
public class CGV_ParentChildServiceLocator extends PSBaseServiceLocator
{
   /**
    * Private constructor 
    */
   private CGV_ParentChildServiceLocator()
   {
      
   }

   /**
    * Returns an instance of Service class for publishing.
    * 
    * @return instance of CGV_OnDemandPublishService
    */
   public static CGV_ParentChildService getCGV_ParentChildService(){ 
	   
	   System.out.println("DEBUG: Getting the bean...");
	   return (CGV_ParentChildService) getBean(CGVPARENTCHILDSERVICE);
   }
   
   /**
    * String representing name of the Service class
    */
   private static final String CGVPARENTCHILDSERVICE = "CGV_ParentChildService"; 
}
