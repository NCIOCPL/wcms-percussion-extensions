/**
 * Field validator for use with folders to check that the sys_title
 * does not cause a conflict with a specified field in content items.
 * 
 * This validator is intended to be used in conjunction with sys_ValidateUniqueName()
 * which checks for folder names not conflicting with other folders.
 */
package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * @author learnb
 *
 */
public class CGV_FolderNameUniqueValidator implements IPSFieldValidator {

	// Helper class to perform the actual validations.
	private CGV_FolderValidateUtils valUtil = null;

	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
	 */
	@Override
	public Boolean processUdf(Object[] params, IPSRequestContext request)
			throws PSConversionException {

		// No processing if this isn't an INSERT or UPDATE operation
        String actionType = request.getParameter("DBActionType");
        if(actionType == null || 
           !(actionType.equals("INSERT") || actionType.equals("UPDATE")))
           return true;

        // Get the proposed folder's name and the field to compare it against.
        PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(valUtil.getExtensionDef(), params, request, log);
        String validateFieldName = h.getRequiredParameter("validateFieldName");
        String uniqueFieldName = h.getRequiredParameter("uniqueFieldName");
        
        // No processing for anything but folders
        if(!request.getCurrentApplicationName().equals("psx_cefolder"))
        	return true;

        // Detection of the parent folder requires a redirect URL.
        // This is not available when folder content items are created.
        Integer folderid = valUtil.getFolderId(request);
        
        String fieldValue = request.getParameter(validateFieldName);
        if (fieldValue == null) {
            log.debug("Field value was null for field: " + validateFieldName);
            return false;
        }


		// Search for content items which use that field and have the
		// field set to the name the folder wants to use.
		// Were any rows found?
		// Yes -- Fail.
		// No -- Success.
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
	 */
	@Override
	public void init(IPSExtensionDef extensionDef, File arg1)
			throws PSExtensionException {

		log.debug("CGV_FolderNameUniqueValidator: init()");
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
            .getLog(CGV_FolderNameUniqueValidator.class);

}
