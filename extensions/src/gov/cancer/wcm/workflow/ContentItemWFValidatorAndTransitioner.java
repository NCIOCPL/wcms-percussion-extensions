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
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * Defines a collection of functions to validate and transition content items
 * in the workflow.  This contains references to the different system services
 * for determining this information.  Since many of these functions would be
 * spread across other classes this is the single place to look.
 * @author bpizzillo
 *
 */
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
		
		//TODO: Error if summary is null.
		
		//TODO: Get transition information.
		
		//Setup the context for validation.
		WorkflowValidationContext wvc = new WorkflowValidationContext(contentItemSummary, log, errorDoc);
				
		//log.debug("Initiating Push for Content Item: " + id + "(" + contentTypeName +")");
		
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
		
		try {
			if (ContentItemWFValidatorAndTransitioner.isTopType(contentItemSummary.getContentTypeId(), wvc)) {
				log.debug("Content Item : " + id + ", is a top type.");
				pushTopType(contentItemSummary, wvc);
			} else {
				//This is not a top type, so it is either shared, and item of its own, or 
				//a component on a page.
				//If component, find top type and push.
				//if no parents, pushContentItem()
				//if shared, pushContentItem()
				
			}
		} catch (WFValidationException validationEx) {
			if (!validationEx.hasBeenLogged())
				log.error("Error Occured while Validating", validationEx);
			//Add Generic error, since the user cannot do anything to fix this.
			PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "System Error Occured.  Please consult the logs.", null);
		}
		
		int[] itemsToTransition = wvc.getItemsToTransition();
		wvc.getLog().debug("Items to transition: " + itemsToTransition.length);
		
		PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Stopping For Testing", null);
		//throw new PSException("STOPPING");
		
	}
	
	/**
	 * Validates a top type and transitions its children through the workflow 
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
	private void pushTopType(
			PSComponentSummary contentItemSummary,
			WorkflowValidationContext wvc			
			) 
		throws WFValidationException
	{
		
		//Validate Dependents
		validateChildRelationships(contentItemSummary, wvc);
	}
	
	/**
	 * Validates a content item and transitions its children through the workflow 
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
	private void pushContentItem(
			PSComponentSummary contentItemSummary,
			WorkflowValidationContext wvc			
			) 
		throws WFValidationException
	{
		//This should replace the push top type above.
		
		//Validate Dependents
		validateChildRelationships(contentItemSummary, wvc);				
	}
	
	/*
	 * Below are class methods for use by the various checks and stop conditions so that
	 * the code is in one place.
	 */

	
	/**
	 * Validates dependents participating in Active Assembly (category) relationships based 
	 * on rules defined in the RelationshipWFTransitionConfig items.
	 * (This may be called recursively)
	 * (Should return items which need to be transitioned??)
	 * @param contentItemSummary
	 */
	public static RelationshipWFTransitionCheckResult validateChildRelationships(PSComponentSummary contentItemSummary, WorkflowValidationContext wvc)
		throws WFValidationException
	{
		List<PSRelationship> rels = new ArrayList<PSRelationship>();
		
		wvc.getLog().debug("Finding relationships for Content ID: " + contentItemSummary.getContentId());
		
		PSRelationshipFilter filter = new PSRelationshipFilter();
		//This is going to be the current/edit revision for this content item.
		filter.setOwner(contentItemSummary.getHeadLocator());
		filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		
		try {
			rels = systemWebService.loadRelationships(filter);
		} catch (Exception ex) {
			wvc.getLog().error("Could not get relationships for Content ID:" + contentItemSummary.getContentId(), ex);
			throw new WFValidationException("Could not get relationships for Content ID:" + contentItemSummary.getContentId(), ex, true);
		}
		
		wvc.getLog().debug("Found " + rels.size() + " relationships for Content ID: " + contentItemSummary.getContentId());
				
		//Loop through relationships and validate.  If a relationship is a follow relationship,
		//then the content ids which should be transition will be added to the WorkflowValidationContext
		//for later use.
		for (PSRelationship rel:rels) {
			String relName = rel.getConfig().getName();
			wvc.getLog().debug("Found " + relName + " relationship for Content ID: " + contentItemSummary.getContentId());
						
			//Check config for relationship with that name. (Or get default)
			BaseRelationshipWFTransitionCheck transitionCheck = workflowConfig.getRelationshipConfigs().GetRelationshipWFTransitionConfigOrDefault(relName);

			RelationshipWFTransitionCheckResult result = transitionCheck.validate(contentItemSummary, rel, wvc);
			if (result == RelationshipWFTransitionCheckResult.StopTransition) {
				
				//We found an issue while validating children so we should stop checking.
				//TODO: Determine if we should continue validating on error just to get all error messages
				//
				return RelationshipWFTransitionCheckResult.StopTransition;
			}
		}
		
		//If we have gotten here then everything is ok.
		return RelationshipWFTransitionCheckResult.ContinueTransition;
	}
		
	/**
	 * Determines if an item is participating in more than one Active Assembly relationship
	 * as a dependent.
	 * @param contentItemLocator
	 * @param wvc
	 * @return
	 * @throws PSErrorException
	 */
	public static boolean isShared(PSLocator contentItemLocator, WorkflowValidationContext wvc)
		throws WFValidationException
	{
		List<PSRelationship> rels = new ArrayList<PSRelationship>();
				
		PSRelationshipFilter filter = new PSRelationshipFilter();		
		//This is going to be the current/edit revision for this content item.
		filter.setDependent(contentItemLocator);		
		filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		
		try {
			rels = systemWebService.loadRelationships(filter);
		} catch (Exception ex) {
			wvc.getLog().error("isShared: Could not get content type name for id: " + contentItemLocator.getId(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);			
		}
		
		//Count the number of follow relationships to see if it is shared.  More than one follow
		//means the item is shared.
		int followRelCount = 0;		
		for(PSRelationship rel: rels) {
			String relName = rel.getConfig().getName();
			BaseRelationshipWFTransitionCheck transitionCheck = workflowConfig.getRelationshipConfigs().GetRelationshipWFTransitionConfigOrDefault(relName);
			if (transitionCheck.getTransitionType() == RelationshipWFTransitionTypes.Follow)
				followRelCount++;
		}
		
		return (followRelCount > 1);
	}
	
	/**
	 * Checks if an item has a public revision.  (I.E. Check that there is a version on
	 * the live site)
	 * @param contentItemLocator
	 * @param wvc
	 * @return
	 */
	//TODO: Update this to hasPublicRevision or is in a workflow state greater than the one we are transitioning to.	
	public static boolean hasPublicRevision(PSLocator contentItemLocator, WorkflowValidationContext wvc) {
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(contentItemLocator.getId());

		if (contentItemSummary == null) {
			wvc.getLog().error("hasPublicRevision: Could not get contentItemSummary from Locator for item: " + contentItemLocator.getId());
			throw new WFValidationException("Could not get contentItemSummary for content item: " + contentItemLocator.getId(), true);
		}
		
		return contentItemSummary.getPublicRevision() != -1;
	}	
	
	/**
	 * Checks to see if the contentTypeID that is passed in is a top type.
	 * @param contentTypeID
	 * @param wvc
	 * @return
	 */
	public static boolean isTopType(long contentTypeID, WorkflowValidationContext wvc) 
		throws WFValidationException
	{
		
		String contentTypeName = null;
		
		try {
			contentTypeName = CGV_TypeNames.getTypeName(contentTypeID);
		} catch (Exception ex) {
			wvc.getLog().error("isTopType: Could not get content type name for id: " + contentTypeID, ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);
		}
		
		ContentTypeConfig config = workflowConfig.getContentTypes().getContentTypeOrDefault(contentTypeName);
		
		if (config == null) {
			wvc.getLog().error("isTopType: Recieved a null content type config when validating an item.");
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);			
		}
		
		return config.getIsTopType();		
	}
	
	/**
	 * Checks to see if a content item is checked out to a user other than the one that is initiating the
	 * workflow transition.
	 * @param contentItemLocator
	 * @param wvc
	 * @return returns the name of the other user, otherwise returns null.
	 */
	public static String isCheckedOutToOtherUser(PSComponentSummary contentItemSummary, WorkflowValidationContext wvc) {
		String userName = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
		String checkedOutUser = contentItemSummary.getCheckoutUserName();
		
		if (checkedOutUser!=null && checkedOutUser.length()>0 && !checkedOutUser.equals(userName)) {
			return checkedOutUser;
		} else {
			return null;
		}
	}
	
	/**
	 * Checks to see if parent navons are required to be public and if so checks to make sure.
	 * @param contentItemSummary
	 * @param wvc
	 * @return
	 * @throws WFValidationException
	 */
	public static boolean isNavonRequiredOrPublic(
			PSComponentSummary contentItemSummary, 
			WorkflowValidationContext wvc)
		throws WFValidationException
	{
		String contentTypeName = null; 
		try {
			contentTypeName = CGV_TypeNames.getTypeName(contentItemSummary.getContentTypeId());
		} catch (Exception ex) {
			wvc.getLog().error("isNavonRequiredOrPublic: Could not get content type name for id: " + contentItemSummary.getContentTypeId(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);
		}
		
		ContentTypeConfig config = workflowConfig.getContentTypes().getContentTypeOrDefault(contentTypeName);
		
		if (config == null) {
			wvc.getLog().error("isNavonRequiredOrPublic: Recieved a null content type config when validating an item.");
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);			
		}

		//Check Navon
		if (config.getRequiresParentNavonsPublic() && ContentItemWFValidatorAndTransitioner.areParentNavonsPublic(contentItemSummary) == false) {
			//Error Out Because public navons are required but they are not public.
			wvc.getLog().debug("Parent Navons are not Public for content item: " + contentItemSummary.getContentId());			
			return false;
		}
		
		return true;
	}

	/**
	 * Checks to see if all Parent/Ancestor navons have a public revision.
	 * @return
	 */
	private static boolean areParentNavonsPublic(PSComponentSummary contentItemSummary) {
		//TODO:Implement areParentNavonsPublic
		//getParentFolderRelationships()
		//wvc.addError(ERR_FIELD, ERR_FIELD_DISP, NAVON_NOT_PUBLIC, args)
		
		return false;
	}

	/**
	 * Helper method to get a PSComponentSummary by contentID.  Classes can call
	 * this and not need a reference to the summaryService.
	 * @param contentId
	 * @return
	 */
	public static PSComponentSummary getSummaryFromId(int contentId) {
		return contentSummariesService.loadComponentSummary(contentId);
	}
	
	/**
	 * Checks to see if a content item is shared.  Share being defined
	 * as "if the item were to move to the public state, then the item 
	 * would be a dependent in more than one follow relationship"  
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
//	private void isShared(IPSRequestContext request, Document errorDoc, PSComponentSummary contentItemSummary) {
//		// find how many "follow" relationships the item is a dependent in.
//	}

	//Below are the formatters for messages
	public static final String ARCHIVE_SHARED = "The content item {System Title} is shared so it cannot be archived.";	
	public static final String NO_PATH_TO_DEST = "Could not promote content item{System Title} to {Destination State} because its child item {System Title } cannot be promoted to {Destination State}.";
	public static final String SHARED_ITEM_PAST_LOWEST = "Could not promote content item{System Title} because its child item {System Title } is being edited.";
	public static final String NAVON_NOT_PUBLIC = "The navon with id {xx} must be promoted to Public before content item {System Title} can be promoted.";
	public static final String ARCHIVE_PARENT_NOT_MOVING = "Could not archive the content item {System Title} because its parent item {System Title} has not been archived.";

	public static final String CHILD_IS_CHECKED_OUT = "Could not promote item {0} because its child item {1} is checked out to {2}.";
	public static final String NON_PUBLIC_CHILD_IS_OTHER_COMMUNITY = "Could not promote item {0} because its child item {1} is in another community and not public.";
	public static final String NON_PUBLIC_CHILD_IS_TOP_TYPE = "Could not promote item {0} because its child item {1} is another page and not public.";
	public static final String NON_PUBLIC_CHILD_IS_SHARED = "Could not promote item {0} because its child item {1} is shared and not public.";
	
	public static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.WorkflowItemValidator.PSExclusionFlag";
	public static final String ERR_FIELD = "TransitionValidation";
	public static final String ERR_FIELD_DISP = "TransitionValidation";	
}
