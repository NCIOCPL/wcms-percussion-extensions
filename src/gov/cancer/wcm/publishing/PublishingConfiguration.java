package gov.cancer.wcm.publishing;

/** Configuration class for publishing.  This replaces the crufty (and unused?)
 * CGV_OnDemandPublishService-beans bean.
 * @author learnb
 *
 */
public class PublishingConfiguration {

	private PublishingJobBlockerCollection publicationBlockers;
	
	/**Retrieve the collection of publishing edition blocking rules.
	 * @return A PublishingJobBlockerCollection object. Never null.
	 */
	public PublishingJobBlockerCollection getPublishingJobBlockers(){
		return this.publicationBlockers;
	}
	
	public PublishingConfiguration(PublishingJobBlockerCollection blockingEditions){
		this.publicationBlockers = blockingEditions;
	}
}
