package gov.cancer.wcm.util;

public class CGV_StateHelper {
	
	public CGV_StateHelper(){
	}
	
	public CGV_StateHelper(String stateName){
		name = stateName;
		state = toStateName(name);
	}
	
	public CGV_StateHelper(StateName stateNameObj){
		state = stateNameObj;
		name = toString(stateNameObj);
	}
	
	public StateName getState() {
		return state;
	}

	public void setState(StateName state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private StateName state;
	private String name;

	public enum StateName implements Comparable<StateName>
	{DRAFT, REVIEW, PUBLIC, ARCHIVED, EDITING, REAPPROVAL;

	//Returns; 0 if equal, -1 if left < right, 1 if left > right, 2 for a null compare.
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
	
	public String toString(){
		return toString(state);
	}
}
