package gov.cancer.wcm.extensions;

import com.percussion.services.PSBaseServiceLocator;

/**
 */
public class CGV_RelationshipHandlerServiceLocator extends PSBaseServiceLocator
{
	   /**
	    * Private constructor 
	    */
	   private CGV_RelationshipHandlerServiceLocator()
	   {
		   
	      
	   }

	   /**
	    * Returns an instance of Service class for publishing.
	    * 
	   
	    * @return instance of CGV_OnDemandPublishService */
	   public static CGV_RelationshipHandlerService getCGV_RelatoinshipHandlerService(){ 
		   return (CGV_RelationshipHandlerService) getBean(CGVRELATIONSHIPHANDLER);
	   }
	   
	   /**
	    * String representing name of the Service class
	    */
	   private static final String CGVRELATIONSHIPHANDLER = "CGV_RelationshipHandlerService"; 
	}