package gov.cancer.wcm.workflow;

/**
 * Defines the outcomes of a RelationshipWFTransitionCheck
 * @author bpizzillo
 *
 */
public enum RelationshipWFTransitionCheckResult {
	/**
	 * States that the transition should be stopped and a message should be shown to the user.
	 */
	StopTransition,
	/**
	 * States that the transition is ok to continue.
	 */
	ContinueTransition	
}
