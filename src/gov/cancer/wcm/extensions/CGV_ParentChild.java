package gov.cancer.wcm.extensions;

import java.util.Collections;
import java.util.List;

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
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;


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
		//Queue<Boolean> moveChildList = new LinkedList<Boolean>();

		
		if( pcm.isCheckedOut(currentItem) ){
			return;
		}
		
		boolean pending = false;
		
		
		Long cid = CGV_ParentChildManager.loadItem(currCID).getContentTypeId();
		//If PAGE TYPE...
		if(!CGV_TopTypeChecker.topType(cid.intValue(),cmgr)){
			
			StateName lowState = StateName.PUBLIC;
			
			List<PSItemSummary> children = null;
			try {
				children = pcm.getChildren(currentItem);
			} catch (PSErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/**
			 * For all children of the parent, check for a pending state.
			 * A Pending state is defined as one of three codes:
			 * 
			 * 1. There is a child item (in any state) checked out by ANY user.
			 * 2. A child is a shared item, and is not allowed to go past the lowest state out of all
			 * 		the parents it is shared in.
			 * 3. There is no path from the child item to the destination of the parent.
			 * 		(This deals with the handling checking for if an item is mapped to states)
			 * 
			 */
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
				System.out.println("\tComparing "+childStateString+" to "+destState.toString());
				if(CGV_StateHelper.compare( childStateString , destState.toString()) == -1
						|| CGV_StateHelper.compare(childStateString, destState.toString()) == 0){
					//moveChildList.add(true);
					System.out.println("the child state was <= the dest state.");
					List<PSItemSummary> childParents = null;
					try {
						childParents = cmgr.findOwners(currChild.getGUID(), null, false);
					} catch (PSErrorException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(childParents.size() > 1){
						numSharedChildren++;
						//find the low state of all the shared parents for this shared child
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
						
						/**
						 * Check if:    destination >= lowestSharedParentState
						 * 			&&	child's state >= lowestSharedParentState
						 * 			&&	child's state < destination
						 */
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
					else if(!stateHelp.existsMappedPath(childStateName, destState) && !stateHelp.isMapping(childStateName, destState)){	//the child cannot reach the destination state in 1 move.
						//Check if there is a path from the current child to the destination (mapped or direct)
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
					if(pcm.isCheckedOut(currChild.getGUID())){	
						//checks to see if the current child is checked out by ANY user
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
				//If the current item is not in the correct destination, transition it.
				if( stateHelp.getDestState() != null ){
					transition(currentItem, currState, destState, stateHelp, pending, true);
				}
				System.out.println("the number of children of the parent = "+children.size());
				//For all children, check to see if they are moving, then move them if needed.
				//Shared children do not get moved/dealt with AFTER PUBLIC state.
				for( PSItemSummary currChild : children ){
					List<PSItemSummary> parentsSize = null;
					try {
						parentsSize = cmgr.findOwners(currChild.getGUID(), null, false);
					} catch (PSErrorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("\t\tThe size of the parents list is " + parentsSize.size() +" for current item "+currChild.getGUID());
					//TODO use the isShared method in the util.
					//Check to see if the child is shared.
					if(parentsSize.size() == 1 ){
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
						System.out.println("The state of the above item is: "+childStateName.toString());

						//If the child needs to be moved, move it.
						if(!stateHelp.isBackwardsMove(childStateName, destState) && stateHelp.existsMappedPath(childStateName, destState)){
							System.out.println("\t\t\tCalling transition because the logic on line 226 passed TRUE");
							transition(currChild.getGUID(), childStateName, destState, stateHelp, pending, false);
						}
						else{
							System.out.println("\t\t\tNot calling transition because of logic on line 226 passed FALSE");
						}

					}
				}
			}
			else{	//if pending
				//if(destState != childHoldState && childHoldRevision != 0 ){
				//revert back to a different state (pending, makes the action fail, parent needs to revert back)
				if(!stateHelp.isMapping(childHoldState,destState)){
					String transitionFind = stateHelp.backwardsPath(currState, destState);
					List<IPSGuid> temp = Collections.<IPSGuid>singletonList(currentItem);
					IPSSystemWs sysws = PSSystemWsLocator.getSystemWebservice();
					
					//Logic to deal with an item having to do 1 (or more) transition(s) to get
					//	back to its previous state.					
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
			/**
			 * If the item is not a top type, and is going to the pending state (between Review and Public),
			 * the item needs to be pushed into the next transition automatically so the user never sees the 
			 * pending state.
			*/
			transition(currentItem, currState, destState, stateHelp, pending, false);
		}
		

	}	

	public boolean canModifyStyleSheet() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Logic dealing with the transition of a content item.
	 * @param source - the GUID for the item that is being transitioned.
	 * @param currState - the current state that the source is in.
	 * @param destState - the destination or target state the source is trying to reach.
	 * @param stateHelp - the CGV_StateHelper object to handle States
	 * @param pending - if the transition is done with a pending flag. (needs to reverse)
	 * @param parent - if the item is a parent (true), or a child item (false)
	 * @return true if the transition was successful, false if not.
	 */
	public static boolean transition(IPSGuid source, StateName currState, StateName destState, CGV_StateHelper stateHelp, boolean pending, boolean parent){
		List<IPSGuid> temp = Collections.<IPSGuid>singletonList(source);
		System.out.println("\t\tTRANSITIONING:");
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
			case REAPPROVAL:
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
			case PUBLIC:
				transition = "ApproveForcetoPublic";
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
		IPSSystemWs sysws = PSSystemWsLocator.getSystemWebservice();
		if( transition != "Null"){
			System.out.println("Parent/Child: transition being called from...");
			System.out.println("\t"+transition+" = "+ stateHelp.toString(currState)+"-->"+stateHelp.toString(destState));
			if(destState == StateName.PENDING ){
				//Pending state requires 2 moves to be handled correctly.
				System.out.println("DEST = PENDING");
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
				//Review state requires some logic from a parent/child POV.
				System.out.println("DEST = REVIEW");
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
				System.out.println("\t!dest review, or !dest pending");
				if(currState == StateName.REVIEW && destState == StateName.PUBLIC){
					System.out.println("currstate is review, destState is public");
					try {
						sysws.transitionItems(temp, "Approve");
					} catch (PSErrorsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (PSErrorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				else{
					System.out.println("currstate is NOT review, destState is  NOT public");
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
			System.out.println("\t\tTRANSITION WAS NOT NULL, RETURNING TRUE");
			return true;
		}
		System.out.println("\t\tTRANSITION WAS NULL, RETURNING FALSE");
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
