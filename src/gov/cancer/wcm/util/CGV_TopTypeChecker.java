/**
 * 
 */
package gov.cancer.wcm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;

/**
 * Utility class for determining if a content type is a topmost type
 * @author whole
 *
 */
public class CGV_TopTypeChecker {
	private static boolean bDebug = false;

	/**
	 * Returns true if this contentTypeId is in the list of topmost content types
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	public static boolean topType(int contentTypeId, IPSContentWs cmgr) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.TOP_CONTENT_TYPE_NAMES;
		for (String s : doNotPublishParentTypes) {
			if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);

				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if this contentTypeId is a navon or nav tree.
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	public static boolean navon(int contentTypeId, IPSContentWs cmgr) {
		//get array of type names
		List<String> navonTypes = new ArrayList<String>();
		navonTypes.add("rffNavon");
		navonTypes.add("rffNavTree");
		for (String s : navonTypes) {
			if (bDebug) System.out.print("DEBUG: navon types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);

				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if this contentTypeId is in the list of content types for multi page containers
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	public static boolean multiPageContainer(int contentTypeId, IPSContentWs cmgr) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.MULTI_PAGE_CONTAINER;
		for (String s : doNotPublishParentTypes) {
			if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);

				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if this contentTypeId is in the list of page types in a multi page.
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	public static boolean multiPagePages(int contentTypeId, IPSContentWs cmgr) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.MULTI_PAGE_PAGES;
		for (String s : doNotPublishParentTypes) {
			if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);

				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if this contentTypeId is in the provided checkList of autoSlot types.
	 * @param	contentTypeId - the value of the content type id.
	 * @param	cmgr - the content Web service
	 * @param	checList - the list of auto Slot needed content types, generated from the onDemand xml config file.
	 * @return a list of content id's that need to be published.  Never null, possibly empty (if no content itds are to be published).
	 */
	public static List<Integer> autoSlotChecker(
			int contentTypeId, IPSContentWs cmgr, Map<String,Map<String,List<String>>> checkList, String site)
			{

		List<Integer> returnThis = new ArrayList<Integer>();
		if(checkList == null && bDebug) {
			System.out.print("DEBUG: checkList is NULL ");
		}
		Set<String> keySet = checkList.keySet();
		Iterator<String> it = keySet.iterator();			

		String curr;
		while( it.hasNext() ){
			curr = it.next();
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(curr);
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);
				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					Map<String, List<String>> siteMapping = checkList.get(curr);
					if(siteMapping != null){
						List<String> m = siteMapping.get(site);
						//List<String> m = checkList.get(curr);
						if(m != null){
							for( String s:m ){
								returnThis.add(Integer.parseInt(s));
							}
						}
					}
				}
			}
		}
		return returnThis;
			}

	/**
	 * Returns true if this contentTypeId is in the list of TCGA content types
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	public static boolean isTCGAContent(int contentTypeId, IPSContentWs cmgr) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.TCGA_TYPES;
		for (String s : doNotPublishParentTypes) {
			if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);

				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if this contentTypeId is in the list of Shared content types
	 * @param contentTypeId - id to check
	 * @return true if in list
	 */
	public static boolean isCrossSiteContent(int contentTypeId, IPSContentWs cmgr) {
		//get array of type names
		String[] doNotPublishParentTypes = CGVConstants.CROSS_SITE_TYPES;
		for (String s : doNotPublishParentTypes) {
			if (bDebug) System.out.print("DEBUG: do not publish parent types " + s);
			//get all summaries matching the current type
			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes(s);
			if (bDebug) System.out.println("the size of the content type summary list is " + summaries.size());
			//get the first item
			if(summaries.size() != 0 ){
				PSContentTypeSummary summaryItem = summaries.get(0);

				if (contentTypeId == summaryItem.getGuid().getUUID()) {
					return true;
				}
			}
		}
		return false;
	}

}
