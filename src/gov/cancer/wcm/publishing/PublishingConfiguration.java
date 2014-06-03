/**
 * 
 */
package gov.cancer.wcm.publishing;

/**
 * @author learnb
 *
 */
public class PublishingConfiguration {

	private PublishingJobBlockerCollection blockingEditions;
	
	public PublishingJobBlockerCollection getBlockingEditions(){
		return this.blockingEditions;
	}
	
	public PublishingConfiguration(PublishingJobBlockerCollection blockingEditions){
		this.blockingEditions = blockingEditions;
	}
}
