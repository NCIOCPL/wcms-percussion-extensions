package gov.cancer.wcm.publishing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Configuration class for publishing.  This replaces the crufty (and unused?)
 * CGV_OnDemandPublishService-beans bean.
 * @author learnb
 *
 */
public class PublishingConfiguration {

	private PublishingJobBlockerCollection publicationBlockers;
	private List<String> needsNextTopTypeList = new ArrayList<String>();
	private Map<String, String> dependentPodTypesMap = new HashMap<String, String>();
	
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
    
    /**
     * Returns true if a content type owns dependents that should also be published on demand.
     * @param ownerType The type of the owner.
     * @return True if dependents exist, otherwise false.
     */
    public boolean hasDependentPodType(String ownerType) {
    	return dependentPodTypesMap.containsKey(ownerType);
    }
    
    /**
     * Gets any dependent content type to be published on demand along with the owner type.
     * @param ownerType The owner's content type.
     * @return A String representing the dependent content type, if any.  Returns null if no dependency
     * 	exists.
     */
    public String getDependentPodType(String ownerType) {
    	return dependentPodTypesMap.get(ownerType);
    }
	
	public PublishingConfiguration(PublishingJobBlockerCollection blockingEditions, List<String> needsNextTopTypes, 
			Map<String, String> dependentPodTypes){
		this.publicationBlockers = blockingEditions;
		this.needsNextTopTypeList.addAll(needsNextTopTypes);
		this.dependentPodTypesMap.putAll(dependentPodTypes);
	}
}

