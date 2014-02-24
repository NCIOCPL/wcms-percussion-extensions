/**
 * Utility methods for working with Relationships.
 */
package gov.cancer.wcm.util;

import gov.cancer.wcm.privateArchive.PrivateArchiveManager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.webservices.PSErrorException;
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
public class RelationshipUtil {

	private static Log log = LogFactory.getLog(PrivateArchiveManager.class);

	// Rhythmyx service interfaces
	private static IPSCmsContentSummaries contentSummariesService;
	private static IPSSystemWs systemWebService;

	/*
	 * This initializes some of the different services
	 */
	static {
		contentSummariesService = PSCmsContentSummariesLocator.getObjectManager();
		systemWebService = PSSystemWsLocator.getSystemWebservice();
	}


	
	/**
	 * Locate all content items which are the owner of an active assembly relationship in which dependentContentItemID
	 * participates as a dependent. 
	 * @param dependentContentItemID The content item to find parents for.
	 * @return A collection of Component Summary objects describing the parent items.
	 * @throws Exception 
	 */
	static public List<PSComponentSummary> FindParentContentItems(int dependentContentItemID) throws PSErrorException{
		log.trace("Enter ParentItemsAreArchived.FindParentContentItems(int dependentContentItemID)");

		// Look up the item's component summary.
		PSComponentSummary contentItemSummary = contentSummariesService.loadComponentSummary(dependentContentItemID);
		
		return FindParentContentItems(contentItemSummary);
	}
	
	
	/**
	 * Locate all content items which are the owner of an active assembly relationship in which dependentContentItemSummary
	 * participates as a dependent. 
	 * @param dependentContentItemSummary The content item to find parents for.
	 * @return A collection of Component Summary objects describing the parent items.
	 * @throws Exception 
	 */
	static public List<PSComponentSummary> FindParentContentItems(PSComponentSummary dependentContentItemSummary) throws PSErrorException {
		log.trace("Enter ParentItemsAreArchived.FindParentContentItems(PSComponentSummary dependentContentItemSummary)");

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
		} catch (PSErrorException ex) {
			log.error("getTransitionRoot: Could not get relationships for id: " + contentItemLocator.getId(), ex);
			throw ex;
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


	
	// Hide the constructor.  This class is not intended to have instances.
	private RelationshipUtil(){
		
	}

}
