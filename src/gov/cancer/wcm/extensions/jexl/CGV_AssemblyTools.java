package gov.cancer.wcm.extensions.jexl;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import com.percussion.pso.jexl.PSOFolderTools;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.finder.PSOReverseSlotContentFinder;
import com.percussion.pso.jexl.PSONavTools;
import com.percussion.pso.jexl.PSORelationshipTools;
import com.percussion.pso.jexl.PSOQueryTools;
import com.percussion.pso.jexl.PSOSlotTools;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.pso.utils.PSOSlotContents;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.pso.utils.PSOSlotRelations;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;


public class CGV_AssemblyTools extends PSJexlUtilBase implements IPSJexlExpression{

	private static Log log = LogFactory.getLog(CGV_AssemblyTools.class);
	private static IPSContentMgr cmgr = null;

	public CGV_AssemblyTools() {
		super();
	}

	@IPSJexlMethod(description = "gets the parent path given a path", params = {
			@IPSJexlParam(name = "path", description = "the path") })
			public String getParentFromPath(String path)
	throws RepositoryException 
	{
		Validate.notNull(path, "The path parameter cannot be null");		
		Validate.notEmpty(path, "The path parameter cannot be empty");
		Validate.isTrue(path.contains("/"), "The path parameter does not contain a / and therefore is an invalid path.");
		Validate.isTrue((!path.equals("/") && !path.equals("//")) , "Since the path is the root (/), there is no parent");

		if (path.endsWith("/"))
			path = path.substring(0,path.length()-2);

		if (path.lastIndexOf("/") == 0)
			path = "/";
		else
			path = path.substring(0, path.lastIndexOf("/"));

		return path;
	}

	@IPSJexlMethod(description = "Returns the number of pages for a dynamic auto slot", params = {
			@IPSJexlParam(name = "path", description = "The entire JSR query") })
			public int pagerCount(String path)
	throws RepositoryException 
	{
		if(cmgr == null)
		{
			cmgr = PSContentMgrLocator.getContentMgr();
		}

		//Query q = cmgr.createQuery(path, Query.SQL);
		PSOQueryTools pso = new PSOQueryTools();
		//QueryResult r =  cmgr.executeQuery(q, -1, null, null);
		//List<Map<String, Value>> eq = new ArrayList<Map<String, Value>>();
		return pso.executeQuery(path, -1, null, null).size();
	}

