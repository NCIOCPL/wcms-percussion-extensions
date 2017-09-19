package gov.cancer.wcm.extensions;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import gov.cancer.wcm.images.ImageValidationConfiguration;
import gov.cancer.wcm.images.ImageValidationConfigurationLocator;

public class CGV_ImageItemValidator extends PSOAbstractItemValidationExit {
	private static Log log = LogFactory.getLog(CGV_ImageItemValidator.class);
	private static ImageValidationConfiguration validatorConfig = null;
		
	/*
	 * This initializes some of the different services
	 */
	static {
		validatorConfig = ImageValidationConfigurationLocator.getImageValidationConfiguration();
	}
	
	/**
	 * Initializes a new instance of the CGV_ImageItemValidator
	 */
	public CGV_ImageItemValidator()
	{
	   super();	   
	}
	   
	/**
	 * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#validateDocs(org.w3c.dom.Document, org.w3c.dom.Document, com.percussion.server.IPSRequestContext, java.lang.Object[])
	 */
	@Override
	protected void validateDocs(Document inputDoc, Document errorDoc,
	      IPSRequestContext req, Object[] params) throws Exception
	{
		log.debug("In validateDocs for ImageItemValidator");
		if(validatorConfig != null){
			log.debug("Successfully loaded Validator Config");
		}
		else {
			log.debug("Validator Config loading broken");
		}
		
	    String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
	    Validate.notEmpty(contentid);
	    String transitionid = req.getParameter(IPSHtmlParameters.SYS_TRANSITIONID);
	    Validate.notEmpty(transitionid);
	    String states = params[0].toString();
	    
	    if(super.matchDestinationState(contentid, transitionid, states))
    	{
	    	log.debug("ImageItemValidator - Testing if transition of item is allowed, valid state for test");
    	}
	    else {
	    	log.debug("ImageItemValidator - Exclusion flag detected");
	    }
	}
}
