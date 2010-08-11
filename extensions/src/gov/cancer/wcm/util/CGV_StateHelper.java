package gov.cancer.wcm.util;

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
		PSOWorkflowInfoFinder workInfo = new PSOWorkflowInfoFinder();
		PSState destinationState = null;
		try {
			destinationState = workInfo.findWorkflowState(request.getParameter("sys_contentid"));
		} catch (PSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("\t\tDestination State: " + destinationState.getName());
				
		int tranID = Integer.parseInt(request.getParameter("sys_transitionid"));
		CGV_StateHelper a = new CGV_StateHelper();
		//System.out.println("\t\tCurrent State: " + a.getCurrState(tranID));
		
		currState = toStateName(a.getCurrState(tranID));		//Set the current state for the object.
		destState = toStateName(destinationState.getName());	//Set the destination state for the object.
		transitionID = tranID;									//Set the transition ID for the object.
	}
	

	/**
	 * Enum containing the different state names for the system.
	 * @author John Walls
	 *
	 */
	public enum StateName implements Comparable<StateName>
	{DRAFT, REVIEW, PUBLIC, ARCHIVED, EDITING, REAPPROVAL;

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
		if( (left == DRAFT) || (left == EDITING) || (left == REAPPROVAL) ){
			if( (right == DRAFT) || (right == EDITING) || (right == REAPPROVAL) )
				return 0;
			else if( (right == REVIEW) || (right == PUBLIC) )
				return -1;
			else if ((right == ARCHIVED))
				return 1;
		}
		else if( left == REVIEW ){
			if( (right == DRAFT) || (right == EDITING) || (right == REAPPROVAL) || (right == ARCHIVED) )
				return 1;
			else if((right == REVIEW))
				return 0;
			else if ((right == PUBLIC))
				return 1;			
		}
		else if( left == PUBLIC ){
			if( (right == DRAFT) || (right == EDITING) || (right == REAPPROVAL) || (right == ARCHIVED) || (right == REVIEW))
				return 1;
			else if ((right == PUBLIC))
				return 0;			
		}
		return 2;
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
	 * Takes a StateName enum type and converts it to its string name.
	 * @param curr - the StateName object
	 * @return the string value of that enum.
	 */
	public String toString(StateName curr){
		if(curr == StateName.DRAFT )
			return "Draft";
		else if(curr == StateName.REVIEW )
			return "Review";
		else if(curr == StateName.PUBLIC )
			return "Public";		
		else if(curr == StateName.ARCHIVED )
			return "Archived";		
		else if(curr == StateName.EDITING )
			return "Editing";		
		else if(curr == StateName.REAPPROVAL )
			return "Reapproval";
		else
			return "Null";
	}
	
	public String getCurrState(int transitionID){
		//TODO: Customize for Blue, and add config file parse.
		switch (transitionID) {
		case 1:
			return "Draft";
		case 2:
			return "Review";
		case 3:
			return "Review";
		case 4:
			return "Public";
		case 5:
			return "Public";
		case 6:
			return "Editing";
		case 7:
			return "Editing";
		case 8:
			return "Editing";
		case 9:
			return "Archived";
		case 10:
			return "Archived";
			default:
				return null;
		}
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return toString(currState);
	}
}
