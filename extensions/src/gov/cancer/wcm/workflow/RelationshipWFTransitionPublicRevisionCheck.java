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
	public RelationshipWFTransitionCheckResult validateDown(
			PSComponentSummary ownerContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Public Revision Check (Down) for dependent: " + rel.getDependent().getId() + " in slot " + rel.getConfig().getLabel());
		
		//Check if the item has a public revision or not.
		if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getDependent(), wvc)) { 
			wvc.getLog().debug("Handling Public Revision Check (Down): dependent: " + rel.getDependent().getId() + " has public revision.");
			return RelationshipWFTransitionCheckResult.ContinueTransition;
		}
		else 
		{
			wvc.getLog().debug("Handling Public Revision Check (Down): dependent: " + rel.getDependent().getId() + " has NO public revision.");
			return RelationshipWFTransitionCheckResult.StopTransition;
		}
	}

	@Override
	public RelationshipWFTransitionCheckResult validateUp(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {
		wvc.getLog().debug("Handling Public Revision Check (Up) for dependent: " + rel.getOwner().getId() + " in slot " + rel.getConfig().getLabel());
		
		//Check if the item has a public revision or not.
		if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(rel.getOwner(), wvc)) 
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
