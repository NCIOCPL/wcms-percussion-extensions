/**
 * Used by Publish On Demand to determine what ancestors of an item
 * should be included in a Publish On Demand job.
 */
package gov.cancer.wcm.publishing;
import gov.cancer.wcm.util.CGV_TopTypeChecker;
import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.util.CGV_TypeNames;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.services.content.data.PSItemSummary;



public class TreeAnalyzer {
	private static final Log log = LogFactory.getLog(TreeAnalyzer.class);
	private IPSGuidManager gmgr;
	private IPSContentWs cmgr;
	private IPSCmsContentSummaries contentSummariesService;

	/**
	 * Constructor
	 * @param gmgr
	 * @param cmgr
	 * @param pcm
	 * @param workflowService
	 * @param contentSummariesService
	 * @param request
	 */
	TreeAnalyzer(IPSGuidManager gmgr,
				IPSContentWs cmgr,
				IPSCmsContentSummaries contentSummariesService
				) {
		this.gmgr = gmgr;
		this.cmgr = cmgr;
		this.contentSummariesService = contentSummariesService;
	}
	
	
	/**
	 * @author John Doyle
	 * @param contentItemID A content item which may be added to the Publish on Demand queue along with its ancestors.
	 * @param isNavon Is this item a Navon?
	 * @param transitionItem Is this the item that was transitioned?  (Did it trigger the POD job?)
	 * @param foundItems Contains the set of all content ID discovered so far.  Used to prevent runaway recursion.
	 * @return
	 */
	private List<Integer> getRelatedPublishableItemsHelper(int contentItemID, 
			boolean isNavon,
			boolean transitionItem,
			HashSet<Integer> foundItems){
		log.trace("Enter getAncestorPublishableItemsHelper");
		if(log.isDebugEnabled()){
			log.debug("contentItemID " + contentItemID);
			log.debug("isNavon " + isNavon);
			log.debug("transitionItem " + transitionItem);
		}
		
		List<Integer> ancestorPublishableItems = new ArrayList<Integer>();
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(contentItemID);
		
		if(transitionItem) {
			log.debug("getAncestorPublishableItems: I am the item that transitioned this workflow: " + contentItemID);
		}
		
		//if the current item is a navon, add it to the list to be republished
		if (isNavon) {
			log.debug("getAncestorPublishableItems: I am a navon - adding to the list");
			ancestorPublishableItems.add(contentItemID);
		}
		else {
			log.debug("getAncestorPublishableItems: I am not a navon! continue processing");
			Long contentTypeID;
			
			
			//get the content type ID from the summary
			contentTypeID = contentItemSummary.getContentTypeId();
			//Check to see if the current content item is a top type
			Boolean isTopType = ContentItemWFValidatorAndTransitioner.isTopType(contentTypeID.intValue());
			Boolean isPublishable = ContentItemWFValidatorAndTransitioner.isPublishable(contentTypeID.intValue());
			
			String contentTypeName = null;
		    try {
		        contentTypeName = CGV_TypeNames.getTypeName(contentTypeID.intValue());
		    } catch (Exception ex) {       
		            String message = String.format("Failed to retrieve type name for contentTypeID = %d.", contentTypeID.intValue());
		            log.warn(message, ex);
		    }
		    
		    PublishingConfiguration pubConfig = PublishingConfigurationLocator.getPublishingConfiguration();
		    boolean needsNextTopType = pubConfig.getNeedsNextTopType(contentTypeName);
		    
		    //if the current item is not the item that was transitioned and it is a top type
			if(!transitionItem && isTopType && !needsNextTopType){
				log.debug("getAncestorPublishableItems: I am not the item transitioned, and I am a top type");
				//add the current item to the publishable items list and do NOT continue to recurse up the tree
				if(isPublishable) {
					ancestorPublishableItems.add(contentItemID);
				}
			}
			//if the current item is not a top type we want to keep searching for its parents
			//Edge Conditions:
			//	1. Item transitioned was a Top Type - we still want to search the tree for its nearest top type ancestors
			else {
				
				List<Integer> parentPublishableItems = new ArrayList<Integer>();
				log.debug("getAncestorPublishableItems: I am either the item transitioned, or not a top type");
				
				//check to see if the current item is publishable
				if(isPublishable) {
					log.debug("getAncestorPublishableItems: I am either the item transitioned, or not a top type && isPublishable");
					
					//add the item to the publishable items list
					ancestorPublishableItems.add(contentItemID);
				}
				
				//find all of my parent items of our current contentItem
				try{
					List<PSItemSummary> ancestorParentItems = findEligibleParentItems(contentItemSummary);
					for(PSItemSummary ancestor : ancestorParentItems) {
				
						Boolean isAncestorNavon = false;
						Integer ancestorContentID = 0;
						//get the parent item content ID and check if it is a navon
						ancestorContentID = ancestor.getGUID().getUUID();
						isAncestorNavon = isNavon(contentTypeID);
						
						// Recurse to find all of the publishable parent items from this parent.
						// If the item was previously found, don't check it again, otherwise we
						// risk runaway recursion.  It's OK for a single item to be the owner of
						// multiple relationships, but if one of the dependents is a child of another,
						// the graph becomes circular.  The recursion should stop when we hit a top type,
						// but this should handle any edge case we've overlooked (e.g. top type not configured as such).
						if(!foundItems.contains(ancestorContentID)){
							foundItems.add(ancestorContentID);
							parentPublishableItems = getRelatedPublishableItemsHelper(ancestorContentID, isAncestorNavon, false, foundItems);
						} else {
							log.debug("Content item " + ancestorContentID + " was not previously found. Not checking again.");
						}
						
						//For each parent item, check to see if its already in the
						//publish list.
						for(Integer parent : parentPublishableItems)
						{
							if(!ancestorPublishableItems.contains(parent))
							{
								PSComponentSummary parentItemSummary = contentSummariesService.loadComponentSummary(parent);
								Long parentContentTypeID = parentItemSummary.getContentTypeId();
								Boolean isParentPublishable = ContentItemWFValidatorAndTransitioner.isPublishable(parentContentTypeID.intValue());
								if(isParentPublishable) {
									ancestorPublishableItems.add(parent);
								}
							}
						}
						
					}
					
					// TEST: find dependent items
					List<PSItemSummary> ancestorDependentItems = findEligibleDependentItems(contentItemSummary);
					for(PSItemSummary ancestor : ancestorDependentItems) {
						log.debug("Found dependent item: " + ancestor.toString());
					}
					
				}
				// Log the error, but otherwise swallow it and return without
				// adding anything to the list of content items.
				catch (PSErrorException e) {
					log.error(e.getMessage());
					log.error(e.getStack());
				}
				
			}

			// Log the IDs being returned, but only if debugging is on!
			if(log.isDebugEnabled()){
				for(Integer ID : ancestorPublishableItems) {
					log.debug("ID to Publish: " + ID);
				}
			}
		}

		log.trace("Exit getAncestorPublishableItemsHelper");
		return ancestorPublishableItems;
	}


