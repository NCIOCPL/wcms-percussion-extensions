package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_FolderValidateUtils;
import gov.cancer.wcm.util.CGV_ParentChildManager;
import gov.cancer.wcm.util.CGV_TopTypeChecker;
import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;


import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.server.IPSRequestContext;
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
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;


/**
 * Queues items and their parents for on-demand publishing
 * @author whole based on mudumby
 *
 */
public class CGV_OnDemandPublishService implements InitializingBean {
	private static final Log log = LogFactory.getLog(CGV_OnDemandPublishService.class);

	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;
	private static IPSWorkflowService workflowService;
	private static IPSCmsContentSummaries contentSummariesService;

	private static CGV_FolderValidateUtils valUtil = null;
	private IPSRequestContext request = null;
	private Map<String,Map<String,List<String>>> editionList;
	private boolean waitForStatus = true;
	private int timeOut = 20000;
	private int waitTime = 100;
	private Map<String,List<String>> autoSlot;

	public Map<String, List<String>> getAutoSlot() {
		return autoSlot;
	}

	public void setAutoSlot(Map<String, List<String>> autoSlot) {
		this.autoSlot = autoSlot;
	}

	public Map<String, Map<String, List<String>>> getEditionList() {
		return editionList;
	}

	public void setEditionList(Map<String, Map<String, List<String>>> editionList) {
		this.editionList = editionList;
	}


	//TODO: replace doNotPublishParentTypes with String[], remove declaration further down, configure in xml
	//as:
	//<property name="doNotPublishParentTyptes">
	//	<list>
	//		<value>sometype</value>
	//		<value>anothertype</value>
	//	</list>
	//</property>
	//	private List<String> doNotPublishParentTypes;

	/**
	 * Initialize service pointers.
	 * 
	 * @param cmgr
	 */
	protected static void initServices() {
		if (rps == null) {
			rps = PSRxPublisherServiceLocator.getRxPublisherService();
			gmgr = PSGuidManagerLocator.getGuidMgr();
			cmgr = PSContentWsLocator.getContentWebservice();
			pcm = new CGV_ParentChildManager();
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
	}

	public CGV_OnDemandPublishService() {

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
	 * Put item and parents on queue, publish the queue
	 * @param contentId
	 * @param contentTypeId
	 */
	public void queueItemSet(int contentId, IPSRequestContext request) {

		//List<String> editions = new ArrayList<String>();

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
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(id);	
		int wfState = contentItemSummary.getContentStateId();
		int wfId = contentItemSummary.getWorkflowAppId();	
		//		PSWorkflow workflow = workflowService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW, wfId));
		PSState startState = workflowService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE, wfState),
				new PSGuid(PSTypeEnum.WORKFLOW,wfId));	   
		//		Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
		//		WorkflowValidationContext wvc = new WorkflowValidationContext(request, contentItemSummary, log, errorDoc, workflow, startState, Integer.parseInt(request.getParameter("sys_transitionid")));
		//		String destination = wvc.getDestinationState().getName();
		String publishingFlag = startState.getContentValidValue();

		log.debug("start of queue item set");

		//log.debug("CGV_OnDemandPublishService::queueItemSet executing...");
		initServices();
		log.debug("after init services has run!");		

		List<Integer> idsToPublish = null;	//the list to publish
		log.debug("before checking of the top type");
		//if this is not the ultimate parent, get parents
		idsToPublish = getParents(contentId, navon);
		log.debug("Item CID: " + contentId);
		log.debug("Need to publish " + idsToPublish.size() + " items");

		try {
			IPSRxPublisherService rxsvc = PSRxPublisherServiceLocator
			.getRxPublisherService();

			if (idsToPublish == null || idsToPublish.size() == 0) {
				log.debug("queueItemSet: no items");
			}
			else {
				log.debug("Processing parents");
				//add the parents and children to the queue
				for (int i : idsToPublish) {
					IPSGuid itemGuid = gmgr.makeGuid(i, PSTypeEnum.LEGACY_CONTENT);
					log.debug("the item guid is " + itemGuid);

					//Get site edition list.-------------------------------------------
					List<String> itemPaths = Arrays.asList(cmgr.findFolderPaths(itemGuid)); 

					for(String path : itemPaths){
						PSDemandWork work = new PSDemandWork();
						List<String> editions = findEditions(path, CGV_TopTypeChecker.navon(contentTypeId.intValue(),cmgr), publishingFlag.equalsIgnoreCase("y"));

						IPSGuid folderGuid = cmgr.getIdByPath(path);
						if (folderGuid != null){
							log.debug("Adding item");
							log.debug("folder id is " + folderGuid);
							log.debug("item guid is " + itemGuid );
							work.addItem(folderGuid, itemGuid);
							log.debug("after adding the item");
						}

						for( String currEdition : editions){
							long workId = rxsvc.queueDemandWork(Integer.parseInt(currEdition), work);
							log.debug("work id is = " +workId);
							Long jobId = rxsvc.getDemandRequestJob(workId);
							log.debug("job id is = " + jobId);

							if (waitForStatus) {
								int totalTime = 0;
								while (jobId == null && totalTime < timeOut) {
									log.debug("in the while loop");
									jobId = rxsvc.getDemandRequestJob(workId);
									if (jobId == null) {
										log.debug("in the if (jobid == null)");
										totalTime += waitTime;
										Thread.sleep(waitTime);
									}
								}
								log.debug("job id is = after while = " + jobId);
								int count;
								if (jobId == null)
									count = -2;
								else {
									IPSPublisherJobStatus.State state;
									totalTime = 0;
									do {
										state = rxsvc.getDemandWorkStatus(workId);
										totalTime += waitTime;
										Thread.sleep(waitTime);
									} while (state == IPSPublisherJobStatus.State.QUEUEING
											&& totalTime < timeOut);
									if (state == IPSPublisherJobStatus.State.QUEUEING)
										count = -1;
									else {
										IPSPublisherJobStatus status = rxsvc.getPublishingJobStatus(jobId.longValue());
										count = status.countTotalItems();

									}
								}
								switch(count){
								case -2:
									log.debug("Queuing the items timed out.");
									break;
								case -1:
									log.debug("Took a long time to queue items");
									break;
								default:
									log.debug("Queued " + count + " items");
								}
							} else {
								log.debug("Tried to send " + idsToPublish.size()
										+ " to Queue, not waiting for response");
							}
						}

					}




				}
			}
		} catch (Exception nfx) {
			log.error("CGVOnDemandPublishServce::queueItemSet", nfx);
		}


		//------------------------------------------------------------------



		//run the editions
		log.debug("DEBUG: running editions");
		log.debug("CGVOnDemandPublishServce::queueItemSet done");

	}

