package gov.cancer.wcm.extensions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
//import com.percussion.pso.workflow.PSOPublishContent;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;


/**
 * Workflow action to manage publication through a queue
 * @author whole based on RM
 *
 */
public class CGV_OnDemandPublishContent extends PSDefaultExtension
      implements IPSWorkflowAction
{
   /**
    * Logger for this class
    */
//   private static final Log LOGGER = LogFactory.getLog(PSOPublishContent.class);
//TODO: what's with PSOPublishContent? Following line is what I think it should be
	private static final Log LOGGER = LogFactory.getLog(CGV_OnDemandPublishContent.class);
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
    */
   private static void initServices()
   {
      if(svc == null)
      {
         svc =  CGV_OnDemandPublishServiceLocator.getCGV_OnDemandPublishService();
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
	   initServices();
//       int contentId = 0;		//TODO: what is this, what should go here? John thinks 0 has special meaning
	   							//Following line is what I think it should be
	   int contentId = wfContext.getContentID();
	   int typeId = Integer.parseInt(request.getParameter(IPSHtmlParameters.SYS_CONTENTTYPEID));
	   svc.queueItemSet(contentId, typeId);
   }
}
