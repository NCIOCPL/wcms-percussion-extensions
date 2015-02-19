/**
 * 
 */
package gov.cancer.wcm.extensions;

import static java.text.MessageFormat.format;
import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.xmldom.PSXdTextToDom;
import com.percussion.xmldom.PSXmlDomContext;

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
@PSHandlesEffectContext(required={PSEffectContext.PRE_CONSTRUCTION})
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
		// test to ensure the context is in either a PreConstruction or PreUpdate state.
		// PreConstruction is seen when a new piece of content is created, or a copy or translation
		// existing content is generate.
		// PreUpdate is specifically for content being moved - in this case, the folder relationship
		// is updated instead of content being created, hence PreUpdate being set instead of
		// PreConstruction.
		if (context.isPreConstruction() || context.isPreUpdate()) {
			
			if (context.isPreConstruction())
			{
				log.debug("[attempt] - context is PreConstruction");
			}
			if (context.isPreUpdate())
			{
				log.debug("[attempt] - context is PreUpdate");
			}
			
			String userName = request.getUserName();
			if (userName == null || userName.isEmpty()) {
				//don't do this if no user (probably migrating)
				result.setSuccess();
				return;
			}
			PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(valUtil.getExtensionDef(), params, request, log);
	        String fieldName = h.getRequiredParameter("fieldName");
	        log.debug("[attempt]fieldName = " + fieldName);

			// retrieve current and originating relationships
	        PSRelationship current = context.getCurrentRelationship();
	        PSRelationship originating = context.getOriginatingRelationship();
	        
	        // current is not guaranteed to be non-null - use originating if current
	        // is null
	        if(current == null && originating != null) {
	        	log.debug("[attempt] current relationship is null - using originating relationship instead.");
	        	current = originating;
	        }
	        
	        // retrieve the name and category of the current relationship config
	        String currentConfigCategory = null;
	        String currentConfigName = null;
	        if (current != null){
	        	currentConfigCategory = current.getConfig().getCategory();
	        	currentConfigName = current.getConfig().getName();
	        }
	        
        	log.debug("[attempt] current relationship config is " + currentConfigName + " (category = " + currentConfigCategory + ")");
	        
        	// skip uniqueness test if the current relationship is not of the folder content type
	        if(!PSRelationshipConfig.CATEGORY_FOLDER.equals(currentConfigCategory))
	        {
	        	String warning = "[attempt]setting success - current relationship is not of a Folder category configuration.";
	        	log.warn(warning);
				result.setWarning(warning);
				return;
	        }
	        
	        // retrieve the originating relationship config name and category
	        String originatingConfigCategory = null;
	        String originatingConfigName = null;
	        if (originating != null){
	        	originatingConfigCategory = originating.getConfig().getCategory();
	        	originatingConfigName = originating.getConfig().getName();
	        }
	        
	        log.debug("[attempt] originating relationship config is " + originatingConfigName + " (category = " + originatingConfigCategory + ")");

	        // for copies and translations, uniqueness test is skipped - instead, uniqueness will
	        // be enforced by the workflow transition.
        	if(PSRelationshipConfig.CATEGORY_COPY.equals(originatingConfigCategory)
        	|| PSRelationshipConfig.CATEGORY_TRANSLATION.equals(originatingConfigCategory))
        	{
        		log.debug("[attempt]setting success - originating relationship category is " + originatingConfigCategory + ", specifically skipping uniqueness test for this relationship.");        
				result.setSuccess();
				return;
        	}
	        
			int contentId = current.getDependent().getId();
			int folderId = current.getOwner().getId();

			log.debug("[attempt]contentId = " + contentId);
			log.debug("[attempt]folderId = " + folderId);

	        String checkPaths = h.getOptionalParameter("checkPaths", null);
			try {
				// if Item is a regular content item, use valUtil.doAttempt() to detect whether it's unique.
				// Otherwise, find out whether this a FolderUpdate request and check whether the
				// proposed folder name is unique
				PSCoreItem item = valUtil.loadItem(String.valueOf(contentId));
				
				if(CGV_FolderValidateUtils.isFolder(item)) {
					log.trace("Item " + contentId + " is a folder");

					// Get the proposed folder name.
					String newName = getRequestFoldername(request);
					
					// Check whether the folder's name will conflict with the value of
					// any of the existing content items 'fieldName' fields.
					valUtil.validateIsFolderNameUnique(contentId, newName, fieldName, folderId, checkPaths, item, result);
				} else {
					log.trace("Item " + contentId + " is not a folder");
					valUtil.doAttempt(contentId, fieldName, folderId, checkPaths, item, result);
				}
			} catch (IllegalArgumentException e) {
			//this happens when you create a folder
		        log.debug("[attempt]setting success - probably a folder");        
				result.setSuccess();
	        } catch (Exception e) {

	        	String msg =  format("An error occured in CGV_UniqueInFolderEffect while checking if " +
		                 "fieldName: {0} was unique for " +
		                 "contentId: {1} in folderId: {2}. Error was: \"{3}\"",
		                 fieldName, contentId, folderId,
		                 e.getMessage() == null ? "" : e.getMessage());

	        	log.error(msg, e);
	        	log.debug("[attempt]setting error - got exception");        
        		result.setError(msg);
	        }
		}
		else {
	        log.debug("[attempt]setting success - not preConstruction or preUpdate");        
			result.setSuccess();
		}
	}
	
	/*
	 * Verifies that request contains a request to update a folder and retrieve the
	 * folder's name.
	 */
	private static String getRequestFoldername(IPSRequestContext request) throws PSErrorException{
		
		if(request == null){
			log.error("Argument 'request' was null in getRequestFoldername().");
			throw new NullArgumentException("request");
		}

		// Assumption: Internal to an effect, requests always have an input document.
		Document doc = request.getInputDocument();
		
		Element root = doc.getDocumentElement();
		if(!root.getNodeName().equals("UpdateFolderRequest")) {
			log.error("getRequestFoldername expected UpdateFolderRequest, found " + root.getNodeName());
			throw new PSErrorException(1, "getRequestFoldername expected UpdateFolderRequest, found " + root.getNodeName(), "");
		}
		
		NodeList nodelist = doc.getElementsByTagName("PSXFolder");
		if(nodelist.getLength() < 1){
			log.error("Node length unexpectedly < 1");
			throw new PSErrorException(1, "getRequestFoldername expected UpdateFolderRequest, found " + root.getNodeName(), "");
		}
		
		// Assumption: the folder PSXFolder element always has a name attribute.
		Node folderInfo = nodelist.item(0);
		NamedNodeMap attr = folderInfo.getAttributes();
		Node nameNode = attr.getNamedItem("name");
		
		return nameNode.getNodeValue();
	}
	
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(CGV_UniqueInFolderEffect.class);

}
