package gov.cancer.wcm.publishing;

import java.util.ArrayList;
import java.util.List;

/** Configuration class for publishing.  This replaces the crufty (and unused?)
 * CGV_OnDemandPublishService-beans bean.
 * @author learnb
 *
 */
public class PublishingConfiguration {

	private PublishingJobBlockerCollection publicationBlockers;
	private List<String> needsNextTopTypeList = new ArrayList<String>();
	
	/**Retrieve the collection of publishing edition blocking rules.
	 * @return A PublishingJobBlockerCollection object. Never null.
	 */
	public PublishingJobBlockerCollection getPublishingJobBlockers(){
		return this.publicationBlockers;
	}
	
	/**Retrieve whether or not a content type needs the next top type for publishing.
     * @return a boolean.
     */
	public boolean getNeedsNextTopType (String contentTypeName) {
		boolean needsNextTopType = false;
		if(needsNextTopTypeList.contains(contentTypeName)) {
			needsNextTopType = true;
		}
		return needsNextTopType;
	}
	 
    /**Retrieve the list of content types that need next top type for publishing.
     * @return a List<String> object.
     */
    public List<String> getNeedsNextTopTypeList() {
        return this.needsNextTopTypeList;
    }
	
	public PublishingConfiguration(PublishingJobBlockerCollection blockingEditions, List<String> needsNextTopTypes){
		this.publicationBlockers = blockingEditions;
		this.needsNextTopTypeList.addAll(needsNextTopTypes);
	}
}

