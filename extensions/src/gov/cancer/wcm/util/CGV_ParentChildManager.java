package gov.cancer.wcm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
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
	 * Returns a list of Integers (content ids) of a specific IPSGuid item.
	 * @param src - the source item, gets the parents content ids.
	 * @return A list of the parent content ids for IPSGuid src.
	 * @throws PSErrorException 
	 */
	@SuppressWarnings("null")
	public List<Integer> getParentCIDs(IPSGuid src) throws PSErrorException{
		List<PSItemSummary> parents = getParents(src);
		List<Integer> returnThis = new ArrayList<Integer>();
		for( PSItemSummary item : parents ){
			returnThis.add(loadItem(item.getGUID()).getContentId());
		}
		return returnThis;
	}
	
	/**
	 * Convenience call to List<Integer> getParentCIDs(IPSGuid src),
	 * passes in this.guid as the IPSGuid to be used.
	 * @return A list of the parent content ids for this.guid.
	 * @throws PSErrorException 
	 */
	public List<Integer> getParentCIDs() throws PSErrorException{
		return getParentCIDs(this.guid);
	}
	
	/**
	 * Returns the IPSGuid item's content id in a list or integers.
	 * @param item - the IPSGuide item we need the content id of.
	 * @return List containing only the integer value for the IPSGuid item's content id.
	 * @throws PSErrorException
	 */
	public List<Integer> getCID(IPSGuid item) throws PSErrorException{
		return Collections.<Integer> singletonList(loadItem(item).getContentId());
	}
	
	/**
	 * Convenience call, calls getCID(IPSGuid item) with 
	 * this.guid as the IPSGuid object.
	 * @return List containing only the integer value for this.guid's content id.
	 * @throws PSErrorException
	 */
	public List<Integer> getCID() throws PSErrorException{
		return getCID(this.guid);
	}

	/**
	 * Loads a IPSGuid item, into a PSCoreItem object.
	 * @param cid - the IPSGuid that we are finding the PSCoreItem of.
	 * @return the PSCoreItem representation of cid.
	 */
	public static PSCoreItem loadItem(IPSGuid cid) {
		List<IPSGuid> glist = Collections.<IPSGuid> singletonList(cid);
		List<PSCoreItem> items = null;
		PSCoreItem item = null;
		try {
			items = PSContentWsLocator.getContentWebservice().loadItems(glist, true, false, false, false);
			item = items.get(0);
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}
	
	public boolean isCheckedOut(IPSGuid cid){
		List<IPSGuid> glist = Collections.<IPSGuid> singletonList(cid);
		List<PSCoreItem> items = null;
		PSCoreItem item = null;
		try {
			items = PSContentWsLocator.getContentWebservice().loadItems(glist, true, false, false, false);
			item = items.get(0);
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("name of the user who has the item checked out = " + item.getCheckedOutByName());
		if(	item.getCheckedOutByName().length() == 0){
			//System.out.println("the name of the user is nothing....");
			return false;
		}
		else{
			//System.out.println("the name of the user is something!!!!");
			return true;
		}
	}
	
	public int getRevision(IPSGuid cid){
		List<IPSGuid> glist = Collections.<IPSGuid> singletonList(cid);
		List<PSCoreItem> items = null;
		PSCoreItem item = null;
		try {
			items = PSContentWsLocator.getContentWebservice().loadItems(glist, true, false, false, false);
			item = items.get(0);
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item.getRevisionCount();
	}
	

	
	/**
	 * Loads a String content id, into a PSCoreItem object.
	 * @param currItemId - the String form of the content ID we want the PSCoreItem of.
	 * @return the PSCoreItem representation of cid.
	 */
	public static PSCoreItem loadItem(String currItemId) {
		List<IPSGuid> glist = Collections.<IPSGuid> singletonList(PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(currItemId)));
		List<PSCoreItem> items = null;
		PSCoreItem item = null;
		try {
			items = PSContentWsLocator.getContentWebservice().loadItems(glist, true, false, false, false);
			item = items.get(0);
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
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