	/**
	 * Finds the content items which are parents to the item described in itemSummary.
	 * If itemSummary describes a site home page, then all items with relationships
	 * via sys_inline_link are discarded.
	 * @param itemSummary
	 * @return a list of PSItemSummary objects for the parent content items.
	 * @throws PSErrorException 
	 */
	private List<PSItemSummary> findEligibleParentItems(PSComponentSummary itemSummary)
		throws PSErrorException {
		if(log.isTraceEnabled()){
			log.trace("Enter findEligibleParentItems with itemSummary = " + itemSummary.toString());
		}

		PSLocator itemLocator = new PSLocator(itemSummary.getContentId());
		IPSGuid contentItemGUID = gmgr.makeGuid(itemLocator);

		// Set up item filter.
		PSRelationshipFilter itemFilter = new PSRelationshipFilter();
		
		// Only the relationships where this item is the dependent. 
		itemFilter.setDependent(itemLocator);
		// We're only interested in active assembly relationships.
		itemFilter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
		// Only bring back items which link from their most recent revision.
		itemFilter.limitToEditOrCurrentOwnerRevision(true);

		// Load the list of parent items.
		List<PSItemSummary> parentItems = cmgr.findOwners(contentItemGUID, itemFilter, false);
		if(log.isDebugEnabled()){
			for(PSItemSummary item : parentItems){
				log.debug("Found parent item: " + item.getGUID().getUUID());
			}
		}

		// If the content item has a site home page type, then we need to do some filtering.
		// For the home page type, we want to exclude all items which own an inline relationship.
		// (i.e. sys_inline_link, though this may expanded.)  We'd like to do this via the findOwners()
		// call above, but the PSRelationshipFilter only has a mechanism for the kinds of relationships
		// you'd like to add versus what you'd like to avoid.
		boolean isSiteHomeType = ContentItemWFValidatorAndTransitioner.isSiteHomeType(itemSummary.getContentTypeId());
		if(isSiteHomeType){
			if(log.isTraceEnabled()){
				log.trace("Content item " + contentItemGUID + " is a site home page." );
			}
		
			// Retrieve the list of relationships which the current item participates in.
			List<PSAaRelationship> relationships = null;
			relationships = cmgr.loadContentRelations(itemFilter, true);
			
			// Filter the list of returned relationships, keeping only a list of the unique
			// owners, and discarding parents with an inline slot relationship.
			HashSet<Integer> filteredOwners = new HashSet<Integer>();
			for(PSAaRelationship rel : relationships){
				PSLocator owner = rel.getOwner();
				String slotname = rel.getSlotName(); 
				if(slotname.compareTo("sys_inline_link") != 0
					&&	!filteredOwners.contains(owner.getId())){
					filteredOwners.add(owner.getId());
					log.debug("Filter is keeping owner: " + owner.getId());
				}
			}
			
			// Reconcile the filtered list of parent items against the list of all parents. 
			ArrayList<PSItemSummary> eligibleParents = new ArrayList<PSItemSummary>();
			for(PSItemSummary parent : parentItems){
				if(filteredOwners.contains(parent.getGUID().getUUID())){
					eligibleParents.add(parent);
					if(log.isDebugEnabled()){
						log.debug("Eligible parent: " + parent.getGUID().getUUID());
					}
				}
				else{
					if(log.isDebugEnabled()){
						log.debug("Parent: " + parent.getGUID().getUUID() + " is not eligible.");
					}
				}
			}

			// Replace the list of eligible parents
			parentItems = eligibleParents;
		}

		if(log.isDebugEnabled()){
			log.debug("Returning " + parentItems.size() + " eligible parent items.");
		}
		log.trace("Exit findEligibleParentItems");
		return parentItems;
	}
	
