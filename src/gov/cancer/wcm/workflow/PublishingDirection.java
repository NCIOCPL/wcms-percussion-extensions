package gov.cancer.wcm.workflow;

/**
 * This enum defines in which direction (Content Creation/Content Archiving)
 * that a Content Type Validator should fire.
 * @author bpizzillo
 *
 */
public enum PublishingDirection {
	/**
	 * This is for content creation which would make an item be publishable
	 */
	Creation,
	/**
	 * This is for content archiving where an item would be removed from the site
	 */
	Archiving
}
