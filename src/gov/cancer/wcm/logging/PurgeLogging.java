package gov.cancer.wcm.logging;

/**
 * Manages Purge Logging.
 *  
 * @author learnb
 *
 */public class PurgeLogging {

	 
	 public void LogItemState(int contentID){
		PurgeLoggingDataAccess plda = new PurgeLoggingDataAccess();
		plda.LogItemState(contentID);
	}
}
