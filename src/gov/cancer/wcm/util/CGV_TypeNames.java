package gov.cancer.wcm.util;

import java.util.ArrayList;
import java.util.List;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;

/**
 */
public class CGV_TypeNames {

	private static PSItemDefManager itemDefMgr;

	protected static void initServices() {
		if (itemDefMgr == null) {
			itemDefMgr = PSItemDefManager.getInstance();
		}
	}
	
	/**
	 * Method getTypeIds.
	 * @param names List<String>
	 * @return List<Long>
	 * @throws PSInvalidContentTypeException
	 */
	public static List<Long> getTypeIds(List<String> names) throws PSInvalidContentTypeException {
		List<Long> ret = new ArrayList<Long>();
		for (String name:names) {
			ret.add(getTypeId(name));
		}
		return ret;
	}
	/**
	 * Method getTypeNames.
	 * @param ids List<Long>
	 * @return List<String>
	 * @throws PSInvalidContentTypeException
	 */
	public static List<String> getTypeNames(List<Long> ids) throws PSInvalidContentTypeException{
		List<String> ret = new ArrayList<String>();
		for (Long id:ids) {
			ret.add(getTypeName(id));
		}
		return ret;
	}
	
	/**
	 * Method getTypeName.
	 * @param id long
	 * @return String
	 * @throws PSInvalidContentTypeException
	 */
	public static String getTypeName(long id) throws PSInvalidContentTypeException {
		initServices();
		return itemDefMgr.contentTypeIdToName(id);
	}

	/**
	 * Method getTypeId.
	 * @param name String
	 * @return long
	 * @throws PSInvalidContentTypeException
	 */
	public static long getTypeId(String name) throws PSInvalidContentTypeException {
		initServices();
		return itemDefMgr.contentTypeNameToId(name);
	}
	
}
