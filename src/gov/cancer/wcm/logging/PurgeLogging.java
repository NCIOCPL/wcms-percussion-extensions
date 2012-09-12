package gov.cancer.wcm.logging;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.error.PSException;
import com.percussion.pso.utils.RxItemUtils;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.*;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.*;

/**
 * Manages Purge Logging.
 *  
 * @author learnb
 *
 */public class PurgeLogging {

	 private IPSGuidManager guidManager = null;
	 
	 public PurgeLogging(){
		 guidManager = PSGuidManagerLocator.getGuidMgr();
	 }

	 /**
	  * Purges a percussion content item after first recording:
	  * 	The item's title,
	  * 	User ID of the person performing the purge,
	  * 	Date/Time of the purge operation,
	  * 	Item's workflow state at the time it was purged, and
	  * 	The path(s), if any, where the item was stored.
	  * 
	 * @param contentID Percussion content ID of the item to purge.
	 * @param username	User id of the user requesting the purge.
	 * @throws LoggingException 
	 */
	public void LogAndPurge(int contentID, String username)
		throws LoggingException{
		
		try {
			PurgeLoggingDataAccess plda = new PurgeLoggingDataAccess();
	
			PSCoreItem item = FetchSingleContentItem(contentID);
			String title = getFieldValue(item, "sys_title");
			String workflowState = getWorkflowState(item);
			
			plda.LogItemState(contentID, title, username, workflowState);
		} catch(LoggingException e) {
			throw e;
		}
	}
	
	/**
	 * Fetches a single content item from Percussion.
	 * 
	 * @param contentID Percussion content ID of the item to purge.
	 * @return	The PSCoreItem representing the content item.
	 * @throws LoggingException
	 */
	private PSCoreItem FetchSingleContentItem(int contentID) 
		throws LoggingException{
		
		IPSGuid contentGuid = guidManager.makeGuid(contentID, PSTypeEnum.LEGACY_CONTENT);
		ArrayList<IPSGuid> itemList = new ArrayList<IPSGuid>();
		itemList.add(contentGuid);
		
		IPSContentWs contentSvc = PSContentWsLocator.getContentWebservice();
	
		try {
			List<PSCoreItem> items = contentSvc.loadItems(itemList, false, false, false, true);
			if(items.isEmpty()){
				String message = format("Unable to fetch item with content id {0}.", contentID);
				throw new LoggingException(message);
			}
			return items.get(0);
		} catch (PSErrorResultsException e) {
			String message = format("Error fetching item with content id {0}.", contentID);
			throw new LoggingException(message, e);
		}
	}

	private String getFieldValue(PSCoreItem item, String fieldname){
		String fieldName = null;
		
		try {
			fieldName = RxItemUtils.getFieldValue(item, "sys_title");
		} catch (PSCmsException e) {
			fieldName = format("Unable to find field '{0}'.", fieldname);
		}
		
		return fieldName;
	}
 
	private String getWorkflowState(PSCoreItem item){

		PSOWorkflowInfoFinder finder = new PSOWorkflowInfoFinder();
		
		try {
			String name = finder.findWorkflowStateName(Integer.toString(item.getContentId()));
			return name;
		} catch (PSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "Gone!";
	}
 }
