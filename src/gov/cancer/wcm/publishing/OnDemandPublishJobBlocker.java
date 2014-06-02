/**
 * Identifies a single publish-on-demand edition and the other publishing
 * editions which should prevent it from running.
 */
package gov.cancer.wcm.publishing;

import java.util.ArrayList;
import java.util.List;

/**
 * @author learnb
 *
 */
public class OnDemandPublishJobBlocker {

	private String blockedEdition = null;
	private List<String> blockingList = new ArrayList<String>();
	
	public OnDemandPublishJobBlocker(String podEdition, List<String> blockingEditions){
		blockedEdition = podEdition;
		blockingList.addAll(blockingEditions);
	}
	
	/*
	 * Returns the name of the edition this blocking list applies to.
	 */
	public String getPODEdition(){
		return blockedEdition;
	}
}
