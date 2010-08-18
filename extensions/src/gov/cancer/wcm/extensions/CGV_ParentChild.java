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
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.content.data.PSContentTypeSummary;
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

	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;

	public CGV_ParentChild() {
		super();
		initServices();
	}
	
	public void performAction(IPSWorkFlowContext arg0, IPSRequestContext request)
			throws PSExtensionProcessingException {
		// TODO Auto-generated method stub
		
		System.out.println("Parent/child: Calling extension...");
		
		
		CGV_StateHelper stateHelp = new CGV_StateHelper(request);
		StateName currState = stateHelp.getCurrState();
		StateName destState = stateHelp.getDestState();
		String currStateString = stateHelp.currStateToString();
		String destStateString = stateHelp.destStateToString();
		String currCID = request.getParameter("sys_contentid");
		int transitionID = stateHelp.getTransitionID();
		System.out.println("Parent/child: Transition = " +transitionID);
		PSOWorkflowInfoFinder workInfo = new PSOWorkflowInfoFinder();
		IPSGuid currentItem =  PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(currCID));
		int numSharedChildren = 0;
		StateName childHoldState = null;
		int childHoldRevision = 0;
		int pendingCode = 0;
		
		if( pcm.isCheckedOut(currentItem) ){
			return;
		}
		
		boolean pending = false;
		
		
		Long cid = CGV_ParentChildManager.loadItem(currCID).getContentTypeId();
		//If PAGE TYPE...
		if(topType(cid.intValue())){
			
			StateName lowState = StateName.PUBLIC;
			
			List<PSItemSummary> children = null;
			try {
				children = pcm.getChildren(currentItem);
			} catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for( PSItemSummary currChild : children ){
				PSState childState = null;
				try {
					childState = workInfo.findWorkflowState(Integer.toString(pcm.getCID(currChild.getGUID()).get(0)));
				} catch (PSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PSErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StateName childStateName = stateHelp.toStateName(childState.getName());
				String childStateString = childState.getName();
				if(CGV_StateHelper.compare( childStateString , destState.toString()) == -1
						|| CGV_StateHelper.compare(childStateString, destState.toString()) == 0){
					
					List<PSItemSummary> childParents = null;
					try {
						childParents = cmgr.findOwners(currChild.getGUID(), null, false);
					} catch (PSErrorException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(childParents.size() > 1){
						numSharedChildren++;
						//find the low state of all the shared parents.
						for( PSItemSummary currParent : childParents ){
							PSState parentsState = null;
							try {
								parentsState = workInfo.findWorkflowState(Integer.toString(pcm.getCID(currParent.getGUID()).get(0)));
							} catch (PSException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (PSErrorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							StateName parState = stateHelp.toStateName(parentsState.getName());
							if( (CGV_StateHelper.compare(parentsState.getName(), lowState.toString())== -1)
									&& (currParent.getGUID() != currentItem) ){ //if( currParent.state < lowState && currParent != current )
								lowState = parState;
							}
						}
						
						if( (CGV_StateHelper.compare(destState.toString(), lowState.toString()) == 1 
								|| CGV_StateHelper.compare(destState.toString(), lowState.toString()) == 0)
								&& (CGV_StateHelper.compare(childStateString, lowState.toString()) == 1 
										|| CGV_StateHelper.compare(childStateString, lowState.toString()) == 0)
								&& CGV_StateHelper.compare(childStateString, destState.toString()) == -1 
								/*|| CGV_StateHelper.compare(childStateString, destState.toString()) == 0) */  )
						{
							System.out.println("Pending code 2");
							if(pendingCode < 2 ){pendingCode = 2;}
							pending = true;
							if( childHoldState == null ){
								childHoldState = stateHelp.toStateName(childStateString);
							}
							else if( CGV_StateHelper.compare(childHoldState.toString(), childStateString) == 1 ){
								childHoldState = stateHelp.toStateName(childStateString);
							}
							childHoldRevision = pcm.getRevision(currChild.getGUID());
						}
					}
					if(!stateHelp.existsDirectPath(currState, destState)){	//the child cannot reach the destination state in 1 move.
						System.out.println("Pending code 3");
						if(pendingCode < 3 ){pendingCode = 3;}
						pending = true;
						if( childHoldState == null ){
							childHoldState = stateHelp.toStateName(childStateString);
						}
						else if( CGV_StateHelper.compare(childHoldState.toString(), childStateString) == 1 ){
							childHoldState = stateHelp.toStateName(childStateString);
						}
					}
					if(pcm.isCheckedOut(currChild.getGUID())){	//if the current child is checked out, then pending.
						System.out.println("Pending code 1");
						if(pendingCode < 1 ){pendingCode = 1;}
						pending = true;
						if( childHoldState == null ){
							childHoldState = stateHelp.toStateName(childStateString);
						}
						else if( CGV_StateHelper.compare(childHoldState.toString(), childStateString) == 1 ){
							childHoldState = stateHelp.toStateName(childStateString);
						}
					}
				}
			}
			//if(pending){System.out.println("we are pending for something!");}
			//end of for statement.	
			if(!pending){
				if( stateHelp.getDestState() != null ){
					transition(currentItem, currState, destState, stateHelp, pending, true);
				}
				
				for( PSItemSummary currChild : children ){
					List<PSItemSummary> parentsSize = null;
					try {
						parentsSize = cmgr.findOwners(currChild.getGUID(), null, false);
					} catch (PSErrorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(parentsSize.size() == 1 ){
						if(!stateHelp.isBackwardsMove(currState, destState)){
							transition(currChild.getGUID(), currState, destState, stateHelp, pending, false);
						}
					}
				}
			}
			else{	//if pending
				//if(destState != childHoldState && childHoldRevision != 0 ){
				if(!stateHelp.isMapping(childHoldState,destState)){
					String transitionFind = stateHelp.backwardsPath(currState, destState);
					List<IPSGuid> temp = Collections.<IPSGuid>singletonList(currentItem);
					IPSSystemWs sysws = PSSystemWsLocator.getSystemWebservice();
					if(transitionFind.equalsIgnoreCase("ResubmitReapproveArchive")){
						try {
							sysws.transitionItems(temp, "Resubmit");
						} catch (PSErrorsException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (PSErrorException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							sysws.transitionItems(temp, "Reapprove");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							sysws.transitionItems(temp, "Archive");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if(transitionFind.equalsIgnoreCase("ReviseResubmit")){
						try {
							sysws.transitionItems(temp, "Revise");
						} catch (PSErrorsException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (PSErrorException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							sysws.transitionItems(temp, "Resubmit");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if(transitionFind.equalsIgnoreCase("ResubmitReapprove")){
						try {
							sysws.transitionItems(temp, "Resubmit");
						} catch (PSErrorsException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (PSErrorException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							sysws.transitionItems(temp, "Reapprove");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						try {
							sysws.transitionItems(temp, transitionFind);
						} catch (PSErrorsException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (PSErrorException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				
				
				//send message for pending;
				
			}

		}	//end of if statement (if top type)
		else if(destState == StateName.PENDING){
			transition(currentItem, currState, destState, stateHelp, pending, false);
		}
		

	}


	/**
	 * Returns true if this contentTypeId is in the list of topmost content types
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	private boolean topType(int contentTypeId) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.TOP_CONTENT_TYPE_NAMES;
		for (String s : doNotPublishParentTypes) {
			//if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			//if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			PSContentTypeSummary summaryItem = summaries.get(0);
			if (contentTypeId == summaryItem.getGuid().getUUID()) {
				return true;
			}
		}
		return false;
	}
	

	public boolean canModifyStyleSheet() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static boolean transition(IPSGuid source, StateName currState, StateName destState, CGV_StateHelper stateHelp, boolean pending, boolean parent){
		List<IPSGuid> temp = Collections.<IPSGuid>singletonList(source);
		System.out.println("transition debug: current GUID: " + source);
		System.out.println("transition debug: current state: " + stateHelp.toString(currState));
		System.out.println("transition debug: destination state: " + stateHelp.toString(destState));
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
			case PENDING:
				transition = "Approve";
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
				transition = "Reapprove";
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
		case PENDING:
			switch (destState){
			case REVIEW:
				transition = "backToReview";
				break;
			case PUBLIC:
				transition = "ForcetoPublic";
				break;
			default:
				transition = "Null";
			}
		default:
			transition = "Null";
			break;	
		}
		if( transition != "Null"){
			System.out.println("Parent/Child: transition being called from...");
			System.out.println("\t"+transition+" = "+ stateHelp.toString(currState)+"-->"+stateHelp.toString(destState));
			IPSSystemWs sysws = PSSystemWsLocator.getSystemWebservice();
			if(destState == StateName.PENDING ){
				if(!parent){
					if(!pending){
						try {
							sysws.transitionItems(temp, transition);
						} catch (PSErrorsException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (PSErrorException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							sysws.transitionItems(temp, "ForcetoPublic");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{}	//nothing for children
				}
				else{	//parent
					if(!pending){
						try {
							sysws.transitionItems(temp, "ForcetoPublic");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						try {
							sysws.transitionItems(temp, "backToReview");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			else if(destState == StateName.REVIEW){
				if(parent){
					if(pending){
						try {
							sysws.transitionItems(temp, "Disapprove");
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else{
					if(!pending){
						try {
							sysws.transitionItems(temp, transition);
						} catch (PSErrorsException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PSErrorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			else{
				System.out.println("!dest review, or dest pending");
				try {
					sysws.transitionItems(temp, transition);
				} catch (PSErrorsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PSErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return true;
		}
		return false;
	}

	private static void initServices() {
		if (rps == null) {
			rps = PSRxPublisherServiceLocator.getRxPublisherService();
			gmgr = PSGuidManagerLocator.getGuidMgr();
			cmgr = PSContentWsLocator.getContentWebservice();
			pcm = new CGV_ParentChildManager();
		}
	}
}
