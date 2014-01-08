package gov.cancer.wcm.publishing;

import gov.cancer.wcm.publishing.PODPublisher;
import gov.cancer.wcm.publishing.PODQueue;
import gov.cancer.wcm.publishing.PODWork;
import gov.cancer.wcm.publishing.PublishItem;
import gov.cancer.wcm.publishing.TreeAnalyzer;
import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;


import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;




/**
 * Queues items and their parents for on-demand publishing
 * @author whole based on mudumby
 *
 */
public class CGV_OnDemandPublishService implements InitializingBean {
	private static final Log log = LogFactory.getLog(CGV_OnDemandPublishService.class);
	
	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSPublisherService pubSvc = null;
	protected static IPSContentWs cmgr = null;
	protected static PODPublisher podPublisher = null;
	private static IPSWorkflowService workflowService;
	private static IPSCmsContentSummaries contentSummariesService;
	private static TreeAnalyzer treeAnalyzer;

	
	
	private static CGV_FolderValidateUtils valUtil = null;
	
	
	private Map<String,Map<String,Map<String,List<String>>>> publishingEditions;
	private List<IPSEdition> allEditions;
	
	
	private boolean waitForStatus = true;
	//TODO: These 2 can go, if the POD code is updated, timeOut and waitTime.  Needs to be updated in the Bean XML
	private int timeOut = 20000;
	private int waitTime = 100;
	private Map<String,Map<String,List<String>>> autoSlot;
	//	private static PODPublisher podService = null;


	/**
	 * Inner class to simplify some of the code for constructing the work.
	 * Combines like work, adds new work.
	 */
	private class PublishingQueueHelper{
		
		//public List<PODWork> workList;
		public Hashtable<Integer, PODWork> workByEdition;

		public PublishingQueueHelper(){
			workByEdition = new Hashtable<Integer, PODWork>();
		}

		/**
		 * If the edition already exists in the list, add the requested
		 * cid (content id) into that existing PODWork item.  
		 * Else, create a new PODWork item and insert it into the list.
		 * @param cid
		 * @param edition
		 * @return True/false, if the edition was ALREADY in the list.
		 */
		public void addWorkToEdition(int cid, IPSGuid folderGuid, IPSGuid itemGuid, int editionID){
			//Check all items in the list
			if(workByEdition.containsKey(editionID))
			{
				PODWork editionWork = workByEdition.get(editionID);
				editionWork.addItem(new PublishItem(folderGuid, itemGuid, cid));
				workByEdition.put(editionID, editionWork);
			}
			else //This edition has not been created yet. 
			{				
				List<PublishItem> publishList = new ArrayList<PublishItem>();
				publishList.add(new PublishItem(folderGuid, itemGuid, cid));
				workByEdition.put(editionID, new PODWork(publishList, editionID));
			}
			
			
		}
				
	}

	public Map<String,Map<String,List<String>>> getAutoSlot() {
		return autoSlot;
	}

	public void setAutoSlot(Map<String,Map<String,List<String>>> autoSlot) {
		this.autoSlot = autoSlot;
	}

	public Map<String,Map<String,Map<String,List<String>>>> getPublishingEditions() {
		return publishingEditions;
	}

	public void setPublishingEditions(Map<String,Map<String,Map<String,List<String>>>> editionList) {
		this.publishingEditions = editionList;
	}


	/**
	 * Initialization function for the CGV_OnDemandPublishingService
	 * @Author John Doyle
	 * 
	 */
	public CGV_OnDemandPublishService() {
		log.debug("Initializing Publishing Service");
		if(podPublisher == null)
		{
			podPublisher = PODPublisher.getInstance();
		}
		if (rps == null) {
			rps = PSRxPublisherServiceLocator.getRxPublisherService();
			gmgr = PSGuidManagerLocator.getGuidMgr();
			cmgr = PSContentWsLocator.getContentWebservice();
			pubSvc = PSPublisherServiceLocator.getPublisherService();
			workflowService = PSWorkflowServiceLocator.getWorkflowService();
			contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		}
		if (valUtil == null) {
			valUtil = new CGV_FolderValidateUtils();
			if (valUtil.getContentManager() == null) valUtil.setContentManager(PSContentMgrLocator.getContentMgr());
			if (valUtil.getContentWs() == null) valUtil.setContentWs(PSContentWsLocator.getContentWebservice());
			if (valUtil.getGuidManager() == null) valUtil.setGuidManager(PSGuidManagerLocator.getGuidMgr());
			if (valUtil.getNodeCataloger() == null) valUtil.setNodeCataloger(new PSONodeCataloger());
			if (valUtil.getSystemWs() == null) valUtil.setSystemWs(PSSystemWsLocator.getSystemWebservice()); 
		}
		//Get a list of all editions in the system.
		if(allEditions == null) {
			allEditions = pubSvc.findAllEditions("");
		}
		if(treeAnalyzer == null)
		{
			treeAnalyzer = new TreeAnalyzer(gmgr,
											cmgr,
											contentSummariesService
											);
			
		}
		
		log.debug("Completed Initialize Service");
	}