	@IPSJexlMethod(description = "Returns the content id that cooresponds to a piece of content's translated copy.", params = {
			@IPSJexlParam(name = "item", description = "The IPSAssemblyItem to find the translation for.") })
			public int translationCID(IPSAssemblyItem item)
	throws RepositoryException 
	{
		PSOSlotTools pso = new PSOSlotTools();
		Map<String,Object> map = new HashMap<String,Object>();
		List<IPSAssemblyItem> li = null;
		try {
			li = pso.getSlotContents(item, "cgvTranslationFinder", map);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(li != null){
			if(!li.isEmpty()){
				return li.get(0).getId().getUUID();
			}
		}
		return 0;
	}

	@IPSJexlMethod(description = "Returns the assembly item that cooresponds to a piece of content's translated copy.", params = {
			@IPSJexlParam(name = "item", description = "The IPSAssemblyItem to find the translation for.") })
			public IPSAssemblyItem translationItem(IPSAssemblyItem item)
	throws RepositoryException 
	{
		PSOSlotTools pso = new PSOSlotTools();
		Map<String,Object> map = new HashMap<String,Object>();
		List<IPSAssemblyItem> li = null;
		try {
			li = pso.getSlotContents(item, "cgvTranslationFinder", map);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(li != null){
			if(!li.isEmpty()){
				return li.get(0);
			}
		}
		return null;
	}
	
	@IPSJexlMethod(description = "Finds the linking url for a microsite index.", params = {
			@IPSJexlParam(name = "item", description = "The IPSAssemblyItem to find the microsite index link for.") })
			public IPSAssemblyItem getMicrositeIndexURL(IPSAssemblyItem item)
	throws RepositoryException 
	{
		PSONavTools nav = new PSONavTools();
		PSOSlotTools pso = new PSOSlotTools();
		IPSContentWs cmgr = null;
		try {
			cmgr = PSContentWsLocator.getContentWebservice();
		} catch (PSMissingBeanConfigurationException e) {
			System.out.println("PC DEBUG: MISSING BEAN!!!");
			e.printStackTrace();
		}


		//1. What is the current navon of, item.
		IPSGuid itemGuid = item.getId();
		String path = "";
		try {
			path = cmgr.findFolderPaths(itemGuid)[0];
		} catch (PSErrorException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<IPSGuid> folderGuidList = null;
		if(path.length() != 0 ){
			try {
				folderGuidList = cmgr.findPathIds(path);
			} catch (PSErrorException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		//Drop 1st in List of paths (site folder)
		if(folderGuidList != null){
			folderGuidList.remove(0);
		}

		IPSGuid folderID = folderGuidList.get(folderGuidList.size()-1);
		IPSNode node = null;

		if(folderID != null)
		{
			try {
				node = nav.findNavNodeForFolder(String.valueOf(folderID.getUUID()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		//2. What is the 1st submenu item (navon) of step 1 (node).
		IPSAssemblyItem clone = (IPSAssemblyItem) item.clone();
		clone.setNode(node);
		
		if(node != null){
			Map<String,Object> map = new HashMap<String,Object>();
			List<IPSAssemblyItem> li = null;
			try {
				li = pso.getSlotContents(clone, "rffNavSubmenu", map);

				if(li != null && li.size() != 0){
					//3. What is the URL of 2.'s landing page.
					List<IPSAssemblyItem> li2 = pso.getSlotContents(li.get(0), "rffNavLandingPage", map);
					if(li2 != null && li2.size() != 0 ){
						return li2.get(0);
					}
				}
			}
			catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Something broke, return null.
		return null;
	}
	
	
	@IPSJexlMethod(description = "Generates the guid of the translation of the guid passed in", params = {
			@IPSJexlParam(name = "parentGuid", description = "The Guid of the parent item.")})
			public IPSGuid getParentTranslationGUID(IPSGuid ownerGuid)
	throws RepositoryException {
        IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
        PSOObjectFinder objFinder = new PSOObjectFinder();
        PSOSlotContents slotContents = new PSOSlotContents();
        PSOReverseSlotContentFinder revSlFinder = new PSOReverseSlotContentFinder();


		
        //IPSGuid ownerGuid = objFinder.getGuidById(parentCID);
		String session = objFinder.getPSSessionId();
		String slotName = "cgvTranslationFinder";
		IPSTemplateSlot slot = null;
		List<PSAaRelationship> aaRels = null;
		String transCID = null;
		
		try{
			slot = asm.findSlotByName(slotName);
		}
		catch(Throwable e){
			System.out.println("Slot not found: Slot name invalid");
		}
		
		//Get all of the relationships between an owner and all dependents in a given slot
		if(slot != null){
			try{
				 aaRels = PSOSlotRelations.getSlotRelations(ownerGuid, slot, session);
			}
			catch(Throwable e){
				System.out.println("List of Relationships not created");
			}
		}

		if (aaRels != null){
			if (aaRels.size()>0){
				transCID = aaRels.get(0).getDependent().getPart("contentid");
				return objFinder.getGuidById(transCID);
			}
		}

		System.out.println("Returning NULL child name");
		return null;
	}

	
	@IPSJexlMethod(description = "Generates the page/slide number to link directly to a given page", params = {
			@IPSJexlParam(name = "ownerGuid", description = "The IPSAssemblyItem of the parent item."),
			@IPSJexlParam(name = "childItem", description = "The IPSAssemblyItem of the child item to generate the url for."),
			@IPSJexlParam(name = "slotName", description = "The slot that the child item is a part of in the parent."),
			@IPSJexlParam(name = "childNameFragment", description = "the prefix to the number that this method returns (i.e. page in 'page2'.")})
			public String generateChildName(IPSGuid ownerGuid, int childContentId, String slotName, String childNameFragment)
	throws RepositoryException {
		
		//Tools
        IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
        PSOObjectFinder objFinder = new PSOObjectFinder();

        //Variables
		int pagenum = 0;
		//IPSGuid ownerGuid = parentItem.getId();
		IPSTemplateSlot slot = null;
		String session = objFinder.getPSSessionId();
		List<PSAaRelationship> aaRels = null;
		String relContentId = null;
		//String childContentId = null;
		
		//Get the slot from the slot name
		try{
			slot = asm.findSlotByName(slotName);
		}
		catch(Throwable e){
			System.out.println("Slot not found: Slot name invalid");
		}
		
		//Get all of the relationships between an owner and all dependents in a given slot
		if(slot != null){
			try{
				 aaRels = PSOSlotRelations.getSlotRelations(ownerGuid, slot, session);
			}
			catch(Throwable e){
				System.out.println("List of Relationships not created");
			}
		}


		//Go through all relationships and compare their dependent to the child item
		if (aaRels != null){
			if (aaRels.size()>0){
				for (PSAaRelationship rel : aaRels){
					pagenum = rel.getSortRank() + 1;
					relContentId = rel.getDependent().getPart("contentid");
					//childContentId = childItem.getNode().getProperty("rx:sys_contentid").getValue().getString();
					if(relContentId.equals(Integer.toString(childContentId))){
						return "/" + childNameFragment + pagenum;
					}
				}
			}
		}

		System.out.println("Returning NULL child name");
		return null;
	}
	
	
	
	@IPSJexlMethod(description = "Finds the parent item for a child.", params = {
			@IPSJexlParam(name = "item", description = "The IPSAssemblyItem to find the booklet page link for."),
			@IPSJexlParam(name = "parentFinderSlot", description = "The slot created to find the parent of a certain content type")})
			public IPSAssemblyItem getBookletParent(IPSAssemblyItem item, String parentFinderSlot)
	throws RepositoryException {

		//Tools
		PSOSlotTools slotTools = new PSOSlotTools();

		//Variables
		List<IPSAssemblyItem> parentBooklet = null;
		Map<String,Object> emParams = new HashMap<String,Object>();

		//Get the parent of the assembly item using a parentFinder slot you have created
		try{
			parentBooklet = slotTools.getSlotContents(item, parentFinderSlot, emParams);
		}
		catch(Throwable e){
			e.printStackTrace();
		}

		//Slot tools returns a list so get the first item.
		if (parentBooklet != null){
			if (parentBooklet.size()>0){
				return parentBooklet.get(0);
			}
		}
		System.out.println("Returning NULL booklet parent");

		return null;
	}
	@IPSJexlMethod(description = "Returns the default template name", params = {
			@IPSJexlParam(name = "itemPath", description = "A percussion path to the item (i.e. //Sites/CancerGov/...) ")})

	public int getSiteIdFromPath(String path){

		String[] pathParts = path.split("/");
		String siteName = pathParts[3];
		IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
		IPSSite aSite = siteManager.loadSite(siteName);
		return aSite.getGUID().getUUID();

	}
	

	@IPSJexlMethod(description = "Returns the default template name", params = {
			@IPSJexlParam(name = "itemPath", description = "A percussion path to the item (i.e. //Sites/CancerGov/...) "),
			@IPSJexlParam(name = "itemNode", description = "The Node of the item to find the default template for"),
			@IPSJexlParam(name = "siteGuid", description = "The GUID of the site to find the default template for")})
			public String NCIFindDefaultTemplate(String itemPath, IPSNode itemNode, String siteName){
				IPSAssemblyService aService = PSAssemblyServiceLocator.getAssemblyService();
				IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
				IPSAssemblyItem aItem = aService.createAssemblyItem();
				PSLocationUtils locationUtils = new PSLocationUtils();	
				
				
				IPSSite aSite = siteManager.loadSite(siteName);
				IPSGuid siteID = aSite.getGUID();
				IPSAssemblyTemplate aTemplate = null;
				
				aItem.setNode(itemNode);
				aItem.setPath(itemPath);
				aItem.setSiteId(siteID);
				try {
					aItem.normalize();
				} catch (PSAssemblyException e) {
					System.out.println("normailzation failed");
					e.printStackTrace();
				}
				
				try {
					aTemplate = locationUtils.findDefaultTemplate(aItem);
				} catch (PSAssemblyException e) {
					System.out.println("Find default Template failed");
					e.printStackTrace();
				}
				
				return aTemplate.getName();			
			}
	

	@IPSJexlMethod(description = "Returns the path if the content item is on the given site, else an empty string", params = {
			@IPSJexlParam(name = "itemPath", description = "A percussion path to the item with the sys_title (i.e. //Sites/CancerGov/...) "),
			@IPSJexlParam(name = "siteName", description = "The name of the site in the path to find the item on (i.e. CancerGov, CCOP, etc.)")})
			public String isOnSite(String itemPath, String siteName){
				String[] pathsList = null;
				IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
				IPSGuid pathGuid = null;
				
				//Takes the items path and finds the id for it
				try {
					pathGuid = contentWS.getIdByPath(itemPath);
				} catch (PSErrorException e1) {
					e1.printStackTrace();
				}
				
				//Uses the id to find all paths where the content resides
				try {
					pathsList = contentWS.findFolderPaths(pathGuid);
				} catch (PSErrorException e) {
					e.printStackTrace();
				}
			
				//as long as there are paths in the list, check each to see if the content is in the siteName path.
				if(pathsList != null){
					for( String path : pathsList){
						String[] pathParts = path.split("/");
						if (pathParts[3].equals(siteName)){
							return path;
						}
					}
				}
								
			return "";
		
		}
	
	@IPSJexlMethod(description = "finds a pages alternate site path", params = {
			@IPSJexlParam(name = "content_id", description = "the content id of the item to find all paths for (String)"),
			@IPSJexlParam(name = "siteName", description = "The name of the site in the path (i.e. CancerGov, CCOP, etc.)") })
			public String findSitePaths(String content_id, String siteName)
	throws RepositoryException {
		IPSGuid itemGUID = PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(Integer.parseInt(content_id)));
		List<String> paths = new ArrayList<String>();
		try{
			paths = Arrays.asList(PSContentWsLocator.getContentWebservice().findFolderPaths(itemGUID));
		}
		catch (PSErrorException e) {e.printStackTrace();}
		for(String path : paths){
			String[] pathParts = path.split("/");
			if (pathParts[3].equals(siteName)){
				//return the path after the //Sites/SITENAME...
				return path.substring(8+siteName.length(), path.length());
			}
		}
		
		return "";
					
	}

}
