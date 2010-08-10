package gov.cancer.wcm.util;

import java.util.List;

import com.percussion.services.content.data.PSItemSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * CGV_ParentChildManager allows the user to access methods that deal with 
 * the management of parent and child items in the active assembly of the
 * Percussion CMS.  Uses a single guid to access a list of parent and 
 * child items for that specific guid's most recent revision.
 * 
 * @author John Walls
 *
 */
public class CGV_ParentChildManager {
	
	/**
	 * IPSGuid guid - is the guid (Percussion object) that the
	 * manager will be using to access parent and children for.
	 */
	private IPSGuid guid;
	
	/**
	 * Returns the guid field.
	 * @return the guid (IPSGuid) object the managing is done with.
	 */
	public IPSGuid getGuid() {
		return guid;
	}

	/**
	 * Sets the guid field.
	 * @param guid - Manager will use this to access this specific guid's parents/children.
	 */
	public void setGuid(IPSGuid guid) {
		this.guid = guid;
	}

	/**
	 * Default constructor.
	 */
	public CGV_ParentChildManager(){
		guid = null;
	}

	/**
	 * Constructor that takes in a IPSGuide object to pre-load in.
	 * @param id
	 */
	public CGV_ParentChildManager(IPSGuid id){
		guid = id;
	}
	
	/**
	 * Returns a List of the parents for this active assembly, based on the GUID
	 * that is passed in.  The list items will be in the type of PSItemSummary.
	 * To call the method without a specific GUID passed in, to use the GUID
	 * that the class holds in the guid field, just call the method with no parameter.
	 * 
	 * @param source - The guid to get the Parent items for.
	 * @return List of PSItemSummary objects for the parents of the guid passed in.
	 * @throws PSErrorException
	 */
	public List<PSItemSummary> getParents(IPSGuid source) throws PSErrorException {
		PSRelationshipFilter filter = new PSRelationshipFilter();
		filter.limitToEditOrCurrentOwnerRevision(true);
		filter.setCategory("rs_activeassembly");
		System.out.println("finding the parents");	//TODO: Change to use LOGGER
		return PSContentWsLocator.getContentWebservice().findOwners(source, filter, false);
	}
	
	/**
	 * Returns a List of the children for this active assembly, based on the GUID
	 * that is passed in.  The list items will be in the type of PSItemSummary.
	 * To call the method without a specific GUID passed in, to use the GUID
	 * that the class holds in the guid field, just call the method with no parameter.
	 * 
	 * @param source - The guid to get the Child items for.
	 * @return List of PSItemSummary objects for the child of the guid passed in.
	 * @throws PSErrorException
	 */
	public List<PSItemSummary> getChildren(IPSGuid source) throws PSErrorException {
		PSRelationshipFilter filter = new PSRelationshipFilter();
		filter.limitToEditOrCurrentOwnerRevision(true);
		filter.setCategory("rs_activeassembly");
		System.out.println("finding the children");		//TODO: Change to use LOGGER
		return PSContentWsLocator.getContentWebservice().findDependents(source, filter, false);
	}
	
	/**
	 * Convenience method, calls getParents(IPSGuid source) with this.guid as IPSGuid source.
	 * Returns a List of the parents for this active assembly.
	 * The list items will be in the type of PSItemSummary.
	 * 
	 * @return List of PSItemSummary objects for the parents.
	 * @throws PSErrorException
	 */
	public List<PSItemSummary> getParents() throws PSErrorException {
		return getParents(this.guid);
	}
	
	/**
	 * Convenience method, calls getChildren(IPSGuid source) with this.guid as IPSGuid source.
	 * Returns a List of the children for this active assembly.
	 * The list items will be in the type of PSItemSummary.
	 * 
	 * @return List of PSItemSummary objects for the children.
	 * @throws PSErrorException
	 */
	public List<PSItemSummary> getChildren() throws PSErrorException {
		return getChildren(this.guid);
	}
	
	/**
	 * Checks to see if a specific IPSGuid is a shared child, meaning it
	 * has more then 1 parent.
	 * 
	 * @param source - the IPSGuid to check if it is shared or not.
	 * @return	True if the GUID is shared, false if not.
	 * @throws PSErrorException
	 */
	public boolean isSharedChild(IPSGuid source) throws PSErrorException {
		List<PSItemSummary> owners = null;
		try {
			owners = getParents(guid);
		} catch (PSErrorException e) {
			e.printStackTrace();
		}
		if( owners.size() > 1 ){
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Convenience method call.  Calls isSharedChild(IPSGuid) with this.guid.
	 * @return True if the GUID is a shared child (>1 parent), false if not.
	 * @throws PSErrorException
	 */
	public boolean isSharedChild() throws PSErrorException{
		return isSharedChild(this.guid);
	}

}
