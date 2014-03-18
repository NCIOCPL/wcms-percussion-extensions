package gov.cancer.wcm.workflow.stopConditions;

/**
 * Defines the possible outcomes for a RelationshipWFTransitionStopCondition
 * @author bpizzillo
 *
 * 
 */
public enum RelationshipWFTransitionStopConditionResult {
	/**
	 * This states that the dependent should cause transitioning of the owner to stop.
	 */
	StopTransition,
	/**
	 * This states that the dependent should not move, but transitioning of
	 * owner should continue.
	 */
	OkStopChecking,
	/**
	 * This states that the dependent is still a candidate for auto-transitioning
	 * and any further stop conditions should be checked.
	 */
	Ok
}
