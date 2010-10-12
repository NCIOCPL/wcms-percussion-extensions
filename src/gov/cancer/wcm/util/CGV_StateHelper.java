package gov.cancer.wcm.util;

import gov.cancer.wcm.extensions.CGV_OnDemandPublishService;
import gov.cancer.wcm.extensions.CGV_OnDemandPublishServiceLocator;
import gov.cancer.wcm.extensions.CGV_ParentChildService;
import gov.cancer.wcm.extensions.CGV_ParentChildServiceLocator;

import java.util.ArrayList;
import java.util.List;

import com.percussion.server.IPSRequestContext;

/**
 * Wrapper class for the StateName enum.
 * Provides the enum and functionality to transform it back
 * and forth between enum values and string values.
 * @author John Walls
 *
 */
public class CGV_StateHelper {
	
	
	/**
	 * Default Constructor
	 */
	public CGV_StateHelper(){
		initServices();
		name = null;
		currState = null;
		destState = null;
		transitionID = 0;
		trigger = null;
	}
		
//	/**
//	 * Constructor that allows a string to be passed in.
//	 * That string will setup the name/statename enum object
//	 * for the CGV_StateHelper object.
//	 * @param current - the name of the current state.
//	 */
//	public CGV_StateHelper(String current, String destination, int transition){
//		initServices();
//		name = current;
//		currState = toStateName(current);
//		destState = toStateName(destination);
//		transitionID = transition;
//	}
//	
	/**
	 * Constructor that allows the request context to be passed in
	 * and that sets up the current, destination, and the transition id.
	 * @param req
	 */
	public CGV_StateHelper(IPSRequestContext req){
		initServices();
		setup(req);
	}
	
	/**
	 * The current state of the object.
	 * The StateName enum object type for this CGV_StateHelper object.
	 */
	private StateName currState;
	
	/**
	 * The destination state of the object.
	 * The StateName enum object type for this CGV_StateHelper object.
	 */
	private StateName destState;
	
	/**
	 * The name of the object's state.
	 */
	private String name;
	
	/**
	 * The transition ID for the object.
	 */
	private int transitionID;
	
	private String trigger;


	/**
	 * Service class to invoke publishing routine
	 */
	private static CGV_ParentChildService svc = null;
	
	private void setup(IPSRequestContext request){
				
		int tranID = Integer.parseInt(request.getParameter("sys_transitionid"));
		transitionID = tranID;
		trigger = svc.getTrigger(tranID);
		currState = toStateName(svc.getCurrState(tranID));		//Set the current state for the object.
		destState = toStateName(svc.getDestState(tranID));		//Set the destination state for the object.
		//System.out.println("JOHN TEST: currentState = " + currState.toString());
		//System.out.println("JOHN TEST: destinationState = " +destState.toString());
		//System.out.println("JOHN TEST: trigger = " + trigger);
	}

	/**
	 * Initializing the Service class. 
	 */
	private static void initServices()
	{
		if(svc == null)
		{
			svc = CGV_ParentChildServiceLocator.getCGV_ParentChildService();
		}
	}

	/**
	 * Enum containing the different state names for the system.
	 * @author John Walls
	 *
	 */
	public enum StateName implements Comparable<StateName>
	{DRAFT, REVIEW, PUBLIC, ARCHIVED, EDITING, REAPPROVAL, PENDING, ARCHIVEAPPROVAL, STAGING, RESTAGING;}

	/**
	 * Returns the StateName enum for the string passed in, null if one DNE.
	 * @param curr - the string to get the enum for.
	 * @return the StateName enum type for that string, or null.
	 */
	public StateName toStateName(String curr){
		if(curr.equalsIgnoreCase("Draft"))
			return StateName.DRAFT;
		else if(curr.equalsIgnoreCase("Review"))
			return StateName.REVIEW;
		else if(curr.equalsIgnoreCase("Public"))
			return StateName.PUBLIC;		
		else if(curr.equalsIgnoreCase("Archived"))
			return StateName.ARCHIVED;		
		else if(curr.equalsIgnoreCase("Editing"))
			return StateName.EDITING;		
		else if(curr.equalsIgnoreCase("Reapproval"))
			return StateName.REAPPROVAL;
		else if(curr.equalsIgnoreCase("Pending"))
			return StateName.PENDING;
		else if(curr.equalsIgnoreCase("ArchiveApproval"))
			return StateName.ARCHIVEAPPROVAL;
		else if(curr.equalsIgnoreCase("Staging"))
			return StateName.STAGING;
		else if(curr.equalsIgnoreCase("Restaging"))
			return StateName.RESTAGING;
		else
			return null;
	}
	
