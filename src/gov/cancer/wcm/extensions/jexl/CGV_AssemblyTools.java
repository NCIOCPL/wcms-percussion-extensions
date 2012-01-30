package gov.cancer.wcm.extensions.jexl;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;
import com.percussion.pso.jexl.PSOFolderTools;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSContentTypeSet;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
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
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.pso.utils.PSOSlotContents;
import com.percussion.security.PSSecurityToken;
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
import com.percussion.services.contentmgr.IPSContentTypeMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.pso.utils.PSOSlotRelations;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;


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
	
	
	@IPSJexlMethod(description = "Returns the Site ID", params = {
			@IPSJexlParam(name = "itemPath", description = "A percussion path to the item (i.e. //Sites/CancerGov/...) ")})

			public int getSiteIdFromPath(String path){
			System.out.println("the path is: " + path );
			String[] pathParts = path.split("/");
			String siteName = pathParts[3];
			IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
			IPSSite aSite = siteManager.loadSite(siteName);
			System.out.println("The site is: "+ siteName);
			System.out.println("The site guid is: "+ aSite.getGUID());
			System.out.println("The site UUID is: "+ aSite.getGUID().getUUID());
			return aSite.getGUID().getUUID();

		}
		
	@IPSJexlMethod(description = "Returns the default template name", params =
			@IPSJexlParam(name = "sitePath", description = "The path of the site to find the default template for"))
			public String getSiteUrlfromPath(String sitePath){
				IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
				String[] pathParts = sitePath.split("/");
				IPSSite aSite = siteManager.loadSite(pathParts[3]);
				return aSite.getBaseUrl();
			}
			
			
	@IPSJexlMethod(description = "Returns the default template name", params = {
			@IPSJexlParam(name = "itemPath", description = "A percussion path to the item (i.e. //Sites/CancerGov/...) "),
			@IPSJexlParam(name = "itemNode", description = "The Node of the item to find the default template for"),
			@IPSJexlParam(name = "sitePath", description = "The path of the site to find the default template for")})
			public String NCIFindDefaultTemplate(String itemPath, IPSNode itemNode, String sitePath){
				IPSAssemblyService aService = PSAssemblyServiceLocator.getAssemblyService();
				IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
				IPSAssemblyItem aItem = aService.createAssemblyItem();
				PSLocationUtils locationUtils = new PSLocationUtils();	
				
				String[] pathParts = sitePath.split("/");

				IPSSite aSite = siteManager.loadSite(pathParts[3]);
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
	
			private IPSGuid getItemId(String folderPath, String itemTitle){
				IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
				IPSGuid pathGuid = null;
				List<PSItemSummary> folderChildren = null;

				
				try {
					pathGuid = contentWS.getIdByPath(folderPath);
				} catch (PSErrorException e) {
					e.printStackTrace();
				}
				if(pathGuid == null){
					int lastIdx = folderPath.lastIndexOf("/");
					if(lastIdx == -1){
				}
					else{
						String newFP = folderPath.substring(0,lastIdx);
						String newIT = folderPath.substring(lastIdx) + itemTitle;
						IPSGuid folderGuid = getItemId(newFP, newIT);
						return folderGuid;
						//have path to folder
						
						}


				}
				else if(itemTitle.equals("") && pathGuid!=null){
					return pathGuid;
				}
				else{
					try {
						folderChildren = contentWS.findFolderChildren(pathGuid, true);
					} catch (PSErrorException e) {
						e.printStackTrace();
					}
					for(PSItemSummary itemSummary : folderChildren){
						
						ArrayList<IPSGuid> guidList = new ArrayList<IPSGuid>();
						guidList.add(itemSummary.getGUID());
						List<PSCoreItem> itemList = null;
						try {
							itemList = contentWS.loadItems(guidList, false, false, false, false);
						} catch (PSErrorResultsException e) {
							e.printStackTrace();
						}
						
						PSItemField sysTitle = itemList.get(0).getFieldByName("sys_title");
						IPSFieldValue sysTitleVal = sysTitle.getValue();
						
						String sysTitleString = "";
						if(sysTitleVal != null){
							try {
								sysTitleString = sysTitleVal.getValueAsString();
							} catch (PSCmsException e) {
								e.printStackTrace();
							}
						}
						
						if(sysTitleString.equals(itemTitle.substring(1))){
							pathGuid = itemSummary.getGUID();
							return pathGuid;
						}

				}
				}
				
				return pathGuid;
				
			}
	

	@IPSJexlMethod(description = "Returns the path if the content item is on the given site, else an empty string", params = {
			@IPSJexlParam(name = "itemPath", description = "A percussion path to the item with the sys_title (i.e. //Sites/CancerGov/...) "),
			@IPSJexlParam(name = "sitePath", description = "A percussion path to the site (i.e. //Sites/CancerGov/...) ")})
			public String isOnSite(String itemPath, String sitePath){
				String[] pathsList = null;
				IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
				IPSGuid pathGuid = null;
				/*if(itemPath.length()>= sitePath.length()){
					if(itemPath.substring(0, sitePath.length()).equals(sitePath)){
						return itemPath;
					}
				}*/
				
				String itemName = "";
				String returnPath = "";
				//Takes the items path and finds the id for it
				pathGuid = getItemId(itemPath, "");

				
				//System.out.println("DEBUG isOnSite: pathGuid: " + pathGuid);

				
				//Uses the id to find all paths where the content resides
				try {
					pathsList = contentWS.findFolderPaths(pathGuid);
				} catch (PSErrorException e) {
					e.printStackTrace();
				}
				
	
			
				//as long as there are paths in the list, check each to see if the content is in the siteName path.
				if(pathsList != null){
					for( String path : pathsList){
						//System.out.println(path);
						if (path.length() >= sitePath.length()){
							//System.out.println(path);
							String subPath = path.substring(0,(sitePath.length()));
							//System.out.println("DEBUG isOnSite: subPath: " +subPath);
							//System.out.println("DEBUG isOnSite: sitePath: " +sitePath);
							if (subPath.equals(sitePath)){
								returnPath =  path;
							}
							if (path.length() <= itemPath.length()){
								if (itemPath.substring(0, path.length()).equals(path)){
									itemName = itemPath.substring(path.length());
								}
							}
						}
					}
				}
				
				if (returnPath.equals("") || itemName.equals("")){
					return "";
				}
				else{
					return returnPath+itemName;
				}
	}
	
	@IPSJexlMethod(description = "Returns a path to the folder that contains the item path passed in   ", params = {
			@IPSJexlParam(name = "itemPath", description = "Path to the item in //Sites/<SITENAME>/folders .../ItemName form.")})
			public String getFolderPathFromItemPath(String itemPath){
				String[] pathsList = null;
				IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
				IPSGuid pathGuid = null;

				//Takes the items path and finds the id for it
				pathGuid = getItemId(itemPath, "");
		
				//Uses the id to find all paths where the content resides
				try {
					pathsList = contentWS.findFolderPaths(pathGuid);
				} catch (PSErrorException e) {
					e.printStackTrace();
				}
				if(pathsList != null){
					for( String path : pathsList){
						if (path.length() <= itemPath.length()){
							if(itemPath.substring(0, path.length()).equals(path)){
								return path;
							}
						}
					}
				}
				return "";
			}
	
	@IPSJexlMethod(description = "Returns a path to the site that contains the item path passed in   ", params = {
			@IPSJexlParam(name = "itemPath", description = "Path to the item in //Sites/<SITENAME>/folders .../ItemName form.")})
			public String getSitePathFromItemPath(String itemPath){
				String sitePath = "";
				String[] itemParts = itemPath.split("/");
				for (int i=0; i<4; i++){
					sitePath = sitePath + itemParts[i] + "/";
				}
					
				return sitePath;
	}

	@IPSJexlMethod(description = "Returns a context variable for a given Site, using a specified ID.  " +
			"Null if the property isn't defined.", params = {
			@IPSJexlParam(name = "sitePath", description = "Path to the site in //Sites/<SITENAME> form."),
			@IPSJexlParam(name = "propertyName", description = "The property to find from the specified site."),
			@IPSJexlParam(name = "contextName", description = "The contextName to use when finding the property.")})
			public String getContextVariable(String itemPath, String propertyName, String contextName){
		String[] pathParts = itemPath.split("/");
		String siteName = pathParts[3];
		IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
		
		IPSSite siteItem;
		try {
			siteItem = siteManager.loadSite(siteName);
		} catch (PSNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		IPSPublishingContext context;
		try {
			context = siteManager.loadContext(contextName);
		} catch (PSNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return siteItem.getProperty(propertyName, context);
	}
	
	@IPSJexlMethod(description = "Returns a context variable for a given Site, using a specified ID.  " +
			"Null if the property isn't defined.", params = {
			@IPSJexlParam(name = "itemNode", description = "The node for the assembly item to get"),
			@IPSJexlParam(name = "itemPath", description = "path to an instance of the item"),
			@IPSJexlParam(name = "sitePath", description = "Path to the site in //Sites/<SITENAME> form.")})
			public IPSAssemblyItem getAssemblyItem(IPSNode itemNode, String itemPath, String sitePath){
		
				IPSAssemblyService aService = PSAssemblyServiceLocator.getAssemblyService();
				IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
				IPSAssemblyItem aItem = aService.createAssemblyItem();
				IPSFilterService fs = PSFilterServiceLocator.getFilterService();
				
				
		
				String[] pathParts = sitePath.split("/");

				IPSSite aSite = siteManager.loadSite(pathParts[3]);
				IPSGuid siteID = aSite.getGUID();
				IPSItemFilter itemFilter = fs.createFilter("phFilter", "this is a placeholder filter");
				String templateName = NCIFindDefaultTemplate(itemPath, itemNode, sitePath);
				
				aItem.setNode(itemNode);
				aItem.setPath(itemPath);
				aItem.setSiteId(siteID);
				aItem.setFilter(itemFilter);
				try {
					aItem.setTemplate(aService.findTemplateByName(templateName));
				} catch (PSAssemblyException e1) {
					e1.printStackTrace();
				}
				
				
				try {
					aItem.normalize();
				} catch (PSAssemblyException e) {
					System.out.println("normalization failed");
					e.printStackTrace();
				}
			
				return aItem;
	}
	@IPSJexlMethod(description = "returns a map of datafield names mapped to a map of other information (fieldLabel, fieldControllerRef ", params = 
			@IPSJexlParam(name = "contentTypeName", description = "the name of the content type to return the fields for"))
			public HashMap<String, HashMap<String, String>> getDataFieldNames(String contentTypeName){
		
				HashMap<String, HashMap<String, String>> retMap = new HashMap<String, HashMap<String, String>>();
				String fieldLabel = "";
				PSItemDefinition ctypedef = null;
				List<String> fieldNamesrx = new ArrayList<String>();
				List<String> fieldNames = new ArrayList<String>();
				PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
				PSONodeCataloger nodeCat = new PSONodeCataloger();
		        PSOObjectFinder objFinder = new PSOObjectFinder();
				String session = objFinder.getPSSessionId();
				PSSecurityToken secToken = new PSSecurityToken(session);

				

				try {
					 fieldNamesrx = nodeCat.getFieldNamesForContentType(contentTypeName);
				} catch (NoSuchNodeTypeException e) {
					e.printStackTrace();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				
				for(String name : fieldNamesrx){
					if(name!=null && name.substring(0, 3).equals("rx:")){
						fieldNames.add(name.substring(3));//cuts off rx: from the front of the field name
					}
				}

				long ctypeid = -1;
				try {
					ctypeid = itemDefMgr.contentTypeNameToId(contentTypeName);
				} catch (PSInvalidContentTypeException e1) {
					e1.printStackTrace();
				}
				
				try {
					ctypedef = itemDefMgr.getItemDef(contentTypeName, secToken);
				} catch (PSInvalidContentTypeException e) {
					e.printStackTrace();
				}
				
				
				for(String fieldName : fieldNames){
					if(ctypedef!=null){
						PSField fld = ctypedef.getFieldByName(fieldName);
						if(fld==null){
							continue;
						}
						boolean systemField = fld.isSystemField();
						if(!systemField){
							PSContentEditor cedit = ctypedef.getContentEditor();
							PSControlRef fieldType = cedit.getFieldControl(fieldName);
							String ftName = fieldType.getName();
							if(!fieldType.equals("sys_webImageFX") && !fieldType.equals("sys_File") && !fieldType.equals("sys_HiddenInput") && !fieldType.equals("sys_Table")){
								HashMap<String, String> fieldMap = new HashMap<String, String>();

								fieldLabel = "";
								fieldLabel = itemDefMgr.getFieldLabel(ctypeid, fieldName);
								fieldMap.put("fieldLabel", fieldLabel);
								fieldMap.put("fieldControlRef", ftName);
								retMap.put(fieldName, fieldMap);
							}
						
						}
					}
				}


				return retMap;

				
		
	}
	
	@IPSJexlMethod(description = "Returns a context variable for a given Site, using a specified ID.  " +
			"Null if the property isn't defined.", params = {
			@IPSJexlParam(name = "folderCID", description = "the content id of the folder to start the report on"),
			@IPSJexlParam(name = "includeSubfolders", description = "boolean whether to include subfolders in the report")})
			public ArrayList<HashMap<String, String>> percReport_customLink(String folderCID, String subfolders){
	
				boolean includeSubfolders = Boolean.parseBoolean(subfolders);

				IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
				PSOSlotTools psoSlotTools = new PSOSlotTools();
				PSOObjectFinder psoObjFinder = new PSOObjectFinder();
				PSLocationUtils locUtils = new PSLocationUtils();
				List<PSItemSummary> folderChildren = null;
				ArrayList<HashMap<String, String>> infoList = new ArrayList<HashMap<String, String>>();
				String itemPath= "";
				Map<String,Object> map = new HashMap<String,Object>();
				IPSAssemblyItem targetItem = null;
				IPSGuid folderGuid = null;
				IPSNode customLinkNode = null;


				
				folderGuid = psoObjFinder.getGuidById(folderCID);
				
				try {
					folderChildren = contentWS.findFolderChildren(folderGuid, true);
				} catch (PSErrorException e) {
					e.printStackTrace();
				}
			
				for(PSItemSummary itemSummary : folderChildren){
					
					if(itemSummary.getContentTypeName().equals("nciGeneral")){
						HashMap<String, String> customLinkInfo = new HashMap<String, String>();
						List<IPSAssemblyItem> li = new ArrayList<IPSAssemblyItem>();

						
						customLinkInfo.put("cl_cid", Integer.toString(itemSummary.getGUID().getUUID())); //adds content id
						
					//get custom link assembly item
						try {
							customLinkNode = psoObjFinder.getNodeByGuid(itemSummary.getGUID());
						} catch (RepositoryException e2) {
							e2.printStackTrace();
						}
						
						try {
							itemPath = contentWS.findFolderPaths(itemSummary.getGUID())[0] +"/"+ itemSummary.getName();
						} catch (PSErrorException e) {
							e.printStackTrace();
						}




						
						
						IPSAssemblyItem customLinkAI = getAssemblyItem(customLinkNode, itemPath, getSitePathFromItemPath(itemPath));
						
					
					//Get custom Link fields
						/*
						ArrayList<IPSGuid> clGuidList = new ArrayList<IPSGuid>();
						clGuidList.add(itemSummary.getGUID());
						List<PSCoreItem> itemList = null;
						try {
							itemList = contentWS.loadItems(clGuidList, false, false, false, false);
						} catch (PSErrorResultsException e) {
							e.printStackTrace();
						}
						customLinkAI.setDeliveryContext(304);
						
						PSItemField clsysTitle = itemList.get(0).getFieldByName("sys_title");
						PSItemField cloverrideTitle = itemList.get(0).getFieldByName("override_title");
						PSItemField cloverrideShortTitle = itemList.get(0).getFieldByName("override_short_title");
						PSItemField cloverrideLongDescription = itemList.get(0).getFieldByName("override_long_description");
						PSItemField cloverrideShortDescription = itemList.get(0).getFieldByName("override_short_description");
						String clsysTitleString = "";
						String cloverrideTitleString = "";
						String cloverrideShortTitleString = "";
						String cloverrideLongDescriptionString = "";
						String cloverrideShortDescriptionString = "";
						
						try {
							clsysTitleString = clsysTitle.getValue().getValueAsString();
							cloverrideTitleString = cloverrideTitle.getValue().getValueAsString();
							cloverrideShortTitleString = cloverrideShortTitle.getValue().getValueAsString();
							cloverrideLongDescriptionString = cloverrideLongDescription.getValue().getValueAsString();
							cloverrideShortDescriptionString = cloverrideShortDescription.getValue().getValueAsString();
						} catch (PSCmsException e) {
							e.printStackTrace();
						}
						
						customLinkInfo.put("cl_sys_title", clsysTitleString);
						customLinkInfo.put("cl_override_long_title", cloverrideTitleString);
						customLinkInfo.put("cl_override_short_title", cloverrideShortTitleString);
						customLinkInfo.put("cl_override_long_desc", cloverrideLongDescriptionString);
						customLinkInfo.put("cl_override_short_desc", cloverrideShortDescriptionString);
						
					*/	
					//get Slot items to get target
						try {
							li = psoSlotTools.getSlotContents(customLinkAI, "cgvBody", map);
						} catch (Throwable e) {
							li = null; 
							e.printStackTrace();
						}

					
					 //get target info
						String tgtsysTitleString = "";
						String tgtTitleString = "";
						String tgtShortTitleString = "";
						String tgtLongDescriptionString = "";
						String tgtShortDescriptionString = "";
						String tgtPath = "";
						
						/*if(li.size() > 0){
							targetItem = li.get(0);*/
						IPSSystemWs systemWebService = PSSystemWsLocator.getSystemWebservice();

						List<PSRelationship> rels = new ArrayList<PSRelationship>();
						PSRelationshipFilter filter = new PSRelationshipFilter();
						//This is going to be the current/edit revision for this content item.
						filter.setOwner(new PSLocator(itemSummary.getGUID().getUUID()));
						filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
							
						try {
							rels = systemWebService.loadRelationships(filter);
						} catch (PSErrorException e1) {
							e1.printStackTrace();
						}
							
						IPSGuid depGuid = null;
						
						for(PSRelationship rel : rels){
							PSLocator dep =  rel.getDependent();
							if(rel.getProperty(IPSHtmlParameters.SYS_SLOTID).equals("525")){		
								depGuid = psoObjFinder.getGuidById(Integer.toString(dep.getId()));
							}
						}
						//get target fields
							ArrayList<IPSGuid> targetGuidList = new ArrayList<IPSGuid>();
							targetGuidList.add(depGuid);
							List<PSCoreItem> targetList = null;
							try {
								targetList = contentWS.loadItems(targetGuidList, false, false, false, false);
							} catch (PSErrorResultsException e) {
								e.printStackTrace();
							}
							
							PSItemField tgtsysTitle = targetList.get(0).getFieldByName("sys_title");
							PSItemField tgtTitle = targetList.get(0).getFieldByName("long_title");
							PSItemField tgtShortTitle = targetList.get(0).getFieldByName("short_title");
							PSItemField tgtLongDescription = targetList.get(0).getFieldByName("long_description");
							PSItemField tgtShortDescription = targetList.get(0).getFieldByName("short_description");
							PSItemField tgtPrettyURL = targetList.get(0).getFieldByName("pretty_url_name");
							
							String tgtPrettyURLString = "";
							IPSFieldValue tgtPrettyURLVal = null;
							
							if(tgtPrettyURL != null){
								tgtPrettyURLVal = tgtPrettyURL.getValue();
								if(tgtPrettyURLVal != null){
									try {
										tgtPrettyURLString = tgtPrettyURLVal.getValueAsString();
									} catch (PSCmsException e) {
										e.printStackTrace();
									}
								}
							}

							
							try {
								tgtsysTitleString = tgtsysTitle.getValue().getValueAsString();
								tgtTitleString = tgtTitle.getValue().getValueAsString();
								tgtShortTitleString = tgtShortTitle.getValue().getValueAsString();
								tgtLongDescriptionString = tgtLongDescription.getValue().getValueAsString();
								tgtShortDescriptionString = tgtShortDescription.getValue().getValueAsString();
							} catch (PSCmsException e) {
								e.printStackTrace();
							}
							//tgtPath = getFolderPathFromItemPath(targetItem.getPath()) + "/" + tgtPrettyURLString;

						
						
						customLinkInfo.put("tgt_sys_title", tgtsysTitleString);
						customLinkInfo.put("tgt_long_title", tgtTitleString);
						customLinkInfo.put("tgt_short_title", tgtShortTitleString);
						customLinkInfo.put("tgt_long_desc", tgtLongDescriptionString);
						customLinkInfo.put("tgt_short_desc", tgtShortDescriptionString);
						customLinkInfo.put("tgt_path", "PATH");

						
						
						infoList.add(customLinkInfo);
						
					}
					if(includeSubfolders == true && itemSummary.getObjectType()== PSItemSummary.ObjectTypeEnum.FOLDER){
						String folderID = Integer.toString(itemSummary.getGUID().getUUID());
						infoList.addAll(percReport_customLink(folderID, subfolders));
					}
					
				}
				
				
				return infoList;
	}
	@IPSJexlMethod(description = "Returns a context variable for a given Site, using a specified ID.  " +
			"Null if the property isn't defined.", params = {
			@IPSJexlParam(name = "folderCID", description = "the content id of the folder to start the report on"),
			@IPSJexlParam(name = "includeSubfolders", description = "boolean whether to include subfolders in the report"),
			@IPSJexlParam(name = "contentType", description = "the system name of the content type to find fields of"),
			@IPSJexlParam(name = "dataField", description = "the field to find the data in")})
			public ArrayList<HashMap<String, String>> percReport_fieldUsage(String folderCID, String subfolders, String contentType, String dataField){
				boolean includeSubfolders = Boolean.parseBoolean(subfolders);
				IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
				PSOObjectFinder psoObjFinder = new PSOObjectFinder();
				List<PSItemSummary> folderChildren = null;
				ArrayList<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
				String itemPath= "";
				IPSGuid folderGuid = null;
				IPSNode fieldUsageNode = null;
				
				folderGuid = psoObjFinder.getGuidById(folderCID);
		
					try {
						folderChildren = contentWS.findFolderChildren(folderGuid, true);
					} catch (PSErrorException e) {
						e.printStackTrace();
					}
	
					for(PSItemSummary itemSummary : folderChildren){
						if(itemSummary.getContentTypeName().equals(contentType)){
							HashMap<String, String> fieldUsageInfo = new HashMap<String, String>();
							
							fieldUsageInfo.put("content_id", Integer.toString(itemSummary.getGUID().getUUID())); //adds content id
							
						//get custom link assembly item
							try {
								fieldUsageNode = psoObjFinder.getNodeByGuid(itemSummary.getGUID());
							} catch (RepositoryException e2) {
								e2.printStackTrace();
							}
							
							try {
								itemPath = contentWS.findFolderPaths(itemSummary.getGUID())[0] +"/"+ itemSummary.getName();
							} catch (PSErrorException e) {
								e.printStackTrace();
							}
							IPSAssemblyItem customLinkAI = getAssemblyItem(fieldUsageNode, itemPath, getSitePathFromItemPath(itemPath));
							
							ArrayList<IPSGuid> clGuidList = new ArrayList<IPSGuid>();
							clGuidList.add(itemSummary.getGUID());
							List<PSCoreItem> itemList = null;
							try {
								itemList = contentWS.loadItems(clGuidList, false, false, false, false);
							} catch (PSErrorResultsException e) {
								e.printStackTrace();
							}
							
							PSItemField sysTitle = itemList.get(0).getFieldByName("sys_title");
							PSItemField fieldValue = itemList.get(0).getFieldByName(dataField);
							PSItemField prettyURL = itemList.get(0).getFieldByName("pretty_url_name");
							
							IPSFieldValue sysTitleVal = null;
							IPSFieldValue fieldValueVal = null;
							IPSFieldValue prettyURLVal = null;
							
							String sysTitleString = "";
							String fieldValueString = "";
							String prettyURLString = "";

							

							sysTitleVal = sysTitle.getValue();
							fieldValueVal = fieldValue.getValue();
							if(prettyURL != null){
								prettyURLVal = prettyURL.getValue();
								if(prettyURLVal != null){
									try {
										prettyURLString = prettyURLVal.getValueAsString();
									} catch (PSCmsException e) {
										e.printStackTrace();
									}
								}
							}

							if(sysTitleVal != null){
								try {
									sysTitleString = sysTitleVal.getValueAsString();
								} catch (PSCmsException e) {
									e.printStackTrace();
								}
							}
							if(fieldValueVal != null){
							try {
								fieldValueString = fieldValueVal.getValueAsString();
							} catch (PSCmsException e) {
								e.printStackTrace();
							}
							}

							String fullPrettyURL = getFolderPathFromItemPath(itemPath) + "/" + prettyURLString;
							
							fieldUsageInfo.put("sys_title",sysTitleString);
							fieldUsageInfo.put("data_field", fieldValueString);
							fieldUsageInfo.put("pretty_url_name", fullPrettyURL);

							retList.add(fieldUsageInfo);
						}
						if(includeSubfolders == true && itemSummary.getObjectType()== PSItemSummary.ObjectTypeEnum.FOLDER){
							String folderID = Integer.toString(itemSummary.getGUID().getUUID());
							retList.addAll(percReport_fieldUsage(folderID, subfolders, contentType, dataField));
						}
					}
					return retList;
	}
}
