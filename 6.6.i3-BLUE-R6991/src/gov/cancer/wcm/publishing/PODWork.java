/**
 * 
 */
package gov.cancer.wcm.publishing;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;

/**
 * @author doylejd
 *
 */

public class PODWork implements Work {

	private static final Log log = LogFactory.getLog(PODWork.class);
	private String workID;
	private Integer transitionItemID;
	private String transitionItemName;
	private String transitionUser;
	private Long timeEntered;
	private List<PublishItem> items;
	private int edition;
	private static final int WAIT_TIME = 500; //half a second

	public PODWork(List<PublishItem> items, int edition){
		this.items = items;
		this.edition = edition;
	}

	public boolean addItem(PublishItem item){
		if(!items.contains(item)){
			items.add(item);
		}
		return true;
	}
	
	public Integer getTransitionItemID(){
		return transitionItemID;
	}
	
	public String getTransitionItemName() {
		return transitionItemName;
	}
	
	public String getTransitionUser() {
		return transitionUser;
	}
	
	public void setTransitionItemID(Integer transitionItemID){
		this.transitionItemID = transitionItemID;
	}
	
	public void setTransitionItemName(String transitionItemName) {
		this.transitionItemName = transitionItemName;
	}
	
	public void setTransitionUser(String transitionItemUser) {
		this.transitionUser = transitionItemUser;
	}
	
	public void setTimeEntered(Long timeEntered) {
		this.timeEntered = timeEntered;
	}
	
	public void setWorkID(String workID) {
		this.workID = workID;
	}
	
	public Long timeWaiting() {
		return System.currentTimeMillis() - timeEntered;
	}

	public int getEdition(){
		return edition;
	}
	
	public String getWorkID(){
		return workID;
	}

	public List<PublishItem> getItems(){
		return items;
	}

	
	@Override public boolean equals(Object rightSide) {
		if(this == rightSide) {return true;}
		
		if(!(rightSide instanceof PODWork)) {return false;}
		
		
		PODWork work = (PODWork) rightSide;
		if (this.workID.equals(work.getWorkID()))
		{
			return true;
		}
		else {return false;}
	}
	

	@Override
	public boolean doWork() {
		publish();
		return true;
	}

	@Override
	public String print() {
		String returnString = "";
		returnString = "Edition: " + edition + "\t\tItems (" + items.size() + "): ";
		for(PublishItem item : items){
			returnString += (item.contentID + ", ");
		}
		returnString += "end.";
		//log.debug(returnString);
		return returnString;
	}

	private void publish(){
		
		PSDemandWork work = new PSDemandWork();
		for(PublishItem item: items){
			work.addItem(item.folderGuid, item.itemGuid);
		}
		IPSRxPublisherService rxsvc = PSRxPublisherServiceLocator
		.getRxPublisherService();

		//Get Work IDS-----------------
		
		//Add the current work item to the Percussion publishing Queue.
		long workId = rxsvc.queueDemandWork(edition, work);
		//get the jobId to find out if this item has been pulled off the queue
		//yet. (Returns null if still in queue)
		Long jobId = rxsvc.getDemandRequestJob(workId);
		
		
		//POD CODE***********************************************************

		int totalTime = 0;
		//while (jobId == null && totalTime < timeOut) {
		while (jobId == null) {
			//we enter this loop if the job is still waiting on the Queue
			jobId = rxsvc.getDemandRequestJob(workId);
			if (jobId == null) {
				totalTime += WAIT_TIME;
				try{Thread.sleep(WAIT_TIME);}catch(InterruptedException e){e.printStackTrace();}
			}
		}
		
		
	} 


}

/*IPSPublisherJobStatus.State state;

totalTime = 0;

do {
	//keep checking the state to find out when it is done Queuing all of the content
	state = rxsvc.getDemandWorkStatus(workId);
	totalTime += WAIT_TIME;
	try{Thread.sleep(WAIT_TIME);}catch(InterruptedException e){e.printStackTrace();}
} while (state == IPSPublisherJobStatus.State.QUEUEING);
//while (state == IPSPublisherJobStatus.State.QUEUEING
//	&& totalTime < timeOut);		//THIS MAY NEED TO BE ADDED BACK IN

//Once it is done queuing, get the publishing status
IPSPublisherJobStatus status = rxsvc.getPublishingJobStatus(jobId.longValue());
log.error("Queued " + status.countTotalItems() + " items");
log.error("It took: " + status.getElapsed() +" ms to publish this item");
//END OF POD CODE***********************************************************/