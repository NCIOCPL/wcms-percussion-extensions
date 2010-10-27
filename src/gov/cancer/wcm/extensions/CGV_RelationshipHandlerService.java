package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_RelItem;
import gov.cancer.wcm.util.CGV_RelationshipConfig;
import gov.cancer.wcm.util.CGV_TransitionDestination;
import gov.cancer.wcm.util.CGV_TypeNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.pso.jexl.PSONavTools;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
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
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 */
public class CGV_RelationshipHandlerService {

	private static IPSSystemWs sws;
	private List<CGV_RelationshipConfig> configs;


	private static Log log = LogFactory.getLog(CGV_RelationshipHandlerService.class);
	private static IPSCmsContentSummaries summ;
	private static IPSContentWs cmgr;
	private static IPSWorkflowService wfService;

	Map<String,CGV_TransitionDestination> transitionMappings;

	private static IPSGuidManager gmgr;
	private static PSOWorkflowInfoFinder workInfo;
	protected static void initServices() {
		if (sws == null) {
			gmgr = PSGuidManagerLocator.getGuidMgr();
			sws = PSSystemWsLocator.getSystemWebservice();
			cmgr = PSContentWsLocator.getContentWebservice();
			summ = PSCmsContentSummariesLocator.getObjectManager();
			wfService = PSWorkflowServiceLocator.getWorkflowService();
		}
	}

	/**
	 * Method findRelations.
	 * @param idList List<Integer>
	 * @return Set<CGV_RelItem>
	 * @throws PSErrorException
	 * @throws PSInvalidContentTypeException
	 */
	public Set<CGV_RelItem> findRelations(List<Integer> idList) throws PSErrorException, PSInvalidContentTypeException {
		initServices();

		Stack<CGV_RelItem> toProcessUp = new Stack<CGV_RelItem>();
		Stack<CGV_RelItem> toProcessDown = new Stack<CGV_RelItem>();
		Set<CGV_RelItem> items = new HashSet<CGV_RelItem>();

		for (int id : idList) {
			CGV_RelItem item = new CGV_RelItem(id);
			item.setUpLevel(1);
			item.setWfFollow(true);
			toProcessUp.push(item);
			toProcessDown.push(item);

		}

		while (!toProcessUp.isEmpty() || !toProcessDown.isEmpty()) {
			boolean isDown = false;
			CGV_RelItem itemToProcess;
			if (!toProcessUp.isEmpty()) {
				itemToProcess=toProcessUp.pop();
			} else {
				itemToProcess=toProcessDown.pop();
				isDown=true;
			}
			List<CGV_RelItem> relatedItems =  getRels(itemToProcess,isDown);
			items.add(itemToProcess);

			for (CGV_RelItem addItem :relatedItems) {
				if (	! itemToProcess.isShared() &&
						!(toProcessDown.contains(addItem) || toProcessUp.contains(addItem) || items.contains(addItem)) ) {
					toProcessUp.push(addItem);
					toProcessDown.push(addItem);
				}
			}

		}		


		return items;
	}

