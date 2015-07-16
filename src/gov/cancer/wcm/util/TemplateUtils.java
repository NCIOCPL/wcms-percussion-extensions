package gov.cancer.wcm.util;



import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.deploy.server.PSJexlHelper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteHelper;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSStopwatchStack;

import gov.cancer.wcm.publishing.PODQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.jexl.Expression;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.aspectj.util.CollectionUtil;


public class TemplateUtils {
	private static final Log log = LogFactory.getLog(TemplateUtils.class);
	
	//This method is copied from source code of percussion
	//that gets the template but without having an Assembly Item
	public static IPSAssemblyTemplate getDefaultTemplate(IPSNode node, String itemPath, String sitePath  ) throws RepositoryException {
	
		PSLocationUtils locationUtils = new PSLocationUtils();	
	
		//the content type of the node passed in
		String contentType = node.getDefinition().getName();
		
		//gets content type guid
		PSLegacyGuid localPSLegacyGuid = (PSLegacyGuid)node.getGuid();
	    IPSCmsObjectMgr localIPSCmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
	    PSComponentSummary localPSComponentSummary = localIPSCmsObjectMgr.loadComponentSummary(localPSLegacyGuid.getContentId());
	    IPSGuid contentTypeGuid = new PSGuid(PSTypeEnum.NODEDEF, localPSComponentSummary.getContentTypeId());
		
	    //initialize list that will hold the templates by content type
	    List templatesByType = null;
	   
	   
	    //try to get all templates associated with a given content type
			try {
				templatesByType = PSLocationUtils.findTemplatesByContentType(contentTypeGuid);
			} catch (PSAssemblyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		//gets the site GUID 
		IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
		String[] pathParts = sitePath.split("/");
		IPSSite aSite = siteManager.loadSite(pathParts[3]);
		IPSGuid siteGuid = aSite.getGUID();
		
		/*
		 * NOTE THE FOLLOWING WAS COPIED FROM SOURCE CODE TO INSURE IT WORKED
		 * the variables and objects have been renamed to help understanding
		 * but still explaining what's happening
		 */
		
		//starts the finding and filling of templates and lists for comparison to get the 1 template
		ArrayList defaultTemplates = new ArrayList();
	    Object siteTemplates = new ArrayList();
	    Object resultTemplate = null;
	    if (siteGuid != null)
	    {
	    //gets templates associated with the site id
	      siteTemplates = siteManager.loadUnmodifiableSite(siteGuid).getAssociatedTemplates();
	      if (((Collection)siteTemplates).size() == 0) {
	        //this.ms_log.debug("No Templates Associated with type " + contentTypeGuid + " and the site " + siteGuid);
	      }
	      //adds the templates to Array list
	      Object localObject3 = new HashSet();
	      ((Set)localObject3).addAll((Collection)siteTemplates);
	      siteTemplates = localObject3;
	    }
	    Object localObject3 = templatesByType.iterator();
	    while (((Iterator)localObject3).hasNext())
	    {
	    	//checks for Default templates and adds them to another array list
	      Object targetTemplate = (IPSAssemblyTemplate)((Iterator)localObject3).next();
	      if (((IPSAssemblyTemplate)targetTemplate).getPublishWhen().equals(IPSAssemblyTemplate.PublishWhen.Default)) {
	        defaultTemplates.add(targetTemplate);
	      }
	    }
	    localObject3 = new Predicate()
	    {
	      public boolean evaluate(Object paramAnonymousObject)
	      {
	    	//checks to see if there are any Snippet templates or global templates associated
	        IPSAssemblyTemplate localIPSAssemblyTemplate = (IPSAssemblyTemplate)paramAnonymousObject;
	        return (localIPSAssemblyTemplate.getOutputFormat() != IPSAssemblyTemplate.OutputFormat.Snippet) && (localIPSAssemblyTemplate.getOutputFormat() != IPSAssemblyTemplate.OutputFormat.Global);
	      }
	    };
	   
	    
	    //filters the array list based on Default templates for the item and filters out the other templates
	    //associated with the content
	    CollectionUtils.filter((Collection)siteTemplates, (Predicate)localObject3);
	    CollectionUtils.filter(templatesByType, (Predicate)localObject3);
	    CollectionUtils.filter(defaultTemplates, (Predicate)localObject3);
	    Object targetTemplate = CollectionUtils.intersection(defaultTemplates, (Collection)siteTemplates);
	    if (defaultTemplates.size() < 1) {
	    log.warn("No default templates associated with the item's content type." + contentTypeGuid);
	    }
	    Object logMessage;
	    if (templatesByType.size() < 1)
	    {
	      logMessage = "no templates associated with this content type :" + contentTypeGuid;
	     log.error(logMessage);
	     // throw new PSAssemblyException(17, new Object[] { paramIPSAssemblyItem.getPath(), contentTypeGuid, logMessage });
	    }
	    if (((Collection)targetTemplate).size() > 0)
	    {
	     log.debug("Using the set of default page templates associated with type " + contentTypeGuid + " and site.");
	      resultTemplate = targetTemplate;
	    }
	    else
	    {
	      if (siteGuid != null)
	      {
	        logMessage = "no default template could be found for content type " + contentTypeGuid + " and site id = " + siteGuid.toString();
	      log.error(logMessage);
	        //throw new PSAssemblyException(17, new Object[] { paramIPSAssemblyItem.getPath(), contentTypeGuid, logMessage });
	      }
	      if (defaultTemplates.size() > 0)
	      {
	       log.debug("No site id available to locate default template.  Using full set of default templates associated with content type " + contentTypeGuid);
	        resultTemplate = defaultTemplates;
	      }
	      else
	      {
	        logMessage = "Error in logic occured";
	        log.error(logMessage);
	        //throw new PSAssemblyException(17, new Object[] { paramIPSAssemblyItem.getPath(), contentTypeGuid, logMessage });
	      }
	    }
	    if (((Collection)resultTemplate).size() > 1)
	    {
	      Object templateResultList = new ArrayList((Collection)resultTemplate);
	      Comparator local2 = new Comparator()
	      {
	        public int compare(Object paramAnonymousObject1, Object paramAnonymousObject2)
	        {
	          IPSAssemblyTemplate localIPSAssemblyTemplate1 = (IPSAssemblyTemplate)paramAnonymousObject1;
	          IPSAssemblyTemplate localIPSAssemblyTemplate2 = (IPSAssemblyTemplate)paramAnonymousObject2;
	          return localIPSAssemblyTemplate1.getName().compareToIgnoreCase(localIPSAssemblyTemplate2.getName());
	        }
	        
	        public int hashCode()
	        {
	          return 0;
	        }
	        
	        public boolean equals(Object paramAnonymousObject)
	        {
	          return false;
	        }
	      };
	      //sorts the collection of result templates (even if more than 1
	      Collections.sort((List)templateResultList, local2);
	      resultTemplate = templateResultList;
	      ArrayList listOfResultTemplates = new ArrayList();
	      Object resultsIterator = ((Collection)resultTemplate).iterator();
	      while (((Iterator)resultsIterator).hasNext())
	      {
	        IPSAssemblyTemplate localIPSAssemblyTemplate = (IPSAssemblyTemplate)((Iterator)resultsIterator).next();
	        listOfResultTemplates.add(localIPSAssemblyTemplate.getName());
	      }
	      resultsIterator = (String)listOfResultTemplates.get(0);
	      if (siteGuid != null) {
	        log.warn("Found multiple default templates: " + listOfResultTemplates.toString() + "for content type " + contentTypeGuid + " picking: " + (String)resultsIterator + " : Fix these templates.  These templates all specify Publish - Default and are assigned to site " + siteGuid.toString() + " there should be only one default template per site for a type ");
	      }
	    }
	    //returns the target template
	    return (IPSAssemblyTemplate)((Collection)resultTemplate).iterator().next();
	
		
	    
		
		
	}
	
	
	
}
