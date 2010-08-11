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
public class CGV_OnDemandPublishService implements InitializingBean {
	private static final Log log = LogFactory
			.getLog(CGV_OnDemandPublishService.class);

	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;

	private int onDemandEditionId;
	private boolean waitForStatus = false;
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
		log.debug("Initializing CGV_OnDemandPublishService...");
	}

	/**
	 * Put item and parents on queue, publish the queue
	 * @param contentId
	 * @param contentTypeId
	 */
	public void queueItemSet(int contentId, int contentTypeId) {
		
		//log.debug("CGV_OnDemandPublishService::queueItemSet executing...");
		initServices();
		
		// get edition id
//TODO: is the constant correct?
		onDemandEditionId = CGVConstants.EDITION_ID;
		
		List<Integer> idsToPublish = null;	//the list to publish
		if (!topType(contentTypeId)) {
			//if this is not the ultimate parent, get parents
			idsToPublish = getParents(contentId);
			System.out.println("\n\tItem CID: " + contentId);
			//log.debug("Need to publish " + idsToPublish.size() + " items");
		}
		try {
			IPSRxPublisherService rxsvc = PSRxPublisherServiceLocator
					.getRxPublisherService();
			PSDemandWork work = new PSDemandWork();
			if (idsToPublish == null || idsToPublish.size() == 0) {
				log.debug("queueItemSet: no items");
			}
			else {
				//add the parents and children to the queue
				for (int i : idsToPublish) {
					IPSGuid itemGuid = gmgr.makeGuid(i, PSTypeEnum.LEGACY_CONTENT);
//TODO: is this correct?
//					IPSGuid itemGuid = i.getGUID();
					String path = cmgr.findFolderPaths(itemGuid)[0];
					IPSGuid folderGuid = cmgr.getIdByPath(path);
					if (folderGuid != null){
						work.addItem(folderGuid, itemGuid);
					}
				}
			}
						
			//work.
			long workId = rxsvc.queueDemandWork(onDemandEditionId, work);
			Long jobId = rxsvc.getDemandRequestJob(workId);

			//Invoke the Publish routine
			IPSPublisherJobStatus status = rxsvc.getPublishingJobStatus(jobId.longValue());

			int count = status.countTotalItems();
			//Undocumented: -1 is apparently a code for something going wrong
			if (count == -1) {
				log.debug("Took a long time to queue items");
			} else {
				log.debug("Queued " + count + " items");
			}
		} catch (Exception nfx) {
			log.error("CGV_OnDemandPublishServce::queueItemSet", nfx);
		}
		log.debug("CGV_OnDemandPublishServce::queueItemSet done");
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
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
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
		List<Integer>localPublishList = null;
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
		
		Long typeId = item.getContentTypeId();
		if (!topType(typeId.intValue())) {
			try {
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
							tempList.add(p);
						}
					}
				}
				for (int tItem : tempList) {
					//add the items to the list to be returned
					localPublishList.add(tItem);
				}
			}
		}
		if (localPublishList == null) {
			localPublishList = new ArrayList<Integer>();
			localPublishList.add(currItemId);
		}
		return localPublishList;
	}

}
