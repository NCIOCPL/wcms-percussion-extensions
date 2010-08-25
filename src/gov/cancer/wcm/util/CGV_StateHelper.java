package gov.cancer.wcm.util;

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
		name = null;
		currState = null;
		destState = null;
	}
		
	/**
	 * Constructor that allows a string to be passed in.
	 * That string will setup the name/statename enum object
	 * for the CGV_StateHelper object.
	 * @param current - the name of the current state.
	 */
	public CGV_StateHelper(String current, String destination){
		name = current;
		currState = toStateName(current);
		destState = toStateName(destination);
	}
	
	/**
	 * Constructor that allows the request context to be passed in
	 * and that sets up the current, destination, and the transition id.
	 * @param req
	 */
	public CGV_StateHelper(IPSRequestContext req){
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
	
	private void setup(IPSRequestContext request){
				
		int tranID = Integer.parseInt(request.getParameter("sys_transitionid"));
		
		currState = toStateName(getCurrState(tranID));		//Set the current state for the object.
		destState = toStateName(getDestState(tranID));		//Set the destination state for the object.
		transitionID = tranID;									//Set the transition ID for the object.
	}
	
	public String getCurrState(int tranID){
		//TODO: Customize for Blue, and add config file parse.
		switch (tranID) {
		case 3:
			return "Draft";
		case 14:
			return "Review";
		case 6:
			return "Review";
		case 9:
			return "Public";
		case 8:
			return "Public";
		case 11:
			return "Editing";
		case 15:
			return "Reapproval";
		case 16:
			return "Reapproval";
		case 12:
			return "Archived";
		//case 2:
		//	return "Archived";
		case 17:
			return "Pending";
		case 18:
			return "Pending";
		case 19:
			return "ArchiveApproval";
		case 20:
			return "ArchiveApproval";
		case 21:
			return "Archived";
		default:
				return null;
		}
	}

	private String getDestState(int tranID) {
		//TODO: Customize for Blue, and add config file parse.
		switch (tranID) {
		case 3:
			return "Review";
		case 14:
			return "Draft";
		case 6:
			return "Pending";
		case 9:
			return "Editing";
		case 8:
			return "ArchiveApproval";
		case 11:
			return "Reapproval";
		case 15:
			return "Public";
		case 16:
			return "Editing";
		case 12:
			return "Public";
		//case 2:
		//	return "Editing";
		case 17:
			return "Review";
		case 18:
			return "Public";
		case 19:
			return "Archived";
		case 20:
			return "Public";
		case 21:
			return "Editing";
		default:
				return null;
		}
	}


	/**
	 * Enum containing the different state names for the system.
	 * @author John Walls
	 *
	 */
	public enum StateName implements Comparable<StateName>
	{DRAFT, REVIEW, PUBLIC, ARCHIVED, EDITING, REAPPROVAL, PENDING, ARCHIVEAPPROVAL;}

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
			case REAPPROVAL:
				returnThis.add("Submit");
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
		else if( left.equalsIgnoreCase("Review") || left.equalsIgnoreCase("ArchiveApproval")){
			if( right.equalsIgnoreCase("Draft")){
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
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("ArchiveApproval")){
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
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") ||
					right.equalsIgnoreCase("ArchiveApproval") || right.equalsIgnoreCase("Pending")){
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
					right.equalsIgnoreCase("Editing")){
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
			if( right.equalsIgnoreCase("Draft") || right.equalsIgnoreCase("Review") || right.equalsIgnoreCase("Pending") ||
					right.equalsIgnoreCase("Editing") || right.equalsIgnoreCase("Reapproval") || right.equalsIgnoreCase("ArchiveApproval")){
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
			case REVIEW:
				return true;
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
			case REAPPROVAL:
				return false;
			default:
				return true;
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
