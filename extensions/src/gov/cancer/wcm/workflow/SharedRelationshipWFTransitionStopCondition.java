package gov.cancer.wcm.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocation;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Defines a RelationshipWFTransitionStopCondition for checking if
 * the dependent of a relationship is shared
 * @author bpizzillo
 *
 */
public class SharedRelationshipWFTransitionStopCondition extends
		BaseRelationshipWFTransitionStopCondition {

	@Override
	public RelationshipWFTransitionStopConditionResult validate(
			PSComponentSummary contentItemSummary,
			PSRelationship rel, 
			WorkflowValidationContext wvc
			) {
			wvc.getLog().debug("Checking Shared Stop Condition for dependent: " + rel.getDependent().getId());
			if (isShared(rel.getDependent()))
				wvc.getLog().debug("Dependent ID: " + rel.getDependent().getId() + " is Shared.");
			else
				wvc.getLog().debug("Dependent ID: " + rel.getDependent().getId() + " is NOT Shared.");

		return RelationshipWFTransitionStopConditionResult.StopTransition;
	}

	private boolean isShared(PSLocator dependent) {
		return false;
	}
}
