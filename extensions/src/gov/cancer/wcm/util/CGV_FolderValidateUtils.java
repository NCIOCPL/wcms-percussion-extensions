/**
 * 
 */
package gov.cancer.wcm.util;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * Utility class to perform folder validations for other classes
 * Based on PSOUniqueFieldWithInFoldersValidator
 * @author holewr
 *
 */
public class CGV_FolderValidateUtils {
    private IPSExtensionDef extensionDef = null;
    private IPSContentWs contentWs = null;
    private IPSGuidManager guidManager = null;
    private IPSContentMgr contentManager = null;
    private PSONodeCataloger nodeCataloger = null;
    private IPSSystemWs systemWs = null; 
    
    /**
     * See if a field value is unique in all the folders that the given existing item resides. 
     * @param contentId id of the item.
     * @param fieldName field name to check for uniqueness.
     * @param fieldValue the value of the field.
      * @return true if its unique
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public boolean isFieldValueUniqueInFolderForExistingItem(int contentId, String fieldName, String fieldValue, String typeList)
    throws PSErrorException, InvalidQueryException, RepositoryException {
    	return this.isFieldValueUniqueInFolderForExistingItem(contentId, fieldName, fieldValue, typeList,null);
    }
   
    /**
     * See if a field value is unique in all the folders that the given existing item resides. 
     * @param contentId id of the item.
     * @param fieldName field name to check for uniqueness.
     * @param fieldValue the value of the field.
     * @param  path If set will check this path for uniqueness,  can use % to check subfolders
     * @return true if its unique
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public boolean isFieldValueUniqueInFolderForExistingItem(int contentId, String fieldName, String fieldValue, String typeList, String path) 
        throws PSErrorException, InvalidQueryException, RepositoryException {
System.out.println("CGV_FolderValidateUtils: isFieldValueUniqueInFolderForExistingItem()");
    	List<String> paths = new ArrayList<String>();
        boolean unique = true;
        IPSGuid guid = guidManager.makeGuid(new PSLocator(contentId, -1));
        List<String> itemPaths = Arrays.asList(contentWs.findFolderPaths(guid)); 
        if (path!=null) {
        	String[] pathSplit = path.split(",");
        	for(String paramPath : pathSplit) {
    			for(String itemPath : itemPaths) {
    				if (itemPath.startsWith(paramPath)) {
    					paths.add(paramPath+"/%");
    				}
    			}
    		}
        	
        } else {
        	paths = itemPaths; 
        }
        if (paths != null && paths.size() != 0) {
        	for (String pathItem : paths ) {
        		if (unique) {
		            String jcrQuery = getQueryForValueInFolders(contentId, fieldName, fieldValue, pathItem, typeList);
		            log.trace(jcrQuery);
		            Query q = contentManager.createQuery(jcrQuery, Query.SQL);
		            QueryResult results = contentManager.executeQuery(q, -1, null, null);
		            RowIterator rows = results.getRows();
		            long size = rows.getSize();
		            unique = size > 0 ? false : true;
        		}
        	}
        }
        else {
            log.debug("The item: " + contentId + " is not in any folders");
        }
        
        return unique;
    
    }
    
    /**
     *  See if a field value is unique in the given folder for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue, String typeList)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
    	return this.isFieldValueUniqueInFolder(folderId, fieldName, fieldValue, typeList, null);
	}
    /**
     *  See if a field value is unique in the given folder for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @param  path If set will check this path for uniqueness,  can use % to check subfolders
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue, String typeList, String path)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
    	boolean unique = true;
    	List<String> paths = new ArrayList<String>();
    	if (folderId != 0) {
    		//if item isn't in folder we don't check for uniqueness
	    	IPSGuid guid = guidManager.makeGuid(new PSLocator(folderId, -1));
			List<PSFolder> folders = contentWs.loadFolders(asList(guid));
	    	if (path == null) {
	    		path = ! folders.isEmpty() ? folders.get(0).getFolderPath() : null;
	    		paths.add(path);
	    	} else {
	    		String[] pathSplit = path.split(",");
	    		for(String paramPath : pathSplit) {
	    			for(PSFolder folder : folders) {
	    				if (folder.getFolderPath().startsWith(paramPath)) {
	    					paths.add(paramPath+"/%");
	    				}
	    			}
	    		}
	    	}
	    	if (paths != null  && paths.size() != 0) {
	    		for (String pathItem : paths ) {
	    			if (unique ) {
	    				String jcrQuery = getQueryForValueInFolder(fieldName, fieldValue, pathItem, typeList);
	    				log.trace(jcrQuery);
	    				Query q = contentManager.createQuery(jcrQuery, Query.SQL);
	    				QueryResult results = contentManager.executeQuery(q, -1, null, null);
	    				RowIterator rows = results.getRows();
	    				long size = rows.getSize();
	    				
	    				unique = size > 0 ? false : true;
	    			}
	    		}
	    	}
	    	else {
	    		log.error("The folder id: " + folderId + " did not have a path (BAD)");
	    	}
    	}
    	return unique;
    }

    public String getQueryForValueInFolders(
            int contentId, 
            String fieldName, 
            String fieldValue, 
            String path,
            String typeList) {
        String jcrQuery = format(
                "select rx:sys_contentid, rx:{0} " +
                "from {4} " +
                "where " +
                "rx:sys_contentid != {1} " +
                "and " +
                "rx:{0} = ''{2}'' " +
                "and " +
                "jcr:path like ''{3}''", 
                fieldName, ""+contentId, 
                fieldValue, path, typeList);
        return jcrQuery;
    }
    
    public String getQueryForValueInFolder(String fieldName, String fieldValue, String path, String typeList) {
        return format(
                "select rx:sys_contentid, rx:{0} " +
                "from {3} " +
                "where " +
                "rx:{0} = ''{1}'' " +
                "and " +
                "jcr:path like ''{2}''", 
                fieldName, fieldValue, path, typeList);
    }
    
    public Integer getFolderId(IPSRequestContext request) {
        // get the target parent folder id from the redirect url
        String folderId = null;
        Integer rvalue = null;
        String psredirect = request.getParameter(
           IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
        if (psredirect != null && psredirect.trim().length() > 0)
        {
System.out.println("CGV_FolderValidateUtils: getFolderId() - got psredirect");
           int index = psredirect.indexOf(IPSHtmlParameters.SYS_FOLDERID);
           if(index >= 0)
           {
System.out.println("CGV_FolderValidateUtils: getFolderId() - index > 0");
              folderId = psredirect.substring(index +
                 IPSHtmlParameters.SYS_FOLDERID.length() + 1);
              index = folderId.indexOf('&');
              if(index > -1)
                 folderId = folderId.substring(0, index);
           }
        }
        else {	//else block is new for testing only
System.out.println("CGV_FolderValidateUtils: getFolderId() - getting folderId from request");
        	folderId = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
        }
        if (StringUtils.isNumeric(folderId) && StringUtils.isNotBlank(folderId)) {
System.out.println("CGV_FolderValidateUtils: getFolderId() - got folderId");
            rvalue = Integer.parseInt(folderId);
        
        }
        
        return rvalue;
    }

    public String makeTypeList(String fieldname) throws RepositoryException
    {
       List<String> types = nodeCataloger.getContentTypeNamesWithField(fieldname);
       StringBuilder sb = new StringBuilder();
       boolean first = true;
       for(String t : types)
       {
          if(!first)
          {
             sb.append(", ");
          }
          sb.append(t);
          first = false;
       }
       return sb.toString();
    }
    
    /**
     * Is this item a promotable version.  Examines the relationships to determine if this item is a 
     * promotable version or not. 
     * @param contentid the content id for the item 
     * @return <code>true</code> if a PV relationship is found. 
     * @throws PSErrorException
     */
    public boolean isPromotable(int contentid) throws PSErrorException
    {
       if(contentid == 0)
       {
          log.debug("no PV for content id 0");
          return false; 
       }
       PSLocator loc = new PSLocator(contentid);
       PSRelationshipFilter filter = new PSRelationshipFilter();
       filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_PROMOTABLE); 
       filter.setDependent(loc); 
      
