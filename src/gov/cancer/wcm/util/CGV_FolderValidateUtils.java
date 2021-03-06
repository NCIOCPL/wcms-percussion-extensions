/**
 * 
 */
package gov.cancer.wcm.util;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.relationship.PSEffectResult;
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
     * Do the actual effect attempt processing
     * @param contentId the contentId of the item
     * @param fieldName name of field to check
     * @param folderId 
     * @param checkPaths
     * @param item
     * @param result
     * @throws Exception
     */
    public void doAttempt(int contentId, String fieldName, int folderId, String checkPaths, PSCoreItem item, PSEffectResult result)
    	throws Exception {
    	log.debug("[doAttempt]getting field");        
    	PSItemField field = item.getFieldByName(fieldName);
    	PSItemDefinition itemDef = item.getItemDefinition();

    	// If the field is null, find out if the item is a folder.
    	// If so, we'll want to compare the sys_title for uniqueness
    	if( field == null && itemDef != null){
    		String typename = item.getItemDefinition().getName();
    		if(typename.equals("Folder")){
    			field = item.getFieldByName("sys_title");
			}
    	}

    	if (field == null) {
    		// If this field still doesn't exist, success is automatic.
    		log.debug("[doAttempt]setting success - field is null");        
    		result.setSuccess();
    	}
    	else {
    		log.debug("[doAttempt]got field");
    		String fieldValue = null;
    		//we need to handle null field values
    		IPSFieldValue val = field.getValue();
    		if (val != null)
				fieldValue = val.getValueAsString();
    		log.debug("[doAttempt]fieldValue = " + fieldValue);        
			String typeList = makeTypeList(fieldName);

			boolean rvalue = true;
			if (folderId != 0) {
				rvalue = isFieldValueUniqueInFolder(folderId, fieldName, fieldValue, typeList, checkPaths, contentId);
			}
			else {
				// Always return OK for items which are not in folders.
				rvalue = true;
			}

			if (rvalue) {
				log.debug("doAttempt() - setting success");
				result.setSuccess();
			}
			else {
				log.debug("doAttempt() - setting error");
				result.setError("Pretty_URL_Name must be unique within folder");
			}
    	}
    }
    

    /*
     * Checks whether a proposed folder name would duplicate the value specified field name in any
     * of the content items in the same parent folder.
     * 
     * @param contentId the contentId of the item
     * @param fieldName name of field to check
     * @param folderId 
     * @param checkPaths
     * @param item
     * @param result
     * @throws Exception
     */
    public void validateIsFolderNameUnique(int contentId, String folderName, String fieldName, int folderId, String checkPaths, PSCoreItem item, PSEffectResult result)
    	throws Exception {
    		
		log.debug("[validateIsFolderNameUnique]folderName = " + folderName);        
		String typeList = makeTypeList(fieldName);

		boolean rvalue = true;
		if (folderId != 0) {
			rvalue = isFieldValueUniqueInFolder(folderId, fieldName, folderName, typeList, checkPaths, contentId);
		}

		if (rvalue) {
			log.debug("validateIsFolderNameUnique() - setting success");
			result.setSuccess();
		}
		else {
			log.debug("validateIsFolderNameUnique() - setting error");
			result.setError(fieldName + " must be unique within folder");
		}
    }
    
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
    throws PSErrorException, PSErrorResultsException, InvalidQueryException, RepositoryException {
    	return this.isFieldValueUniqueInFolderForExistingItem(contentId, fieldName, fieldValue, typeList, null);
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
        throws PSErrorException, PSErrorResultsException, InvalidQueryException, RepositoryException {
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
        		boolean uniqueAmongFields;
        		boolean uniqueAmongSubfolders;
        		
        		// Continue until a conflict is found.
        		if (!unique)
        			break;

        		if( fieldValue != null && !fieldValue.trim().isEmpty() ) {
	        		// Check for conflicting content items.
		            String jcrQuery = getQueryForValueInFolders(contentId, fieldName, fieldValue, pathItem, typeList);
		            RowIterator rows = executeQuery(jcrQuery);
		            uniqueAmongFields = rows.getSize() > 0 ? false : true;
	
		            // Check for a conflicting folder name.
		            jcrQuery = getQueryForFolderMatchingValue(fieldValue, pathItem);
		            rows = executeQuery(jcrQuery);
		            uniqueAmongSubfolders = rows.getSize() > 0 ? false : true;
		            
		            unique = uniqueAmongFields && uniqueAmongSubfolders;
        		}
        		else
        		{
        			unique = isNullUnique(fieldName, pathItem, typeList, contentId);
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
     * @param contentId the content ID of the item
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue, String typeList, int contentId)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
    	return this.isFieldValueUniqueInFolder(folderId, fieldName, fieldValue, typeList, null, contentId);
	}
    /**
     *  See if a field value is unique in the given folder for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @param  path If set will check this path for uniqueness,  can use % to check subfolders
     * @param contentId the content ID of the item
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue, String typeList, String path, int contentId)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
		log.debug("isFieldValueUniqueInFolder(): folderId = "+ folderId + " fieldName = " + fieldName + " fieldValue = " + fieldValue + " contentId = " + contentId);
    	boolean unique = true;
    	List<String> paths = new ArrayList<String>();
    	if (folderId != 0) {
    		log.debug("isFieldValueUniqueInFolder(): folderId != 0");

    		// Build up a list of paths to check from a comma-separated list of values
    		// or, if no list is available, the path of the passed folder ID.
    		IPSGuid guid = guidManager.makeGuid(new PSLocator(folderId, -1));
			List<PSFolder> folders = contentWs.loadFolders(asList(guid));
	    	if (path == null) {
	    		log.debug("isFieldValueUniqueInFolder(): path = null");
	    		path = ! folders.isEmpty() ? folders.get(0).getFolderPath() : null;
	    		paths.add(path);
	    	} else {
	    		log.debug("isFieldValueUniqueInFolder(): path != null");
	    		String[] pathSplit = path.split(",");
	    		for(String paramPath : pathSplit) {
	    			for(PSFolder folder : folders) {
	    				if (folder.getFolderPath().startsWith(paramPath)) {
	    					paths.add(paramPath+"/%");
	    				}
	    			}
	    		}
	    	}

    		//if the item isn't in folder we don't check for uniqueness
	    	if (paths != null  && paths.size() != 0) {
	    		log.debug("isFieldValueUniqueInFolder(): got path list");
	    		for (String pathItem : paths ) {
	    			// Continue until we hit a non-unique return.
	    			if (!unique)
	    				break;

	    			if (fieldValue != null && !fieldValue.isEmpty()) {
	    				String jcrQuery = "";
	    				boolean uniqueFieldName;
	    				boolean uniqueFolderName;

	    				// Compare to fields.
	    				// Retrieve appropriate query for new vs. update.
	    				if (contentId == 0)
	    					jcrQuery = getQueryForValueInFolder(fieldName, fieldValue, pathItem, typeList);
	    				else
	    					jcrQuery = getQueryForValueInFolderWithCid(fieldName, fieldValue, pathItem, typeList, contentId);
	    				log.debug("isFieldValueUniqueInFolder(): jcrQuery = " + jcrQuery);
	    				log.trace(jcrQuery);
	    				Query q = contentManager.createQuery(jcrQuery, Query.SQL);
	    				QueryResult results = contentManager.executeQuery(q, -1, null, null);

	    				// Were other items found with this field value?
	    				RowIterator rows = results.getRows();
	    				long size = rows.getSize();
	    				
	    				uniqueFieldName = size > 0 ? false : true;
	    				
	    				// Additional check to verify that the value doesn't
	    				// conflict with a folder name.
	    				jcrQuery = getQueryForFolderMatchingValue(fieldValue, pathItem);
	    				q = contentManager.createQuery(jcrQuery, Query.SQL);
	    				results = contentManager.executeQuery(q, -1, null, null);

	    				// Any matches?
	    				rows = results.getRows();
	    				uniqueFolderName  = rows.getSize() > 0 ? false : true;
	    				
	    				unique = uniqueFieldName && uniqueFolderName;
	    				log.debug("isFieldValueUniqueInFolder(): unique = " + unique);
    				}
    				else {
    					unique = isNullUnique(fieldName, pathItem, typeList, contentId);
    				}
	    		}
	    	}
	    	else {
	    		log.error("The folder id: " + folderId + " did not have a path (BAD)");
	    	}
    	}
    	return unique;
    }
    
    private boolean isNullUnique(String fieldName, String pathItem, String typeList, int contentId)
        throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
    	boolean unique = true;
		String jcrQuery = "";
		if (contentId == 0) {
			jcrQuery = getQueryForValueListInFolder(fieldName, pathItem, typeList);
		}
		else {
			jcrQuery = getQueryForValueListInFolderWithCid(fieldName, pathItem, typeList, contentId);
		}
		log.debug("isNullUnique(): jcrQuery = " + jcrQuery);

		// Check whether any other null rows exist.
		RowIterator rows = executeQuery(jcrQuery);
		try {
			while (rows.hasNext() && unique) {
				Row row = (Row)rows.next();
				Value value = row.getValue("rx:" + fieldName);
				if (value == null || value.getString().trim().isEmpty()) {
					//should never get here
					unique = false;
				}
			}
		}
		catch (ItemNotFoundException e) {
			//must have found a null value
			unique = false;
		}
    	return unique;
    }

    /**
     * Build JCR query for search across folders
     * @param contentId
     * @param fieldName
     * @param fieldValue
     * @param path
     * @param typeList
     * @return
     */
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
    
    /**
     * Build JCR query for search in one folder
     * @param fieldName
     * @param fieldValue
     * @param path
     * @param typeList
     * @return
     */
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
    
    /**
     * Build JCS query for search in one folder, ignoring this content id
     * in order to not find the new item
     * @param fieldName
     * @param fieldValue
     * @param path
     * @param typeList
     * @param contentId
     * @return
     */
    public String getQueryForValueInFolderWithCid(String fieldName, String fieldValue, String path, String typeList, int contentId) {
        return format(
                "select rx:sys_contentid, rx:{0} " +
                "from {3} " +
                "where " +
                "rx:{0} = ''{1}'' " +
                "and " +
                "rx:sys_contentid != {4} " +
                "and " +
                "jcr:path like ''{2}''", 
                fieldName, fieldValue, path, typeList, String.valueOf(contentId));
    }

    /**
     * Build JCS query to search in a folder for existing folders matching a name.
     * @param folderName Folder name to search for
     * @param path Parent folder to search in.
     * @return
     */
    public String getQueryForFolderMatchingValue(String folderName, String path){
    	return format(
    			"select rx:sys_contentid " +
    			"from rx:folder " +
    			"where rx:sys_title = ''{0}'' " +
    			"and " +
    			"jcr:path like ''{1}''",
    			folderName, path );
    }
    
    /**
     * Build JCR query for search in one folder
     * @param fieldName
     * @param fieldValue
     * @param path
     * @param typeList
     * @return
     */
    public String getQueryForValueListInFolder(String fieldName, String path, String typeList) {
        return format(
                "select rx:sys_contentid, rx:{0} " +
                "from {2} " +
                "where " +
                "jcr:path like ''{1}''", 
                fieldName, path, typeList);
    }
    
    /**
     * Build JCS query for search in one folder, ignoring this content id
     * in order to not find the new item
     * @param fieldName
     * @param fieldValue
     * @param path
     * @param typeList
     * @param contentId
     * @return
     */
    public String getQueryForValueListInFolderWithCid(String fieldName, String path, String typeList, int contentId) {
        return format(
                "select rx:sys_contentid, rx:{0} " +
                "from {2} " +
                "where " +
                "rx:sys_contentid != {3} " +
                "and " +
                "jcr:path like ''{1}''", 
                fieldName, path, typeList, String.valueOf(contentId));
    }
    
    /**
     * get the target parent folder id from the redirect url
     * @param request
     * @return
     */
    public Integer getFolderId(IPSRequestContext request) {
        String folderId = null;
        Integer rvalue = null;
        String psredirect = request.getParameter(
           IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
        if (psredirect != null && psredirect.trim().length() > 0)
        {
           int index = psredirect.indexOf(IPSHtmlParameters.SYS_FOLDERID);
           if(index >= 0)
           {
              folderId = psredirect.substring(index +
                 IPSHtmlParameters.SYS_FOLDERID.length() + 1);
              index = folderId.indexOf('&');
              if(index > -1)
                 folderId = folderId.substring(0, index);
           }
        }
        else {	//else block is new for testing only
        	folderId = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
        }
        if (StringUtils.isNumeric(folderId) && StringUtils.isNotBlank(folderId)) {
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
    
    /**
     * Load a content item from the content ID
     * @param contentId
     * @return the item object
     * @throws PSErrorResultsException
     */
    public PSCoreItem loadItem(String contentId)
    		throws PSErrorResultsException
	{
		IPSGuid cid = getGuidManager().makeGuid(new PSLocator(contentId));
		List<IPSGuid> glist = Collections.<IPSGuid>singletonList(cid);
		List<PSCoreItem> items = contentWs.loadItems(glist, true, false, false, false);
    	PSCoreItem item = items.get(0);
		return item;
	}

    /*
     * Examines a content item and determines whether it represents a folder.
     */
    public static boolean isFolder(PSCoreItem item) {
    	if( item == null){
    		log.error("isFolder requires that argument 'item' must not be null.");
    		throw new NullArgumentException("item");
    	}
    	
    	boolean isFolder;
    	
    	PSItemDefinition itemDef = item.getItemDefinition();
		String typename = itemDef.getName();
		
		isFolder = typename.equals("Folder");

    	return isFolder;
    }
    
    /**
     * Returns a list of Strings containing all of the sites that a 
     * piece of content lives in.
     * @param contentid
     * @param navon - is the item a navon?  If it is, append "Navon" to the site name.
     * @return
     * @throws PSErrorException
     */
    public List<String> getSites(int contentid, boolean navon) throws PSErrorException
    {	
		List<String> sites = new ArrayList<String>();
		IPSGuid guid = guidManager.makeGuid(new PSLocator(contentid, -1));
		List<String> itemPaths = Arrays.asList(contentWs.findFolderPaths(guid)); 
		for( String currPath : itemPaths ){
			StringTokenizer st = new StringTokenizer(currPath, "/");
			String sitePath = "";
			if(st.countTokens() >= 2){
				st.nextToken();
				sitePath = st.nextToken();
			}
			else{
				throw new PSErrorException();
			}
			if(!navon){
				if(!sitePath.equalsIgnoreCase("") && !sites.contains(sitePath)){
					sites.add(sitePath);
				}
			}
			else{
				if(sitePath != ""){
					sitePath += "Navon";
					if(!sites.contains(sitePath)){
						sites.add(sitePath);
					}
				}
			}
		}
		
    	return sites;
    }
    
    /**
     * Returns a list of Strings containing all of the site paths that a piece 
     * of content lives in.
     * "//Sites/<Site Name>/%"
     * @param contentid
     * @return
     * @throws PSErrorException
     */
    public List<String> getSitePaths(int contentid) throws PSErrorException
    {	
		List<String> paths = new ArrayList<String>();
		IPSGuid guid = guidManager.makeGuid(new PSLocator(contentid, -1));
		List<String> itemPaths = Arrays.asList(contentWs.findFolderPaths(guid)); 
		for( String currPath : itemPaths ){
			StringTokenizer st = new StringTokenizer(currPath, "/");
			String sitePath = "//";
			if(st.countTokens() >= 2){
				sitePath += st.nextToken()+"/"+st.nextToken()+"/";
			}
			else{
				throw new PSErrorException();
			}
			if(sitePath != "" && !paths.contains(sitePath)){
				paths.add(sitePath);
			}
		}
		
    	return paths;
    }
    
    /**
     *  See if a field value is unique in the given site for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @param  path If set will check this path for uniqueness,  can use % to check subfolders
     * @param contentId the content ID of the item
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     * @throws PSException 
     */
    public boolean isFieldValueUniqueInSite(int folderId, String fieldName, String fieldValue, String typeList, int contentId)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException, PSException {
		log.debug("isFieldValueUniqueInSite(): folderId = "+ folderId + " fieldName = " + fieldName + " fieldValue = " + fieldValue + " contentId = " + contentId);
    	boolean unique = true;
    	List<String> paths = new ArrayList<String>();
    	if (folderId != 0) {
//			List<String> sitePaths = new ArrayList<String>();
//			try {
//				sitePaths = getSitePaths(contentId);
//			} catch (PSErrorException e) {
//				e.printStackTrace();
//			}
//			if(sitePaths.isEmpty()){
//				throw new PSException("No site path found for the current object.");
//			}
    		
    		log.debug("isFieldValueUniqueInSite(): folderId != 0");
    		//if item isn't in folder we don't check for uniqueness
    		IPSGuid guid = guidManager.makeGuid(new PSLocator(folderId, -1));
    		List<PSFolder> folders = contentWs.loadFolders(asList(guid));
    		//else {
    		log.debug("isFieldValueUniqueInFolder(): path = null");
    		String path = ! folders.isEmpty() ? folders.get(0).getFolderPath() : null;
			StringTokenizer st = new StringTokenizer(path, "/");
			String sitePath = "//";
			if(st.countTokens() >= 2){
				sitePath += st.nextToken()+"/"+st.nextToken()+"/%";
			}
			else{
				throw new PSErrorException();
			}
    		paths.add(sitePath);
    		//}
	    	if (paths != null  && paths.size() != 0) {
	    		log.debug("isFieldValueUniqueInSite(): got path list");
	    		for (String pathItem : paths ) {
	    			if (unique ) {
	    				if (fieldValue != null) {
		    				String jcrQuery = "";
		    				if (contentId == 0)
		    					jcrQuery = getQueryForValueInFolder(fieldName, fieldValue, pathItem, typeList);
		    				else
		    					jcrQuery = getQueryForValueInFolderWithCid(fieldName, fieldValue, pathItem, typeList, contentId);
		    				log.debug("isFieldValueUniqueInSite(): jcrQuery = " + jcrQuery);
		    				log.trace(jcrQuery);
		    				Query q = contentManager.createQuery(jcrQuery, Query.SQL);
		    				QueryResult results = contentManager.executeQuery(q, -1, null, null);
		    				RowIterator rows = results.getRows();
		    				long size = rows.getSize();
		    				
		    				unique = size > 0 ? false : true;
		    				log.debug("isFieldValueUniqueInSite(): unique = " + unique);
	    				}
	    				else {
	    					unique = isNullUnique(fieldName, pathItem, typeList, contentId);
	    				}
	    			}
	    		}
	    	}
	    	else {
	    		log.error("The folder id: " + folderId + " did not have a path (BAD)");
	    	}
    	}
    	return unique;
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

	/**
	 * Convenience method to encapsulate execution of JCR queries.
	 * @param jcrQuery A JSR-170 content query
	 * @return A RowIterator object containing the query results.
	 * @throws RepositoryException
	 */
	private RowIterator executeQuery(String jcrQuery)
		throws RepositoryException {
        log.trace(jcrQuery);
        Query q = contentManager.createQuery(jcrQuery, Query.SQL);
        QueryResult results = contentManager.executeQuery(q, -1, null, null);
        RowIterator rows = results.getRows();

        return rows;
	}
}
