/**
 * Identifies a single publish-on-demand edition and the other publishing
 * editions which should prevent it from running.
 */
package gov.cancer.wcm.publishing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author learnb
 *
 */
public class PublishingJobBlocker {
	private static final Log log = LogFactory.getLog(PublishingJobBlocker.class);
	
	static final boolean BLOCKED = true;
	static final boolean NOT_BLOCKED = false;

	private String blockedEdition = null;
	private List<String> blockingList = new ArrayList<String>();
	
	public PublishingJobBlocker(String podEdition, List<String> blockingEditions){
		blockedEdition = podEdition;
		blockingList.addAll(blockingEditions);
	}

	/**
	 * @param helper EditionInfoHelper object containing details about the system's publishing editions.
	 * @return True if the edition should be prevented from running, False otherwise.
	 */
	public boolean editionIsBlocked(EditionInfoHelper helper){
		log.trace("Enter editionIsBlocked for " + blockedEdition);

		boolean isBlocked = NOT_BLOCKED;
		
		for(String edition : blockingList){
			// If any blocking edition is running, then the edition
			// is considered to be blocked.
			if(helper.EditionIsRunning(edition)){
				log.debug("Publishing edition " + blockedEdition + " is blocked by " + edition);
				isBlocked = BLOCKED;
				break;
			}
		}
				 
		return isBlocked;
	}

	
	/*
	 * Returns the name of the edition this blocking list applies to.
	 */
	public String getPODEdition(){
		return blockedEdition;
	}
	
	public List<String> getBlockingEditions(){
		return blockingList;
	}
}
