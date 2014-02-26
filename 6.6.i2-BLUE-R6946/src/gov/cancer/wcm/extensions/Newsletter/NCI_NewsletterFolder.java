package gov.cancer.wcm.extensions.Newsletter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import gov.cancer.wcm.extensions.jexl.CGV_AssemblyTools;
import gov.cancer.wcm.workflow.PercussionWFTransition;

import java.util.StringTokenizer;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

/**
 * This preprocessor creates subfolders for an item when the item gets created.
 * Pass in a comma separated list of the names of the sub-folders and ones that
 * do not already exist get created.
 *
 *
 * @author wallsjt
 */
public class NCI_NewsletterFolder extends PSDefaultExtension implements
IPSResultDocumentProcessor {
	private static IPSContentWs cws = null;
	protected static IPSGuidManager gmgr = null;

	/**
	 * Service class to invoke publishing routine
	 */
	private static NCI_NewsletterFolderService svc = null;


	public NCI_NewsletterFolder() {
		super();
	}


	/**
	 * Initializing the Service class. 
	 * @param request IPSRequestContext
	 */
	private static void initServices()
	{
		if (cws == null) {
			cws = PSContentWsLocator.getContentWebservice();
			gmgr = PSGuidManagerLocator.getGuidMgr();
		}
		if(svc == null)
		{
			svc = NCI_NewsletterFolderLocator.getNCI_NewsletterFolderService();
		}
	}

	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
	 */
	public void init(IPSExtensionDef extensionDef, File arg1)
	throws PSExtensionException {
	}

	/*
	 * The log instance to use for this class, never <code>null</code>.
	 */
	private static final Log log = LogFactory
	.getLog(NCI_NewsletterFolder.class);

	@Override
	public boolean canModifyStyleSheet() {
		return false;
	}


	@Override
	public Document processResultDocument(Object[] params,
			IPSRequestContext request, Document doc)
	throws PSParameterMismatchException, PSExtensionProcessingException {

		initServices();
		
		log.debug("NCI_NewsletterFolder starting...");

		//Check if the current request is from a modify of an item.
		String cmd = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
		CGV_AssemblyTools aTools = new CGV_AssemblyTools();

		if (cmd != null && cmd.equalsIgnoreCase("modify")) {

			String actionType = request.getParameter("DBActionType");
			if(actionType.equals("INSERT")){

				String contentType = request.getRequestPage(false);
				//Find the current item's GUID then find where it is stored (site path)
				IPSGuid itemGuid = gmgr.makeGuid(new PSLocator(request.getParameter("sys_contentid")));

				String long_title = request.getParameter("long_title");
				String long_title_no_spaces = long_title.replaceAll(" ", "");

				List<String> itemPaths = null;
				try {
					itemPaths = Arrays.asList(cws.findFolderPaths(itemGuid));
				} catch (PSErrorException e) {
					e.printStackTrace();
				}

				if(contentType.equalsIgnoreCase("genNewsletterIssue")){ //Newsletter - setup container folder
					//If there was no error in getting the path of the current item.
					if(itemPaths != null && itemPaths.size() > 0){

						//Build out the path string of the current item
						String fullPath = "";
						fullPath = itemPaths.get(0);
						//Proceed if the path is not blank.
						if(!fullPath.equals("")){
							boolean folderExists = false;

							String[] paths = fullPath.split("/");
							if(paths[paths.length-1] == long_title_no_spaces){
								folderExists = true;
							}

							if(!folderExists){
								//Create a new folder for the newsletter (foldername = long_title without spaces)
								log.debug("LongTitle= "+ long_title_no_spaces + "FullPath= " + fullPath);
								try {
									cws.addFolder(long_title_no_spaces, fullPath);
								} catch (PSErrorException e1) {
									e1.printStackTrace();
								}

								//Checkout the newsletter
								try {
									cws.checkoutItems(Collections.<IPSGuid> singletonList(itemGuid), "");
								} catch (PSErrorsException e) {
									e.printStackTrace();
								}

								//Update the path string to mimic the new folder structure
								fullPath = fullPath + "/" + long_title_no_spaces;

								//Move the newsletter item to the new folder.
								List<PSCoreItem> newsletter = null;
								try {
									newsletter = cws.loadItems(Collections.<IPSGuid> singletonList(itemGuid), false, false, false, true);
								} catch (PSErrorResultsException e1) {
									e1.printStackTrace();
								}
								if(newsletter != null && newsletter.size() > 0){
									newsletter.get(0).setFolderPaths(Collections.<String> singletonList(fullPath));
								}
								//Save newsletter
								try {
									cws.saveItems(Collections.<PSCoreItem> singletonList(newsletter.get(0)), false, false);
								} catch (PSErrorResultsException e) {
									e.printStackTrace();
								}
															
							}
							else{
								//folderExists == true
								//Do nothing
							}
						}
					}
				}
				else if(contentType.equalsIgnoreCase("genNewsletterDetails")){	//Newsletter Index - setup index page + sub folders
					//If there was no error in getting the path of the current item.
					if(itemPaths != null && itemPaths.size() > 0){

						//Build out the path string of the current item
						String fullPath = "";
						fullPath = itemPaths.get(0);
						//Proceed if the path is not blank.
						if(!fullPath.equals("")){
							//Get the configuration mappings
							//Map<String,String> slotMapping = svc.getFolderList(getSiteName(fullPath));	
							//Set<String> newFolders = slotMapping.keySet();	
							
							Map<List<String>, Map<String, String>> newFolders = new HashMap<List<String>, Map<String, String>>();
							try {
								newFolders = getNewFolderNames(aTools.getParentFromPath(fullPath));
							} catch (RepositoryException e1) {
								e1.printStackTrace();
							}
							//Add the folders
							
							int slotPosition = 0;
							List<String> folderOrder = new ArrayList<String>();
							Set<List<String>> folderOrderSet = newFolders.keySet();
							Map<String, String> newFoldersMap = new HashMap<String, String>();
							for(List<String> foList : folderOrderSet){
								folderOrder = foList;
								newFoldersMap= newFolders.get(folderOrder);
							}
							
							for(String categoryName : folderOrder){
								String currFolder = newFoldersMap.get(categoryName);
								log.debug("CurrFolder=|"+ currFolder + "|FullPath=|" + fullPath +"|");
								try {
									cws.addFolder(currFolder, fullPath);
								} catch (PSErrorException e) {
									log.debug(e.getErrorMessage());
								}

								//Build site path string.
								String sitePath = fullPath + "/" + currFolder;

								List<IPSGuid> newsletterCategory = null;
								//Create Aggro Widget
								newsletterCategory = createNewsletterCategories(sitePath, categoryName);


								//Create lists to hold the unique items in the folders.
								List<PSItemSummary> navonSummary = getContentTypesInFolder(sitePath, "rffNavon"); //Navon item
								List<PSItemSummary> newsletterCategorySummary = getContentTypesInFolder(sitePath, "genNewsletterCategory");	//newsletter category item

								if(newsletterCategory != null && newsletterCategory.size() > 0){

									//Checkout the navon
									try {
										cws.checkoutItems(Collections.<IPSGuid> singletonList(navonSummary.get(0).getGUID()), "");
									} catch (PSErrorsException e) {
										e.printStackTrace();
									}

									//Set the Aggro Widget as the landing page of its navon
									setItemAsNavLandingPage(navonSummary.get(0), newsletterCategory.get(0));

									//Set the Aggro Widget in the slot on the newsletter index page
									addRelationship(newsletterCategory.get(0), itemGuid, "genSlotBody", "genSnNewsletterCategory", slotPosition);
									//newItemGuids.add(newsletterCategory.get(0));



									//Transition the navon
									transitionNavons(navonSummary);

									//Transition the Aggro Widget
									transitionPage(newsletterCategorySummary);

									//Checkin the navon
									try {
										cws.checkinItems(Collections.<IPSGuid> singletonList(navonSummary.get(0).getGUID()), "");
									} catch (PSErrorsException e) {
										e.printStackTrace();
									}
								}
								slotPosition++;
							}
							//Checkin the newsletter
							try {
								cws.checkinItems(Collections.<IPSGuid> singletonList(itemGuid), "");
							} catch (PSErrorsException e) {
								e.printStackTrace();
							}
		

							/**
							 * MAKE THE NEWSLETTER INDEX THE LANDING PAGE
							 */

							List<PSItemSummary> newFolderNavon = getContentTypesInFolder(fullPath, "rffNavon");

							//Checkout the navon
							try {
								cws.checkoutItems(Collections.<IPSGuid> singletonList(newFolderNavon.get(0).getGUID()), "");
							} catch (PSErrorsException e) {
								e.printStackTrace();
							}

							//Set the newsletter as the landing page of its navon
							setItemAsNavLandingPage(newFolderNavon.get(0), itemGuid);
							
							//Transition the navon
							transitionNavons(newFolderNavon);

							
						}
					}
				}
			}
		}
		else{
			//For now, don't do anything if the action is not INSERT.
		}
		return doc;
	}
	
	private Map<List<String>, Map<String, String>> getNewFolderNames(String fullPath)
	throws PSExtensionProcessingException{
		//find configuration item genConfigurationText with long_title Newsletter
		IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
		
		List<String> folderOrder = new ArrayList<String>();
		Map<String, String> folderMap = new HashMap<String, String>();
		Map<List<String>, Map<String, String>> retMap = new HashMap<List<String>, Map<String, String>>();
		String configString = "";
		
		String qry = "select jcr:path, rx:sys_contentid, rx:long_title, rx:configuration_text from rx:genConfigurationText where jcr:path like '"+fullPath+"%' and rx:long_title = 'Newsletter'";
		Query query = null;
		QueryResult qresults = null;
		
			try {
				query = contentMgr.createQuery(qry, Query.SQL);
	
				if(query != null){
					qresults = contentMgr.executeQuery(query, -1, null, null);
				}
				if(qresults != null){
					RowIterator rows = qresults.getRows();
					if(rows.hasNext()){
						Row row = rows.nextRow();
						Value configValue = row.getValue("rx:configuration_text");
						if(configValue != null){
							configString = configValue.getString();
						}
					}
				}
			} catch (InvalidQueryException e) {
				e.printStackTrace();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		//parse comma separated text into a list
			if(configString == ""){
				return retMap;
			}
			String[] configArray = configString.split("\\r?\\n");
			for(int i=0; i < configArray.length; i++){
				int lastIdx = configArray[i].lastIndexOf("|");
				String folderName = "category" + i;
				String categoryName = "Category" + i;
				if(lastIdx == -1){
					String errorMessage = "Invalid Configuration File, please make sure the configuration file has lines of structure <Category Name> | <Folder Name>";
					log.error(errorMessage);
				}
				else{
					folderName = configArray[i].substring(lastIdx+1);
					categoryName = configArray[i].substring(0, lastIdx);
					if(folderName.trim().length() != 0 && categoryName.trim().length() != 0){
						folderMap.put(categoryName.trim(), folderName.trim());
						folderOrder.add(categoryName.trim());
					}
				}
			}
		//return list
		retMap.put(folderOrder, folderMap);
		return retMap;
	}

	/**
	 * Transitions the navons that are provided in the list of items.
	 * @param items - PSItemSummary list of navon items.
	 */
	private void transitionNavons(List<PSItemSummary> items){
		for(PSItemSummary item: items){
			try {
				List<String> navonTransitions = svc.getNavonTranstions();
				for(String currTransition : navonTransitions){
					PercussionWFTransition.transitionItem(item.getGUID().getUUID(), currTransition, "", null);
				}
			} catch (PSErrorException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Transition a list of pages (newsletters) into public.
	 * @param items - the list of PSItemSummary newsletter pages
	 */
	private void transitionPage(List<PSItemSummary> items){
		for(PSItemSummary item: items){
			try {
				List<String> navonTransitions = svc.getPageTranstions();
				for(String currTransition : navonTransitions){
					PercussionWFTransition.transitionItem(item.getGUID().getUUID(), currTransition, "", null);
				}
			} catch (PSErrorException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates aggro widgets in the folder provided, using a provided title.
	 * @param folder - folder to put the aggro widget into (folder path)
	 * @param title - title of the aggro widget (will be changed a bit for some title fields).
	 * @return - List of aggro widget items that are created, or null (for errors)
	 */
	private List<IPSGuid> createAggroWidgets(String folder, String title){
		List<PSCoreItem> newItems = null;
		try {
			newItems = cws.createItems("genAggroWidget", 1);
		} catch (PSUnknownContentTypeException e) {
			e.printStackTrace();
		} catch (PSErrorException e) {
			e.printStackTrace();
		}

		//String length of the existing path
		List<IPSGuid> guidList = null;

		if(newItems.size() > 0){
			PSCoreItem singleItem = newItems.remove(0);

			//Set the folder path of the item
			singleItem.setFolderPaths(Collections.<String> singletonList(folder));

			//Set the fields
			setField(singleItem, "sys_title", title+" Aggro Widget");
			setField(singleItem, "long_title", title);
			setField(singleItem, "short_title", title);
			setField(singleItem, "content_type", "genGeneral");
			try {
				guidList = cws.saveItems(Collections.<PSCoreItem> singletonList(singleItem), false, false);
			} catch (PSErrorResultsException e) {
				e.printStackTrace();
			}

		}
		else
		{
			log.error("More folder paths provided, than available items.");
		}

		return guidList;
	}
	
	private List<IPSGuid> createNewsletterCategories(String folder, String title){
		
		List<PSCoreItem> newItems = null;
		try {
			newItems = cws.createItems("genNewsletterCategory", 1);
		} catch (PSUnknownContentTypeException e) {
			e.printStackTrace();
		} catch (PSErrorException e) {
			e.printStackTrace();
		}

		//String length of the existing path
		List<IPSGuid> guidList = null;

		if(newItems.size() > 0){
			PSCoreItem singleItem = newItems.remove(0);

			//Set the folder path of the item
			singleItem.setFolderPaths(Collections.<String> singletonList(folder));

			//Set the fields
			setField(singleItem, "sys_title", title);
			setField(singleItem, "long_title", title);
			setField(singleItem, "short_title", title);
			//setField(singleItem, "pretty_url_name", title_no_spaces);
			try {
				guidList = cws.saveItems(Collections.<PSCoreItem> singletonList(singleItem), false, false);
			} catch (PSErrorResultsException e) {
				e.printStackTrace();
			}

		}
		else
		{
			log.error("More folder paths provided, than available items.");
		}

		return guidList;
	}
	


	/**
	 * Sets the field for an item.
	 * Sets *item* field "fieldName* to *value*
	 * @param item - the item to change the field for.
	 * @param fieldName - the name of the field to change
	 * @param value - the new value of the field
	 */
	private void setField(PSCoreItem item, String fieldName, String value){
		PSItemField fld = item.getFieldByName(fieldName);
		fld.clearValues();
		fld.addValue(new PSTextValue(value));
	}
	
	/**
	 * Finds all items in a folder(path) that match a certain content type.
	 * @param path - path of the folder to check in
	 * @param contentType - name of the content type to search for
	 * @return - a list of the PSItemSummary of the content types in folder (path).
	 * 	Never NULL, can be empty.
	 */
	private List<PSItemSummary> getContentTypesInFolder(String path, String contentType){
		List<PSItemSummary> folderChildren = null;
		try {
			folderChildren = cws.findFolderChildren(path,true);
		} catch (PSErrorException e) {
			e.printStackTrace();
		}
		List<PSItemSummary> items = new ArrayList<PSItemSummary>();
		for(PSItemSummary summary : folderChildren){
			if(summary.getContentTypeName().equalsIgnoreCase(contentType)){
				items.add(summary);
			}
		}
		return items;
	}

	/**
	 * Sets an item as the landing page for a navon.
	 * @param navon - the navon PSItemSummary
	 * @param dependent - IPSGuid of the dependent item to be the landing page.
	 */
	private void setItemAsNavLandingPage(PSItemSummary navon, IPSGuid dependent){
		addRelationship(dependent, navon.getGUID(), "rffNavLandingPage", svc.getLandingPageTemplate(), -1);
	}

	/**
	 * Add relationship between dependent and owner, in slotName using template.
	 * @param dependent - the dependent in the rel.
	 * @param owner - the owner of the rel.
	 * @param slotName - name of the slot to create the rel for.
	 * @param template - name of the template to assign dependent to in the slotName.
	 */
	private void addRelationship(IPSGuid dependent, IPSGuid owner, String slotName, String template, int slotIdx){

		try {
			cws.addContentRelations(owner, Collections.<IPSGuid>singletonList(dependent),slotName, template, "", slotIdx);
		} catch (PSErrorException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds the site root name for any folder path.
	 * @param fullPath - full string for any folder.
	 * @return - just the Site name of that folder.
	 */
	private String getSiteName(String fullPath){
		//Get the names of the new folders from the config file.
		StringTokenizer st = new StringTokenizer(fullPath, "/");
		if(st.countTokens() >= 2){
			st.nextToken();
			return st.nextToken();
		}
		else{
			return null;
		}
	}
}
