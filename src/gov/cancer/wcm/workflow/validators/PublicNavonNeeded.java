package gov.cancer.wcm.workflow.validators;

import java.util.List;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.PublishingDirection;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.pso.jexl.PSONavTools;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

public class PublicNavonNeeded extends BaseContentTypeValidator {

	@Override
	public boolean validate(PSComponentSummary dependentContentItemSummary,
			PSRelationship rel, WorkflowValidationContext wvc) {		
		
		wvc.getLog().debug("Checking to see if all navons are in public for item with content id: " + dependentContentItemSummary.getContentId());
		
		PSONavTools nav = new PSONavTools();
		IPSNode node = null;
		
		//Find folderid
		IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
		IPSGuid itemGuid = gmgr.makeGuid(dependentContentItemSummary.getCurrentLocator());
		IPSContentWs cmgr = PSContentWsLocator.getContentWebservice();
		IPSCmsContentSummaries summ = PSCmsContentSummariesLocator.getObjectManager();
		IPSWorkflowService wfService = PSWorkflowServiceLocator.getWorkflowService();
		String path = "";
		boolean allNavonsPublic = true;
		try {
			path = cmgr.findFolderPaths(itemGuid)[0];
		} catch (PSErrorException e2) {
			e2.printStackTrace();
		}
		List<IPSGuid> folderGuidList = null;
		if(path.length() != 0 ){
			try {
				folderGuidList = cmgr.findPathIds(path);
			} catch (PSErrorException e2) {
				e2.printStackTrace();
			}
		}
		//Drop 1st in List of paths (site folder)
		if(folderGuidList != null){
			folderGuidList.remove(0);
		}

		for( IPSGuid folderID : folderGuidList ){
			if(folderID != null)
			{
				try {
					node = nav.findNavNodeForFolder(String.valueOf(folderID.getUUID()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(node != null){
				PSComponentSummary summary = summ.loadComponentSummary(node.getGuid().getUUID());
				int stateid = summary.getContentStateId();
				int wfid = summary.getWorkflowAppId();		   

				PSState state = wfService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,stateid),
						new PSGuid(PSTypeEnum.WORKFLOW,wfid));
				String validFlag = state.getContentValidValue();

				//return (validFlag.equals("y") || validFlag.equals("i"));	
				//if(!validFlag.equals("y") && !validFlag.equals("i"))
				if(!validFlag.equals("y") && !validFlag.equals("e") && !validFlag.equals("s") && !validFlag.equals("i"))
				{
					allNavonsPublic = false;
					wvc.getLog().debug("The navon with content id: " + summary.getContentId() + ", is not in Public." );
					wvc.addError(
							ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
							ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
							ContentItemWFValidatorAndTransitioner.NAVON_NOT_PUBLIC,
							new Object[]{summary.getContentId(), dependentContentItemSummary.getContentId()});
				}
			}
		}
		wvc.getLog().debug("All navons are in public for item with content id: " + dependentContentItemSummary.getContentId());
		return allNavonsPublic; //There is no navon, so no need to check.
	}
	
	public PublicNavonNeeded(List<PublishingDirection> validationDirections) {
		super(validationDirections);
	}

}