	/**
	 * Method getRels.
	 * @param itemToProcess CGV_RelItem
	 * @param isDown boolean
	 * @return List<CGV_RelItem>
	 * @throws PSErrorException
	 * @throws PSInvalidContentTypeException
	 */
	private List<CGV_RelItem> getRels(CGV_RelItem itemToProcess, boolean isDown) throws PSErrorException, PSInvalidContentTypeException {
		int id = itemToProcess.getId();
		PSComponentSummary summary = summ.loadComponentSummary(id);
		String itemContentType = CGV_TypeNames.getTypeName(summary.getContentTypeId());
		List<PSRelationship> rels = new ArrayList<PSRelationship>();
		List<CGV_RelItem> retItems = new ArrayList<CGV_RelItem>();
		for (CGV_RelationshipConfig config : configs) {
			log.debug("Processing config :"+config.getName());
			PSRelationshipFilter filter = new PSRelationshipFilter();
			if (isDown) {
				log.debug("isDown");
				itemToProcess.setDownComplete(true);
				if ( (config.getParentTypes()==null || config.getParentTypes().contains(itemContentType))
						&&  (config.getChildTypeIds()==null || config.getChildTypeIds().size()>0)
						// Have we reached the max level for this config.
						&&  (itemToProcess.getDownLevel() <= config.getMaxDown())
				) {
					log.debug("Processing config"+config.getName());
					filter.setOwner(new PSLocator(itemToProcess.getId()));
					if (config.getChildTypeIds()!= null) {
						log.debug("Setting type filter "+config.getChildTypes());
						filter.setDependentContentTypeIds(config.getChildTypeIds());
					}
					log.debug("Setting relationship filter "+config.getRelationshipNames());
					filter.setNames(config.getRelationshipNames());

					rels = sws.loadRelationships(filter);
					log.debug("Found "+rels.size()+ " results");
					Set<Integer> childIds = new HashSet<Integer>();
					for (PSRelationship rel:rels) {
						int depId = rel.getDependent().getId();	
						CGV_RelItem item = new CGV_RelItem(depId);

						if (!retItems.contains(item)) {
							item.setDownLevel(itemToProcess.getDownLevel()+1);
							childIds.add(depId);
							retItems.add(item);
						} else {
							item = retItems.get(retItems.indexOf(item));
						}

						//isUp is processed first so we assume isShared is set correctly.

						if (!itemToProcess.isShared() && itemToProcess.isWfFollow() && config.isWfFollow()) {
							item.setWfFollow(true);
						}



					}

					if (itemToProcess.getChildren()==null) {
						itemToProcess.setChildren(childIds);
					} else {
						itemToProcess.getChildren().addAll(childIds);
					}

					log.debug("Children size is "+itemToProcess.getChildren().size());


				}
			} else {
				log.debug("isUp");
				itemToProcess.setUpComplete(true);
				filter.setDependent(new PSLocator(itemToProcess.getId()));
				filter.limitToEditOrCurrentOwnerRevision(true);

				if ( (config.getChildTypes()==null || config.getChildTypes().contains(itemContentType))
						&&  (config.getParentTypeIds()==null || config.getParentTypeIds().size()>0)
						// Have we reached the max level for this config.
						&&  (itemToProcess.getUpLevel() <= config.getMaxUp())
				) {
					log.debug("Processing config"+config.getName());
					filter.setDependent(new PSLocator(itemToProcess.getId()));
					if (config.getParentTypeIds()!= null && config.getParentTypeIds().size()==1) {
						//Can only filter parent by single id. will post process otherwise.
						log.debug("Setting type filter "+config.getParentTypes());
						filter.setOwnerContentTypeId(config.getChildTypeIds().get(0));
					}
				}
				log.debug("Setting relationship filter "+config.getRelationshipNames());
				filter.setNames(config.getRelationshipNames());


				rels = sws.loadRelationships(filter);


				Set<Integer> parentIds = new HashSet<Integer>();
				for (PSRelationship rel:rels) {
					int ownerId = rel.getOwner().getId();
					log.debug("Testing rel owner "+ownerId);
					boolean correctType = true;
					if (config.getParentTypeIds()!= null && config.getParentTypeIds().size()>1) {
						log.debug("Filtering result content types "+config.getParentTypes());

						long typeId = summ.loadComponentSummary(ownerId).getContentTypeId();
						correctType = config.getParentTypeIds().contains(typeId);
					}
					if (correctType) {
						CGV_RelItem item = new CGV_RelItem(ownerId);
						if (!retItems.contains(item)) {
							item.setUpLevel(itemToProcess.getUpLevel()+1);
							retItems.add(item);
							parentIds.add(ownerId);
						} else {
							item = retItems.get(retItems.indexOf(item));
						}

						if (!itemToProcess.isShared() && itemToProcess.isWfFollow() && config.isWfFollow()) {
							// Parents will not follow
							//	item.setWfFollow(true);
						}


					}}

				if (itemToProcess.getParents()==null) {
					itemToProcess.setParents(parentIds);
					if (itemToProcess.getParents().size()>1) {
						log.debug("Setting item as shared");
						itemToProcess.setShared(true);
						itemToProcess.setWfFollow(false);
						// Set shared items to not follow;
						for (CGV_RelItem item : retItems) {
							item.setWfFollow(false);
						}
					}
				} else {
					itemToProcess.getParents().addAll(parentIds);

				}

				log.debug("Parents size is "+itemToProcess.getParents().size());


			}

		}

		log.debug("Found "+retItems.size()+" related items");
		return retItems;
	}




