package gov.cancer.wcm.workflow.validators;

import gov.cancer.wcm.workflow.WorkflowValidationContext;
import gov.cancer.wcm.workflow.checks.BaseRelationshipWFTransitionCheck;

import java.util.List;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;

public class CTValidatorCollection {
	private List<BaseContentTypeValidator> validatorList;

	public List<BaseContentTypeValidator> getValidatorList() {
		return validatorList;
	}

	public void setValidatorList(List<BaseContentTypeValidator> validatorList) {
		this.validatorList = validatorList;
	}

	/**
	 * Gets a WFTransitionConfig based on the name of the relationship.
	 * @param relationshipName
	 * @return
	 */
	public boolean validate(
			PSComponentSummary dependentContentItemSummary,
			PSRelationship rel,
			WorkflowValidationContext wvc
	) {

		for(BaseContentTypeValidator currValidator : validatorList) {
			if(!currValidator.validate(dependentContentItemSummary, rel, wvc)){
				return false;
			}
		}
		return true;

	}

	public CTValidatorCollection(
			List<BaseContentTypeValidator> validatorList
	) {
		this.validatorList = validatorList;
	}
}
