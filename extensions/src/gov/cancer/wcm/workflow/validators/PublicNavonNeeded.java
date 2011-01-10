package gov.cancer.wcm.workflow.validators;

import gov.cancer.wcm.workflow.WorkflowValidationContext;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.util.PSItemErrorDoc;

public class PublicNavonNeeded extends BaseContentTypeValidator {

	@Override
	public boolean validate(PSComponentSummary dependentContentItemSummary,
			PSRelationship rel, WorkflowValidationContext wvc) {
		System.out.println("PublicNavonNeeded: returning true.");
		
		return true;
	}

}
