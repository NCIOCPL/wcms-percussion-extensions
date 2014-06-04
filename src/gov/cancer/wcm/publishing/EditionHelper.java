package gov.cancer.wcm.publishing;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class EditionHelper {
	private static final Log log = LogFactory.getLog(EditionHelper.class);
	
	private IPSPublisherService pubSvc = PSPublisherServiceLocator.getPublisherService();
	private PublishingConfiguration pubConfig = PublishingConfigurationLocator.getPublishingConfiguration();

	// Dual-mapping for ways to look up edition information.
	private HashMap<Integer, IPSEdition> editionIDMap = new HashMap<Integer, IPSEdition>();
	private HashMap<String, IPSEdition> editionNameMap = new HashMap<String, IPSEdition>();
	
	public EditionHelper(){
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
	 * Determines whether editionID should be allowed to run, or prevented from running
	 * because one of its blocking editions is running.
	 * 
	 * @param editionID - ID of the edition we 
	 * @return true if the edition should be blocked from running, false if it should
	 * be allowed to run.
	 */
	public boolean EditionIsBlocked(int editionId){
		boolean NOT_BLOCKED = false;
		boolean BLOCKED = true; 
		
		log.trace("EditionIsBlocked(" + editionId + ")" );

		// Find the edition
		String editionName = GetEditionName(editionId);
		if(editionName == null){
			log.debug("Edition " + editionId + "not found. Handling as not-blocked.");
			return NOT_BLOCKED;
		}
		
		// Get the edition's list of blockers.
		PublishingJobBlocker blockerConfig = pubConfig.getBlockingEditions().getBlockingEditions(editionName);
		if(blockerConfig == null){
			log.debug("No blocking configuration found for edition: " + editionName);
			return NOT_BLOCKED;
		}
		
		// Are any of the blockers running?
		boolean isBlocked = NOT_BLOCKED;
		for(String edition : blockerConfig.getBlockingEditions()){
			// If any blocking edition is running, then the edition
			// is considered to be blocked.
			if(EditionIsRunning(edition)){
				log.debug("Publishing edition " + editionName + " is blocked by " + edition);
				isBlocked = BLOCKED;
				break;
			}
		}
				 
		return isBlocked;
	}

	
	/**
	 * @param editionId ID of the edition to look up.
	 * @return True if the edition is currently running.
	 */
	public boolean EditionIsRunning(int editionId){
		String editionName = GetEditionName(editionId);
		return EditionIsRunning(editionName);
	}
	
	/**
	 * @param editionName Name of the edition to look up.
	 * @return True if the edition is currently running.
	 */
	public boolean EditionIsRunning(String editionName){

		
		return true;
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
}
