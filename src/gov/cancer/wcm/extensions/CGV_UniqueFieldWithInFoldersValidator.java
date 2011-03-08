/**************************************
 * 
 * Based on:
 * com.precussion.pso.validation PSOUniqueFieldWithInFoldersValidator
 *  
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author agent 
 * modified by holewr to allow use on items outside of folders
 *
 */
package gov.cancer.wcm.extensions;

import static java.text.MessageFormat.format;

import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * This is a field validator that checks whether or not a 
 * field is unique for all the folders that the item resides
 * (or to be created) in.
 * <p>
 * There are 3 parameters:
 * <ul>
 * <li>The field value - defaults to the current value of the field.</li>
 * <li>The field name - defaults to the name of the current field.</li>
 * <li>Exclude Promotable Versions flag -- specify <code>true</code> or <code>false</code></li>
 * </ul> 
 * 
 * See the <code>Extensions.xml</code> for more information.
 * @author adamgent
 *
 */
public class CGV_UniqueFieldWithInFoldersValidator implements IPSFieldValidator {
	private CGV_FolderValidateUtils valUtil = null;
	
    /* (non-Javadoc)
     * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
     */
    public Boolean processUdf(Object[] params, IPSRequestContext request)
            throws PSConversionException {
    	log.debug("CGV_UniqueFieldWithInFoldersValidator: processUdf()");
        String cmd = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
        String actionType = request.getParameter("DBActionType");
        if(actionType == null || 
           !(actionType.equals("INSERT") || actionType.equals("UPDATE")))
           return true;
        
        PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(valUtil.getExtensionDef(), params, request, log);
        String fieldName = h.getRequiredParameter("fieldName");
        
        String fieldValue = request.getParameter(fieldName);
        if (fieldValue == null) {
            log.debug("Field value was null for field: " + fieldName);
            return true;
        }
        boolean xpv = h.getOptionalParameterAsBoolean("excludePromotableVersions", false);
        String checkPaths = h.getOptionalParameter("checkPaths", null);
        
        Number contentId = new Integer(0);
        try {
           if (actionType.equals("UPDATE")) {
               contentId = h.getRequiredParameterAsNumber("sys_contentid");
           }
           if(xpv)
           {
              log.debug("excluding promotable versions");
             
              //sys_command is modify if this is a user update, not a clone, new copy
              //or new version.
              if(StringUtils.isNotBlank(cmd) && !cmd.equalsIgnoreCase("modify"))
              {
                 log.debug("command is not modify - " + cmd); 
                 return true;
              }
                 
              if(valUtil.isPromotable(contentId.intValue()))
              {
                 return true; 
              }
           }


           String typeList = valUtil.makeTypeList(fieldName);
           boolean rvalue = true;
           if (actionType.equals("UPDATE")) {
              rvalue = valUtil.isFieldValueUniqueInFolderForExistingItem(contentId.intValue(), fieldName, fieldValue, typeList, checkPaths);
           }
           else {
              Number folderId = valUtil.getFolderId(request);
              if (folderId != null)
                 rvalue = valUtil.isFieldValueUniqueInFolder(folderId.intValue(), fieldName, fieldValue, typeList, checkPaths, 0);
              else
                 rvalue = true;	//was false, but we want to OK items not in folders
           }
           return rvalue;
        } catch (Exception e) {
           log.error(format("An error happend while checking if " +
                 "fieldName: {0} was unique for " +
                 "contentId: {1} with " +
                 "fieldValue: {2}",
                 fieldName, request.getParameter("sys_contentid"), fieldValue), e);
           return false;
        }
    }
    
    /* (non-Javadoc)
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    public void init(IPSExtensionDef extensionDef, File arg1)
            throws PSExtensionException {
    	log.debug("CGV_UniqueFieldWithInFoldersValidator: init()");
    	if (valUtil == null) {
	    	valUtil = new CGV_FolderValidateUtils();
	    	valUtil.setExtensionDef(extensionDef);
	        if (valUtil.getContentManager() == null) valUtil.setContentManager(PSContentMgrLocator.getContentMgr());
	        if (valUtil.getContentWs() == null) valUtil.setContentWs(PSContentWsLocator.getContentWebservice());
	        if (valUtil.getGuidManager() == null) valUtil.setGuidManager(PSGuidManagerLocator.getGuidMgr());
	        if (valUtil.getNodeCataloger() == null) valUtil.setNodeCataloger(new PSONodeCataloger());
	        if (valUtil.getSystemWs() == null) valUtil.setSystemWs(PSSystemWsLocator.getSystemWebservice()); 
    	}
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(CGV_UniqueFieldWithInFoldersValidator.class);

}
