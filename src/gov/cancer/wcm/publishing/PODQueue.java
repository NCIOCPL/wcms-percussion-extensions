/**
 * Implements a FIFO queue for storing items to be published.
 */
package gov.cancer.wcm.publishing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PODQueue {
	private static final Log log = LogFactory.getLog(PODQueue.class);
	private static BlockingQueue<PODWork> publishingQueue = null;
	private static final int ARRAY_SIZE = 1000;
	
	static{
		publishingQueue = new LinkedBlockingDeque<PODWork>(ARRAY_SIZE);
	}
	
	
	public BlockingQueue<PODWork> getPublishingQueue() {
		return publishingQueue;
	}

	/*
	 * Retrieve the next publish on demand task eligible for processing.
	 */
	public static Work take(){
		log.trace("Entering take()");

		PODWork itemToPublish = null;
		
		try {
			/*
			 * POD tasks must not be performed when a blocking job (e.g. Full Publish)
			 * is in progress. To determine the first (if any) task eligible:
			 * 
			 * If no items are found, block until something is found.
			 * Once an item is found, put it back so that if it's not
			 * eligible to run, it won't lose its place in line.
			 */
			log.debug("Trying to take work off the queue.");
			itemToPublish = publishingQueue.take();
			publishingQueue.put(itemToPublish);
			log.trace("Found an item to publish: " + itemToPublish.getEdition());

			/*
			 * PODPublisher.run() goes in a loop, calling PODQueue.take() and when it returns, 
			 * it gets back a Work object which has a doWork() method. The secret sauce 
			 * which makes PODQueue work is that it's a wrapper around a BlockingQueue<T>. 
			 * The take() method on BlockingQueue<T> doesn't return until the queue has 
			 * something in it. (This is how we create a thread that just runs in an infinite 
			 * loop without spending 100% of the CPU time checking whether true is still 
			 * true.) 
			 * 
			 * Our wrapper also exposes a take() method, and the first thing this 
			 * take() method does is to create an EditionInfoHelper object, which is where 
			 * all the logic lives for figuring out whether it's OK for a POD job to run. 
			 * The EditionInfoHelper object is where the CGV_PublishingConfiguration bean 
			 * is used. The fix for the immediate issue is to move the creation of the 
			 * EditionInfoHelper object after the first take()/put() pair, but before the while
			 * loop.
			 */
			EditionInfoHelper editionHelper = new EditionInfoHelper();

			/*  Now that an item has been found, find the first that's eligible
			 *  to be published. If the item is not eligible to publish, examine
			 *  the rest of the queue, polling until something becomes eligible.
			 */
			itemToPublish = null;
			while(true){ // Continue until something is eligible.
				log.trace("Checking for eligible publishing jobs.");

				// For each item in the queue
				for(PODWork workItem : publishingQueue){
					log.trace("Checking Work ID: " + workItem.getWorkID());
					
					// Is the item blocked?
					PublishingJobBlocker blocker = editionHelper.GetEditionBlocker(workItem.getEdition());
					boolean blocked = blocker.editionIsBlocked(editionHelper);
					
					if(blocked){
						// Yes: Skip to the next item.
						log.debug("Edition " + workItem.getEdition() + " is blocked. Continuing.");
						continue;
					} else {
						// No: Return this item.
						log.debug("Edition " + workItem.getEdition() + " is not blocked.");
						itemToPublish = workItem;
						// Explicitly remove since we're only using take() for blocking.
						publishingQueue.remove(itemToPublish);
						break;
					}
				}
				
				// Did we find an item that is not blocked?
				//	Yes: Break out of the loop so the item can be returned.
				//	No: Sleep briefly, then repeat.
				if(itemToPublish != null)
					break;
				else
					Thread.sleep(30000);
			}

		} catch (Throwable e) {
			log.error(e.getMessage());
		}
		return itemToPublish;
	}
	
	/**
	 * Puts work into the queue.  
	 * If the edition already exists, adds the content id list into the already existing work.
	 * Else, adds the new work job into the queue.
	 * @param e - new Work to add
	 */
	public static void put(PODWork workItem){
		log.debug("Putting work on the queue.");
		
		 //For now, We just want to add all new jobs to the Queue.
		
		try {
				if (publishingQueue.remove(workItem)) {
					log.debug("Removed Old work item sitting in Queue: " +workItem.getWorkID());
				}
				log.debug("Putting Work Item on Queue: " + workItem.getWorkID());
				publishingQueue.put(workItem);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//log.info("Queue size after addition: " + publishingQueue.size());
	}
}