	/**
	 * Method isValidChildState.
	 * @param wfName String
	 * @param destState PSState
	 * @param errorDoc Document
	 * @return boolean
	 */
	boolean isValidChildState(String wfName,PSState destState,Set<CGV_RelItem> items, Document errorDoc) {
		log.debug("Checking state "+destState.getName());
		CGV_TransitionDestination tDest = transitionMappings.get(destState.getName());
		boolean success=true;
		if (tDest != null) {

			for (CGV_RelItem item : items) {
				if (item.getWfStateName().equals(destState.getName())) {
					log.debug("Item " + item.getId() + " already in state "+destState.getName());
				} else {

					if (tDest.getValidChildStates().contains(item.getWfStateName()) ) {
						log.debug("Item " + item.getId() + " is in valid child state "+item.getWfStateName() );
					} else {
						log.debug("Item " + item.getId() + " is in an invalid child state "+item.getWfStateName() +" it needs to be in one of "+tDest.getValidChildStates());

						PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Dependent item {0} is in an invalid state {1} for the transition, it needs to be in one of the following {2} to continue", new Object[]{item.getId(),item.getWfStateName(),tDest.getValidChildStates()});

						success=false;
					}
				}
			} 

		}else  {
			PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Cannot find configuration for destination state {0}", new Object[]{destState});

			log.error("Cannot find mappings for state "+destState+" in transitionMappings Check spring config");
			success=false;
		}
		return success;
	}

	/**
	 * Method checkCheckoutState.
	 * @param items Set<CGV_RelItem>
	 * @param errorDoc Document
	 * @return boolean
	 */
	public boolean checkCheckoutState(
			Set<CGV_RelItem> items, Document errorDoc) {
		boolean success=true;

		String userName = (String) PSRequestInfo.getRequestInfo(
				PSRequestInfo.KEY_USER);

		for (CGV_RelItem item : items) {

			String checkedOutUser = item.getCheckedOutTo();
			log.debug("CheckedOutUser = "+checkedOutUser);
			if (checkedOutUser!=null && checkedOutUser.length()>0 && !checkedOutUser.equals(userName)) {
				PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Dependent item {0} is is checked out to {1}", new Object[]{item.getId(),checkedOutUser});	
				success=false;
			}

		}
		return success;
	}
	/**
	 * Method checkTransition.
	 * @param wfName String
	 * @param destState PSState
	 * @param currItem CGV_RelItem
	 * @param items Set<CGV_RelItem>
	 * @param errorDoc Document
	 * @return boolean
	 */
	public boolean checkTransition(String wfName, PSState destState,CGV_RelItem currItem,
			Set<CGV_RelItem> items, Document errorDoc) {

		CGV_TransitionDestination tDest = transitionMappings.get(destState.getName());
		boolean success=true;
		if (tDest != null) {

			Stack<CGV_RelItem> processItems = new Stack<CGV_RelItem>();
			processItems.addAll(items);

			List<String> autoTransitions = tDest.getAutoTransitionNames();
			log.debug("Configured auto Transition names = "+autoTransitions);
			while(!processItems.isEmpty()) {

				CGV_RelItem item = processItems.pop();
				log.debug("Checking item with id "+item.getId());
				if (item.getWfStateName().equals(destState.getName())) {
					log.debug("Item is already in destination state "+destState.getName());
				}  else {
					log.debug("Item is in state "+item.getWfStateName()+" it needs to move to "+destState.getName());
					IPSGuid itemGuid = gmgr.makeGuid(new PSLocator(item.getId()));
					Map<String, String> transitions = sws.getAllowedTransitions(Collections.singletonList(itemGuid));
					List<String> availableTransitionNames = new ArrayList<String>(transitions.keySet());
					log.debug("Available transitions for item "+item.getId()+" are "+availableTransitionNames);
					availableTransitionNames.retainAll(autoTransitions);
					if (availableTransitionNames.size()>1) {
						log.debug("Multiple available transitions will pick first"+availableTransitionNames);
					} else if (tDest.getValidChildStates().contains(item.getWfStateName())){
						log.debug("Already in the state, or further");
					} else if (availableTransitionNames.size()==0) {
						PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Cannot transition dependent item {0}.  Item is in state {1} and cannot transition with any of {2}", new Object[]{item.getId(),item.getWfStateName(),autoTransitions});	
						success=false;
					} else {
						String tName = availableTransitionNames.get(0);
						log.debug("Will transition with "+tName);
						try {
							int oldState = item.getWfState();
							sws.transitionItems(Collections.singletonList(itemGuid), tName);
							item.resetStatus();
							if (item.getWfState()==oldState) {
								PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Error transition dependent item {0}, with transition name {1} item remained in same state {2}", new Object[]{item.getId(),tName,item.getWfStateName()});	
								success=false;
							} else {
								processItems.push(item);
							}
						} catch (Exception e) {
							log.error("Failed to transition item "+item.getId()+" with transition "+tName,e);
							PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Error transition dependent item {0}, with transition name {1}", new Object[]{item.getId(),tName});	
							success=false;
						}


					}
				}


			}
		} else  {
			PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Cannot find configuration for destination state {0}", new Object[]{destState});

			log.error("Cannot find mappings for state "+destState+" in transitionMappings Check spring config");
			success=false;
		}
		return success;
	}

