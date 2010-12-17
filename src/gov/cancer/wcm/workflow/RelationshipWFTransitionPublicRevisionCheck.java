package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionCheck which checks to see if the
 * dependent of a relationship has a public revision.
 * @author bpizzillo
 *
 */
public class RelationshipWFTransitionPublicRevisionCheck extends
		BaseRelationshipWFTransitionCheck {
	
	/**
	 * Gets the Transition Type for this Relationship
	 */
	public RelationshipWFTransitionTypes getTransitionType(){
		return RelationshipWFTransitionTypes.Stop;
	}

	@Override
	public RelationshipWFTransitionCheckResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Public Revision Check for dependent: " + rel.getDependent().getId() + " in slot " + rel.getConfig().getLabel());
		
		//Check if the item has a public revision or not.
		if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) 
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		else 
		{
			return RelationshipWFTransitionCheckResult.StopTransition;
		}
	}
	
	/**
	 * Initializes an instance of the RelationshipWFTransitionPublicRevisionCheck class 
	 * @param relationshipName The name of the relationship this check is for.
	 */
	public RelationshipWFTransitionPublicRevisionCheck(String relationshipName) {
		super(relationshipName);
	}
}
