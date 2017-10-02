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
import com.percussion.util.PSItemErrorDoc;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.services.workflow.data.PSState;

import gov.cancer.wcm.util.CGV_TypeNames;
import gov.cancer.wcm.images.*;


public class CGV_ImageItemValidator extends PSOAbstractItemValidationExit {
	private static Log log = LogFactory.getLog(CGV_ImageItemValidator.class);
	private static ImageValidationConfiguration validatorConfig = null;
	private static IPSCmsContentSummaries contentSummariesService;
	private static IPSOWorkflowInfoFinder finder = null;	
	
	/*
	 * This initializes some of the different services
	 */
	static {
		validatorConfig = ImageValidationConfigurationLocator.getImageValidationConfiguration();
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		finder = new PSOWorkflowInfoFinder();
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
	    
	    // If transitioning to Staging (D), validate the image size according to constraints.
	    if(super.matchDestinationState(contentid, transitionid, states))
    	{
	    	ArrayList<ImageValidationError> validationErrors = new ArrayList<ImageValidationError>();
		    
	    	if(validatorConfig != null) {
		    	if(validatorConfig.hasImageCTValidator(contentTypeName)) {
		    		// Use CT Validator for specified image content type
		    		ImageCTValidator imgCTValidator = validatorConfig.getImageCTValidator(contentTypeName);
		    		
		    		// Get fields to validate that are specifically for this image content type
		    		ArrayList<String> fieldsToValidate = imgCTValidator.getFieldsToValidate();
		    		
		    		if(!fieldsToValidate.isEmpty()) {
		    			log.debug("Fields to validate from " + imgCTValidator.getContentTypeName() + "CT validator: ");
		    			for(String field : fieldsToValidate) {
		    				log.debug(field);
		    			}
		    			
		    			// Get values of fields on image content item
		    			HashMap<String, String> imageData = getFieldValuesFromDocument(fieldsToValidate, inputDoc, imgCTValidator);
		    			
		    			if(!imageData.isEmpty()) {
		    				// Validate all data from the image content item
		    				validationErrors = imgCTValidator.validateItems(imageData);
		    				
		    				for(ImageValidationError err : validationErrors) {
		    					log.debug(err.getFieldName() + ": " + err.getErrorMessage());
		    				}
		    				
		    				if(!validationErrors.isEmpty()) {
		    					for(ImageValidationError err : validationErrors) {
		    						// Add each validation error to display table on error message in Content Explorer
		    						Element field = super.getFieldElement(inputDoc, err.getFieldName());
		    						String label = super.getFieldLabel(field);
		    						PSItemErrorDoc.addError(errorDoc, err.getFieldName(), label, err.getErrorMessage(), new Object[]{field});
		    						continue;
		    					}
		    				}
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
	    	
	    	log.debug("ImageItemValidator - Correct destination state");
    	}
	    else {
	    	log.debug("ImageItemValidator - Incorrect destination state - must be Staging (D) or Staging (P)");
	    }
	}
	
	private HashMap<String, String> getFieldValuesFromDocument(ArrayList<String> imageFields, Document inputDoc, ImageCTValidator ctValidator)
	{
		HashMap<String, String> fieldValues = new HashMap<String, String>();
		
		for(String field : imageFields) {
			// Get element for each individual field specified by the image CT validator
			Element fieldElem = super.getFieldElement(inputDoc, field);
			if(fieldElem != null) {
				String fieldVal = super.getFieldValue(fieldElem);
				if(fieldVal != null) {
					log.debug("Adding [" + field + ", " + fieldVal + "] to field values from document");
					if(!fieldValues.containsKey(field)) {
						// If field hasn't been added yet, add field and value to map
						fieldValues.put(field, super.getFieldValue(fieldElem));
					}
				}
				else {
					log.debug("Adding [" + field + ", null] to field values from document");
					if(!fieldValues.containsKey(field)) {
						// If field hasn't been added yet, add field and null to map
						fieldValues.put(field, null);
					}
				}
			}
		}
		
		return fieldValues;
	}
}
