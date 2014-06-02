/**
 * Implements a FIFO queue for storing items to be published.
 */
package gov.cancer.wcm.publishing;

import java.util.concurrent.BlockingQueue;
/**
 * @author doylejd
 *
 */

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PODQueue {
	private static final Log log = LogFactory.getLog(PODQueue.class);
	private static BlockingQueue<PODWork> publishingQueue = null;
	private static final int ARRAY_SIZE = 1000;
	
	static{
		publishingQueue = new ArrayBlockingQueue<PODWork>(ARRAY_SIZE);
	}
	
	
	public BlockingQueue<PODWork> getPublishingQueue() {
		return publishingQueue;
	}

	/*
	 * Retrieve the next publish on demand task eligible for processing.
	 */
	public static Work take(){
		log.debug("Trying to take work off the queue.");

		Work itemToPublish = null;
		
		try {
			/*
			 * POD tasks must not be performed when a blocking job (e.g. Full Publish)
			 * is in progress. To determine the first (if any) task eligible:
			 * 
			 * If no items are found, block until something is found.
			 */
			itemToPublish = publishingQueue.take();

			/*  Now that an item has been found, test whether it's eligible
			 *  to be published. If the item is not eligible to publish, examine
			 *  the rest of the queue, polling until something becomes eligible.
			 */
			
			
			/*  For each item in the queue
			 *  	Look at the item's edition.\
			 *  	Get the edition's list of blockers.
			 *  	Are any of the blockers running?
			 *  		Yes: Skip to the next item.
			 *  		No: Return this item.
			 *  
			 */
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	
	/**
	 * Return a copy of the current queue for other functions to act on.
	 * NOTE:  This is just a copy of the current queue, since it is a static object.
	 */
	public static BlockingQueue<PODWork> returnQueueCopy(){
		//BlockingQueue<PODWork> returnThis = publishingQueue;
		return publishingQueue;
	}
	/*
	private static void checkInit(){
		if(publishingQueue == null ){
			publishingQueue = new ArrayBlockingQueue<PODWork>(ARRAY_SIZE);
		}
	}*/
	

}


