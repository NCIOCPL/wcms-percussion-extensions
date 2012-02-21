/**
 * 
 */
package gov.cancer.wcm.publishing;
//TODO: ask John about 'site' parameter to top type checker, new definition of autoslot
import gov.cancer.wcm.util.CGV_TopTypeChecker;
import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.services.content.data.PSItemSummary;
/**
 * @author John Doyle based on holewr
 */

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
	 * @param contentItemID
	 * @param isNavon
	 * @param transitionItem
	 * @return
	 */
	public List<Integer> getAncestorPublishableItemsHelper(int contentItemID, 
			boolean isNavon,
			boolean transitionItem){
		
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
			//if the current item is not the item that was transitioned and it is a top type
			if(!transitionItem && isTopType){
				log.debug("getAncestorPublishableItems: I am not the item transitioned, and I am a top type");
				//add the current item to the publishable items list and do NOT continue to recurse up the tree
				ancestorPublishableItems.add(contentItemID);				
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
				
				IPSGuid contentItemGUID = gmgr.makeGuid(new PSLocator(contentItemID));
				PSRelationshipFilter contentItemFilter = new PSRelationshipFilter();
				contentItemFilter.setCategory("rs_activeassembly");
				contentItemFilter.limitToOwnerRevision(true);
				//find all of my parent items of our current contentItem
				try{
					List<PSItemSummary> ancestorParentItems = cmgr.findOwners(contentItemGUID, contentItemFilter, false);
					for(PSItemSummary ancestor : ancestorParentItems) {
						//log.debug("parents Name: " + parent.getName());
						//log.debug("Content Type Name: " + parent.getContentTypeName() + " Content GUID: " + parent.getGUID()+ " Content ID: " + parent.getGUID().getUUID() );
				
						Boolean isAncestorNavon = false;
						Integer ancestorContentID = 0;
						//get the parent item content ID and check if it is a navon
						ancestorContentID = ancestor.getGUID().getUUID();
						isAncestorNavon = isNavon(contentTypeID);
						
						//recurse to find all of the publishable parent items from this parent
						parentPublishableItems = getAncestorPublishableItemsHelper(
								ancestorContentID, 
								isAncestorNavon, 
								false);
						
						//For each parent item, check to see if its already in the
						//publish list.
						for(Integer parent : parentPublishableItems)
						{
							if(!ancestorPublishableItems.contains(parent))
							{
								ancestorPublishableItems.add(parent);
							}
						}
						
					}
				}
				catch (PSErrorException e) {
					e.printStackTrace();
				}
				
			}
			for(Integer ID : ancestorPublishableItems) {
				log.debug("ID to Publish: " + ID);
			}
		}
		return ancestorPublishableItems;
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
		
		
		publishableItems = getAncestorPublishableItemsHelper(contentItemID, isNavon, transitionItem);
		
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