	public boolean preventAncestorPull(CGV_RelItem item,PSState destState) {
		CGV_TransitionDestination tDest = transitionMappings.get(destState.getName());
		return (tDest.isPreventAncestorPull() && item.getParents().size()>0);
	}
	/**
	 * Method isNavonPublic.
	 * @param folderID IPSGuid
	 * @return Boolean
	 */
	private static Boolean isNavonPublic(IPSGuid folderID){

		PSONavTools nav = new PSONavTools();
		IPSNode node = null;

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

			return (validFlag.equals("y") || validFlag.equals("i"));	
		}
		return false;
	}

	public boolean checkNavonState(	
			Set<CGV_RelItem> items, Document errorDoc) throws PSCmsException {
		boolean success=true;

		Set<String> paths = new HashSet<String>();
		for(CGV_RelItem item :items) {
			paths.addAll(getFolderPaths(item.getId()));
		}

		for (String path : paths) {
			for (IPSGuid folderGuid : getFolderGuid(path)) {
				if (!isNavonPublic(folderGuid)) {
					PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Navon item with id {0} and path {1} is not in a public state ", new Object[]{folderGuid.getUUID(),path});	
					success=false;
				}
			}
		}
		return success;
	}

	public List<IPSGuid> getFolderGuid(String path) throws PSCmsException {
		PSRequest req = (PSRequest) PSRequestInfo
		.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
		List<IPSGuid>ret = folderproc.findMatchingFolders(path);
		return ret;
	}

	public List<String> getFolderPaths(int id) throws PSCmsException {
		PSRequest req = (PSRequest) PSRequestInfo
		.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
		String[] ret = folderproc.getFolderPaths(new PSLocator(id,-1));
		return Arrays.asList(ret);
	}
	/**
	 * Method setConfigs.
	 * @param configs List<CGV_RelationshipConfig>
	 */
	public void setConfigs(List<CGV_RelationshipConfig> configs) {
		this.configs = configs;
	}


	/**
	 * Method getConfigs.
	 * @return List<CGV_RelationshipConfig>
	 */
	public List<CGV_RelationshipConfig> getConfigs() {
		return configs;
	}


	/**
	 * Method getTransitionMappings.
	 * @return Map<String,CGV_TransitionDestination>
	 */
	public Map<String, CGV_TransitionDestination> getTransitionMappings() {
		return transitionMappings;
	}

	/**
	 * Method setTransitionMappings.
	 * @param transitionMappings Map<String,CGV_TransitionDestination>
	 */
	public void setTransitionMappings(
			Map<String, CGV_TransitionDestination> transitionMappings) {
		this.transitionMappings = transitionMappings;
	}

	private static final String ERR_FIELD = "TransitionValidation";
	private static final String ERR_FIELD_DISP = "TransitionValidation";



}
