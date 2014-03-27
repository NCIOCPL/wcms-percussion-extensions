/**
 * 
 */
package gov.cancer.wcm.workflow.validators;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import gov.cancer.wcm.workflow.PublishingDirection;
import gov.cancer.wcm.workflow.WFValidationException;
import gov.cancer.wcm.workflow.WorkflowConfiguration;
import gov.cancer.wcm.workflow.WorkflowConfigurationLocator;
import gov.cancer.wcm.workflow.WorkflowUtil;
import gov.cancer.wcm.workflow.WorkflowValidationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * @author learnb
 *
 */
/**
 * @author learnb
 *
 */
public class ParentItemsAreArchived extends BaseContentTypeValidator {

	// Rhythmyx service interfaces
	private static IPSCmsContentSummaries contentSummariesService;
	private static IPSWorkflowService workflowService;
	private static IPSSystemWs systemWebService;
	
	static {
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		systemWebService = PSSystemWsLocator.getSystemWebservice();
		workflowService = PSWorkflowServiceLocator.getWorkflowService();
	}
	
	// NOTE: The WorkflowUtil is explicitly *NOT* declared as static. This avoids potential threading problems.
	private WorkflowUtil	_workflowUtil;

	// Message constants.
	private static final String WRONG_TRANSITION_TYPE_MESSAGE = "ParentItemsAreArchived.Validate() was called for a non-archiving transition.";
	

	/* (non-Javadoc)
	 * @see gov.cancer.wcm.workflow.validators.BaseContentTypeValidator#validate(com.percussion.cms.objectstore.PSComponentSummary, com.percussion.design.objectstore.PSRelationship, gov.cancer.wcm.workflow.WorkflowValidationContext)
	 */
	@Override
	public boolean isValid(PSComponentSummary dependentContentItemSummary,
			PSRelationship rel, WorkflowValidationContext wvc) {
		Log log = wvc.getLog();

		log.trace("Enter ParentItemsAreArchived.Validate()");
		if(!wvc.isArchiveTransition()){
			log.error(WRONG_TRANSITION_TYPE_MESSAGE);
			throw new WFValidationException(WRONG_TRANSITION_TYPE_MESSAGE);
		}

		// The return value.
		boolean okToTransition = true;

		// In order to validate correctly, we need to know whether the item being transitioned is already
		// in an archived state.
		boolean startingFromArchive = isItemArchived(dependentContentItemSummary);
		
		// Validate the collection of parent items.
		List<PSComponentSummary> parentItems = FindParentContentItems(dependentContentItemSummary, wvc);
		for(PSComponentSummary parentItem : parentItems){
			
			// All parent items must be in an archived state.
			if(!isItemArchived(parentItem)){
				okToTransition = false;
				log.debug(String.format("Could not archive the content item %d because its parent item %s has not been archived.", dependentContentItemSummary.getCurrentLocator().getId(), parentItem.getName()));
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.ARCHIVE_PARENT_NOT_MOVING,
						new Object[]{dependentContentItemSummary.getContentId(), parentItem.getName()});
				continue;
			}
			else{
				log.debug("Item in archived state: " + parentItem.getCurrentLocator().getId());
			}

			// If the item being transitioned is moving between archive states, each parent's state
			// must also have a greater weight than the state of the item which is being transitioned.
			if(startingFromArchive && !isParentArchiveStateGreaterOrEqual(wvc, parentItem)){
				okToTransition = false;
				log.debug(String.format("Could not archive the content item %d because its parent item %s is not more deeply archived.", dependentContentItemSummary.getCurrentLocator().getId(), parentItem.getName()));
				wvc.addError(
						ContentItemWFValidatorAndTransitioner.ERR_FIELD, 
						ContentItemWFValidatorAndTransitioner.ERR_FIELD_DISP, 
						ContentItemWFValidatorAndTransitioner.ARCHIVE_PARENT_NOT_MOVING,
						new Object[]{dependentContentItemSummary.getContentId(), parentItem.getName()});
				continue;
			}
		}

