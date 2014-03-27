package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.pso.utils.RxItemUtils;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;


@PSHandlesEffectContext(required={PSEffectContext.PRE_CONSTRUCTION})
public class CGV_AutoShare extends PSBaseServiceLocator implements IPSEffect, InitializingBean{
	private CGV_FolderValidateUtils valUtil = null;
    private static final Log log = LogFactory.getLog(CGV_AutoShare.class);
    
    
	private Map<String,Map<String, String>> autoShareRules;
	
	public Map<String, Map<String, String>> getAutoShareRules() {
		return autoShareRules;
	}

	public void setAutoShareRules(Map<String, Map<String, String>> newAutoShareRules) {
		this.autoShareRules = newAutoShareRules;
	}
	
	public String getSourcePath(String contentType, String locale){
		Map<String,String> cTypeRules = autoShareRules.get(contentType);
		if (locale.equals("es-us")){
			return cTypeRules.get("srcPathES");
		}
		else{
			return cTypeRules.get("srcPathEN");
		}
	}
	
	public String getTargetPath(String contentType, String locale){
		Map<String,String> cTypeRules = autoShareRules.get(contentType);
		if (locale.equals("es-us")){
			return cTypeRules.get("tgtPathES");
		}
		else{
			return cTypeRules.get("tgtPathEN");
		}
	}