	/**
	 * Initialize services.
	 * 
	 * @param extensionDef
	 * @param codeRoot
	 * @throws PSExtensionException
	 */
	public void init(IPSExtensionDef extensionDef, File codeRoot)
	throws PSExtensionException {
		log.debug("Initializing CGV_OnDemandPublishService...");
	}

	
	/**
	 * PRECONDITION: This function is only called ONCE per POD Transition through the workflow. It is called 
	 * only on the item that was transitioned. 
	 * 
	 * publishOnDemand(): This function handles the majority of the Publish on Demand process. 
	 * 		1) Takes in the content item that has been transitioned and:
	 * 			a) Finds all Nearest Top-Type parents of the item
	 * 			b) Finds all Non-Top-Type parents which are marked as Publishable
	 * 		2) Finds all folder paths for publishable items (items may live simultaneously in multiple folders
	 * and must be published out separately to each of these locations) 
	 * 		3) Adds each instance of an item (each folder path it lives in) to the necessary publishing edition lists - This works
	 * across sites - so if one of the transitioned items parents lives in a different site, it will add that sites publishing editions.
	 * 		4) Once all parents have been added to the necessary editions, all editions are added separately to the publishing
	 * queue which will be picked up by a separate thread to publish out the content to the system. 
	 * @author Doyle
	 * @param contentId
	 * @param contentTypeId
	 * 
	 */
	public void publishOnDemand(int contentID) {
		
		//Initialization of global elements being used in this function. 
		List<String> paths = new ArrayList<String>();
		List<Integer> ancestorsToPublish = new ArrayList<Integer>();
		PSComponentSummary contentItemSummary;
		Boolean isNavon = false;
		
		
		//get the content summary of the current item
		contentItemSummary = contentSummariesService.loadComponentSummary(contentID);
		
		isNavon = treeAnalyzer.isNavon(contentItemSummary.getContentTypeId());
		
		log.debug("getAncestorPublishableItems: Getting Workflow Information");
		//get the workflow state ID of the current item
		int wfStateID = contentItemSummary.getContentStateId();
		//get the workflow ID of the current item
		int wfID = contentItemSummary.getWorkflowAppId();		
		//get the workflow of the current item.
		PSWorkflow workflow = workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, wfID));
		//get the Workflow State 
		PSState wfstate = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfStateID),
				new PSGuid(PSTypeEnum.WORKFLOW,wfID));	   
		
		String workflowName = workflow.getName();
		String workflowStateName = wfstate.getName();
				
		log.debug("Root item of this publish on Demand is: " + contentItemSummary.getName());
		
		//Get Ancestor Top Types
		//Along the way - check if items have (isPublishable) set that are not
		//top types
		//Recursive function to find ancestors to publish.
		ancestorsToPublish = treeAnalyzer.getAncestorPublishableItems(contentID, 
				isNavon, 
				true);

		
		//Get folder paths of the current item (could be multiple) and get the site name for where the content
		//lives.
		PublishingQueueHelper publishingQueueHelper = new PublishingQueueHelper();
		for(Integer publishCID : ancestorsToPublish) {
			//get the content item GUID
			IPSGuid publishItemGUID = gmgr.makeGuid(new PSLocator(publishCID));
			PSComponentSummary publishItemSummary = contentSummariesService.loadComponentSummary(publishCID);
			
			
			try{
				paths = Arrays.asList(cmgr.findFolderPaths(publishItemGUID));
				//For each path that this content item lives in...
				//(Content items can live in multiple folders and should be published
				//out to all of them separately.)
				for(String path : paths){
					String sitePath = getSitePath(path);
					//Get all editions for this item by its path
					List<Integer> editionIDs = getEditions(sitePath, 
							treeAnalyzer.isNavon(publishItemSummary.getContentTypeId()), 
							workflowName,
							workflowStateName);
					//Get the folder GUID
					IPSGuid folderGuid = cmgr.getIdByPath(path);
					log.debug("Editions for content ID " +  publishCID );
					//For each edition - Add this item to the list of content to be published
					for( Integer currEdition : editionIDs){
						log.debug("		Edition: " +  currEdition );
						publishingQueueHelper.addWorkToEdition(
								publishCID, folderGuid, publishItemGUID, currEdition);
					}
					
				}
			}
			catch (PSErrorException e) {e.printStackTrace();}
				
			
		}
		
		//Now we want to put all of our work onto the queue
		
		//Get a list of all keys in the workByEdition Hashtable.
		Enumeration<Integer> keys = publishingQueueHelper.workByEdition.keys();
		//While keys exist
		while(keys.hasMoreElements())
		{
			log.debug("Adding Work Item to the Publishing Queue");
			//pull out the first key
			Object key = keys.nextElement();
			//get the PODWork Item based on the key
			PODWork workItem = publishingQueueHelper.workByEdition.get(key);
			workItem.setTransitionItemID(contentID);
			workItem.setTransitionUser(contentItemSummary.getContentLastModifier());
			workItem.setTransitionItemName(contentItemSummary.getName());
			workItem.setTimeEntered(System.currentTimeMillis());
			workItem.setWorkID(contentID + "-" + workItem.getEdition());
			
			if(log.isDebugEnabled()){
				log.debug("Publishing Work Edition with the following Info:");
				log.debug("Edition ID: " + workItem.getEdition());
				for(PublishItem p : workItem.getItems()) {
					log.debug("Content ID: " + p.contentID);
				}
			}

			//put the PODWork item on the Queue.
			PODQueue.put(workItem);
		}
		log.debug("Finished adding all items to the queue for this PoD");
		
		
	}
	
	
	
	
	
	/**
	 * 
	 */
	public void afterPropertiesSet() throws Exception {
		
	}


	
	/**
	 * getEditions - Function to get a list of Edition ID's 
	 * @param sitePath - The Site Path of the current item (ex. "//Sites/CancerGov/")
	 * @param isNavon - Is this item a navon?
	 * @param workflowStateName - Workflow State that the item just transitioned to. 
	 * @return - A list of Edition ID's in which the current item needs to be published with.
	 */
	private List<Integer> getEditions(String sitePath, Boolean isNavon,String workflowName, String workflowStateName ){
		List<Integer> editionIDs = new ArrayList<Integer>();
		
		//get the map of all of the different sites. 
		Map<String, Map<String,List<String>>> siteMap = publishingEditions.get(sitePath);
		if(siteMap != null)
		{
			//get the map of all of the differnt workflows for the current site.
			Map<String, List<String>> siteWorkflowMap = siteMap.get(workflowName);
			if(siteWorkflowMap != null)
			{
				//for each edition inside this workflow map (by the state we just moved too)
				List<String> editionList = siteWorkflowMap.get(workflowStateName);
				if(editionList != null) 
				{
					for(String edition : editionList)
					{
						//Call getEditionIdByName() to get the edition ID's
						Integer editionID = getEditionIdByName(edition);
						if(editionID != 0)
						{
							//add the edition to the EditionIDs list to be returned
							editionIDs.add(editionID);
						}
						else {log.error("Item with path of: " + sitePath + " does not have any publishing editions associated with it \n" +
								"This is most likely because there is a problem with the Publish on Demand config file \n" +
								"or the publish on demand extension is being called for a workflow state that it should not be (ie. Review)" );}
						//check edition name against allEditions and return a list of integers.
					}
				}
				else {log.error("Item with path of: " + sitePath + " does not have any publishing editions associated with it \n" +
						"This is most likely because there is a problem with the Publish on Demand config file \n" +
						"or the publish on demand extension is being called for a workflow state that it should not be (ie. Review)" );}
			}
			else {log.error("Item with path of: " + sitePath + " does not have any publishing editions associated with it \n" +
					"This is most likely because there is a problem with the Publish on Demand config file \n" +
					"or the publish on demand extension is being called for a workflow state that it should not be (ie. Review)" );}
			
		}
		else {log.error("Item with path of: " + sitePath + " does not have any publishing editions associated with it \n" +
				"This is most likely because there is a problem with the Publish on Demand config file \n" +
				"or the publish on demand extension is being called for a workflow state that it should not be (ie. Review)" );}
		
		return editionIDs;
	}
	
	
	/**
	 * getEditionIdByName() - This function takes in the text name of an edition and returns the ID 
	 * based on the list of all editions in a site at the time of the last percussion startup.
	 * @author John Doyle
	 * @param editionName - The name of the edition that needs to be run. 
	 * @return - Integer containing the edition ID of the given edition Name. 0 if no edition was found.
	 */
	private Integer getEditionIdByName(String editionName) {
		Integer editionID = 0;
		List<IPSEdition> editionList = allEditions;
		for(IPSEdition edition : editionList) {
			if(edition.getDisplayTitle().equalsIgnoreCase(editionName)) {
				editionID = edition.getGUID().getUUID();
			}
			if(editionID != 0)
				break;
		}
		
		return editionID;
	}
	
	/**
	 * Finds the site path of the current site. Returns in the format of:
	 * "//Sites/xxxxxx/" where xxxxxx is the site name. 
	 * @author John Doyle
	 * @param path
	 * @return
	 */
	private String getSitePath(String path) {
		StringTokenizer st = new StringTokenizer(path, "/");
		String sitePath = "";
		assert(st.countTokens() >= 2);
		sitePath = "//" + st.nextToken() + "/" + st.nextToken() + "/";
		return sitePath;
	}
	/**
	 * @return boolean waitForStatus
	 */
	public boolean isWaitForStatus() {
		return waitForStatus;
	}

	/**
	 * @param waitForStatus
	 */
	public void setWaitForStatus(boolean waitForStatus) {
		this.waitForStatus = waitForStatus;
	}

	/**
	 * @return int waitTime
	 */
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * @param waitTime
	 */
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	/**
	 * @param timeOut
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * @return int timeOut
	 */
	public int getTimeOut() {
		return timeOut;
	}


}

