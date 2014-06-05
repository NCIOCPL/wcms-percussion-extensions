/**
 * Collection of Publishing edition blocking rules.
 * should prevent them from running.
 */
package gov.cancer.wcm.publishing;

import java.util.HashMap;
import java.util.List;

/**
 * @author learnb
 *
 */
public class PublishingJobBlockerCollection {

	// Allows an O(1) look up of the blocker object for a specific POD edition.
	HashMap<String, PublishingJobBlocker> editionMap = new HashMap<String, PublishingJobBlocker>();
	PublishingJobBlocker defaultBlocker;
	
	public PublishingJobBlockerCollection(List<PublishingJobBlocker> blockers, PublishingJobBlocker defaultBlocker){
		for (PublishingJobBlocker item : blockers) {
			editionMap.put(item.getPODEdition(), item);
		}
		
		this.defaultBlocker = defaultBlocker;
	}
	
	/** Locates the PublishingJobBlocker for the named edition.
	 * @param editionName name of the edition to retrieve a PublishingJobBlocker.
	 * @return The matching PublishingJobBlocker object, or null if one does not exist.
	 */
	public PublishingJobBlocker getPublishingBlocker(String editionName){
		PublishingJobBlocker blocker;
		if(editionMap.containsKey(editionName))
			blocker = editionMap.get(editionName);
		else
			blocker = defaultBlocker;
		
		return blocker;
	}
}
