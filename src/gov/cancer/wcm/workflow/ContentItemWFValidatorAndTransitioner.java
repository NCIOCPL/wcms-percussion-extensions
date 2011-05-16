package gov.cancer.wcm.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.cancer.wcm.util.CGV_TypeNames;
import gov.cancer.wcm.workflow.checks.BaseRelationshipWFTransitionCheck;
import gov.cancer.wcm.workflow.checks.RelationshipWFTransitionCheckResult;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
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
	private static IPSWorkflowService workflowService;
	private static PSOWorkflowInfoFinder workInfoFinder;
	protected static IPSGuidManager gmgr = null;
	protected static IPSContentWs cmgr = null;

	static {
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		workflowConfig = WorkflowConfigurationLocator.getWorkflowConfiguration();
		systemWebService = PSSystemWsLocator.getSystemWebservice();
		workflowService = PSWorkflowServiceLocator.getWorkflowService();
		workInfoFinder = new PSOWorkflowInfoFinder();
		gmgr = PSGuidManagerLocator.getGuidMgr();
		cmgr = PSContentWsLocator.getContentWebservice();
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

		log.debug("performTest: Workflow Item Validator: Performing Test...");

		String transition = request.getParameter("sys_transitionid");
		int transitionID = Integer.parseInt(transition);

		String currCID = request.getParameter("sys_contentid");
		int id = Integer.parseInt(currCID);		

		//Get information about the content item this lets us get the GUID
		//and allows us to get the content Type Name
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(id);					

		//TODO: Error if summary is null.

		//TODO: Get transition information.
		log.debug("performTest: Getting Workflow Info");
		int wfState = contentItemSummary.getContentStateId();
		int wfId = contentItemSummary.getWorkflowAppId();		   

		PSWorkflow workflow = workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, wfId));
		PSState state = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfState),
				new PSGuid(PSTypeEnum.WORKFLOW,wfId));	   

		//Check to see if the workflow code is going to be ignored (configurable list of triggers/workflows to ignore)

		//Get the configuration bean.
		WorkflowConfiguration config = WorkflowConfigurationLocator.getWorkflowConfiguration();
		ValidatorIgnoreConfig ignoreCheck = config.getValidatorIgnore();

		for( String currWkflw : ignoreCheck.getIgnoreWorkflows() ){
			//If the current workflow is in the list of ignored workflows, return.
			if(workflow.getName().equalsIgnoreCase(currWkflw)){
				return;
			}
		}

		//Find the trigger name.
		String triggerName = null;
		for (PSTransition tran : state.getTransitions()){
			if (tran.getGUID().getUUID() == transitionID){
				triggerName = tran.getTrigger();
			}
		}
		if(triggerName != null){
			//If the trigger name is in the list of ignored triggers, return.
			if(ignoreCheck.getIgnoreTriggers().contains(triggerName)){
				return;
			}
		}

		//Setup the context for validation.
		WorkflowValidationContext wvc = new WorkflowValidationContext(request, contentItemSummary, log, errorDoc, workflow, state, transitionID);

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

		//The following holds true for non-archiving transitions.
		PSComponentSummary pushRoot = null;
		RelationshipWFTransitionCheckResult result = RelationshipWFTransitionCheckResult.StopTransition;
		try {
			pushRoot = getTransitionRoot(contentItemSummary, wvc);
			//Perform one archive test before validating.
			if(!archiveSharedCheck(pushRoot, wvc)){
				//Cannot archive, error.
				log.error("Error Occured while Validating for Archiving");
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.SHARED_ARCHIVE,
						new Object[]{pushRoot.getContentId()});
			}
			else{
				if(validate(pushRoot, wvc)){
					result = pushContentItem(pushRoot, wvc);
				}
			}
		} catch (WFValidationException validationEx) {
			if (!validationEx.hasBeenLogged())
				log.error("Error Occured while Validating", validationEx);
			//Add Generic error, since the user cannot do anything to fix this.
			PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "System Error Occured.  Please consult the logs.", null);
		}


		//If there was not an error, transition?
		//else, error
		if(result == RelationshipWFTransitionCheckResult.ContinueTransition){

			//---------------------------------------------
			if(pushRoot != null){
				//If root == initial transitioned item: remove it from the list, and add the root.
				//It is already going to transition (that is what runs the code)
				if(!pushRoot.equals(contentItemSummary)){
					wvc.removeItemToTransition(contentItemSummary);
					wvc.addItemToTransition(pushRoot);
				}
			}
			//---------------------------------------------

			PSComponentSummary[] itemsToTransition = wvc.getItemsToTransition();
			wvc.getLog().debug("Items to transition: " + itemsToTransition.length);
			
			//if any of the items are checked out to the same user, check them in
			checkin( Arrays.asList(itemsToTransition), wvc);

			for(PSComponentSummary item : itemsToTransition){
				/**
				 * Transition Items
				 * --------------------------------------------------------------------------
				 * PRECURSOR: Check if the item is in an ignore workflow.
				 * 1.  Find the state item is in.
				 * 2.  From the config, get the list of transition mappings/triggers.
				 * 3.  Compare original context destination, to current state of item.
				 * 4.  Find that mapping (item current --> context destination). Store in a list.
				 * 5.  For all triggers in the list from Step 4, PercussionTransition(item). 
				 * --------------------------------------------------------------------------
				 */

				//PRECURSOR:  Check if the item is in an ignore workflow
				int itemState = item.getContentStateId();
				int itemWFId = item.getWorkflowAppId();		   

				PSWorkflow itemWorkflow = workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, itemWFId));
				PSState iState = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, itemState),
						new PSGuid(PSTypeEnum.WORKFLOW,itemWFId));	   

				//Check to see if the workflow code is going to be ignored (configurable list of triggers/workflows to ignore)

				boolean ignore = false;
				for( String currWkflw : ignoreCheck.getIgnoreWorkflows() ){
					//If the current workflow is in the list of ignored workflows, return.
					if(itemWorkflow.getName().equalsIgnoreCase(currWkflw) && !ignore){
						ignore = true;
					}
				}

				if(!ignore){
					//1. Find the state item is in.
					PSState itemStartState = null;
					try {
						itemStartState = workInfoFinder.findWorkflowState(Integer.toString(item.getContentId()));
					} catch (PSException e) {
						e.printStackTrace();
						//TODO: Log the error.
					}
					if(itemStartState != null ){
						//2, 3, and 4.
						List<PSTransition> transitionList = wvc.getTransitions(itemStartState.getName());

						//5. For all triggers in the list from Step 4, PercussionTransition(item).
						//					IPSGuid guid = guidManager.makeGuid(item.getCurrentLocator());
						for( PSTransition t :transitionList ){
							//						List<IPSGuid> temp = Collections.<IPSGuid>singletonList(guid);
							//try {
							wvc.getLog().debug("Transitioning item with content id: "+item.getContentId()+
									", Trigger Name: "+t.getTrigger());
							
							PercussionWFTransition.transitionItem(item.getContentId(), t.getTrigger(), "", null);
							//systemWebService.transitionItems(temp, t.getTrigger());

							//						} catch (PSErrorsException e) {
							//							// TODO Auto-generated catch block
							//							e.printStackTrace();
							//						}
						}
					}
				}
			}
		}
		else{
			wvc.getLog().debug("There was an error while trying to transition dependents.  See log for more info.");
			//throw new PSException("There was an error while trying to transition dependents.  See log for more info.");
		}


		//PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Stopping For Testing", null);
		//throw new PSException("STOPPING");

	}
	
	private void checkin(List<PSComponentSummary> items, WorkflowValidationContext wvc){
		List<IPSGuid> checkinList = new ArrayList<IPSGuid>();
		for(PSComponentSummary item : items){
			//isCheckedOutToOtherUser returns null if the user is the same as the checked out user
			if(isCheckedOutToOtherUser(item, wvc) == null){
				checkinList.add(gmgr.makeGuid(new PSLocator(item.getContentId())));
			} 
		}
		try {
			if(!checkinList.isEmpty()){
				cmgr.checkinItems(checkinList, null);
			}
		} catch (PSErrorsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PSComponentSummary getTransitionRoot(PSComponentSummary contentItemSummary, WorkflowValidationContext wvc) {

		wvc.getLog().debug("getTransitionRoot: Checking for root for content item : " + contentItemSummary.getContentId());

		//Check if it is a top type
		if (ContentItemWFValidatorAndTransitioner.isTopType(contentItemSummary.getContentTypeId(), wvc)) {
			wvc.getLog().debug("getTransitionRoot: Content Item : " + contentItemSummary.getContentId() + ", is a top type.");
			return contentItemSummary;
		}

		//Find Follow Relationships

		List<PSRelationship> rels = new ArrayList<PSRelationship>();

		PSRelationshipFilter filter = new PSRelationshipFilter();		
		//This is going to be the current/edit revision for this content item.
		filter.setDependent(contentItemSummary.getHeadLocator());		
		//filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		//filter.limitToEditOrCurrentOwnerRevision(true);
		filter.setCategory("rs_activeassembly");

		try {
			rels = systemWebService.loadRelationships(filter);
		} catch (Exception ex) {
			wvc.getLog().error("getTransitionRoot: Could not get relationships for id: " + contentItemSummary.getContentId(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);			
		}

		//Count the number of follow relationships to see if it is shared.  More than one follow
		//means the item is shared.

		ArrayList<PSRelationship> followRels = new ArrayList<PSRelationship>();
		ArrayList<Integer> follows = new ArrayList<Integer>();
		ArrayList<BaseRelationshipWFTransitionCheck> followChecks = new ArrayList<BaseRelationshipWFTransitionCheck>();
		for(PSRelationship rel: rels) {
			String relName = rel.getConfig().getName();
			BaseRelationshipWFTransitionCheck transitionCheck = workflowConfig.getRelationshipConfigs().GetRelationshipWFTransitionConfigOrDefault(relName);
			if (transitionCheck.getTransitionType() == RelationshipWFTransitionTypes.Follow){
				if(!follows.contains(rel.getOwner().getId())){
					follows.add(rel.getOwner().getId());
					followRels.add(rel);
					followChecks.add(transitionCheck);
				}
			}
		}

		//If the number of rels is not 1 it is either 0, which means that this component is not shared but is transitioned
		//on its own, or it is > 1 which means it is shared.
		if (followRels.size() > 1) {
			wvc.getLog().debug("getTransitionRoot: Content Item : " + contentItemSummary.getContentId() + ", is shared.");
			return contentItemSummary;
		} else if (followRels.size() < 1) {
			wvc.getLog().debug("getTransitionRoot: Content Item : " + contentItemSummary.getContentId() + ", has no relationships.");
			return contentItemSummary;			
		}

		//Now since there is only one relationship it is the parent of this follower, so we need to get it
		//so that we can see its parent.  However, just like the rules we use to test when going down,
		//we need to follow those rules going up.
		RelationshipWFTransitionCheckResult result = null;
		if( wvc.isArchiveTransition() ){
			result = followChecks.get(0).archiveValidateUp(contentItemSummary, followRels.get(0), wvc);
		}
		else{
			result = followChecks.get(0).validateUp(contentItemSummary, followRels.get(0), wvc);
		}
		if ( result == RelationshipWFTransitionCheckResult.ContinueTransition) 
		{
			//We would be able to be pushed by our parent, so, we need to try and see if our parent
			//is the root.
			//Get the PSComponentSummary for our parent and call this method again.

			//Get the summary
			PSComponentSummary ownerSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(followRels.get(0).getOwner().getId());

			if (ownerSummary == null) {
				//Do not add PSError since that will be added for us when the WFValidationException is thrown
				wvc.getLog().error("getTransitionRoot: Could not get Component Summary for id: " + followRels.get(0).getOwner().getId());
				throw new WFValidationException("System Error Occured. Please Check the logs.", true);
			}

			wvc.getLog().debug("getTransitionRoot: Content Item : " + contentItemSummary.getContentId() + ", is not a root, continuing up.");
			return getTransitionRoot(ownerSummary, wvc);

		} else {
			//Basically, we hit an item where if we pushed the parent then the WFStopCondition would
			//be met and we could never be pushed.  This means we are the root of the transition.
			wvc.getLog().debug("getTransitionRoot: Content Item : " + contentItemSummary.getContentId() + ", should be pushed because up check failed.");
			return contentItemSummary;
		}
	}

	/**
	 * Validates a content item and transitions its children through the workflow 
	 * @param request
	 * @param errorDoc
	 * @param contentItemSummary
	 */
	private RelationshipWFTransitionCheckResult pushContentItem(
			PSComponentSummary contentItemSummary,
			WorkflowValidationContext wvc			
	) 
	throws WFValidationException
	{
		return validateChildRelationships(contentItemSummary, wvc);

		//Validate Dependents
		//RelationshipWFTransitionCheckResult result = validateChildRelationships(contentItemSummary, wvc);

		//		if(result == RelationshipWFTransitionCheckResult.ContinueTransition){
		//			//Find mapping that leads to the correct destination for the dependents.
		//		}
		//		else{
		//			//An error occurred and we stop transition.
		//		}
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

			//JOHN TODO: Change this so it uses the correct validate (archiveDown vs validateDown), based off the triggers in the
			//transition mapping.
			RelationshipWFTransitionCheckResult result = null;
			if( wvc.isArchiveTransition() ){
				result = transitionCheck.archiveValidateDown(contentItemSummary, rel, wvc);
			}
			else{
				result = transitionCheck.validateDown(contentItemSummary, rel, wvc);
			}
			if(result != null){
				if (result == RelationshipWFTransitionCheckResult.StopTransition) {

					//We found an issue while validating children so we should stop checking.
					//TODO: Determine if we should continue validating on error just to get all error messages
					return RelationshipWFTransitionCheckResult.StopTransition;
				}
			}
			else{	//result failed, stop transition
				return RelationshipWFTransitionCheckResult.StopTransition;
			}
		}

		//If we have gotten here then everything is ok.
		return RelationshipWFTransitionCheckResult.ContinueTransition;
	}

	/**
	 * Finds how many follow Active Assembly relationship the item is a part of
	 * as a dependent.
	 * @param contentItemLocator
	 * @param wvc
	 * @return the number of follow relationships the contentItemLocator is a part of.
	 * @throws PSErrorException
	 */
	public static int numberOfUniqueFollowRels(PSLocator contentItemLocator, WorkflowValidationContext wvc)
	throws WFValidationException
	{
		List<PSRelationship> rels = new ArrayList<PSRelationship>();

		PSRelationshipFilter filter = new PSRelationshipFilter();		
		//This is going to be the current/edit revision for this content item.
		filter.setDependent(contentItemLocator);		
		//filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		//filter.limitToEditOrCurrentOwnerRevision(true);
		filter.setCategory("rs_activeassembly");

		try {
			rels = systemWebService.loadRelationships(filter);
		} catch (Exception ex) {
			wvc.getLog().error("getTransitionRoot: Could not get relationships for id: " + contentItemLocator.getId(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);			
		}

		//Count the number of follow relationships to see if it is shared.  More than one follow
		//means the item is shared.
		//ArrayList<PSRelationship> followRels = new ArrayList<PSRelationship>();
		ArrayList<Integer> uniqueOwners = new ArrayList<Integer>();

		for(PSRelationship rel: rels) {
			String relName = rel.getConfig().getName();
			BaseRelationshipWFTransitionCheck transitionCheck = workflowConfig.getRelationshipConfigs().GetRelationshipWFTransitionConfigOrDefault(relName);
			if (transitionCheck.getTransitionType() == RelationshipWFTransitionTypes.Follow){
				if(!uniqueOwners.contains(rel.getOwner().getId())){
					uniqueOwners.add(rel.getOwner().getId());
				}
			}
		}

		return uniqueOwners.size();
	}

	/**
	 * Checks how many relationships exist coming into the item.  This will check
	 * ALL relationships, not just ones that are marked as follow relationships.
	 * @param contentItemLocator
	 * @param wvc
	 * @return the number of relationships the contentItemLocator is a part of.
	 * @throws PSErrorException
	 */
	public static int numberOfRelationships(PSLocator contentItemLocator, WorkflowValidationContext wvc)
	throws WFValidationException
	{
		List<PSRelationship> rels = new ArrayList<PSRelationship>();

		PSRelationshipFilter filter = new PSRelationshipFilter();		
		//This is going to be the current/edit revision for this content item.
		filter.setDependent(contentItemLocator);		
		//filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		filter.limitToEditOrCurrentOwnerRevision(true);
		filter.setCategory("rs_activeassembly");

		try {
			rels = systemWebService.loadRelationships(filter);
		} catch (Exception ex) {
			wvc.getLog().error("getTransitionRoot: Could not get relationships for id: " + contentItemLocator.getId(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);			
		}

		//Count the number of follow relationships to see if it is shared.  More than one follow
		//means the item is shared.
		//ArrayList<PSRelationship> followRels = new ArrayList<PSRelationship>();
		ArrayList<Integer> uniqueOwners = new ArrayList<Integer>();

		for(PSRelationship rel: rels) {
			if(!uniqueOwners.contains(rel.getOwner().getId())){
				uniqueOwners.add(rel.getOwner().getId());
			}
		}

		return uniqueOwners.size();
	}


	/**
	 * Determines if an item is participating in more than one Active Assembly relationship
	 * as a dependent. (checks the follow relationships)
	 * @param contentItemLocator
	 * @param wvc
	 * @return
	 * @throws PSErrorException
	 */
	public static boolean isShared(PSLocator contentItemLocator, WorkflowValidationContext wvc)
	throws WFValidationException
	{

		return (numberOfUniqueFollowRels(contentItemLocator, wvc) > 1);


		//		List<PSRelationship> rels = new ArrayList<PSRelationship>();
		//
		//		PSRelationshipFilter filter = new PSRelationshipFilter();		
		//		//This is going to be the current/edit revision for this content item.
		//		filter.setDependent(contentItemLocator);		
		//		filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
		//
		//		try {
		//			rels = systemWebService.loadRelationships(filter);
		//		} catch (Exception ex) {
		//			wvc.getLog().error("isShared: Could not get content type name for id: " + contentItemLocator.getId(), ex);
		//			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);			
		//		}
		//
		//		//Count the number of follow relationships to see if it is shared.  More than one follow
		//		//means the item is shared.
		//		int owner = -1;
		//		for(PSRelationship rel: rels) {
		//
		//			if(owner == -1){
		//				owner = rel.getOwner().getId();
		//			}
		//			else if(owner != rel.getOwner().getId())
		//			{
		//				return true;
		//			}
		//		}
		//		return false;
	}

	/**
	 * Checks if an item has a public revision.  (I.E. Check that there is a version on
	 * the live site)
	 * @param contentItemLocator
	 * @param wvc
	 * @return
	 * @throws Exception 
	 */
	//TODO: Update this to hasPublicRevision or is in a workflow state greater than the one we are transitioning to.	
	public static boolean hasPublicRevisionOrGreater(PSComponentSummary contentItemSummary, WorkflowValidationContext wvc) throws WFValidationException {		
		boolean greaterOrEqual = false;

		int wfState = contentItemSummary.getContentStateId();
		int wfId = contentItemSummary.getWorkflowAppId();		   

		PSState state = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfState),
				new PSGuid(PSTypeEnum.WORKFLOW,wfId));	
		PSWorkflow workflow = workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, wfId)); 

		//Check to see if the workflow is going to be ignored (configurable list of triggers/workflows to ignore)
		//Get the configuration bean.
		WorkflowConfiguration config = WorkflowConfigurationLocator.getWorkflowConfiguration();
		ValidatorIgnoreConfig ignoreCheck = config.getValidatorIgnore();

		for( String currWkflw : ignoreCheck.getIgnoreWorkflows() ){
			//If the current workflow is in the list of ignored workflows, return.
			if(workflow.getName().equalsIgnoreCase(currWkflw)){
				//Just check for a public revision since the workflow state is not in the config.
				return (contentItemSummary.getPublicRevision() != -1);
			}
		}

		greaterOrEqual = workflowConfig.getWorkflowStates().greaterThanOrEqual(state.getName(), wvc.getDestinationState().getName());
		return (contentItemSummary.getPublicRevision() != -1) || greaterOrEqual;

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
		//JOHN TODO: Change this to use the new "validator" class that is in a list, inside of a CTListConfig object.
		//		if (config.getRequiresParentNavonsPublic() && ContentItemWFValidatorAndTransitioner.areParentNavonsPublic(contentItemSummary) == false) {
		//			//Error Out Because public navons are required but they are not public.
		//			wvc.getLog().debug("Parent Navons are not Public for content item: " + contentItemSummary.getContentId());			
		//			return false;
		//		}

		return true;
	}

	/**
	 * Checks to see if all Parent/Ancestor navons have a public revision.
	 * @return
	 */
	//	private static boolean areParentNavonsPublic(PSComponentSummary contentItemSummary) {
	//TODO:Implement areParentNavonsPublic
	//getParentFolderRelationships()
	//wvc.addError(ERR_FIELD, ERR_FIELD_DISP, NAVON_NOT_PUBLIC, args)

	//		return false;
	//	}

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
	 * set the exclusion flag.
	 * 
	 * @param req the request context of the caller.
	 * @param b the new exclusion value. <code>true</code> means that
	 *           subsequent effects should not interfere with event processing.
	 */
	public static void setExclusive(IPSRequestContext req, boolean b)
	{
		//req.setSessionPrivateObject(EXCLUSION_FLAG, b);
		req.setPrivateObject(EXCLUSION_FLAG, b);
	}

	/**
	 * tests if the exclusion flag is on.
	 * 
	 * @param req the parent request context.
	 * @return <code>true</code> if the exclusion flag is set.
	 */
	public static boolean isExclusive(IPSRequestContext req)
	{
		//Boolean b = (Boolean) req.getSessionPrivateObject(EXCLUSION_FLAG);
		Boolean b = (Boolean) req.getPrivateObject(EXCLUSION_FLAG);
		if (b == null)
			return false;
		return b.booleanValue();
	}

	public static boolean validate(PSComponentSummary contentItemSummary, WorkflowValidationContext wvc){
		String contentTypeName = "";
		try {
			contentTypeName = CGV_TypeNames.getTypeName(contentItemSummary.getContentTypeGUID().getUUID());
		} catch (Exception ex) {
			wvc.getLog().error("isTopType: Could not get content type name for id: " + contentItemSummary.getContentTypeGUID().getUUID(), ex);
			throw new WFValidationException("System Error Occured. Please Check the logs.", ex, true);
		}
		if(	!workflowConfig.getContentTypes().
				getContentTypeOrDefault(contentTypeName).
				getValidatorCollection().
				validate(contentItemSummary, null, wvc) ){
			//Failed the validation
			wvc.getLog().error("Failed the validation check for item with content id: " + 
					contentItemSummary.getContentId() + ", see log for errors.", null);
			return false;
		}
		return true;
	}

	/**
	 * Checks to see if the root that is pushing, is the dependent in more then one relationship.
	 * @param pushRoot - the root to check
	 * @param wvc - the workflow validation context.
	 * @return true if the check passes, false if not.
	 */
	private boolean archiveSharedCheck(PSComponentSummary pushRoot,
			WorkflowValidationContext wvc) {
		if(wvc.isArchiveTransition() && 
				(ContentItemWFValidatorAndTransitioner.numberOfRelationships(pushRoot.getCurrentLocator(), wvc) > 0 ))
		{
			return false;
		}
		else{
			return true;
		}
	}

	private static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.WorkflowItemValidator.PSExclusionFlag";

	//Below are the formatters for messages
	public static final String ARCHIVE_SHARED = "The content item {System Title} is shared so it cannot be archived.";	
	public static final String NO_PATH_TO_DEST = "Could not promote content item{System Title} to {Destination State} because its child item {System Title } cannot be promoted to {Destination State}.";
	public static final String SHARED_ITEM_PAST_LOWEST = "Could not promote content item{System Title} because its child item {System Title } is being edited.";
	public static final String NAVON_NOT_PUBLIC = "The navon with id {0} must be promoted to Public before content item {1} can be promoted.";
	public static final String ARCHIVE_PARENT_NOT_MOVING = "Could not archive the content item {System Title} because its parent item {System Title} has not been archived.";

	public static final String PARENT_HAS_NO_AVAILABLE_TRANSITION = "Could not promote item {0} because the user does not have permission to transition its parent item {1}.";
	public static final String PARENT_IS_CHECKED_OUT = "Could not promote item {0} because its parent item {1} is checked out to {2}.";
	public static final String CHILD_IS_CHECKED_OUT = "Could not promote item {0} because its child item {1} is checked out to {2}.";
	public static final String NON_PUBLIC_CHILD_IS_OTHER_COMMUNITY = "Could not promote item {0} because its child item {1} is in another community and not public.";
	public static final String NON_PUBLIC_CHILD_IS_OTHER_WORKFLOW = "Could not promote item {0} because its child item {1} uses another workflow and is not public.";
	public static final String NON_PUBLIC_CHILD_IS_TOP_TYPE = "Could not promote item {0} because its child item {1} is another page and not public.";
	public static final String NON_PUBLIC_CHILD_IS_SHARED = "Could not promote item {0} because its child item {1} is shared and not public.";
	public static final String NO_TRANSITION_AVAILABLE = "Could not promote item {0} because the user does not have permission to transition child item {1}.";
	public static final String NO_PUBLIC_REVISION = "Could not promote item {0} because its child item {1} has no public revision.";
	public static final String SHARED_ARCHIVE = "Could not archive item {0} because it is a dependent of more than 1 content item.";

	public static final String ERR_FIELD = "N/A";
	public static final String ERR_FIELD_DISP = "N/A";	
}
