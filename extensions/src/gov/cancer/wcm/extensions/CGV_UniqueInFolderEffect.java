/**
 * 
 */
package gov.cancer.wcm.extensions;

import static java.text.MessageFormat.format;
import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * Relationship effect to check uniqueness of a field in an item when added to a folder.
 * Skips this check if the content item is a translation (so you can create a translation)
 * 
 * <p>
 * There are 2 parameters:
 * <ul>
 * <li>The field name - defaults to the name of the current field.</li>
 * <li>Exclude Promotable Versions flag -- specify <code>true</code> or <code>false</code></li>
 * </ul> 
 * 
 * See the <code>Extensions.xml</code> for more information.
 * @author holewr
 *
 */
public class CGV_UniqueInFolderEffect implements IPSEffect {
	private CGV_FolderValidateUtils valUtil = null;

    /* (non-Javadoc)
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    public void init(IPSExtensionDef extensionDef, File arg1)
            throws PSExtensionException {
    	if (valUtil == null) {
	    	valUtil = new CGV_FolderValidateUtils();
	    	valUtil.setExtensionDef(extensionDef);
	        if (valUtil.getContentManager() == null) valUtil.setContentManager(PSContentMgrLocator.getContentMgr());
	        if (valUtil.getContentWs() == null) valUtil.setContentWs(PSContentWsLocator.getContentWebservice());
	        if (valUtil.getGuidManager() == null) valUtil.setGuidManager(PSGuidManagerLocator.getGuidMgr());
	        if (valUtil.getNodeCataloger() == null) valUtil.setNodeCataloger(new PSONodeCataloger());
	        if (valUtil.getSystemWs() == null) valUtil.setSystemWs(PSSystemWsLocator.getSystemWebservice()); 
    	}
    	log.debug("CGV_UniqueInFolderEffect: end of init()");
    }

	/* (non-Javadoc)
	 * @see com.percussion.relationship.IPSEffect#recover(java.lang.Object[], com.percussion.server.IPSRequestContext, com.percussion.relationship.IPSExecutionContext, com.percussion.extension.PSExtensionProcessingException, com.percussion.relationship.PSEffectResult)
	 */
	public void recover(Object[] params, IPSRequestContext request, IPSExecutionContext context, PSExtensionProcessingException e, PSEffectResult result) {
		//do nothing
		result.setSuccess(); 
	}
	
	/* (non-Javadoc)
	 * @see com.percussion.relationship.IPSEffect#test(java.lang.Object[], com.percussion.server.IPSRequestContext, com.percussion.relationship.IPSExecutionContext, com.percussion.relationship.PSEffectResult)
	 */
	public void test(Object[] params, IPSRequestContext request, IPSExecutionContext context, PSEffectResult result) {
		//do nothing
		result.setSuccess(); 
	}

	/* (non-Javadoc)
	 * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[], com.percussion.server.IPSRequestContext, com.percussion.relationship.IPSExecutionContext, com.percussion.relationship.PSEffectResult)
	 */
	public void attempt(Object[] params, IPSRequestContext request, IPSExecutionContext context, PSEffectResult result) {
		if (context.isPreConstruction()) {
		//only do this check for preDestruction execution context
			PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(valUtil.getExtensionDef(), params, request, log);
	        String fieldName = h.getRequiredParameter("fieldName");
	        log.debug("[attempt]fieldName = " + fieldName);        
			PSRelationship originating = context.getOriginatingRelationship(); 
			int contentId = originating.getDependent().getId();
			log.debug("[attempt]contentId = " + contentId);
			int folderId = originating.getOwner().getId();
			log.debug("[attempt]folderId = " + folderId);
			String fieldValue = "";
	        String checkPaths = h.getOptionalParameter("checkPaths", null);
			try {
				PSCoreItem item = valUtil.loadItem(String.valueOf(contentId));
				String sysTitle = item.getFieldByName("sys_title").getValue().getValueAsString();
				if (sysTitle.startsWith("[es-us]")) {
					//don't check field if this is a translation
					//this is a gross kluge and I'd like to find a better way to know
			        log.debug("[attempt]setting success - probably a translation");        
		            result.setSuccess();
				}
				else {
					valUtil.doAttempt(contentId, fieldName, folderId, checkPaths, item, result);
				}
			} catch (IllegalArgumentException e) {
			//this happens when you create a folder
		        log.debug("[attempt]setting success - probably a folder");        
				result.setSuccess();
	        } catch (Exception e) {
	           log.error(format("An error happened while checking if " +
	                 "fieldName: {0} was unique for " +
	                 "contentId: {1} with " +
	                 "fieldValue: {2}",
	                 fieldName, request.getParameter("sys_contentid"), fieldValue), e);
		       log.debug("[attempt]setting error - got exception");        
	           result.setError("Pretty_URL_Name must be unique within folder");
	        }
		}
		else {
	        log.debug("[attempt]setting success - not preConstruction");        
			result.setSuccess();
		}
	}
	
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(CGV_UniqueInFolderEffect.class);

}
