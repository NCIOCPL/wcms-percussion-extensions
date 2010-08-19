/**
 * 
 */
package gov.cancer.wcm.util;

import java.util.List;

import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.webservices.content.IPSContentWs;

/**
 * Utility class for determining if a content type is a topmost type
 * @author whole
 *
 */
public class CGV_TopTypeChecker {
	private static boolean bDebug = true;
	
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
			PSContentTypeSummary summaryItem = summaries.get(0);
			if (contentTypeId == summaryItem.getGuid().getUUID()) {
				return true;
			}
		}
		return false;
	}

}
