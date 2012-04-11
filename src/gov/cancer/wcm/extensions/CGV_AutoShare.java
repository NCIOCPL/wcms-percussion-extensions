package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import java.io.File;
import java.text.SimpleDateFormat;
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
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.pso.utils.RxItemUtils;
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
		if (locale.equals("es")){
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
		if (context.isPreConstruction()) {
			IPSContentWs contentWs = valUtil.getContentWs();
			PSRelationship originating = context.getOriginatingRelationship(); 
			IPSGuid depGuid = valUtil.getGuidManager().makeGuid(originating.getDependent());
			List<PSItemStatus> statusList = null;
			List<PSCoreItem> items = null;
			PSCoreItem itemToShare = null;
			PSComponentSummary depSum = null;
			try {
				 depSum = PSOItemSummaryFinder.getSummary(depGuid);
			} catch (PSException e4) {
				result.setError("Cannot get item summary, may be folder");
				e4.printStackTrace();
				return;
			}
			if(depSum.isFolder()){
				result.setSuccess();
				return;
			}
		
			List<IPSGuid> glist = Collections.<IPSGuid>singletonList(depGuid);
			try {
				statusList = contentWs.prepareForEdit(glist);
				items = contentWs.loadItems(glist, true, false, false, false);

			} catch (PSErrorResultsException e3) {
				result.setError("Cannot load item to share");
				e3.printStackTrace();
				return;

			}
			if (items != null && items.size()>0){
				itemToShare = items.get(0);
			}
			else{
				result.setError("Cannot load item to share");
				return;
			}
			String typeName = "";
			if( itemToShare.getItemDefinition() != null) {
			  // The "friendly name" is retrieved with getLabel().
			  typeName = itemToShare.getItemDefinition().getName();
			}
			boolean isInBean = false;
			for(String cTypeKey : autoShareRules.keySet()){
				if (cTypeKey.equals(typeName)){
					isInBean = true;
				}
			}
			if(!isInBean){
				result.setSuccess();
				return;
			}
			String tfShared = "true";
			String locale = "";
			try {
				tfShared = RxItemUtils.getFieldValue(itemToShare, "hasBeenAutoShared");
				locale = RxItemUtils.getFieldValue(itemToShare, "sys_lang");
			} catch (PSCmsException e2) {
				result.setError("Cannot parse hasBeenAutoShared field");
				e2.printStackTrace();
				return;
			}
			if(tfShared == null){tfShared = "true";}
			boolean alreadyShared = Boolean.parseBoolean(tfShared);


			if (!alreadyShared){
				RxItemUtils.setFieldValue(itemToShare, "hasBeenAutoShared", "true");
				String destPath;
				try {
					destPath = getDestinationPath(typeName, locale, itemToShare);
				} catch (PSCmsException e1) {
					result.setError("Failed to get date for destination path");
					e1.printStackTrace();
					return;
				}

				try {
					contentWs.saveItems(items, false, false);
				} catch (PSErrorResultsException e2) {
					result.setError("Failed to save item after edit");
					e2.printStackTrace();
					return;
				}
				try {
					contentWs.releaseFromEdit(statusList, false);
				} catch (PSErrorsException e2) {
					result.setError("Failed to release item after edit");
					e2.printStackTrace();
					return;
				}
				
				String[] folders = new String[1];
				folders[0] = destPath;
				try {
					List<PSFolder> folderList = contentWs.loadFolders(folders);
				} catch (PSErrorResultsException e1) {
					result.setSuccess();
					return;
				}
				

				try {
					contentWs.addFolderChildren(destPath, glist);
				} catch (PSErrorException e) {
					result.setError("Failed to share content to secondary folder");
					e.printStackTrace();
					return;
				}
				
				result.setSuccess();
				return;
			}
			else{
				try {
					contentWs.saveItems(items, false, false);
				} catch (PSErrorResultsException e2) {
					result.setError("Failed to save item without edit");
					e2.printStackTrace();
					return;
				}
				try {
					contentWs.releaseFromEdit(statusList, false);
				} catch (PSErrorsException e2) {
					result.setError("Failed to release item without edit");
					e2.printStackTrace();
					return;
				}
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