/**
 * Put item and parents on queue, publish the queue
 * 
 * **Doyle
 * The content ID that gets passed into this function is the contentID of the item that was pushed
 * through the workflow. This is important because we need to know this in the getParents function. 
 * **
 * @param contentId
 * @param contentTypeId
 */

/*public void queueItemSet(int contentId, IPSRequestContext request) {

	//List<String> editions = new ArrayList<String>();
	initServices(); //init the publishing bean. 
	if(treeAnalyzer == null)
	{
		treeAnalyzer = new TreeAnalyzer(gmgr,
										cmgr,
										pcm,
										workflowService,
										contentSummariesService
										);
	}
	Boolean navon = false;

	List<IPSGuid> loadList = Collections.<IPSGuid> singletonList(gmgr.makeGuid(new PSLocator(request.getParameter("sys_contentid"))));
	List<PSCoreItem> items = null;
	PSCoreItem item = null;
	try {
		items = cmgr.loadItems(loadList, true, false, false, false);
		item = items.get(0);
	} catch (PSErrorResultsException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	Long contentTypeId = item.getContentTypeId();
	 
	int id = Integer.parseInt(request.getParameter("sys_contentid"));
	//Check to see if the current item being published is a Navon		
	navon = treeAnalyzer.isNavon(id);
	PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(id);	
	int wfState = contentItemSummary.getContentStateId(); //get the workflow state of current item
	int wfId = contentItemSummary.getWorkflowAppId();	//get the workflow state ID of current item
	PSState startState = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfState),
			new PSGuid(PSTypeEnum.WORKFLOW,wfId));	   //Load up the starting workflow state.
	String publishingFlag = startState.getContentValidValue();

	log.debug("start of queue item set");

	
	
	log.debug("after init services has run!");		
	
	//make guid for the current item being moved through the workflow using the GUID manager.
	IPSGuid guidToFindSite = gmgr.makeGuid(id, PSTypeEnum.LEGACY_CONTENT);  
	
	List<String> paths;
	String siteName = null;
	
	//Get folder paths of the current item (could be multiple) and get the site name for where the content
	//lives.
	try {
		paths = Arrays.asList(cmgr.findFolderPaths(guidToFindSite));
		siteName = getSiteName(paths.get(0));
	} catch (PSErrorException e) {
		e.printStackTrace();
	}
	
	//Now we need to get the other peices of content that should be published with this item.
	List<Integer> idsToPublish = null;	//the list to publish
	log.debug("before checking of the top type");
	
	//transitionItem - true if current item was the one transitioned through workflow
	//				 - false if not transition item calling the getParent function
	boolean transitionItem = true;
	
	
	//See Requirements Document for explicit details on the getParents function. 
	//getParents is a recursive function that will return a list of all content items which need to be published
	//as a result of the current content item moving through the workflow. 
	idsToPublish = treeAnalyzer.getParents(contentId, navon, siteName, request, transitionItem);
	
	log.debug("Item CID: " + contentId);
	log.debug("Need to publish " + idsToPublish.size() + " items");

	
	//Now that we have everything that we want to publish, lets publish it!
	try {
		IPSRxPublisherService rxsvc = PSRxPublisherServiceLocator
		.getRxPublisherService();

		//Add items to the local consolidated Work queue to be passed into the global static queue.
		if (idsToPublish == null || idsToPublish.size() == 0) {
			log.debug("queueItemSet: no items");
		}
		else {
			//List of PODWork items to be added into the POD Queue
			//NOTE: Private Inner class ^^^^^^
			WorkToAddList addToPODList = new WorkToAddList();

			log.debug("Processing parents");
			//add the parents and children to the queue
			for (int i : idsToPublish) {
				IPSGuid itemGuid = gmgr.makeGuid(i, PSTypeEnum.LEGACY_CONTENT);
				log.debug("the item guid is " + itemGuid);

				//Get site edition list.-------------------------------------------
				List<String> itemPaths = Arrays.asList(cmgr.findFolderPaths(itemGuid)); 

				for(String path : itemPaths){
					List<String> editions = findEditions(path, treeAnalyzer.isNavon(i), publishingFlag.equalsIgnoreCase("y"));
					IPSGuid folderGuid = cmgr.getIdByPath(path);
					if (folderGuid != null){
						for( String currEdition : editions){
							addToPODList.addWork(i, folderGuid, itemGuid, Integer.parseInt(currEdition));
						}
					}
				}
			}
			//0------------New-Publish-On-Demand-Code----------------------0
			//Add the items in the "add this list" into the static POD Queue
			for(PODWork w: addToPODList.workList){
				PODQueue.put(w);
			}
			//0------------------------------------------------------------0
			//Old PODQueue Test Code-------------------------------
			//Make list of items
			//List<Integer> workList = new ArrayList<Integer>();
			//workList.add(i);
			//PODWork newWork = new PODWork(workList, Integer.parseInt(currEdition));
			//Put the work on the queue
			//for(int iii=0; iii<=20; iii++){
			//	PODQueue.put(newWork);
			//}
			//------------------------------------------------------
		}
	} catch (Exception nfx) {
		log.error("CGVOnDemandPublishServce::queueItemSet", nfx);
	}


	//------------------------------------------------------------------



	//run the editions
	log.debug("DEBUG: running editions");
	log.debug("CGVOnDemandPublishServce::queueItemSet done");

}*/

