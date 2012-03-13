package gov.cancer.wcm.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.cancer.wcm.util.CGV_FolderValidateUtils;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.PSSystemWsLocator;


public class CGV_AutoShare implements IPSEffect{
	private CGV_FolderValidateUtils valUtil = null;
    private static final Log log = LogFactory.getLog(CGV_AutoShare.class);
    private HashMap<String, HashMap<String,String>> rulesMap = null;

	
	public boolean copyItemAsLink(IPSGuid itemGuid, String destinationFolderPath){
		IPSContentWs contentWs = valUtil.getContentWs();
		
		if (itemGuid == null){
			return false;
		}
		ArrayList<IPSGuid> gList = new ArrayList<IPSGuid>();
		gList.add(itemGuid);
		try {
			contentWs.addFolderChildren(destinationFolderPath, gList);
		} catch (PSErrorException e2) {
			e2.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void attempt(Object[] params, IPSRequestContext request,
			IPSExecutionContext context, PSEffectResult result)
			throws PSExtensionProcessingException, PSParameterMismatchException {
		/*if (context.isPreConstruction()) {
			PSOObjectFinder psoObjFinder = new PSOObjectFinder();
			PSLocationUtils locUtils = new PSLocationUtils();
			IPSContentWs contentWs = valUtil.getContentWs();
			PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(valUtil.getExtensionDef(), params, request, log);
			PSRelationship originating = context.getOriginatingRelationship(); 
			IPSGuid depGuid = valUtil.getGuidManager().makeGuid(originating.getDependent());
			IPSGuid ownerGuid = valUtil.getGuidManager().makeGuid(originating.getOwner());
			PSItemDefManager iDefMgr = PSItemDefManager.getInstance();
			IPSNode folder = null;
			String folderPath = "";
			
			Map<String,String> paramMap = h.getExtensionParameters();
	        long  cTypeId = Long.parseLong(paramMap.get("contentTypeId"));
	        String contentTypeName = "";

			try {
				contentTypeName = iDefMgr.contentTypeIdToName(cTypeId);
			} catch (PSInvalidContentTypeException e1) {
				result.setError("Invalid Content type id");
				e1.printStackTrace();
				return;
			}
			
			try {
				folder = psoObjFinder.getNodeByGuid(ownerGuid);
				if(folder != null){
					folderPath = locUtils.path(folder);
				}
			} catch (RepositoryException e) {
				result.setError("Unable to find source folder path");
				e.printStackTrace();
				return;
			}
			String destPath = "//Sites/MobileCancerGov/Testing";
			PSCoreItem item = null;

			boolean contentTypeMatch = contentTypeName.equals("cgvPressRelease");
			HashMap<String, String> rulesSet = rulesMap.get("cgvPressRelease");
			boolean inRightFolder = folderPath.contains(rulesSet.get("srcPath")) || folderPath.contains(rulesSet.get("srcPathES"));
			
			contentTypeMatch = contentTypeMatch && inRightFolder;

			if (!alreadyShared && contentTypeMatch ){
				ArrayList<IPSGuid> gList = new ArrayList<IPSGuid>();
				gList.add(depGuid);
				List<Node> nList = null;
				try {
					nList = valUtil.getContentManager().findItemsByGUID(gList, null);
				} catch (RepositoryException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Node n = nList.get(0);
				//get dest path

	
				request.setPrivateObject("alreadyShared", "true");
				
				try {
					contentWs.addFolderChildren(destPath, gList);
				} catch (PSErrorException e) {
					e.printStackTrace();
				}
				result.setSuccess();
				return;
			}
			result.setSuccess();
			return;
		}*/
		result.setSuccess();
		return;
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
		if (rulesMap == null){
			rulesMap = new HashMap<String, HashMap<String,String>>();
			HashMap<String,String> prRules = new HashMap<String,String>();
			prRules.put("srcPath", "//Sites/CancerGov/newscenter/pressreleases/");
			prRules.put("tgtPath", "//Sites/MobileCancerGov/news/pressreleases/");
			prRules.put("srcPathES", "//Sites/CancerGov/espanol/noticias/");
			prRules.put("tgtPathES", "//Sites/MobileCancerGov/es/noticias/comunicadosdeprensa/");
			rulesMap.put("cgvPressRelease", prRules);
		}
		
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


}