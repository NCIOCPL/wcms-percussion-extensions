/**
 * 
 */
package gov.cancer.wcm.publishing;

/**
 * @author doylejd
 *
 */

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;

import gov.cancer.wcm.util.CGV_Logger;


public class PODPublisher implements Runnable{
	private static final Log log = LogFactory.getLog(PODPublisher.class);
	private static Thread t;
	private static PODPublisher INSTANCE = null; 
	private CGV_Logger cgvLog = new CGV_Logger();
	
	public static PODPublisher getInstance(){
		log.debug("Starting the PODThread...");
		if(INSTANCE == null){
			log.debug("Instance was null");
			INSTANCE = new PODPublisher();
			t = new Thread(INSTANCE);
			t.start();
		}
		log.debug("Returning the thread");
		return INSTANCE;
	}
	
	private PODPublisher(){
		}
	
	/**
	 * Initialize services.
	 * 
	 * @param extensionDef
	 * @param codeRoot
	 * @throws PSExtensionException
	 */
	public void init(IPSExtensionDef extensionDef, File codeRoot)
	throws PSExtensionException {
	}

	@Override
	public void run() {
		log.debug("Start of run...");
		while(true){
			try{
				PODQueue.take().doWork();
			}
			catch(Throwable e){
				log.error("Error processing POD job.");
				log.error(cgvLog.StackTraceToString(e.getStackTrace()));
			}
		}
	}
}

/*if(work != null){
log.info("Time to get back to work...");

work.doWork();
log.info("I finished my work!");
}
else{
try {
	Thread.sleep(2000);
} catch (InterruptedException e) {
	e.printStackTrace();
}
}*/

