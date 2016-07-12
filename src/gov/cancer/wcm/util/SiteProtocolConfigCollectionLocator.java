package gov.cancer.wcm.util;

import com.percussion.services.PSBaseServiceLocator;

/**
 */
public class SiteProtocolConfigCollectionLocator extends PSBaseServiceLocator
{
	   /**
	    * Private constructor 
	    */
	   private SiteProtocolConfigCollectionLocator()
	   {
		   
	      
	   }
	   
	   /**
	    * 
	    * @return instance of SiteProtocolConfigCollection
	    */
	   public static SiteProtocolConfigCollection getSiteProtocolConfigCollection(){ 
		   return (SiteProtocolConfigCollection) getBean(SITE_PROTOCOL_CONFIG_COLLECTION_BEAN_NAME);
	   }
	   
	   /**
	    * String representing name of the Service class
	    */
	   private static final String SITE_PROTOCOL_CONFIG_COLLECTION_BEAN_NAME = "SiteProtocolConfigs"; 
}