/**
 * 
 */
package gov.cancer.wcm.util;

import java.util.Map;


/**
 * @author learnb
 * 
 *         Utility methods for use within JSP files.
 */
public final class JSPUtil {

	/**
	 * Retrieve a list of values associated with the same name
	 * from a request's Parameter map.
	 * 
	 * @param parameterMap	Parameter map from the JSP request object.  Retrieved as request.getParameterMap().
	 * @param parameterName	Name of the parameter to retrieve values for.
	 * @return
	 */
	public static int[] GetIntParameterList(Map<String, String[]> parameterMap,
			String parameterName) {

		String[] valueList = parameterMap.get("sys_contentid");
		int[] results = new int[valueList.length];

		int i = 0;
		while (i < valueList.length) {
			results[i]=Integer.parseInt(valueList[i]);
			i++;
		}
		
		return results;
	}
}
