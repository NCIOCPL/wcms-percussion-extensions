package gov.cancer.wcm.extensions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;


/**
 * Workflow action to manage publication through a queue
 * @author whole based on RM
 *
 * @version $Revision: 1.0 $
 */
public class CGV_OnDemandPublishContent extends PSDefaultExtension
      implements IPSWorkflowAction
{
   /**
    * Logger for this class
    */
	private static final Log LOGGER = LogFactory.getLog(CGV_OnDemandPublishContent.class);
	private boolean bDebug = true;	//print to console if true
	/**
    * Service class to invoke publishing routine
    */
   private static CGV_OnDemandPublishService svc = null;
   
   /**
    *	Constructor 
    */
   public CGV_OnDemandPublishContent()
   {
      super();
   }
   
   /**
    * Initializing the Service class. 
    * @param request IPSRequestContext
    */
   private static void initServices(IPSRequestContext request)
   {
      if(svc == null)
      {
         svc = CGV_OnDemandPublishServiceLocator.getCGV_OnDemandPublishService();
//    	  svc = new CGV_OnDemandPublishService();
      }
      svc.setRequest(request);
   }
   
   /**
    * This is the action method for the workflow action.  It adds the content id of the content item 
    * that got updated to the queue set.  It invokes the util method of the CGV_OnDemandPublishService()
    * util class
    * 
    * @see com.percussion.extension.IPSWorkflowAction#performAction(com.percussion.extension.IPSWorkFlowContext, com.percussion.server.IPSRequestContext)
    */
   public void performAction(IPSWorkFlowContext wfContext, 
		   IPSRequestContext request) throws PSExtensionProcessingException{
	   
	   if (bDebug) System.out.println("DEBUG: performAction");
	   initServices(request);
	   if (bDebug) System.out.println("DEBUG: performAction Initted services");
	   int contentId = wfContext.getContentID();
	   if (bDebug) System.out.println("DEBUG: performAction content id is " + contentId);
	   svc.queueItemSet(contentId, request);
	   if (bDebug) System.out.println("DEBUG: performAction queue item set is done running");
   }
}
