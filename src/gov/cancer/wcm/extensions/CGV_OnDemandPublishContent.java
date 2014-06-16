package gov.cancer.wcm.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import gov.cancer.wcm.publishing.CGV_OnDemandPublishService;
import gov.cancer.wcm.publishing.CGV_OnDemandPublishServiceLocator;
import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;

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
	private static final Log log = LogFactory.getLog(CGV_OnDemandPublishContent.class);
	
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
	   
	   //if the content item does not have isExclusive set, we know that it is
	   //the item which was transitioned (the last item to move through the workflow)
	   if(!ContentItemWFValidatorAndTransitioner.isExclusive(request)){
		   log.debug("performAction");
		   initServices(request);
		   
		   log.debug("performAction Initted services");
		   int contentId = wfContext.getContentID();
		   
		   log.debug("performAction content id is " + contentId);
		   svc.publishOnDemand(contentId);
		   
		   log.debug("performAction queue item set is done running");
	   }
	   else
	   {
		   // get content id and type for debug message
		   String page = request.getRequestPage(false);
		   int contentId = wfContext.getContentID();
		   log.debug("performAction skipping excluded content id of " + contentId + " (type = " + page + ")");
	   }
	   
   }
}
