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
	public static Work take(){
		log.debug("Trying to take work off the queue.");
		//checkInit();
		try {
			return publishingQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
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


