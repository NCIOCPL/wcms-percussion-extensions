/**
 * Collection of publish-on-demand editions and the publishing editions which
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
	
	public PublishingJobBlockerCollection(List<PublishingJobBlocker> blockers){
		for (PublishingJobBlocker item : blockers) {
			editionMap.put(item.getPODEdition(), item);
		}
	}
}
