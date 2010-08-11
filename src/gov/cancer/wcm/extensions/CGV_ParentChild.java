package gov.cancer.wcm.extensions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.cancer.wcm.util.*;
import gov.cancer.wcm.util.CGV_StateHelper.StateName;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;					//exception
import com.percussion.extension.PSExtensionProcessingException;		//exception
import com.percussion.server.IPSRequestContext;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;


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

				
		//boolean pending = false;	//There are no dependents.
		//PSItemStatus item = request.getParameterObject(CGVConstant.PSITEMSTATUS);
		//String currentCid = request.getParameter("sys_contentid");
		//String guid = request.getParameter(CGVConstants.GUID);
		
		//FUNCTIONALITY
		//PSLocator loc = PSOItemSummaryFinder.getCurrentOrEditLocator(request.getParameter("sys_contentid"));
		//String currStateString = request.getParameter("sys_contentstateid");
		//StateName currentState = getStateName(currStateString);
		//StateName destinationState = ;	//TODO: Find the destination state of the workflow for the current item
		//PSItemSummary current = request.getParameterObject("currentItem");
		//String currentType = request.getParameter("sys_contenttypeid"); 	//current.getContentTypeName();


		
		
		
		
		CGV_StateHelper stateHelp = new CGV_StateHelper(request);
		//CGV_ParentChildManager pcmgr = new CGV_ParentChildManager();
		//StateName currState = stateHelp.getCurrState();
		//StateName destState = stateHelp.getDestState();
		String currCID = request.getParameter("sys_contentid");
		
		System.out.println("DEBUG: typeID: " + CGV_ParentChildManager.loadItem(currCID).getContentTypeId());
		System.out.println("DEBUG: currentState: " + stateHelp.currStateToString());
		System.out.println("DEBUG: destinationState: " + stateHelp.destStateToString());
		System.out.println("DEBUG: transitionID: " + stateHelp.getTransitionID());
//		if( currentType == "page")
//		{
//			List<PSItemSummary> children = getChildren(current.getGUID());
//			//List<PSItemSummary> children = cws.findDependents(current.getGUID(), null, true);
//			for( PSItemSummary currChild : children ){
//				StateName childState = currChild.state;
//				if( StateName.compare(childState, destinationState) == -1){ //currChild.state < destinationState 
//					List<PSItemSummary> childParents = cws.findOwners(currChild.getGUID(), null, false);
//					if(childParents.size() > 1){
//						//Find lowest parent state
//						StateName lowState = StateName.PUBLIC;
//						//PSState lowState = staticHighestState;
//						for( PSItemSummary currParent : childParents ){
//							StateName parState = currParent.state;
//							if( (StateName.compare(parState, lowState)== -1)
//									&& (currParent.getGUID() != current.getGUID()) ){ //if( currParent.state < lowState && currParent != current )
//								lowState = parState;
//							}
//						}
//						if( ((StateName.compare(destinationState,childState)==0)||(StateName.compare(destinationState,childState)==1))
//								&& ((StateName.compare(childState,lowState)==0)||(StateName.compare(childState,lowState)==1))){ 
//							//if( destinationState >= currChild.state && currChild.state >= lowState )
//							//currChild.transition( findTransition(currChild.state, lowerState(destinationState, lowState));
//							transition(currChild, childState, destinationState);
//							if( currChild.state == lowState){
//								pending = true;
//							}
//						}
//					}
//					else{	// !sharedChild(cws, currChild.getGUID())
//						transition(currChild, childState, destinationState);
//					}
//				}
//			}
//			if(pending && destinationState == StateName.REVIEW){
//				//current.transition( findTransition(current.state, destinationState));
//				transition(current, current.state, destinationState);
//			}
//			else if(!pending){
//				//current.transition(findTransition(current.state, destinationState));
//				transition(current, current.state, destinationState);
//			}
//			else{
//				//TODO: error msg
//				//LOGGER.debug("Cannot move the current item into the destinationState, there are dependencies.");
//			}
//		}
//		else{	//currType != page
//			if( destinationState > current.state ){
//				//current.transition(findTransition(current.state, destinationState));
//			}
//
//		}
		
		//DEBUG CODE-----------------------------------------------------------
//		System.out.println("Debugging the workflow action for parent child");
//		IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
//		IPSGuid cid = gmgr.makeGuid(new PSLocator(request.getParameter("sys_contentid")));
//		
//		CGV_ParentChildManager pcm = new CGV_ParentChildManager(cid);
//		List<PSItemSummary> children = null;
//		try {
//			children = pcm.getChildren();
//		} catch (PSErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("\n\tParent Item CID: " + request.getParameter("sys_contentid"));
//		
//		System.out.println("\tPrinting children content ids");
//		for( PSItemSummary a : children ){
//			System.out.println("\t\tType: " + a.getContentTypeId());
//			System.out.println("\t\tGUID: " + a.getGUID());
//			System.out.println("\t\tContent id: " + gmgr.makeLocator(a.getGUID()).getId());
//			List<IPSGuid> items = Collections.<IPSGuid>singletonList(a.getGUID());
//			try {
//				PSSystemWsLocator.getSystemWebservice().transitionItems(items, "DirecttoPublic");
//			} catch (PSMissingBeanConfigurationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
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
	
	public static boolean transition(PSItemSummary source, StateName currState, StateName destState){
		List<IPSGuid> temp = Collections.<IPSGuid>singletonList(source.getGUID());
		String transition;
		switch (currState){
		case DRAFT:
			switch (destState){
			case REVIEW:
				transition = "Submit";
				break;
			default:
				transition = "Null";
			}
			break;
		case REVIEW:
			switch (destState){
			case DRAFT:
				transition = "Disapprove";
				break;
			case PUBLIC:
				transition = "ForcetoPublic";
				break;
			default:
				transition = "Null";
			}
			break;
		case PUBLIC:
			switch (destState){
			case ARCHIVED:
				transition = "Archive";
				break;
			case EDITING:
				transition = "Quick Edit";
				break;
			default:
				transition = "Null";
			}
			break;
		case EDITING:
			switch (destState){
			case REAPPROVAL:
				transition = "Resubmit";
				break;
			default:
				transition = "Null";
			}
			break;
		case REAPPROVAL:
			switch (destState){
			case EDITING:
				transition = "Disapprove";
				break;
			case PUBLIC:
				transition = "Approve";
				break;
			default:
				transition = "Null";
			}
			break;
		case ARCHIVED:
			switch (destState){
			case EDITING:
				transition = "Revive";
				break;
			case PUBLIC:
				transition = "Republish";
				break;
			default:
				transition = "Null";
			}
			break;
		default:
			transition = "Null";
			break;	
		}
		if( transition != "Null"){
			try {
				PSSystemWsLocator.getSystemWebservice().transitionItems(temp, transition);
			} catch (PSMissingBeanConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PSErrorsException e) {
				// TODO Auto-generated catch blocks
				e.printStackTrace();
			} catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
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
