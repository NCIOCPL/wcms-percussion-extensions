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
import org.apache.log4j.Logger;
import org.aspectj.util.CollectionUtil;


public class TemplateUtils {
	
	public static IPSAssemblyTemplate getDefaultTemplate(IPSNode node, String siteId, String sitePath  ) throws RepositoryException {
	
		PSLocationUtils locationUtils = new PSLocationUtils();	
	
		String contentType = node.getDefinition().getName();
		
		//gets content type guid
		PSLegacyGuid localPSLegacyGuid = (PSLegacyGuid)node.getGuid();
	    IPSCmsObjectMgr localIPSCmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
	    PSComponentSummary localPSComponentSummary = localIPSCmsObjectMgr.loadComponentSummary(localPSLegacyGuid.getContentId());
	    IPSGuid contentTypeGuid = new PSGuid(PSTypeEnum.NODEDEF, localPSComponentSummary.getContentTypeId());
		
	    ArrayList defaultTemplates = new ArrayList();
	    List templatesByType = null;
	    Object allTemplates = new ArrayList();
	    
	    Object templates = new ArrayList();
	   
			try {
				templatesByType = PSLocationUtils.findTemplatesByContentType(contentTypeGuid);
			} catch (PSAssemblyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
		String[] pathParts = sitePath.split("/");

		IPSSite aSite = siteManager.loadSite(pathParts[3]);
		
		IPSGuid siteGuid = aSite.getGUID();
		
		if(siteGuid != null){
			allTemplates =  siteManager.loadUnmodifiableSite(siteGuid).getAssociatedTemplates();
		}
		Object templatesIterator = templatesByType.iterator();
	    while (((Iterator) templatesIterator).hasNext())
	    {
	      Object localObject4 = (IPSAssemblyTemplate)((Iterator)templatesIterator).next();
	      if (((IPSAssemblyTemplate)localObject4).getPublishWhen().equals(IPSAssemblyTemplate.PublishWhen.Default)) {
	        ((ArrayList) templates).add(localObject4);
	      }
	    }
	    Object filter = new Predicate()
	    {
	      public boolean evaluate(Object paramAnonymousObject)
	      {
	        IPSAssemblyTemplate localIPSAssemblyTemplate = (IPSAssemblyTemplate)paramAnonymousObject;
	        return (localIPSAssemblyTemplate.getOutputFormat() != IPSAssemblyTemplate.OutputFormat.Snippet) && (localIPSAssemblyTemplate.getOutputFormat() != IPSAssemblyTemplate.OutputFormat.Global);
	      }
	    };
	    CollectionUtils.filter((Collection)allTemplates, (Predicate)filter);
	    CollectionUtils.filter((Collection)templates,  (Predicate)filter);
		Collection intersection = CollectionUtils.intersection((Collection)allTemplates, (Collection)templates);
		
		
		if(intersection.size()>0){
			if(intersection.size()>1){
				
			}
			else{
				return (IPSAssemblyTemplate)((Collection)intersection).iterator().next();
		
			}
		}
		
		
		return null;
	}
}
