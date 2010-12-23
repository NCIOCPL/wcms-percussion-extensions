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
		
		//Get the summary
		PSComponentSummary dependentContentItemSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getDependent().getId());
		
		if (dependentContentItemSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("PublicRevision Check(down): Could not get Component Summary for id: " + rel.getDependent().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}
		
		
		//Check if the item has a public revision or not.
		if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(dependentContentItemSummary, wvc)) { 
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
		
		//Get the summary
		PSComponentSummary ownerContentItemSummary = ContentItemWFValidatorAndTransitioner.getSummaryFromId(rel.getOwner().getId());
		
		if (ownerContentItemSummary == null) {
			//Do not add PSError since that will be added for us when the WFValidationException is thrown
			wvc.getLog().error("Public Revision Check(up): Could not get Component Summary for id: " + rel.getOwner().getId());
			throw new WFValidationException("System Error Occured. Please Check the logs.", true);
		}
		
		//Check if the item has a public revision or not.
		if (ContentItemWFValidatorAndTransitioner.hasPublicRevision(ownerContentItemSummary, wvc)) 
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
