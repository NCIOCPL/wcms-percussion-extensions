package gov.cancer.wcm.extensions;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.pso.validation.PSOItemXMLSupport;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.cms.objectstore.PSComponentSummary;

import gov.cancer.wcm.util.CGV_TypeNames;
import gov.cancer.wcm.images.*;


public class CGV_ImageItemValidator extends PSOAbstractItemValidationExit {
	private static Log log = LogFactory.getLog(CGV_ImageItemValidator.class);
	private static ImageValidationConfiguration validatorConfig = null;
	private static IPSCmsContentSummaries contentSummariesService;
		
	/*
	 * This initializes some of the different services
	 */
	static {
		validatorConfig = ImageValidationConfigurationLocator.getImageValidationConfiguration();
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
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
	    String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
	    Validate.notEmpty(contentid);
	    String transitionid = req.getParameter(IPSHtmlParameters.SYS_TRANSITIONID);
	    Validate.notEmpty(transitionid);
	    String states = params[0].toString();
	    
	    contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();

	    int id = Integer.parseInt(contentid);		

	    //Get information about the content item this lets us get the GUID
	    //and allows us to get the content Type Name
	    PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(id);

	    long contentTypeID = contentItemSummary.getContentTypeId();
	    String contentTypeName;

	    try {
	    	contentTypeName = CGV_TypeNames.getTypeName(contentTypeID);
	    } catch (Exception ex) {		
	    	String message = String.format("Failed to retrieve type name for contentTypeID = %d.", contentTypeID);
	    	throw new Exception();
	    }
	    
	    ArrayList<ImageValidationError> validationErrors = new ArrayList<ImageValidationError>();
	    if(validatorConfig != null) {
	    	if(validatorConfig.hasImageCTValidator(contentTypeName)) {
	    		ImageCTValidator imgCTValidator = validatorConfig.getImageCTValidator(contentTypeName);
	    		
	    		ArrayList<String> fieldsToValidate = imgCTValidator.getFieldsToValidate();
	    		
	    		if(!fieldsToValidate.isEmpty()) {
	    			log.debug("Fields to validate from " + imgCTValidator.getContentTypeName() + "CT validator: ");
	    			for(String field : fieldsToValidate) {
	    				log.debug(field);
	    			}
	    			
	    			HashMap<String, String> imageData = getFieldValuesFromDocument(fieldsToValidate, inputDoc, imgCTValidator);
	    			
	    			if(!imageData.isEmpty()) {
	    				validationErrors = imgCTValidator.validateItems(imageData);
	    				
	    				for(ImageValidationError err : validationErrors) {
	    					log.debug(err.getFieldName() + ": " + err.getErrorMessage());
	    				}
	    				
	    				/*
	    				ArrayList<ImageValidationError> errors = new ArrayList<ImageValidationError>();
	    				ArrayList<String> constraintFields = imgCTValidator.getConstraintFields();
	    				
	    				for(String cons : constraintFields) {
	    					log.debug("Constraint field: " + constr);
	    				}
	    				
	    				for(ImageValidationError err : validationErrors) {
	    					for(String constraint : constraintFields) {
	    						log.debug("Checking if " + err.getFieldName() + " contains " + constraint);
	    						if(err.getFieldName().contains(constraint)) {
	    							String errorFieldName = err.getFieldName().replace(constraint, "");
	    							log.debug("Replacing error for " + err.getFieldName() + " with " + errorFieldName);
	    							errors.add(new ImageValidationError(errorFieldName, err.getErrorMessage()));
	    						}
	    					}
	    				}*/
	    			}
	    			
	    		}
	    	}
	    	else {
	    		log.debug("Unable to find ImageCTValidator for: " + contentTypeName);
	    	}
	    }
	    else {
	    	log.debug("Error getting ImageItemValidator config.");
	    }
	    
	    // Get fields to validate from CTValidator
	    //ArrayList<String> validationFields = CTValidator.getFieldsToValidate();
	    
	    // Get field values to validate for each field from item
	    //HashMap<String, String> fieldValues = getFieldValuesFromDocument(imageTypes, imageFields, inputDoc);
	    
	    if(super.matchDestinationState(contentid, transitionid, states))
    	{
	    	log.debug("ImageItemValidator - Testing if transition of item is allowed, valid state for test");
    	}
	    else {
	    	log.debug("ImageItemValidator - Exclusion flag detected");
	    }
	}
	
	private HashMap<String, String> getFieldValuesFromDocument(ArrayList<String> imageFields, Document inputDoc, ImageCTValidator ctValidator)
	{
		HashMap<String, String> fieldValues = new HashMap<String, String>();
		
		for(String field : imageFields) {
			Element fieldElem = super.getFieldElement(inputDoc, field);
			if(fieldElem != null) {
				String fieldVal = super.getFieldValue(fieldElem);
				if(fieldVal != null) {
					log.debug("Adding [" + field + ", " + fieldVal + "] to fieldValues");
					if(!fieldValues.containsKey(field)) {
						fieldValues.put(field, super.getFieldValue(fieldElem));
					}
				}
				else {
					log.debug("Adding [" + field + ", null] to fieldValues");
					if(!fieldValues.containsKey(field)) {
						fieldValues.put(field, null);
					}
				}
			}
		}
		
		return fieldValues;
	}
}