		return okToTransition;
	}

	
	
	
	/**
	 * Locate all content items which are the owner of an active assembly relationship in which dependentContentItemSummary
	 * participates as a dependent. 
	 * @param dependentContentItemSummary The content item to find parents for.
	 * @param wvc The current validation context.
	 * @return A collection of Component Summary objects describing the parent items.
	 */
	static private List<PSComponentSummary> FindParentContentItems(PSComponentSummary dependentContentItemSummary,
			WorkflowValidationContext wvc){
		Log log = wvc.getLog();
		log.trace("Enter ParentItemsAreArchived.FindParentContentItems()");

		List<PSRelationship> relationships;
		PSLocator contentItemLocator = dependentContentItemSummary.getCurrentLocator();
		log.debug("Content item PSLocator: " + contentItemLocator);

		// Find all incoming relationships for the specified content item.
		// We don't care about owners items which were only owners in a previous
		// revision, so limit the results to their current public or editing revisions.
		PSRelationshipFilter filter = new PSRelationshipFilter();		
		filter.setDependent(contentItemLocator);
		filter.limitToEditOrCurrentOwnerRevision(true);
		filter.setCategory("rs_activeassembly");

		try {
			relationships = systemWebService.loadRelationships(filter);
		} catch (Exception ex) {
			wvc.getLog().error("getTransitionRoot: Could not get relationships for id: " + contentItemLocator.getId(), ex);
			throw new WFValidationException("System Error Occured. Could not get relationships for content item :" + contentItemLocator.getId(), ex, true);			
		}

		// Get a list of owner items.
		ArrayList<Integer> relationshipOwners = new ArrayList<Integer>();
		for(PSRelationship relationship: relationships) {
			relationshipOwners.add(relationship.getOwner().getId());
		}
		if(log.isDebugEnabled()){
			log.debug("Number of relationships: " + relationships.size());
			String idlist = "";
			for(Integer ownerid: relationshipOwners){
				idlist = idlist += " " + ownerid;
			}
			log.debug("Relationship owners:" + idlist);
		}

		List<PSComponentSummary> itemSummaries = contentSummariesService.loadComponentSummaries(relationshipOwners);

		return itemSummaries;
	}

	/**
	 * Determines whether an item is in an archived state.  Convenience method to wrap the logic to
	 * hide the details for a PSComponentSummary.
	 * @param item ComponentSummary object of the object to check.
	 * @return True if the item is in an archived workflow state, false otherwise.
	 */
	private boolean isItemArchived(PSComponentSummary item){
		//Get the configuration bean.
		WorkflowConfiguration config = WorkflowConfigurationLocator.getWorkflowConfiguration();
		
		PSWorkflow workflow = _workflowUtil.getWorkflow(item);
		PSState state = _workflowUtil.getWorkflowState(item);
		
		return config.getTransitionMappings().isArchiveState(workflow, state);
	}
	
	/**
	 * Determines whether a given parent item is at a "deeper" (more advanced) archival state than the
	 * current transition's destination state.  "Depth" is defined in the workflow configuration bean.
	 * @param wvc The current workflow validation context.
	 * @param parentItem The parent content item.
	 * @return True if parentItem's workflow state is "deeper" than the transition's destination. 
	 */
	private boolean isParentArchiveStateGreaterOrEqual(WorkflowValidationContext wvc, PSComponentSummary parentItem){
		
		WorkflowConfiguration config = WorkflowConfigurationLocator.getWorkflowConfiguration();
		
		// Make sure parent item is at least as deep an archive state as the item's destination state.
		PSState destinationState = wvc.getDestinationState();
		PSState parentState = _workflowUtil.getWorkflowState(parentItem);
		return config.getWorkflowStates().greaterThanOrEqual(parentState, destinationState);
	}

	/**
	 * @param validationDirections
	 */
	public ParentItemsAreArchived(List<PublishingDirection> validationDirections,
			ValidationIgnoreConditionCollection ignoreConditions) {
		super(validationDirections, ignoreConditions);
		
		_workflowUtil = new WorkflowUtil();
	}

}