	public String toString(StateName state){
		if( state == StateName.DRAFT )
			return "Draft";
		else if(state == StateName.REVIEW )
			return "Review";
		else if(state == StateName.PUBLIC )
			return "Public";		
		else if(state == StateName.ARCHIVED )
			return "Archived";		
		else if(state == StateName.EDITING )
			return "Editing";		
		else if(state == StateName.REAPPROVAL )
			return "Reapproval";
		else if(state == StateName.PENDING)
			return "Pending";
		else if(state == StateName.ARCHIVEAPPROVAL)
			return "ArchiveApproval";
		else if(state == StateName.STAGING)
			return "Staging";	
		else if(state == StateName.RESTAGING)
			return "Restaging";
		else
			return "Null";
	}

	/**
	 * Gets the string version of the enum value for the
	 * current state of the object.
	 * @return the string value of that enum.
	 */
	public String currStateToString(){
		return toString(currState);
	}
	
	/**
	 * Gets the string version of the enum value for the
	 * current state of the object.
	 * @return the string value of that enum.
	 */
	public String destStateToString(){
		return toString(destState);
	}
	
	public StateName getCurrState() {
		return currState;
	}

	public void setCurrState(StateName currState) {
		this.currState = currState;
	}

	public StateName getDestState() {
		return destState;
	}

