package gov.cancer.wcm.extensions;
 
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import gov.cancer.wcm.util.CGVConstants;
import gov.cancer.wcm.util.CGV_ParentChildManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 

/**
 * This Validation extension checks to make sure the sys_title does not contain
 * spaces or special characters for a folder object.
 *
 * Below are the characters allowed:
 * A-Z, a-z, 0-9, "-" "_" "."
 * @author John Walls
 */
public class CGV_FolderValidate implements IPSFieldValidator
{
	private static Log LOGGER = LogFactory.getLog(CGV_TitlePopulate.class);
 
	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
	 */
	public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
	{
		
		//PSExtensionParams ep = new PSExtensionParams(params);
		Iterator<Object> i = request.getParametersIterator();
		while(i.hasNext()){
			Set o = (Set)i.next();
			String[] aList = (String[]) o.toArray();
			for( String s : aList){
				System.out.println("String = " + s);
			}
		}
		
		//String currCID = request.getParameter("sys_contentid");
		//String sys = request.getParameter("sys_title");
		//System.out.println("current CID is " + currCID);
		//System.out.println("sys title is " + sys);
//		if( currCID != null ){
//			IPSContentWs cmgr = PSContentWsLocator.getContentWebservice();
//			List<PSContentTypeSummary> summaries = cmgr.loadContentTypes("Folder");
//			IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
//			List<IPSGuid> glist = Collections.<IPSGuid> singletonList(gmgr.makeGuid(new PSLocator(currCID)));
//			List<PSCoreItem> items = null;
//
//			PSCoreItem item = null;
//			try {
//				items = cmgr.loadItems(glist, true, false, false, false);
//			} catch (PSErrorResultsException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			item = items.get(0);
//			Long typeId = item.getContentTypeId();
//			System.out.println("The content type id is " + typeId);
//
//			if(summaries.size() != 0 ){
//				PSContentTypeSummary summaryItem = summaries.get(0);

//	    		if(value.equalsIgnoreCase("101")){
//				//if (typeId.intValue() == summaryItem.getGuid().getUUID()) {
//					System.out.println("Folder!");
//					String systitle = request.getParameter("sys_title");
//					if( systitle != null ){
//						System.out.println("sys title = " + systitle);
//						return validateFolder(systitle);
//					}
//					else{
//						return true;
//					}
//				}
//				else{
//					System.out.println("Not a folder");
//				}
////			}
////		}
		return true;

	}
 
	/**
	 * Validate that URL contains only a-b, A-B, 0-9, and "-" and "_"
	 * @param url to validate
	 * @return boolean true if valid
	 */
	private Object validateFolder(String url) {
		if( url.isEmpty() )
			return true;
		String regex = "[A-Za-z0-9\\-\\_\\.]*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		return m.matches();
	}

	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
      //
    }
}
 
