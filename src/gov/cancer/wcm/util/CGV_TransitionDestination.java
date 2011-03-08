package gov.cancer.wcm.util;

import java.util.List;

/**
 */
public class CGV_TransitionDestination {
	private String name;
	private List<String> autoTransitionNames;
	private List<String> validChildStates;
	boolean preventAncestorPull;
	
	
	/**
	 * Method getName.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	/**
	 * Method setName.
	 * @param name String
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Method getAutoTransitionNames.
	 * @return List<String>
	 */
	public List<String> getAutoTransitionNames() {
		return autoTransitionNames;
	}
	/**
	 * Method setAutoTransitionNames.
	 * @param autoTransitionNames List<String>
	 */
	public void setAutoTransitionNames(List<String> autoTransitionNames) {
		this.autoTransitionNames = autoTransitionNames;
	}
	/**
	 * Method getValidChildStates.
	 * @return List<String>
	 */
	public List<String> getValidChildStates() {
		return validChildStates;
	}
	/**
	 * Method setValidChildStates.
	 * @param validChildStates List<String>
	 */
	public void setValidChildStates(List<String> validChildStates) {
		this.validChildStates = validChildStates;
	}
	
	/**
	 * Method isPreventAncestorPull.
	 * @return boolean
	 */
	public boolean isPreventAncestorPull() {
		return preventAncestorPull;
	}

	/**
	 * Method setPreventAncestorPull.
	 * @param preventAncestorPull boolean
	 */
	public void setPreventAncestorPull(boolean preventAncestorPull) {
		this.preventAncestorPull = preventAncestorPull;
	}
	
}