	/**
	 * 
	 */
	public void afterPropertiesSet() throws Exception {
		initServices();
	}

	/**
	 * Get recursive list of all parent content items to this item
	 * @param currItemId
	 * @return List of parent items
	 */
	private List<Integer> getParents(int currItemId, boolean navon) {
		log.debug("getParents: beginning of get parent");
		List<Integer>localPublishList = null;	//list of items to return

		if(navon){
			localPublishList = new ArrayList<Integer>();	//list of items to return
			localPublishList.add(currItemId);
		}
		else{
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
			if (psCS.getContentId() != currItemId) {
				log.debug("getParents: not transition root, id:" + currItemId);
				localPublishList = new ArrayList<Integer>();	//list of items to return
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
			Long typeId = item.getContentTypeId();
			//		if(CGV_TopTypeChecker.URLAutoSlotType(typeId.intValue(),cmgr) ){
			//			//|| 	CGV_TopTypeChecker.TopicSearchAutoSlotType(typeId.intValue(),cmgr)){
			//			try {
			//				IPSGuid cid = gmgr.makeGuid(new PSLocator(currItemId));
			//				localPublishList = pcm.getParentCIDs(cid, true, autoSlotConfigType);	//gets 1 layer of parents
			//			} catch (PSErrorException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//				return null;
			//			}
			//		}
			boolean isTop = ContentItemWFValidatorAndTransitioner.isTopType(typeId.intValue(),wvc);
			if(isTop){
				//if this is a topmost content type, don't get the parents
				//if didn't get any parents, create list and add current item to it
				log.debug("getParents: is top type, got into the null list");
				if (localPublishList == null) {
					localPublishList = new ArrayList<Integer>();	//list of items to return
				}
				localPublishList.add(currItemId);
			}
			else
			{
				log.debug("getParents: !top type statement");
				try {
					log.debug("getParents: before get parents cids");
					IPSGuid cid = gmgr.makeGuid(new PSLocator(currItemId));
					localPublishList = pcm.getParentCIDs(cid, false, 0);	//gets 1 layer of parents
					log.debug("getParents: got localPublishList");
				} catch (PSErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				if (localPublishList != null && localPublishList.size() > 0) {
					//create a temp list to hold new parents items so we don't screw the loop
					List<Integer>tempList = new ArrayList<Integer>();
					for (int sItem : localPublishList) {
						List<Integer> parentsList = this.getParents(sItem, navon);	//recurses! foiled again! 
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
						localPublishList.add(tItem);
					}
				}
			}
			List<Integer> addToList = new ArrayList<Integer>();
			//Check auto slot list in the config file.
			addToList = CGV_TopTypeChecker.autoSlotChecker(typeId.intValue(),cmgr, autoSlot);
			if( !addToList.isEmpty() ){
				if(localPublishList == null)
					localPublishList = new ArrayList<Integer>();
				for( Integer addInteger : addToList ){
					localPublishList.add(addInteger);
				}
			}

			//Always add the current item to the list
			if (localPublishList == null) {
				log.debug("getParents: null list, creating and adding individual item " + currItemId);
				//if didn't get any parents, create list and add current item to it
				localPublishList = new ArrayList<Integer>();
				localPublishList.add(currItemId);
			}
			else{
				if (!isTop) {
					//if top type, already on list
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
		return localPublishList;
	}

	/**
	 * Returns the list of editions to run for an item based off of
	 * its site path, and if it is a navon or not.
	 * @param path - path of the item
	 * @param navon - is the item a navon or not? (different site's in the edition list)
	 * @param publicOnDemand - find editions for preview on demand?
	 * @return the list of editions to run, null if there was any issues.
	 */
	private List<String> findEditions(String path, boolean navon, boolean publicOnDemand){

		StringTokenizer st = new StringTokenizer(path, "/");
		String sitePath = "";
		if(st.countTokens() >= 2){
			st.nextToken();
			sitePath = st.nextToken();
		}
		else{
			return null;
		}
		if(!navon){
			if(sitePath.equalsIgnoreCase("")){
				return null;
			}
		}
		else{
			if(!sitePath.equalsIgnoreCase("")){
				sitePath += "Navon";
			}
		}

		Map<String,List<String>> siteEditionMap = null;
		List<String> editions = new ArrayList<String>();

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

	public void setRequest(IPSRequestContext request) {
		this.request = request;
	}


}
