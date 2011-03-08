package gov.cancer.wcm.extensions.jexl;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.jexl.PSONavTools;
import com.percussion.pso.jexl.PSOQueryTools;
import com.percussion.pso.jexl.PSOSlotTools;
import com.percussion.pso.utils.PSOSlotContents;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.utils.guid.IPSGuid;
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

}
