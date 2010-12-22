package gov.cancer.wcm.workflow;

/**
 * Defines in what direction a RelationshipWFTransitionStopCondition should be checked.
 * @author bpizzillo
 *
 */
public enum RelationshipWFTransitionStopConditionDirection {
	/**
	 * Check only when going up.
	 */
	Up,
	/**
	 * Check only when going down.
	 */
	Down,
	/**
	 * Check going both up and down.  Never pass this in to a method as a parameter which
	 * is defining the direction to check. (It should only be up or down)
	 */
	Both
}
