/**
 * 
 */
package gov.cancer.wcm.privateArchive;

import gov.cancer.wcm.util.RelationshipUtil;
import gov.cancer.wcm.workflow.WorkflowConfiguration;
import gov.cancer.wcm.workflow.WorkflowConfigurationLocator;
import gov.cancer.wcm.workflow.WorkflowUtil;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * @author learnb
 *
 */
public class PrivateArchiveManager {

	private static Log log = LogFactory.getLog(PrivateArchiveManager.class);

	// Rhythmyx service interfaces
	private static IPSCmsContentSummaries contentSummariesService;
	protected static IPSGuidManager guidManager;
	private static IPSSystemWs systemWebService;
	static {
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		guidManager = PSGuidManagerLocator.getGuidMgr();
		systemWebService = PSSystemWsLocator.getSystemWebservice();
	}

	
	/**
	 * Different parts of PrivateArchiveManager need to identify the content
	 * using the integer ID, the IPSGuid ID, or the entire PSComponentSummary. Some of
	 * these are used to retrieve the others. Rather than look them each up multiple
	 * times, we create a local class to hold them.
	 * 
	 *  This is slightly cleaner than cluttering the main routine with multiple lookups.
	 *  
	 * @author learnb
	 */
	private class ItemMetadate {
		private int _itemId;
		private IPSGuid _itemGuid;
		PSComponentSummary _itemSummary;
		
		public int getId(){
			return _itemId;
		}
		
		public IPSGuid getGuid(){
			return _itemGuid;
		}
		
		public PSComponentSummary getSummary(){
			return _itemSummary;
		}
		
		public ItemMetadate(int itemId, IPSGuid itemGuid, PSComponentSummary itemSummary){
			_itemId = itemId;
			_itemGuid = itemGuid;
			_itemSummary = itemSummary;
		}
	}
	
	// Utility class for looking up workflow information.
	WorkflowUtil _workflowUtil;
	
	/**
	 * Retrieves metadata about the content items which are relationship owners
	 * relative to the item identified by dependentID.
	 * @param dependentID Identifies a content item.
	 * @return A list of zero or more ContentSummary objects.
	 */
	public List<ContentSummary> getParentItems(int dependentID){
		log.trace("Entering getParentItems(int dependentID)");
		log.debug("dependentID = " + dependentID);
		
		List<ContentSummary> theList = new ArrayList<ContentSummary>();
		
		try {
			List<PSComponentSummary> itemDetails = RelationshipUtil.FindParentContentItems(dependentID);
			log.debug("Number of items found " + itemDetails.size());
			
			for(PSComponentSummary item: itemDetails){
				log.debug(item);
				ContentSummary summary = new ContentSummary(item.getName(), _workflowUtil.getWorkflowStateName(item), item.getContentId());
				theList.add(summary);
			}
			
		} catch (PSErrorException e) {
			// Deliberately swallow the exception.  Worst case is mis-reporting
			// the number of parent items instead of breaking the Private archive page.
			log.error(e);
		}

		return theList;
	}

	/**
	 * Moves dependentID to the next available archive state.  If no archive transition
	 * is available, an error is logged.
	 * @param dependentID Content ID of the item to be archived.
	 * @throws PrivateArchiveException 
	 */
	public void performArchiveTransition(int dependentID) throws PrivateArchiveException{
		log.trace("Entering performArchiveTransition(int dependentID)");
		log.debug("dependentID = " + dependentID);

		// One-stop-shopping for the item's IDs.
		ItemMetadate idInfo = getItemMetadata(dependentID);
		
		// Get the archive transition.
		String transitionName = getArchiveTransition(idInfo);

		// If we found the transition name, peform the transition.
		// Otherwise, record the error.
		if(transitionName != null){
			IPSGuid[] guidArray = new IPSGuid[1];
			guidArray[0] = idInfo.getGuid();
			try {
				PrivateArchiveWFTransition.transitionItem(dependentID, transitionName, "", null);
				//systemWebService.transitionItems(Arrays.asList(guidArray), transitionName);
			} catch (Exception e) {
				// Handling both PSErrorException and PSErrorsException.
				log.error("Error performing transition " + transitionName, e);
				throw new PrivateArchiveException("Unable to perform transition " + transitionName, e);
			}
		} else {
			log.error("No archive transition found.");
			throw new PrivateArchiveException("No archive transition found.");
		}
	}
	
	private String getArchiveTransition(ItemMetadate dependent){
		log.trace("Entering getArchiveTransition(ItemMetadate dependent)");
		
		String transitionName;
		
		// Get the workflow name.
		PSWorkflow workflow = _workflowUtil.getWorkflow(dependent.getSummary());
		PSState workflowState = _workflowUtil.getWorkflowState(dependent.getSummary());
		log.debug("Workflow = " + workflow.getName() + " State = " + workflowState.getName());

		// Determine which, if any, of the available transitions is used for archiving.
		WorkflowConfiguration config = WorkflowConfigurationLocator.getWorkflowConfiguration();
		transitionName = config.getTransitionMappings().getArchiveTransitionName(workflow, workflowState);
		
		return transitionName;
	}
	
	private ItemMetadate getItemMetadata(int dependentID){
		log.trace("Entering getItemMetadata(int dependentID)");
		
		// Get the PSGuid for dependentID.
		PSComponentSummary itemSummary = contentSummariesService.loadComponentSummary(dependentID);
		IPSGuid[] guidArray = new IPSGuid[1];
		guidArray[0] = guidManager.makeGuid(itemSummary.getCurrentLocator());

		ItemMetadate data = new ItemMetadate(dependentID, guidArray[0], itemSummary);
		return data;
	}

	public PrivateArchiveManager(){
		_workflowUtil = new WorkflowUtil();
	}
}