	/**
	 * Finds the content items which are dependents to the item described in itemSummary.
	 * @param itemSummary
	 * @return a list of PSItemSummary objects for the dependent content items.
	 * @throws PSErrorException 
	 */
	private List<PSItemSummary> findEligibleDependentItems(PSComponentSummary itemSummary)
		throws PSErrorException {
		if(log.isTraceEnabled()){
			log.trace("Enter findEligibleDependentItems with itemSummary = " + itemSummary.toString());
		}

		PSLocator itemLocator = new PSLocator(itemSummary.getContentId());
		IPSGuid contentItemGUID = gmgr.makeGuid(itemLocator);

		// Set up item filter.
		PSRelationshipFilter itemFilter = new PSRelationshipFilter();
		
		// Only the relationships where this item is the dependent. 
		itemFilter.setOwner(itemLocator);
		// We're only interested in active assembly relationships.
		itemFilter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
		// Only bring back items which link from their most recent revision.
		itemFilter.limitToEditOrCurrentOwnerRevision(true);

		// Load the list of dependent items.
		List<PSItemSummary> dependentItems = cmgr.findDependents(contentItemGUID, itemFilter, false);
		if(log.isDebugEnabled()){
			for(PSItemSummary item : dependentItems){
				log.debug("Found dependent item: " + item.getGUID().getUUID());
			}
		}

		if(log.isDebugEnabled()){
			log.debug("Returning " + dependentItems.size() + " eligible dependent items.");
		}
		log.trace("Exit findEligibleDependentItems");
		return dependentItems;
	}
	
	
	/**
	 * getAncestorPublishableItems entry function
	 * @author Doyle
	 * @calls getAncestorPublishableItemsHelper - Recursive function to find all parents
	 * of the current item being transitioned through the workflow using Publish on Demand.
	 */
	public List<Integer> getAncestorPublishableItems(int contentItemID, 
									boolean isNavon,
									boolean transitionItem) {
		
		log.debug("getAncestorPublishableItems: current content item is: " + contentItemID);
		
		List<Integer> publishableItems = new ArrayList<Integer>();
		
		HashSet<Integer> foundItems = new HashSet<Integer>();

		// The current content item is already known, add it to the list of known items.
		foundItems.add(contentItemID);
		publishableItems = getRelatedPublishableItemsHelper(contentItemID, isNavon, transitionItem, foundItems);
		
		return publishableItems;
		
		
	}
	