	@Override
	public void attempt(Object[] params, IPSRequestContext request,
			IPSExecutionContext context, PSEffectResult result)
			throws PSExtensionProcessingException, PSParameterMismatchException {
		if (!context.isPreConstruction()) {
			result.setSuccess();
			return;
		}
		IPSContentWs contentWs = valUtil.getContentWs();
		PSRelationship originating = context.getOriginatingRelationship(); 
		PSLocator depContentId = originating.getDependent();
		if(depContentId.getId() == Integer.MAX_VALUE){
			//This is copy as new, we would not autoshare this.
			result.setSuccess();
			return;
		}
		IPSGuid depGuid = valUtil.getGuidManager().makeGuid(originating.getDependent());
		List<PSItemStatus> statusList = null;
		List<PSCoreItem> items = null;
		PSCoreItem itemToShare = null;
		PSComponentSummary depSum = null;
		try {
			 depSum = PSOItemSummaryFinder.getSummary(depGuid);
		} catch (PSException e4) {
			result.setSuccess();
			//result.setError("Cannot get item summary from dependent GUID");
			e4.printStackTrace();
			return;
		}
		//This code is only concerned with content items, not folders
		if(depSum.isFolder()){
			result.setSuccess();
			return;
		}
		
	
		//Creates a list with a single item (the new item) to use in the load items call
		List<IPSGuid> glist = Collections.<IPSGuid>singletonList(depGuid);
		try {
			items = contentWs.loadItems(glist, true, false, false, false);

		} catch (PSErrorResultsException e3) {
			result.setSuccess();
			//result.setError("Cannot load item to share");
			e3.printStackTrace();
			return;

		}
		//Checks if an item was returned
		if (items != null && items.size()>0){
			itemToShare = items.get(0);
		}
		else{
			result.setSuccess();
	    	log.debug("No items loaded - no item to share");
	    	//result.setError("No items loaded - no item to share");
			return;
		}
		//Gets Content Type Name
		String typeName = "";
		if( itemToShare.getItemDefinition() != null) {
		  typeName = itemToShare.getItemDefinition().getName();
		}
		//check if the content type is in the autoShare bean, if not, return out of the autoshare code
		boolean isInBean = false;
		for(String cTypeKey : autoShareRules.keySet()){
			if (cTypeKey.equals(typeName)){
				isInBean = true;
			}
		}
		//if the content type is not in the bean it shouldnt be autoshared, 
		//so we can exit out of the Autoshare code
		if(!isInBean){
			result.setSuccess();
			return;
		}
		String tfShared = "true";
		String locale = "";
		//Check if the item has been autoshared before, if yes, do not share again
		try {
			tfShared = RxItemUtils.getFieldValue(itemToShare, "hasBeenAutoShared");
			locale = RxItemUtils.getFieldValue(itemToShare, "sys_lang");
		} catch (PSCmsException e2) {
			result.setSuccess();
			//result.setError("Cannot parse hasBeenAutoShared or sys_lang field");
			e2.printStackTrace();
			return;
		}
		
		// If "already shared" is not explicitly false, force it true.
		// This way, any non-false value (e.g. blank) is treated as
		// not needing to be shared.  This prevents unintended
		// sharing of legacy content.
		if(!tfShared.equals("false")){tfShared = "true";}
		boolean alreadyShared = Boolean.parseBoolean(tfShared);


		if (!alreadyShared){
			
			//Get the list of folder paths for the item.  
			//It should be null, because the item has not been added to the first folder yet
			//If it is null we set it to be an empty list
			List<String> folderPaths = itemToShare.getFolderPaths();	
			if(folderPaths == null){
				folderPaths = new ArrayList<String>();
			}
			//Gets the path to share to based on the content type name, locale and item
			//The content type name and locale are used to lookup the base path in the bean
			//the item provides a date, which we use to append a year to the base path
			String destPath;
			try {
				destPath = getDestinationPath(typeName, locale, itemToShare);
			} catch (PSCmsException e1) {
				result.setSuccess();
				//result.setError("Failed to get destination path");
				e1.printStackTrace();
				return;
			}
			
			//Check to see if the item is already in the destination folder.
			//If it is, return out of the autoshare code
			if (folderPaths.contains(destPath)){
				result.setSuccess();
				return;
			}				
			
			//We have an item that will be shared, but we need to edit it so it does not get shared again
			try {
				statusList = contentWs.prepareForEdit(glist);
			} catch (PSErrorResultsException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

			
			// Mark item as previously shared.
			RxItemUtils.setFieldValue(itemToShare, "hasBeenAutoShared", "true");
			
			//add item to destination folder
			folderPaths.add(destPath);
			itemToShare.setFolderPaths(folderPaths);

			// Save updated item.
			try {
				contentWs.saveItems(items, false, false);
			} catch (PSErrorResultsException e2) {
				result.setSuccess();
				//result.setError("Failed to save item after edit");
				e2.printStackTrace();
				return;
			}
			try {
				contentWs.releaseFromEdit(statusList, false);
			} catch (PSErrorsException e2) {
				result.setSuccess();
				//result.setError("Failed to release item after edit");
				e2.printStackTrace();
				return;
			}
						
			result.setSuccess();
			return;
		}
	
		result.setSuccess();
		return;
	}

	
	private String getDestinationPath(String contentTypeName, String locale, PSCoreItem itemToShare) 
			throws PSCmsException{
		String basePath = getTargetPath(contentTypeName, locale);
		if(!basePath.endsWith("/")){
			basePath = basePath + "/";
		}
		Date pubDate = null;
		try {
			pubDate = RxItemUtils.getFieldDate(itemToShare, "date_first_published");
		} catch (PSCmsException e) {
			throw new PSCmsException(e);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		return basePath+sdf.format(pubDate);
		
	}

	@Override
	public void recover(Object[] arg0, IPSRequestContext arg1,
			IPSExecutionContext arg2, PSExtensionProcessingException arg3,
			PSEffectResult result) throws PSExtensionProcessingException {
		result.setSuccess();

	}

	@Override
	public void test(Object[] arg0, IPSRequestContext arg1,
			IPSExecutionContext arg2, PSEffectResult result)
			throws PSExtensionProcessingException, PSParameterMismatchException {
		result.setSuccess();

	}

	@Override
	public void init(IPSExtensionDef extensionDef, File arg1)
			throws PSExtensionException {
		CGV_AutoShare beanObj = (CGV_AutoShare) getBean("CGV_AutoShare");
		setAutoShareRules(beanObj.getAutoShareRules());
		
		if (valUtil == null) {
	    	valUtil = new CGV_FolderValidateUtils();
	    	valUtil.setExtensionDef(extensionDef);
	        if (valUtil.getContentManager() == null) valUtil.setContentManager(PSContentMgrLocator.getContentMgr());
	        if (valUtil.getContentWs() == null) valUtil.setContentWs(PSContentWsLocator.getContentWebservice());
	        if (valUtil.getGuidManager() == null) valUtil.setGuidManager(PSGuidManagerLocator.getGuidMgr());
	        if (valUtil.getNodeCataloger() == null) valUtil.setNodeCataloger(new PSONodeCataloger());
	        if (valUtil.getSystemWs() == null) valUtil.setSystemWs(PSSystemWsLocator.getSystemWebservice()); 
    	}
    	log.debug("CGV_AutoShare: end of init()");
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {	
	}


}