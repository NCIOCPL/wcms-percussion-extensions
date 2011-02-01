/**
 * 
 */
package gov.cancer.wcm.extensions;

import gov.cancer.wcm.linkcheck.CGV_LinkChecker;
import gov.cancer.wcm.linkcheck.LinkDataAccess;
import gov.cancer.wcm.linkcheck.LinkItem;
import gov.cancer.wcm.linkcheck.LinkResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.schedule.IPSTask;
import com.percussion.services.schedule.IPSTaskResult;

/**
 * Scheduled task that fetches all nciLink objects from the Percussion db,
 * extracts the URLs, queries them, and stores all 300, 400, and 500-series
 * responses in the database for reporting.
 * 
 * @author holewr
 *
 */
public class CGV_LinkCheckTask implements IPSTask {
    private IPSContentMgr contentManager = null;
    LinkDataAccess lda = null;
    
	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
	 */
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
        if (contentManager == null) contentManager = PSContentMgrLocator.getContentMgr();
		lda = new LinkDataAccess(contentManager);
		boolean haveTable = lda.reportTableExists();
		if (!haveTable) {
			lda.createTable();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.percussion.services.schedule.IPSTask#perform(java.util.Map)
	 */
	public IPSTaskResult perform(Map<String,String> parameters) {
//TODO: pass in path to site?
		nciTaskResult result = new nciTaskResult();
		result.setSuccess(true);
		
		try {
			//get the relevant fields from all nciLink items in the site
			ArrayList<LinkItem> itemList = lda.getLinkItems("//Sites/CancerGov");
			ArrayList<LinkItem> itemErrorList = new ArrayList<LinkItem>();
			//check each link for validity, put ones that don't return 200 in list
			for (LinkItem item : itemList) {
				LinkResult linkResult = CGV_LinkChecker.checkLink(item.getUrl());
				if (linkResult.getCode() != 200) {
					LinkItem badItem = new LinkItem();
					badItem.setContentId(item.getContentId());
					badItem.setSysTitle(item.getSysTitle());
					badItem.setUrl(item.getUrl());
					badItem.setResponse(linkResult.getCode());
					badItem.setMessage(linkResult.getMessage());
					itemErrorList.add(badItem);
				}
				if (lda.checkForOrphanedLink(Integer.parseInt(item.getContentId()))) {
					LinkItem badItem = new LinkItem();
					badItem.setContentId(item.getContentId());
					badItem.setSysTitle(item.getSysTitle());
					badItem.setUrl(item.getUrl());
					badItem.setResponse(1000);
					badItem.setMessage("Orphaned link");
					itemErrorList.add(badItem);
				}
			}
			if (itemErrorList.size() > 0) {
				boolean ok = lda.saveBadLinkItems(itemErrorList);
				if (!ok) {
					result.setSuccess(false);
					result.setProblemDescription("Error in saving link report to database");
				}
			}
		}
		catch (Exception e) {
			log.error("Failure in checking links: " + e.getLocalizedMessage());
			result.setProblemDescription("Failure in checking links: " + e.getLocalizedMessage());
			result.setSuccess(false);
		}
		return result;
	}

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(CGV_LinkCheckTask.class);

    /**
     * Implementation of IPSTaskResult
     * @author holewr
     *
     */
    private class nciTaskResult implements IPSTaskResult {
    	private String problemDescription;
		private boolean success = true;
    	
    	/* (non-Javadoc)
    	 * @see com.percussion.services.schedule.IPSTaskResult#getNotificationVariables()
    	 */
    	public Map<String,Object> getNotificationVariables() {
    		//does nothing
    		Map<String,Object> map = new HashMap<String,Object>();
    		return map;
    	}
    	
    	/* (non-Javadoc)
    	 * @see com.percussion.services.schedule.IPSTaskResult#getProblemDescription()
    	 */
    	public String getProblemDescription() {
    		return problemDescription;
    	}
    	
    	/* (non-Javadoc)
    	 * @see com.percussion.services.schedule.IPSTaskResult#wasSuccess()
    	 */
    	public boolean wasSuccess() {
    		return success;
    	}
    	
    	/**
    	 * @param problemDescription
    	 */
    	public void setProblemDescription(String problemDescription) {
			this.problemDescription = problemDescription;
		}

		/**
		 * @param success
		 */
		public void setSuccess(boolean success) {
			this.success = success;
		}
    }
    
}
