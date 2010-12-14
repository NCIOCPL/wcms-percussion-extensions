package gov.cancer.wcm.workflow;

import java.util.ArrayList;
import java.util.List;

import gov.cancer.wcm.util.CGV_TypeNames;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

public class ContentItemWFValidatorAndTransitioner {

	private Log log;

	private static IPSCmsContentSummaries contentSummariesService;
	private static WorkflowConfiguration workflowConfig;
	private static IPSSystemWs systemWebService;
	
	static {
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		workflowConfig = WorkflowConfigurationLocator.getWorkflowConfiguration();
		systemWebService = PSSystemWsLocator.getSystemWebservice();
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
		throws PSException, PSErrorException {

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
				pushTopType(request, errorDoc, contentItemSummary, config);
			} else {
				//This is not a top type				
				
			}
		}
		
		// Step 2. 
		
		PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Error getting related ids for item with id {0}", new Object[]{id});
		
	}
	
	/**
	 * Validates a top type and transitions its children through the workflow 
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
	private void pushTopType(IPSRequestContext request,Document errorDoc, PSComponentSummary contentItemSummary, ContentTypeConfig config) 
		throws PSErrorException
	{

		//Check Navon
		if (config.getRequiresParentNavonsPublic() && areParentNavonsPublic(contentItemSummary) == false) {
			//Error Out Because public navons are required but they are not public.
			log.debug("Parent Navons are not Public for content item: " + contentItemSummary.getContentId());
		}
		
		//Validate Dependents
		validateChildRelationships(contentItemSummary);
				
	}
	
	/**
	 * Validates dependents participating in Active Assembly (category) relationships based 
	 * on rules defined in the RelationshipWFTransitionConfig items.
	 * (This may be called recursively)
	 * (Should return items which need to be transitioned??)
	 * @param contentItemSummary
	 */
	private void validateChildRelationships(PSComponentSummary contentItemSummary)
		throws PSErrorException
	{
		List<PSRelationship> rels = new ArrayList<PSRelationship>();
		
		log.debug("Finding relationships for Content ID: " + contentItemSummary.getContentId());
		
		PSRelationshipFilter filter = new PSRelationshipFilter();
		//This is going to be the current/edit revision for this content item.
		filter.setOwner(contentItemSummary.getHeadLocator());
		filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		rels = systemWebService.loadRelationships(filter);
		
		log.debug("Found " + rels.size() + " relationships for Content ID: " + contentItemSummary.getContentId());
		
		//Setup the context for validation.  This should probably be done higher up.
		WorkflowValidationContext wvc = new WorkflowValidationContext(log);
		
		for (PSRelationship rel:rels) {
			String relName = rel.getConfig().getName();
			log.debug("Found " + relName + " relationship for Content ID: " + contentItemSummary.getContentId());
			
			
			//Check config for relationship with that name. (Or get default)
			BaseRelationshipWFTransitionCheck transitionCheck = workflowConfig.getRelationshipConfigs().GetRelationshipWFTransitionConfigOrDefault(relName);

			RelationshipWFTransitionCheckResult result = transitionCheck.validate(contentItemSummary, rel, wvc);
			if (result == RelationshipWFTransitionCheckResult.StopTransition) {
				//Stop Processing. Maybe do something else.  The original code that PSO gave us
				//attempted to find all of the issues and show a list all at once.  That would
				//exclude us from breaking here.
				break;
			}
			
			//THIS CODE HAS NOW BEEN REFACTORED.  REMOVE THE COMMENT AND CLEAN UP
			//So yeah, this should get moved into the bean? at some point in time since it is silly to have this 
			//conditional here.  However it is ok for initial development while we figure out what info we are 
			//going to need.  
			//For example: is there a follow stop condition for, RelationshipCheckOk, then does that
			//need to return a list of dependents that need to be transitioned, and a list of errors??  Does everyone
			//need to return these?
			
			//Furthermore, is it the config that does all of the checking?  Anyway, that is just an idea for now, lets
			//get this thing working first.
			
			//TODO: Refactor this code into RelationshipWFTransitionConfigs
			
		//////if (config instanceof RelationshipWFTransitionFollowCheck) {
		//////				//If follow, then check all stop conditions.				
		//////				for(RelationshipWFTransitionStopConditions condition : ((RelationshipWFTransitionFollowCheck)config).getStopConditions()) {
		//////log.debug("Handling follow Config for dependent: " + rel.getDependent().getId());					
		//////	switch(condition) {
		//////		case Shared : {
		//////			break;
		//////		}
		//////		case OtherCommunity: {
		//////			break;
		//////		}
		//////		case OtherWorkflow: {
		//////			break;							
		//////		}
		//////		case OtherUserCheckedOut: {
		//////			break;
		//////		}
		//////		case TopType: {
		//////			break;
		//////		}
		//////	}
		//////}
		//////} else if (config instanceof RelationshipWFTransitionPublicRevisionCheck) {
				//If stop, then check if there is a public revision
		//////log.debug("Handling Stop Config for dependent: " + rel.getDependent().getId());
		//////} else if (config instanceof RelationshipWFTransitionIgnoreCheck) {
				//If ignore, then ignore,
				
		//////} else {
				//Should never happen, only when a new type has been added and this is not updated. Default to stop.
		//////log.error("validateChildRelationships: Unknown BaseRelationshipWFTransitionConfig: " + config.getClass().getName());
		//////log.debug("Handling Stop Config for dependent: " + rel.getDependent().getId());
		//////}
			
		}
	}
	
	/**
	 * Determines if an item is participating in more than one Active Assembly relationship
	 * as a dependent.
	 * @param contentItemLocator
	 * @return
	 * @throws PSErrorException
	 */
	private boolean isShared(PSLocator contentItemLocator)
		throws PSErrorException
	{
		List<PSRelationship> rels = new ArrayList<PSRelationship>();
		
		PSRelationshipFilter filter = new PSRelationshipFilter();		
		//This is going to be the current/edit revision for this content item.
		filter.setDependent(contentItemLocator);		
		filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		rels = systemWebService.loadRelationships(filter);
		
		return (rels.size() > 1);
	}
		
	
	
	/**
	 * Checks to see if all Parent/Ancestor navons have a public revision.
	 * @return
	 */
	private boolean areParentNavonsPublic(PSComponentSummary contentItemSummary) {
		//TODO:Implement areParentNavonsPublic
		//getParentFolderRelationships()
		return false;
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
