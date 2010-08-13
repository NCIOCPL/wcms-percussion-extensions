package gov.cancer.wcm.util;

import gov.cancer.wcm.util.CGV_StateHelper.StateName;

import com.percussion.error.PSException;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.workflow.data.PSState;

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
	
	//TODO: Eventually add a constructor and methods to allow stateid to be passed in.
	
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
		//IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
		//IPSGuid cid = gmgr.makeGuid(new PSLocator(request.getParameter("sys_contentid")));
		
		//System.out.println("\n\tParent Item CID: " + request.getParameter("sys_contentid"));
		//IPSOWorkflowInfoFinder workFinder = IPSOWorkflowInfoFinder();
//		PSOWorkflowInfoFinder workInfo = new PSOWorkflowInfoFinder();
//		PSState destinationState = null;
//		try {
//			destinationState = workInfo.findWorkflowState(request.getParameter("sys_contentid"));
//		} catch (PSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//System.out.println("\t\tDestination State: " + destinationState.getName());
				
		int tranID = Integer.parseInt(request.getParameter("sys_transitionid"));
		//CGV_StateHelper a = new CGV_StateHelper();
		//System.out.println("\t\tCurrent State: " + getCurrState(tranID));
		
		currState = toStateName(getCurrState(tranID));		//Set the current state for the object.
		destState = toStateName(getDestState(tranID));		//Set the destination state for the object.
		//destState = toStateName(destinationState.getName());	//Set the destination state for the object.
		transitionID = tranID;									//Set the transition ID for the object.
		//System.out.println("\t\tTransitionID: " + transitionID);
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
		case 2:
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
			return "Public";
		case 9:
			return "Editing";
		case 8:
			return "Archived";
		case 11:
			return "Reapproval";
		case 15:
			return "Public";
		case 16:
			return "Editing";
		case 12:
			return "Public";
		case 2:
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
	{DRAFT, REVIEW, PUBLIC, ARCHIVED, EDITING, REAPPROVAL;

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
			else
				return "Null";
		}
	}

	/**
	 * Returns the StateName enum for the string passed in, null if one DNE.
	 * @param curr - the string to get the enum for.
	 * @return the StateName enum type for that string, or null.
	 */
	public StateName toStateName(String curr){
		if(curr == "Draft" )
			return StateName.DRAFT;
		else if(curr == "Review" )
			return StateName.REVIEW;
		else if(curr == "Public" )
			return StateName.PUBLIC;		
		else if(curr == "Archived" )
			return StateName.ARCHIVED;		
		else if(curr == "Editing" )
			return StateName.EDITING;		
		else if(curr == "Reapproval" )
			return StateName.REAPPROVAL;
		else
			return null;
	}

	/**
	 * Gets the string version of the enum value for the
	 * current state of the object.
	 * @return the string value of that enum.
	 */
	public String currStateToString(){
		if(currState == StateName.DRAFT )
			return "Draft";
		else if(currState == StateName.REVIEW )
			return "Review";
		else if(currState == StateName.PUBLIC )
			return "Public";		
		else if(currState == StateName.ARCHIVED )
			return "Archived";		
		else if(currState == StateName.EDITING )
			return "Editing";		
		else if(currState == StateName.REAPPROVAL )
			return "Reapproval";
		else
			return "Null";
	}
	
	/**
	 * Gets the string version of the enum value for the
	 * current state of the object.
	 * @return the string value of that enum.
	 */
	public String destStateToString(){
		if(destState == StateName.DRAFT )
			return "Draft";
		else if(destState == StateName.REVIEW )
			return "Review";
		else if(destState == StateName.PUBLIC )
			return "Public";		
		else if(destState == StateName.ARCHIVED )
			return "Archived";		
		else if(destState == StateName.EDITING )
			return "Editing";		
		else if(destState == StateName.REAPPROVAL )
			return "Reapproval";
		else
			return "Null";
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
	 * Compares two StateName objects and returns the operator that figures how they are related.
	 * To call, create the Helper object, and pass in two new objects.
	 * Exp: CGV_StateHelper( )  //TODO: Fix this, does not provide correct functionality from this space.
	 * 
	 * @param left - the left side of the compare (exp: left < right)
	 * @param right - the right side of the compare (exp: left < right)
	 * @return 0 if equal, -1 if left < right, 1 if left > right, 2 for a null compare.
	 */
	public static int compare(StateName left, StateName right){
		if( (left == StateName.DRAFT) || (left == StateName.EDITING) || (left == StateName.REAPPROVAL) ){
			if( (right == StateName.DRAFT) || (right == StateName.EDITING) || (right == StateName.REAPPROVAL) )
				return 0;
			else if( (right == StateName.REVIEW) || (right == StateName.PUBLIC) )
				return -1;
			else if ((right == StateName.ARCHIVED))
				return 1;
		}
		else if( left == StateName.REVIEW ){
			if( (right == StateName.DRAFT) || (right == StateName.EDITING) || (right == StateName.REAPPROVAL) || (right == StateName.ARCHIVED) )
				return 1;
			else if((right == StateName.REVIEW))
				return 0;
			else if ((right == StateName.PUBLIC))
				return 1;			
		}
		else if( left == StateName.PUBLIC ){
			if( (right == StateName.DRAFT) || (right == StateName.EDITING) || (right == StateName.REAPPROVAL) || (right == StateName.ARCHIVED) || (right == StateName.REVIEW))
				return 1;
			else if ((right == StateName.PUBLIC))
				return 0;			
		}
		return 2;
	}

}