	/**
	 * getDependentPublishableItems entry function
	 * @author youngdr
	 * @calls getDependentPublishableItemsHelper - Recursive function to find all dependents
	 * of the current item being transitioned through the workflow using Publish on Demand.
	 */
	public List<Integer> getDependentPublishableItems(int contentItemID, 
									boolean isNavon,
									boolean transitionItem) {
		
		log.debug("getDependentPublishableItems: current content item is: " + contentItemID);
		
		List<Integer> publishableItems = new ArrayList<Integer>();
		
		HashSet<Integer> foundItems = new HashSet<Integer>();

		// The current content item is already known, add it to the list of known items.
		foundItems.add(contentItemID);
		publishableItems = getRelatedPublishableItemsHelper(contentItemID, isNavon, transitionItem, foundItems);
		
		return publishableItems;
		
		
	}

	/**
	 * Returns if an item is a navon or not.
	 * @return
	 */
	public boolean isNavon(Long contentID){
		
		return CGV_TopTypeChecker.navon(contentID.intValue(),cmgr);
	}



}



/**
 * Get recursive list of all parent content items to this item
 * Conditions:
 * 1) Publish a non-top-type:
 * 		-Get list of parents
 * 		-If top type: Stop
 * 		-If not top type: Recurse
 * 
 * @param currItemId
 * @param navon - is the item a navon?
 * @return List of parent items
 */
