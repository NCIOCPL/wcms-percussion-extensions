package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGVConstants;
import gov.cancer.wcm.util.CGV_ParentChildManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;


import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;


/**
 * Queues items and their parents for on-demand publishing
 * @author whole based on mudumby
 *
 */
public class CGV_OnDemandPublishService implements InitializingBean {
	private static final Log log = LogFactory.getLog(CGV_OnDemandPublishService.class);
	private boolean bDebug = true;	//if true print statements to console
//TODO: set bDebug to false when done debugging
	
	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;

	private int onDemandEditionId = 315;
	private boolean waitForStatus = true;
	private int timeOut = 20000;
	private int waitTime = 100;

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
//		log.debug("Initializing CGV_OnDemandPublishService...");
		if (bDebug) System.out.println("Initializing CGV_OnDemandPublishService...");
	}

	/**
	 * Put item and parents on queue, publish the queue
	 * @param contentId
	 * @param contentTypeId
	 */
	public void queueItemSet(int contentId) {
		if (bDebug) System.out.println("start of queue item set");

		//log.debug("CGV_OnDemandPublishService::queueItemSet executing...");
		initServices();
		if (bDebug) System.out.println("after init services has run!");		

		List<Integer> idsToPublish = null;	//the list to publish
		if (bDebug) System.out.println("before checking of the top type");
		//if (!topType(contentTypeId.intValue())) {
		//if this is not the ultimate parent, get parents
		idsToPublish = getParents(contentId);
		if (bDebug) System.out.println("\n\tItem CID: " + contentId);
		log.debug("Need to publish " + idsToPublish.size() + " items");
		try {
			IPSRxPublisherService rxsvc = PSRxPublisherServiceLocator
			.getRxPublisherService();
			PSDemandWork work = new PSDemandWork();
			if (idsToPublish == null || idsToPublish.size() == 0) {
				log.debug("queueItemSet: no items");
				if (bDebug) System.out.println("DEBUG: queueItemSet: no items");
			}
			else {
				if (bDebug) System.out.println("DEBUG: Processing parents");
				//add the parents and children to the queue
				for (int i : idsToPublish) {
					IPSGuid itemGuid = gmgr.makeGuid(i, PSTypeEnum.LEGACY_CONTENT);
					if (bDebug) System.out.println("DEBUG: the item guid is " + itemGuid);
					String path = cmgr.findFolderPaths(itemGuid)[0];
					IPSGuid folderGuid = cmgr.getIdByPath(path);
					if (folderGuid != null){
						if (bDebug) System.out.println("DEBUG: Adding item");
						if (bDebug) System.out.println("folder id is " + folderGuid);
						if (bDebug) System.out.println("item guid is " + itemGuid );
						work.addItem(folderGuid, itemGuid);
						if (bDebug) System.out.println("after adding the item");
					}
				}
			}

			long workId = rxsvc.queueDemandWork(onDemandEditionId, work);
			System.out.println("work id is = " +workId);
			Long jobId = rxsvc.getDemandRequestJob(workId);
			System.out.println("job id is = " + jobId);
			if (waitForStatus) {
				int totalTime = 0;
				while (jobId == null && totalTime < timeOut) {
					System.out.println("in the while loop");
					jobId = rxsvc.getDemandRequestJob(workId);
					if (jobId == null) {
						System.out.println("in the if (jobid == null)");
						totalTime += waitTime;
						Thread.sleep(waitTime);
					}
				}
				System.out.println("job id is = after while = " + jobId);
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
					if (bDebug) System.out.println("Queuing the items timed out.");
					break;
				case -1:
					log.debug("Took a long time to queue items");
					if (bDebug) System.out.println("Took a long time to queue items");
					break;
					default:
						log.debug("Queued " + count + " items");
						if (bDebug) System.out.println("Queued " + count + " items");
				}
			} else {
				log.debug("Tried to send " + idsToPublish.size()
						+ " to Queue, not waiting for response");
			}
		} catch (Exception nfx) {
			log.error("GSAOnDemandPublishServce::queueItemSet", nfx);
		}
		log.debug("GSAOnDemandPublishServce::queueItemSet done");
	}

	/**
	 * 
	 */
	public void afterPropertiesSet() throws Exception {
		initServices();
	}

	/**
	 * Returns true if this contentTypeId is in the list of topmost content types
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	private boolean topType(int contentTypeId) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.TOP_CONTENT_TYPE_NAMES;
		for (String s : doNotPublishParentTypes) {
			if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			PSContentTypeSummary summaryItem = summaries.get(0);
			if (contentTypeId == summaryItem.getGuid().getUUID()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get recursive list of all parent content items to this item
	 * @param currItemId
	 * @return List of parent items
	 */
	private List<Integer> getParents(int currItemId) {
		if (bDebug) System.out.println("beginning of get parent");
		List<Integer>localPublishList = null;	//list of items to return
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
		if (bDebug) System.out.println("before checking the top type");

		Long typeId = item.getContentTypeId();
		if (!topType(typeId.intValue())) {
			//if this is a topmost content type, don't get the parents
			if (bDebug) System.out.println("!top type statement");
			try {
				if (bDebug) System.out.println("getParents before get parents cids");
				IPSGuid cid = gmgr.makeGuid(new PSLocator(currItemId));
				localPublishList = pcm.getParentCIDs(cid);	//gets 1 layer of parents
				if (bDebug) System.out.println("got localPublishList");
			} catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			if (localPublishList != null && localPublishList.size() > 0) {
				//create a temp list to hold new parents items so we don't screw the loop
				List<Integer>tempList = new ArrayList<Integer>();
				for (int sItem : localPublishList) {
					List<Integer> parentsList = this.getParents(sItem);	//recurses! foiled again! 
					if (parentsList != null) {
						for (int p : parentsList) {
							if (bDebug) System.out.println("DEBUG: parent item CID: " + p);
							tempList.add(p);
						}
					}
				}
				if (bDebug) System.out.println("before temp list");
				for (int tItem : tempList) {
					//add the items to the list to be returned
					localPublishList.add(tItem);
				}
			}
		}
		if (localPublishList == null) {
			//if didn't get any parents, create list and add current item to it
			if (bDebug) System.out.println("got into the null list");
			localPublishList = new ArrayList<Integer>();
			localPublishList.add(currItemId);
		}
		return localPublishList;
	}

	/**
	 * @param onDemandEditionId
	 */
	public void setOnDemandEditionId(int onDemandEditionId) {
		this.onDemandEditionId = onDemandEditionId;
	}

	/**
	 * @return int onDemandEditionId
	 */
	public int getOnDemandEditionId() {
		return onDemandEditionId;
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