/**
 * Returns the list of editions to run for an item based off of
 * its site path, and if it is a navon or not.
 * @param path - path of the item
 * @param navon - is the item a navon or not? (different site's in the edition list)
 * @param publicOnDemand - find editions for preview on demand?
 * @return the list of editions to run, null if there was any issues.
 *
private List<String> findEditions(String path, boolean navon, boolean publicOnDemand){

	List<String> editions = new ArrayList<String>();
	Map<String, Map<String,List<String>>> siteEditionMap = null;
	
	StringTokenizer st = new StringTokenizer(path, "/");
	String sitePath = "";
	if(st.countTokens() >= 2){
		st.nextToken();
		sitePath = st.nextToken();
	}
	else{
		return editions;
	}
	if(!navon){
		if(sitePath.equalsIgnoreCase("")){
			return editions;
		}
	}
	else{
		if(!sitePath.equalsIgnoreCase("")){
			sitePath += "Navon";
		}
	}

	

	siteEditionMap = editionList.get(sitePath);
	if (publicOnDemand) {
		for(String edition : siteEditionMap.get("publish_onDemandEditionId")){
			editions.add(edition);
		}
	}
	else {
		for(String edition : siteEditionMap.get("preview_onDemandEditionId")){
			editions.add(edition);
		}
	}
	return editions;
}
*/

