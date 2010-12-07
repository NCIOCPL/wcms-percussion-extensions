package gov.cancer.wcm.workflow;

import gov.cancer.wcm.util.CGV_TypeNames;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.util.PSItemErrorDoc;

public class ContentItemWFValidatorAndTransitioner {

	private Log log;

	private static IPSCmsContentSummaries contentSummariesService;
	private static WorkflowConfiguration workflowConfig;
		
	static {
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		workflowConfig = WorkflowConfigurationLocator.getWorkflowConfiguration();
	}
	
	public ContentItemWFValidatorAndTransitioner(Log log) {
		this.log = log;
	}
	
	/**
	 * Tests an item to see if the item should be allowed through the workflow.
	 * If it is, and dependents which should transition with the item should
	 * be transitioned through the workflow. 
	 * @param request IPSRequestContext
	 * @param errorDoc Document
	 * @throws PSException
	 */
	public void performTest(IPSRequestContext request,Document errorDoc)
		throws PSException {

		log.debug("Workflow Item Validator: Performing Test...");

		String transition = request.getParameter("sys_transitionid");
		String currCID = request.getParameter("sys_contentid");
		int id = Integer.parseInt(currCID);		
			
		//Get information about the content item this lets us get the GUID
		//and allows us to get the content Type Name
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(id);		 
		String contentTypeName = CGV_TypeNames.getTypeName(contentItemSummary.getContentTypeId());
		
		log.debug("Initiating Push for Content Item: " + id + "(" + contentTypeName +")");
		
		/*
		 * We need to check if:
		 *  A) we are either
		 *  	1. A top type - We must transition our children
		 *  	2. A shared item - We are used multiple items
		 *  	   and therefore should be transitioned separately.
		 *  	   A shared item can still have children which will
		 *         need to be transitioned.
		 *      3. A component which must be moved on its own and
		 *         does not follow any fancy rules.
		 *  B) we are a component of a page 
		 */

		//We should really check if we are updating, first publish, or unpublishing content.
		
		// Step 1. Check if this is a top type and handle appropriately
		ContentTypeConfig config = workflowConfig.getContentTypes().getContentTypeOrDefault(contentTypeName);
		
		if (config == null) {
			log.error("Recieved a null content type config when validating an item.");
			PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Error getting workflow config for item with id {0}", new Object[]{id});
		} else {
			if (config.getIsTopType()) {
				//This is a top type.
				log.debug("Content Item : " + id + ", of type: " + config.getName() +  ", is a top type.");
				pushTopType(request, errorDoc, contentItemSummary);
			} else {
				//This is not a top type				
				
			}
		}
		
		// Step 2. 
		
		PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Error getting related ids for item with id {0}", new Object[]{id});
		throw new PSException("Stopping");
	}
	
	/**
	 * Validates a top type and transitions its children through the workflow 
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
	private void pushTopType(IPSRequestContext request,Document errorDoc, PSComponentSummary contentItemSummary) {
		
	}
	
	/**
	 * Checks to see if a content item is shared.  Share being defined
	 * as "if the item were to move to the public state, then the item 
	 * would be a dependent in more than one follow relationship"  
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
	private void isShared(IPSRequestContext request, Document errorDoc, PSComponentSummary contentItemSummary) {
		// find how many "follow" relationships the item is a dependent in.
	}

	
	private static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.WorkflowItemValidator.PSExclusionFlag";
	private static final String ERR_FIELD = "TransitionValidation";
	private static final String ERR_FIELD_DISP = "TransitionValidation";	
}
