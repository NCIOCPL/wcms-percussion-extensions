package gov.cancer.wcm.extensions;

import java.util.Collections;
import java.util.List;
import gov.cancer.wcm.util.*;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;					//exception
import com.percussion.extension.PSExtensionProcessingException;		//exception
import com.percussion.server.IPSRequestContext;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.content.PSContentWsLocator;


/**
 * This Preprocessor adds the current Content Item and its specific parent
 * and child item into a workflow transition so that families of content move
 * through workflows together.
 * 
 * The CGov_childParentMoving extends PSDefaultExtension class and 
 * implements IPSRequestPreProcessor interface.
 * 
 * @author John Walls
 *
 */
public class CGV_ParentChild extends PSDefaultExtension implements
IPSWorkflowAction {
	//private static Log LOGGER = LogFactory.getLog(CGov_TitlePopulate.class);
	private static IPSContentWs cws = null;
	public CGV_ParentChild() {
		super();
		initServices();
	}

	/**
	 * Takes all children items and moves them along the workflow with their parent item.  Some
	 * logic determines what state an item can go into.
	 * @see com.percussion.extension.IPSWorkflowAction#performAction(com.percussion.extension.IPSWorkFlowContext, com.percussion.server.IPSRequestContext)
	 */
	public void performAction(IPSWorkFlowContext wfContext, 
			IPSRequestContext request) throws PSExtensionProcessingException{
		
		System.out.println("Debugging the workflow action for parent child");
		IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
		IPSGuid cid = gmgr.makeGuid(new PSLocator(request.getParameter("sys_contentid")));
		
		CGV_ParentChildManager pcm = new CGV_ParentChildManager(cid);
		List<PSItemSummary> children = null;
		try {
			children = pcm.getChildren();
		} catch (PSErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n\tParent Item CID: " + request.getParameter("sys_contentid"));
		
		System.out.println("\tPrinting children content ids");
		for( PSItemSummary a : children ){
			System.out.println("\t\tType: " + a.getContentTypeId());
			System.out.println("\t\tGUID: " + a.getGUID());
			System.out.println("\t\tContent id: " + gmgr.makeLocator(a.getGUID()).getId());
			List<IPSGuid> items = Collections.<IPSGuid>singletonList(a.getGUID());
			try {
				PSSystemWsLocator.getSystemWebservice().transitionItems(items, "DirecttoPublic");
			} catch (PSMissingBeanConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PSErrorsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		if( request.getParameter("syts_contentid") == "322" )
//		{
//			System.out.println("kicking into the if statement for the debug");
//			IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
//			IPSGuid cid = gmgr.makeGuid(new PSLocator(322));
//			List<IPSGuid> glist = Collections.<IPSGuid>singletonList(cid);
//			IPSSystemWs sws = PSSystemWsLocator.getSystemWebservice();
//			try {
//				sws.transitionItems(glist, "DirecttoPublic");
//			} catch (PSErrorsException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (PSErrorException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

	}



	public boolean canModifyStyleSheet() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void debugStatement(IPSRequestContext request) {
		System.out.println(" ");

		String currentCid = request.getParameter("sys_contentid");
		System.out.println("DEBUG: Current content id = " + currentCid);
		
		String currStateString = request.getParameter("sys_contentstateid");
		System.out.println("DEBUG: Current state = " + currStateString);
		
		String state = request.getParameter("sys_state_name");
		System.out.println("DEBUG: Current state = " +state);
		
		String curr = request.getParameter("sys_contenttypeid");
		System.out.println("DEBUG: Current content type ID = " + curr);
		
		String folder = request.getParameter("sys_folderid");
		System.out.println("DEBUG: Folder if = " + folder);
		
		String syspath = request.getParameter("sys_path");
		System.out.println("DEBUG: The system path is = " + syspath);
		
		String trans = request.getParameter("sys_transitionid");
		System.out.println("DEBUG: The transition id = " + trans);
		
		String work = request.getParameter("sys_workflowid");
		System.out.println("DEBUG: Workflow id = " + work);
	}

	private static void initServices() {
		if (cws == null) {
			cws = PSContentWsLocator.getContentWebservice();
		}
	}

//	@Override
//	public void performAction(IPSWorkFlowContext arg0, IPSRequestContext arg1)
//			throws PSExtensionProcessingException {
//		// TODO Auto-generated method stub
//
//		String currentCid = arg1.getParameter("sys_contentid");
//		System.out.println("DEBUG: Current content id = " + currentCid);
//		
//		//String currStateString = arg1.getParameter("sys_statename");
//		//System.out.println("DEBUG: Current state = " + currStateString);
//		
//		System.out.println("DEBUG: The current state ID is " + arg0.getStateID());
//		
//	}
}
