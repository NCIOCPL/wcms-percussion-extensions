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
import com.percussion.services.content.data.PSItemSummary;
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
public class CGV_OnDemandPublishService /*implements InitializingBean */{
	private static final Log log = LogFactory
	.getLog(CGV_OnDemandPublishService.class);

	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;

	private int onDemandEditionId;
	private boolean waitForStatus = true;	//TODO: set to true in BEAN and false in the class
	private int timeOut = 20000;
	private int waitTime = 100;

	private List<String> doNotPublishParentTypes;

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
		log.debug("Initializing CGV_OnDemandPublishService...");
	}

	/**
	 * Put item and parents on queue, publish the queue
	 * @param contentId
	 * @param contentTypeId
	 */
	public void queueItemSet(int contentId) {
		System.out.println("start of queue item set");

		//log.debug("CGV_OnDemandPublishService::queueItemSet executing...");
		initServices();
		System.out.println("after init services has run!");		
		// get edition id
		//TODO: is the constant correct?
		onDemandEditionId = CGVConstants.EDITION_ID;

		List<Integer> idsToPublish = null;	//the list to publish
		Long contentTypeId = CGV_ParentChildManager.loadItem(Integer.toString(contentId)).getContentTypeId();
		System.out.println("before checking of the top type");
		//if (!topType(contentTypeId.intValue())) {
		//if this is not the ultimate parent, get parents
		idsToPublish = getParents(contentId);
		System.out.println("\n\tItem CID: " + contentId);
		//log.debug("Need to publish " + idsToPublish.size() + " items");
		//}
		try {
			IPSRxPublisherService rxsvc = PSRxPublisherServiceLocator
			.getRxPublisherService();
			PSDemandWork work = new PSDemandWork();
			if (idsToPublish == null || idsToPublish.size() == 0) {
				log.debug("queueItemSet: no items");
				System.out.println("DEBUG: queueItemSet: no items");
			}
			else {
				System.out.println("DEBUG: Processing parents");
				//add the parents and children to the queue
				for (int i : idsToPublish) {
					IPSGuid itemGuid = gmgr.makeGuid(i, PSTypeEnum.LEGACY_CONTENT);
					//TODO: is this correct?
					//					IPSGuid itemGuid = i.getGUID();
					System.out.println("DEBUG: the item guid is " + itemGuid);
					String path = cmgr.findFolderPaths(itemGuid)[0];
					IPSGuid folderGuid = cmgr.getIdByPath(path);
					if (folderGuid != null){
						System.out.println("DEBUG: Adding item");
						System.out.println("folder id is " + folderGuid);
						System.out.println("item guid is " + itemGuid );
						work.addItem(folderGuid, itemGuid);
						System.out.println("after adding the item");
					}
				}
			}

			long workId = rxsvc.queueDemandWork(onDemandEditionId, work);
			Long jobId = rxsvc.getDemandRequestJob(workId);
			if (waitForStatus) {
				int totalTime = 0;
				while (jobId == null && totalTime < timeOut) {
					jobId = rxsvc.getDemandRequestJob(workId);
					if (jobId == null) {
						totalTime += waitTime;
						Thread.sleep(waitTime);
					}
				}
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
						IPSPublisherJobStatus status = rxsvc
						.getPublishingJobStatus(jobId.longValue());
						count = status.countTotalItems();

					}
				}
				if (count == -1) {
					log.debug("Took a long time to queue items");
				} else {
					log.debug("Queued " + count + " items");
				}
			} else {
				log.debug("Tried to send " + idsToPublish.size()
						+ " to Queue, not waiting for response");
			}
		} catch (Exception nfx) {
			log.error("GSAOnDemandPublishServce::queueItemSet", nfx);
		}
		log.debug("GSAOnDemandPublishServce::queueItemSet done");


		//work.
		//			System.out.println("DEBUG: before queue on demand work");
		//			
		//			long workId = rxsvc.queueDemandWork(onDemandEditionId, work);
		//			
		//			System.out.println("ondemand edition id is = to " + onDemandEditionId);
		//			System.out.println("work id is = to " + workId);
		//			System.out.println("DEBUG before get demand request job");
		//			
		//			Long jobId = rxsvc.getDemandRequestJob(workId);
		//			
		//			System.out.println("long jobID = " + jobId);
		//			System.out.println("DEBUG: after getDemandrequest job");
		//
		//			//Invoke the Publish routine
		//			IPSPublisherJobStatus status = rxsvc.getPublishingJobStatus(jobId.longValue());
		//			System.out.println("get status passed");
		//			int count = status.countTotalItems();
		//			System.out.println("count is = to " + count );
		//			//Undocumented: -1 is apparently a code for something going wrong
		//			if (count == -1) {
		//				System.out.println("Took a long time to queue items");
		//				//log.debug("Took a long time to queue items");
		//			} else {
		//				System.out.println("Queued " + count + " items");
		//				//log.debug("Queued " + count + " items");
		//			}
		//		} catch (Exception nfx) {
		//			System.out.println("Throwing the error, and was caught");
		//			log.error("CGV_OnDemandPublishServce::queueItemSet", nfx);
		//		}
		//		log.debug("CGV_OnDemandPublishServce::queueItemSet done");
	}

	/**
	 * 
	 */
	public void afterPropertiesSet() throws Exception {
		initServices();
	}

	public void setOnDemandEditionId(int onDemandEditionId) {
		this.onDemandEditionId = onDemandEditionId;
	}

	public int getOnDemandEditionId() {
		return onDemandEditionId;
	}


	public boolean isWaitForStatus() {
		return waitForStatus;
	}

	public void setWaitForStatus(boolean waitForStatus) {
		this.waitForStatus = waitForStatus;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * Returns true if this contentTypeId is in the list of topmost content types
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	private boolean topType(int contentTypeId) {
		String[] doNotPublishParentTypes = CGVConstants.TOP_CONTENT_TYPE_NAMES;
		for (String s : doNotPublishParentTypes) {
			System.out.print("DEBUG: do not publish parent types " + s);
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			System.out.println("the size of the content type summary list is " + summaries.size());
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
		System.out.println("beginning of get parent");
		List<Integer>localPublishList = null;
		List<IPSGuid> glist = Collections.<IPSGuid> singletonList(gmgr.makeGuid(new PSLocator(currItemId)));
		List<PSCoreItem> items = null;	//TODO: use the parentchild manager codeS
		PSCoreItem item = null;
		try {
			items = cmgr.loadItems(glist, true, false, false, false);
			item = items.get(0);
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("before checking the top type");

		Long typeId = item.getContentTypeId();
		if (!topType(typeId.intValue())) {
			System.out.println("!top type statement");
			try {
				System.out.println("get parents before get parents cids");
				IPSGuid cid = gmgr.makeGuid(new PSLocator(currItemId));
				localPublishList = pcm.getParentCIDs(cid);	//gets 1 layer of parents
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
							System.out.println("DEBUG: parent item CID: " + p);
							tempList.add(p);
						}
					}
				}
				System.out.println("before temp list");
				for (int tItem : tempList) {
					//add the items to the list to be returned
					localPublishList.add(tItem);
				}
			}
		}
		if (localPublishList == null) {
			System.out.println("got into the null list");
			localPublishList = new ArrayList<Integer>();
			localPublishList.add(currItemId);
		}
		return localPublishList;
	}

}