       List<PSRelationship> rels = systemWs.loadRelationships(filter);
       log.debug("there are " + rels.size() + " PV relationships");
       
       return (rels.size() > 0) ? true : false; 
    }
    
    public void init(IPSExtensionDef extensionDef, File arg1)
    throws PSExtensionException {
		setExtensionDef(extensionDef);
		if (contentManager == null) setContentManager(PSContentMgrLocator.getContentMgr());
		if (contentWs == null) setContentWs(PSContentWsLocator.getContentWebservice());
		if (guidManager == null) setGuidManager(PSGuidManagerLocator.getGuidMgr());
		if (nodeCataloger == null) setNodeCataloger(new PSONodeCataloger());
		if (systemWs == null) setSystemWs(PSSystemWsLocator.getSystemWebservice()); 
	}
    
    public IPSExtensionDef getExtensionDef() {
        return extensionDef;
    }

    public void setExtensionDef(IPSExtensionDef extensionDef) {
        this.extensionDef = extensionDef;
    }

    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(CGV_FolderValidateUtils.class);

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }

    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }

    public void setContentManager(IPSContentMgr contentManager) {
        this.contentManager = contentManager;
    }

   /**
    * @param nodeCataloger the nodeCataloger to set
    */
   public void setNodeCataloger(PSONodeCataloger nodeCataloger)
   {
      this.nodeCataloger = nodeCataloger;
   }

   /**
    * @param systemWs the systemWs to set
    */
   public void setSystemWs(IPSSystemWs systemWs)
   {
      this.systemWs = systemWs;
   }

	public IPSContentWs getContentWs() {
		return contentWs;
	}
	
	public IPSGuidManager getGuidManager() {
		return guidManager;
	}
	
	public IPSContentMgr getContentManager() {
		return contentManager;
	}
	
	public PSONodeCataloger getNodeCataloger() {
		return nodeCataloger;
	}
	
	public IPSSystemWs getSystemWs() {
		return systemWs;
	}

}