	public void setDestState(StateName destState) {
		this.destState = destState;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTransitionID() {
		return transitionID;
	}

	public void setTransitionID(int transitionID) {
		this.transitionID = transitionID;
	}
	
	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	
	/**
	 * Returns the correct workflow transition(s) to allow the item
	 * to move from one state to another.
	 * @param currState - current state of the item
	 * @param destState - destination state of the item.
	 * @return - the list of string representation of the transition triggers that are called 
	 * 				to make the item get from current->destination
	 */
	public List<String> backwardsPath(StateName currState, StateName destState){
		//return forwardTransition(destState, currState);
		//TODO: Make configurable
		List<String> returnThis = new ArrayList<String>();
		switch (currState){
		case ARCHIVEAPPROVAL:
			switch (destState){
			case PUBLIC:
				returnThis.add("RequestArchive");
				break;
			case ARCHIVED:
				returnThis.add("Republish");
				returnThis.add("RequestArchive");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case DRAFT:
			switch (destState){
			case REVIEW:
				returnThis.add("Disapprove");
				break;
			case STAGING:
				returnThis.add("backToDraft");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case STAGING:
			switch (destState){
			case DRAFT:
				returnThis.add("Staging");
				break;
			case REVIEW:
				returnThis.add("Disapprove");
				returnThis.add("Staging");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case REVIEW:
			switch (destState){
			case DRAFT:
				returnThis.add("Submit");
				break;
			case PENDING:
				returnThis.add("backToReview");
				break;
			case STAGING:
				returnThis.add("submitForReview");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case PUBLIC:
			switch (destState){
			case ARCHIVEAPPROVAL:
				returnThis.add("DisapproveArchive");
				break;
			case EDITING:
				returnThis.add("Resubmit");
				returnThis.add("Reapprove");
				break;
//			case ARCHIVED:
//				returnThis.add("Republish");
//				break;
			default:
				returnThis.add("Null");
			}
			break;
		case EDITING:
			switch (destState){
			case REAPPROVAL:
				returnThis.add("Disapprove");
				break;
			case RESTAGING:
				returnThis.add("backToEdit");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case REAPPROVAL:
			switch (destState){
			case EDITING:
				returnThis.add("Resubmit");
				break;
			case PUBLIC:
				returnThis.add("Revise");
				returnThis.add("Resubmit");
				break;
			case RESTAGING:
				returnThis.add("Resubmit for Review");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case ARCHIVED:
			switch (destState){
			case EDITING:
				returnThis.add("Resubmit");
				returnThis.add("Reapprove");
				returnThis.add("RequestArchive");
				returnThis.add("ApproveArchive");
				break;
			case PUBLIC:
				returnThis.add("RequestArchive");
				returnThis.add("ApproveArchive");
				break;
//			case ARCHIVEAPPROVAL:
//				returnThis.add("ApproveArchive");
//				break;
			default:
				returnThis.add("Null");
			}
			break;

		case RESTAGING:
			switch (destState){
			case EDITING:
				returnThis.add("Preview");
				break;
			case REAPPROVAL:
				returnThis.add("Disapprove");
				returnThis.add("Preview");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case PENDING:
			switch (destState){
			case REVIEW:
				returnThis.add("Approve");
				break;
			default:
				returnThis.add("Null");
			}
		default:
			returnThis.add("Null");
			break;	
		}
		return returnThis;
	}
	
	public static List<String> forwardTransition(StateName currState, StateName destState){
		List<String> returnThis = new ArrayList<String>();
		switch (currState){
		case ARCHIVEAPPROVAL:
			switch(destState){
			case ARCHIVED:
				returnThis.add("ApproveArchive");
				break;
			case PUBLIC:
				returnThis.add("DisapproveArchive");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case DRAFT:
			switch (destState){
			case REVIEW:
				returnThis.add("Submit");
				break;
			case STAGING:
				returnThis.add("Staging");
				break;
			case RESTAGING:
				returnThis.add("Staging");
				break;
			case REAPPROVAL:
				returnThis.add("Submit");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case STAGING:
			switch (destState){
			case REVIEW:
				returnThis.add("submitForReview");
				break;
			case REAPPROVAL:
				returnThis.add("submitForReview");
				break;
			case DRAFT:
				returnThis.add("backToDraft");
				break;
			case EDITING:
				returnThis.add("backToDraft");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case REVIEW:
			switch (destState){
			case DRAFT:
				returnThis.add("Disapprove");
				break;
			case PENDING:
				returnThis.add("Approve");
				break;
			case PUBLIC:
				returnThis.add("Approve");
				returnThis.add("ForcetoPublic");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case PUBLIC:
			switch (destState){
			case ARCHIVEAPPROVAL:
				returnThis.add("RequestArchive");
				break;
			case EDITING:
				returnThis.add("Quick Edit");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case EDITING:
			switch (destState){
			case REAPPROVAL:
				returnThis.add("Resubmit");
				break;
			case RESTAGING:
				returnThis.add("Preview");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case RESTAGING:
			switch (destState){
			case REAPPROVAL:
				returnThis.add("Resubmit for Review");
				break;
			case EDITING:
				returnThis.add("backToEdit");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case REAPPROVAL:
			switch (destState){
			case EDITING:
				returnThis.add("Disapprove");
				break;
			case PUBLIC:
				returnThis.add("Reapprove");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case ARCHIVED:
			switch (destState){
			case EDITING:
				returnThis.add("Revive");
				break;
			case PUBLIC:
				returnThis.add("Republish");
				break;
			default:
				returnThis.add("Null");
			}
			break;
		case PENDING:
			switch (destState){
			case REVIEW:
				returnThis.add("backToReview");
				break;
			case PUBLIC:
				returnThis.add("ForcetoPublic");
				break;
			default:
				returnThis.add("Null");
			}
		default:
			returnThis.add("Null");
			break;	
		}
		return returnThis;
	}
	
	/**
	 * Compares two StateName objects and returns the operator that figures how they are related.
	 * To call, create the Helper object, and pass in two new objects.
	 * Exp: CGV_StateHelper( )  //TODO: Fix this, does not provide correct functionality from this space.
	 * 
	 * @param left - the left side of the compare (exp: left < right)
	 * @param right - the right side of the compare (exp: left < right)
	 * @return 0 if equal, -1 if left < right, 1 if left > right, 2 for a null compare.
	 */
	public static int compare(String left, String right){
		//System.out.println("JDB: comparing... " + left + " to " + right);
		if( left.equalsIgnoreCase("Draft")){
			if(right.equalsIgnoreCase("Draft")){
				System.out.println(left + " equal " + right);
				return 0;
			}
			else{
				return -1;
			}
		}
		else if( left.equalsIgnoreCase("Staging") ){
			if( right.equalsIgnoreCase("Draft")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Staging")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if( left.equalsIgnoreCase("Restaging") ){
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("Staging")  
					|| right.equalsIgnoreCase("ArchiveApproval") || right.equalsIgnoreCase("Pending") || right.equalsIgnoreCase("Editing")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Restaging")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if( left.equalsIgnoreCase("Review") || left.equalsIgnoreCase("ArchiveApproval")){
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Staging")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("ArchiveApproval")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if (left.equalsIgnoreCase("Pending")){
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Staging") 
					|| right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("ArchiveApproval")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Pending")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if (left.equalsIgnoreCase("Editing")){
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("Staging")  
					|| right.equalsIgnoreCase("ArchiveApproval") || right.equalsIgnoreCase("Pending")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Editing")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if (left.equalsIgnoreCase("Reapproval")){
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("Pending") ||
					right.equalsIgnoreCase("Editing") || right.equalsIgnoreCase("Staging") || right.equalsIgnoreCase("Restaging")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Reapproval") || right.equalsIgnoreCase("ArchiveApproval")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if (left.equalsIgnoreCase("Archived")){
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("Pending") || right.equalsIgnoreCase("Staging") 
					|| right.equalsIgnoreCase("Editing") || right.equalsIgnoreCase("Reapproval") || right.equalsIgnoreCase("ArchiveApproval") || right.equalsIgnoreCase("Restaging")){
				System.out.println(left+" > " +right);
				return 1;
			}
			else if(right.equalsIgnoreCase("Archived")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return -1;		
			}
		}
		else if (left.equalsIgnoreCase("Public")){
			if(right.equalsIgnoreCase("Public")){
				System.out.println(left+" equal " +right);
				return 0;
			}
			else {
				return 1;		
			}
		}
		else{
			return 0;
		}
	}

	/**
	 * Returns true if there is a 1 step work flow path from the current to the
	 * destination state.  Uses a mapping so an item not yet in public can
	 * be moved into a corresponding state when its parent (who might be in public)
	 * transitions and calls the child to move in sync.
	 * @param currState - current state the item is in
	 * @param destState - the destination state.
	 * @return true if the single path exists, if it is more then 1 step, rtn false.
	 */
	public boolean existsMappedPath(StateName currState, StateName destState) {
		switch (currState){
		case DRAFT:
			switch (destState){
			case STAGING:
				return true;
			case RESTAGING:
				return true;
			case REVIEW:
				return true;
			case REAPPROVAL:
				return true;
			default:
				return false;
			}
		case STAGING:
			switch (destState){
			case DRAFT:
				return true;
			case REVIEW:
				return true;
			case EDITING:
				return true;	//TODO: This one might not be needed.
			case REAPPROVAL:
				return true;
			default:
				return false;
			}
		case REVIEW:
			switch (destState){
			case DRAFT:
				return true;
			case EDITING:
				return true;
			case PENDING:
				return true;
			case PUBLIC:
				return true;
			default:
				return false;
			}
		case PUBLIC:
			switch (destState){
//			case ARCHIVED:
//				return true;
			case ARCHIVEAPPROVAL:
				return true;
			case EDITING:
				return true;
			default:
				return false;
			}
		case EDITING:
			switch (destState){
			case REAPPROVAL:
				return true;
			case RESTAGING:
				return true;
			default:
				return false;
			}
		case RESTAGING:
			switch (destState){
			case REAPPROVAL:
				return true;
			case EDITING:
				return true;
			default:
				return false;
			}
		case REAPPROVAL:
			switch (destState){
			case EDITING:
				return true;
			case PUBLIC:
				return true;
			default:
				return false;
			}
		case ARCHIVED:
			switch (destState){
			case EDITING:
				return true;
			case PUBLIC:
				return true;
			default:
				return false;
			}
		case PENDING:
			switch (destState){
			case REVIEW:
				return true;
			case PUBLIC:
				return true;
			default:
				return false;
			}
		case ARCHIVEAPPROVAL:
			switch (destState){
			case ARCHIVED:
				return true;
			case PUBLIC:
				return true;
			default:
				return false;
			}
		default:
			return false;
		}
	}
	
	/**
	 * Returns true if this is a backwards workflow movement.
	 * @param currState - current state we are in.
	 * @param destState - the destination state.
	 * @return true if the move from current to destination is a "backwards" move, if not rtn false.
	 */
	public boolean isBackwardsMove(StateName currState, StateName destState) {
		switch (currState){
		case DRAFT:
			switch (destState){
			case REVIEW:
				return false;
			case STAGING:
				return false;
			case REAPPROVAL:
				return false;
			case RESTAGING:
				return false;
			default:
				return true;
			}
		case STAGING:
			switch (destState){
			case DRAFT:
				return true;
			case REVIEW:
				return false;
			case REAPPROVAL:
				return false;
			case EDITING:
				return true;
			default: 
				return false;
			}
		case REVIEW:
			switch (destState){
			case DRAFT:
				return true;
			case PENDING:
				return false;
			case PUBLIC:
				return false;
			default:
				return false;
			}
		case PUBLIC:
			switch (destState){
//			case ARCHIVED:
//				return false;
			case ARCHIVEAPPROVAL:
				return false;
			case EDITING:
				return false;
			default:
				return false;
			}
		case EDITING:
			switch (destState){
			case REAPPROVAL:
				return false;
			case RESTAGING:
				return false;
			default:
				return false;
			}
		case RESTAGING:
			switch (destState){
			case EDITING:
				return true;
			case REAPPROVAL:
				return false;
			default: 
				return false;
			}
		case REAPPROVAL:
			switch (destState){
			case EDITING:
				return true;
			case PUBLIC:
				return false;
			default:
				return false;
			}
		case ARCHIVED:
			switch (destState){
			case EDITING:
				return false;
			case PUBLIC:
				return false;
			default:
				return false;
			}
		case PENDING:
			switch (destState){
			case REVIEW:
				return true;
			case PUBLIC:
				return false;
			default:
				return false;
			}
		case ARCHIVEAPPROVAL:
			switch (destState){
			case ARCHIVED:
				return false;
			case PUBLIC:
				return true;
			default: 
				return false;
			}
		default:
			return false;
		}
	}

	/**
	 * Creates a mapping for states on both sides of PUBLIC.  Allows parents
	 * to transition its children in lower states before the child has reached PUBLIC.
	 * @param childState - the current state of the child item.
	 * @param parentDestinationState - the parent item's destination state.
	 * @return - true if a mapping exists, false if not.
	 */
	public boolean isMapping(StateName childState, StateName parentDestinationState) {
		switch (childState){
		case DRAFT:
			switch (parentDestinationState){
			case EDITING:
				return true;
			case DRAFT:
				return true;
			default:
				return false;
			}
		case REVIEW:
			switch (parentDestinationState){
			case REAPPROVAL:
				return true;
			case REVIEW:
				return true;
			default:
				return false;
			}
		case STAGING:
			switch (parentDestinationState){
			case STAGING:
				return true;
			case RESTAGING:
				return true;
			default:
				return false;
			}
		case RESTAGING:
			switch (parentDestinationState){
			case STAGING:
				return true;
			case RESTAGING:
				return true;
			default:
				return false;
			}
		case PUBLIC:
			switch (parentDestinationState){
			case ARCHIVED:
				return true;
			case PUBLIC:
				return true;
			case PENDING:
				return true;
			default:
				return false;
			}
		case EDITING:
			switch (parentDestinationState){
			case DRAFT:
				return true;
			case EDITING:
				return true;
			default:
				return false;
			}
		case REAPPROVAL:
			switch (parentDestinationState){
			case REVIEW:
				return true;
			case REAPPROVAL:
				return true;
			default:
				return false;
			}
		case ARCHIVED:
			switch (parentDestinationState){
			case PUBLIC:
				return true;
			case ARCHIVED:
				return true;
			case PENDING:
				return true;
			default:
				return false;
			}
		case PENDING:
			switch (parentDestinationState){
			case PUBLIC:
				return true;
			case ARCHIVED:
				return true;
			case PENDING:
				return true;
			default:
				return false;
			}
		default:
			return false;
		}
	}

}
