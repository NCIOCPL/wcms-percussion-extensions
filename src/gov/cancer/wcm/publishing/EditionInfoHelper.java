package gov.cancer.wcm.publishing;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

/*
 * Utility class for working with publishing editions.
 */
/**
 * @author learnb
 *
 */
public class EditionInfoHelper {
	private static final Log log = LogFactory.getLog(EditionInfoHelper.class);
	
	private IPSPublisherService pubSvc = PSPublisherServiceLocator.getPublisherService();
	private IPSRxPublisherService rxPubSvc = PSRxPublisherServiceLocator.getRxPublisherService();
	private PublishingConfiguration pubConfig = PublishingConfigurationLocator.getPublishingConfiguration();

	// Dual-mapping for ways to look up edition information.
	private HashMap<Integer, IPSEdition> editionIDMap = new HashMap<Integer, IPSEdition>();
	private HashMap<String, IPSEdition> editionNameMap = new HashMap<String, IPSEdition>();
	
	public EditionInfoHelper(){
		log.trace("Create EditionHelper");

		// There's a certain temptation to move the edition retrievals into a static block so it only
		// loads once.  The problem is, this requires a restart in order to pick up any new editions.
		// Maybe put it into a static block, and then reload it in the case where an edition isn't
		// found? But that gets tricky when you decide to avoid having the same code twice.
		List<IPSEdition> editionList = pubSvc.findAllEditions("");
		for(IPSEdition edition : editionList){
			String name = edition.getName();
			int id = edition.getGUID().getUUID();
			
			editionIDMap.put(id, edition);
			editionNameMap.put(name, edition);
		}
	}
	
	
	/**
	 * Retrieves the PublishingJobBlocker object for the specified edition ID.
	 * 
	 * @param editionID - ID of the edition to look up. 
	 * @return The PublishingJobBlocker object matching editionId. If none is found,
	 * the default blocker is returned.
	 */
	public PublishingJobBlocker GetEditionBlocker(int editionId){
		log.trace("EditionIsBlocked(" + editionId + ")" );

		// Find the edition
		String editionName = GetEditionName(editionId);
		if(editionName == null){
			log.debug("Edition " + editionId + "not found. Using default.");
			editionName = ""; // Name shouldn't matter. We just want whatever the default blocker is. 
		}
		
		// Get the edition's list of blocking rules.
		return pubConfig.getPublishingJobBlockers().getPublishingBlocker(editionName);
	}

	
	/**
	 * @param editionId ID of the edition to look up.
	 * @return The edition's friendly name, or null if not found.
	 */
	public String GetEditionName(int editionId){
		log.trace("Enter GetEditionName(" + editionId + ")");
		
		String editionName;
		
		// Get the edition's config info.
		if(editionIDMap.containsKey(editionId)){
			editionName = editionIDMap.get(editionId).getName();
		} else {
			log.debug("Edition " + editionId + "not found.");
			editionName = null;
		}
		
		return editionName;
	}

	
	/**
	 * @param editionName Name of the edition to look up.
	 * @return True if the edition is currently running.
	 */
	public boolean EditionIsRunning(String editionName){
		log.trace("Enter EditionIsRunning( " + editionName + " )");
		
		boolean IS_RUNNING = true;
		boolean NOT_RUNNING = false;
		
		boolean runningStatus;

		// Get edition details.
		IPSEdition editionDetails = null;
		if(editionNameMap.containsKey(editionName))
			editionDetails = editionNameMap.get(editionName);
		
		// Query whether it's running.
		if(editionDetails != null){
			long jobId = rxPubSvc.getEditionJobId(editionDetails.getGUID());
			if(jobId > 0) {
				runningStatus = IS_RUNNING;
				log.debug("Edition " + editionName + " is currently running.");
			} else {
				runningStatus = NOT_RUNNING;
				log.debug("Edition " + editionName + " is not running.");
			}
		} else {
			log.error("Unable to locate editions details for " + editionName + ". Treating as not running.");
			runningStatus = NOT_RUNNING;
		}

		return runningStatus;
	}
	
}
