/**
 * 
 */
package gov.cancer.wcm.util;

import gov.cancer.wcm.util.CGV_StateHelper.StateName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.percussion.services.content.data.PSContentTypeSummary;
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
	 * Returns true if this contentTypeId is in the provided checkList of autoSlot types.
	 * @param	intValue - the value of the content type id.
	 * @param	cmgr - the content Web service
	 * @param	checList - the list of auto Slot needed content types, generated from the onDemand xml config file.
	 * @return a list of content id's that need to be published.  Never null, possibly empty (if no content itds are to be published).
	 */
	public static List<Integer> autoSlotChecker(int intValue, IPSContentWs cmgr, Map<String,List<String>> checkList) {
		
		List<Integer> returnThis = new ArrayList<Integer>();
		if( checkList.containsKey(Integer.toString(intValue)) )
		{
			List<String> m = checkList.get(Integer.toString(intValue));
			for( String s: m ){
				returnThis.add(Integer.parseInt(s));
			}
		}
		return returnThis;
		
	}
	

}
