package gov.cancer.wcm.util;

import javax.jcr.RepositoryException;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

/**
 * Author: daquinohd
 */

public class ViewURLHelper {

	private static PSLocationUtils locUtils = new PSLocationUtils();	
	public static boolean isCopyableURL = false;
	
	/*
	 * Get GUID from content id:
	 *   - Retrieve component summary from Content ID
	 *   - Get GUID from Content ID and Public Revision ID (pulled from summary)
	 *   - The content ID AND revision ID must be passed in. Field values will not load correctly 
	 *     if there is no revision ID.
	 * getGuidById() can be found in PSOToolkit: 
	 * https://github.com/percussion/PSOToolkit/blob/master/src/com/percussion/pso/jexl/PSOObjectFinder.java
	 * @param cid
	 */
	public static IPSGuid getGuid(int cid) {
		IPSCmsContentSummaries summ = PSCmsContentSummariesLocator.getObjectManager();
		PSOObjectFinder psoObjFinder = new PSOObjectFinder();
		PSComponentSummary summary = null;
		IPSGuid guid = null;
		
		try {
			summary = summ.loadComponentSummary(cid);
			guid = psoObjFinder.getGuidById(Integer.toString(cid), Integer.toString(summary.getPublicRevision()));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return guid;
	}
	
	/*
	 *  Get node object based on GUID
	 *  @param cid
	 */
	public static IPSNode getNode(int cid) {
		PSOObjectFinder psoObjFinder = new PSOObjectFinder();
		IPSNode node = null;
		
		try {
			node = psoObjFinder.getNodeByGuid(getGuid(cid));
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return node;
	}

	/*
	 *  Get node's folder path based on node object
	 *  @param cid
	 */
	public static String getPath(int cid) {
		String path = "";
		
		try {
			path = locUtils.folderPath(getNode(cid));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	/* 
	 * Get the site ID based on the selected content's path. 
	 * Path is passed via getPath(). Method splits path name and retrieves GUID based 
	 * on first item in path, which is the site name.
	 * TODO: Figure out a better way to get the site ID; could be problematic if site folder structure
	 * changes in the future
	 * @param path
	 */
	public static String getSIDFromGUID(String path) {
		IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
		String[] sGuid = null;

		try {
			String[] pathArray = path.split("/");
			IPSSite site = siteManager.loadSite(pathArray[3]);		
			IPSGuid siteGuid = site.getGUID();
			String guid = siteGuid.toString();
			sGuid = guid.split("-");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return sGuid[2];
	}
	
	
	/*
	 * Get the content item's live URL from the site ID, site ID, content ID, filter type, & context.
	 * Return a notification message if the content does not have a live URL.
	 * @param sid
	 * @param fid
	 * @param cid
	 * @param filter
	 * @param context
	*/
	public static String getPublishedURL(int sid, int cid, String filter, int context) {
		
		IPSAssemblyTemplate template = null;
		IPSNode node = getNode(cid);
		
		String path = getPath(cid);
		String templateName = null;
		String url = "";
		String message = null;
		String result = "";

		// Get default template based on node, site ID, and path
		try {
			template = TemplateUtils.getDefaultTemplate(node, Integer.toString(sid), path);
		} catch (NullPointerException e) {
			e.printStackTrace();
			message = ("<p>Error assembling template name.<p>" + e);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}	
		
		// Get template name and pass in other params to generate lcation string
		if(node != null && template != null) {
			templateName = template.getName();
			try {
				// Get the actual URL
				String location = locUtils.generate(templateName, node, path, filter, sid, context);
				url = location;
			} catch (Exception e) {
				message = ("<p>Error assembling URL.<p>" + e);
				e.printStackTrace();
			}
		} else {
			message = ("<p>This content does not have a live URL."); 
		}
		
		if (message == null) {
			result = url;
			isCopyableURL = true;
		} else {
			result = message;
			isCopyableURL = false;
		}
		return result;
	}
}