/*public List<Integer> getParents(int currItemId, boolean navon, String site, IPSRequestContext request, boolean transitionItem) {
	log.debug("getParents: beginning of get parent");
	List<Integer>localPublishList = new ArrayList<Integer>();	//list of items to return
	List<Integer>relatedContentList = null;

	
	if(navon){	//list of items to return
		localPublishList.add(currItemId);				//if current item is a Navon, add it to the list.
	}
	else{ //If its not a navon
		//if this item is not the transition root, don't do anything
		ContentItemWFValidatorAndTransitioner validator = new ContentItemWFValidatorAndTransitioner(log);
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(currItemId);					
		log.debug("getParents: Getting Workflow Info");
		String transition = request.getParameter("sys_transitionid");
		int transitionID = Integer.parseInt(transition);
		int wfState = contentItemSummary.getContentStateId();
		int wfId = contentItemSummary.getWorkflowAppId();		   
		PSWorkflow workflow = workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, wfId));
		PSState state = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfState),
				new PSGuid(PSTypeEnum.WORKFLOW,wfId));	   
		Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
		WorkflowValidationContext wvc = new WorkflowValidationContext(request, contentItemSummary, log, errorDoc, workflow, state, transitionID);
		PSComponentSummary psCS = validator.getTransitionRoot(contentItemSummary, wvc);
		//The transition root is  
		//
		//
		if (psCS.getContentId() != currItemId) {
			log.debug("getParents: not transition root, id:" + currItemId);
			return localPublishList;
		}
	
					
		//if we get here this item is the transition root.
		List<IPSGuid> glist = Collections.<IPSGuid> singletonList(gmgr.makeGuid(new PSLocator(currItemId)));
		List<PSCoreItem> items = null;
		PSCoreItem item = null;
		try {
			items = cmgr.loadItems(glist, true, false, false, false);
			item = items.get(0);
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("getParents: before checking the top type");
		Long typeId = item.getContentTypeId();//get content type ID
		//check to see if the current content item is a top type.
		boolean isTop = ContentItemWFValidatorAndTransitioner.isTopType(typeId.intValue());
		//if it is a top type, we want to stop traversing the tree and add this item to the publishing
		//list.
		if(isTop && !transitionItem){
			//if this is a topmost content type, don't get the parents
			//if didn't get any parents, create list and add current item to it
			log.debug("getParents: is top type, got into the null list");
			localPublishList.add(currItemId);
		}
		
		 *TODO: (By Doyle 9/1/2011) Make sure to check that the current item has not been visited. We saw an issue 
		 *recently where if we had 2 non-top-types in which both pieces of content had the other
		 *in a slot relationship, it would hit an infinite loop. 
		 *TODO: Create a list of visited items to satisfy the item above.
		 
		
		//If the current item is not a top type, we want to keep searching up the tree if it 
		//has any parents. 
		else
		{
			if(isTop) {
				log.debug("getParents: is top type, but it is the item transitioned so keep looking for parents");
				localPublishList.add(currItemId);
			}
			
			log.debug("getParents: !top type statement (or top type+transitionItem)");
			
			try {
				log.debug("getParents: before get parents cids");
				IPSGuid cid = gmgr.makeGuid(new PSLocator(currItemId));//get the content ID of the current item
				relatedContentList = pcm.getParentCIDs(cid, false, 0);	//gets 1 layer of parents (gets top & non-top types)
				log.debug("getParents: got localPublishList");
			} 
			catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			//if there are items inside our publishList, add them to a temp list
			if (relatedContentList != null && relatedContentList.size() > 0) {
				//create a temp list to hold new parents items so we don't screw the loop
				List<Integer>tempList = new ArrayList<Integer>();
				for (int sItem : relatedContentList) {
					//get the parents of each parent of the current parent (if its not already a top type and the condition above
					//doesnt exist) This will work as currently implemented.
					List<Integer> parentsList = this.getParents(sItem, isNavon(sItem), site, request, false);	//recurses! foiled again! 
					if (parentsList != null) {
						for (int p : parentsList) {
							log.debug("getParents: DEBUG: parent item CID: " + p);
							tempList.add(p);
						}
					}
				}
				log.debug("getParents: before temp list");
				for (int tItem : tempList) {
					//add the items to the list to be returned
					isTop = false;
					typeId = null;
					glist = null;
					//if we get here this item is the transition root.
					glist = Collections.<IPSGuid> singletonList(gmgr.makeGuid(new PSLocator(tItem)));
					items = null;
					item = null;
					try {
						items = cmgr.loadItems(glist, true, false, false, false);
						item = items.get(0);
					} catch (PSErrorResultsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					typeId = item.getContentTypeId();
					isTop = ContentItemWFValidatorAndTransitioner.isTopType(typeId,wvc);
					if(!localPublishList.contains(tItem) && isTop)
						localPublishList.add(tItem);
				}
			}
		}
		/*List<Integer> addToList = new ArrayList<Integer>();
		//Check auto slot list in the config file.
		addToList = CGV_TopTypeChecker.autoSlotChecker(typeId.intValue(),cmgr, pubSvc.getAutoSlot(), site);
		if( !addToList.isEmpty() ){
			for( Integer addInteger : addToList ){
				localPublishList.add(addInteger);
			}
		}*/

		//Always add the current item to the list
		//Doyle: Do not always add current item to list - only add the current item if it is a publishable
		//type. 
		//IF NOT A TOP TYPE, DO NOTHING!
		/*if (localPublishList == null) {
			log.debug("getParents: null list, creating and adding individual item " + currItemId);
			//if didn't get any parents, create list and add current item to it
			localPublishList = new ArrayList<Integer>();
			localPublishList.add(currItemId);
		}
		else{
			if (!isTop) {
				//if top type, already on list
				//Check to make sure that this is a publishable type.
				
				log.debug("getParents: we had a non null list, add in the individual item " + currItemId);
				localPublishList.add(currItemId);
			}
		}
	}

	log.debug("getParents: Printing out the list to publish....");
	if(localPublishList != null){
		for( Integer printInt : localPublishList ){
			log.debug(printInt);
		}
	}
	//Go through the list and make sure all of the items should be published.
	
	
	return localPublishList;
}*/