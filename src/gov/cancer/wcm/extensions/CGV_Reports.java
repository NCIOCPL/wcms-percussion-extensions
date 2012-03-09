package gov.cancer.wcm.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import gov.cancer.wcm.extensions.jexl.CGV_AssemblyTools;
import com.percussion.services.guidmgr.PSGuidManagerLocator;

public class CGV_Reports extends PSDefaultExtension{
	
	CGV_AssemblyTools cgvTools = new CGV_AssemblyTools();
	IPSContentWs contentWS = PSContentWsLocator.getContentWebservice();
	
	//Pass in a list of names of fields and the guid of the item to get the fields for 
	//and get a list of values as strings in the order that you passed the field names in.
	public ArrayList<String> getFieldValues(List<String> fields, IPSGuid itemGuid){
		ArrayList<IPSGuid> clGuidList = new ArrayList<IPSGuid>();
		clGuidList.add(itemGuid);
		List<PSCoreItem> itemList = null;
		ArrayList<String> valList = new ArrayList<String>();

		try {
			itemList = contentWS.loadItems(clGuidList, false, false, false, false);
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}
		for(int fieldIdx = 0; fieldIdx<fields.size(); fieldIdx++){
			PSItemField itemField = itemList.get(0).getFieldByName(fields.get(fieldIdx));
			String fieldString = "";
			IPSFieldValue fieldValue = null;
			fieldValue = itemField.getValue();
			if(fieldValue != null){
				try {
					fieldString = fieldValue.getValueAsString();
					} catch (PSCmsException e) {
						valList.add("");
						e.printStackTrace();
					}
				valList.add(fieldString);
			}
			else{valList.add("");}
		}
		
		return valList;

	}
	
	public ArrayList<HashMap<String, String>> report_CustomLink(String folderCID, String subfolders){
		
		IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
		boolean includeSubfolders = Boolean.parseBoolean(subfolders);

		PSOObjectFinder psoObjFinder = new PSOObjectFinder();
		ArrayList<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
		IPSGuid folderGuid = null;
		IPSNode customLinkNode = null;
		PSLocationUtils locUtils = new PSLocationUtils();
		IPSNode folder = null;
		String folderPath = "";
        IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
	    IPSTemplateSlot slot = null;
		try {
			slot = asm.findSlotByName("cgvCustomLink");
		} catch (PSAssemblyException e3) {
			e3.printStackTrace();
		}
	    int slotidint = slot.getGUID().getUUID();
	    String slotid = Integer.toString(slotidint);

        	
		folderGuid = psoObjFinder.getGuidById(folderCID);

		try {
			folder = psoObjFinder.getNodeByGuid(folderGuid);
			if(folder != null){
				folderPath = locUtils.path(folder);
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		String qry = "select jcr:path, rx:sys_contentid, rx:sys_title, rx:override_title, rx:override_short_title, rx:override_long_description, rx:override_short_description from rx:cgvCustomLink where jcr:path like '"+folderPath+"%'";
		Query query = null;
		QueryResult qresults = null;
		
		try {
			query = contentMgr.createQuery(qry, Query.SQL);
			if(query != null){
				qresults = contentMgr.executeQuery(query, -1, null, null);
			}
			if(qresults != null){
				RowIterator rows = qresults.getRows();
				while(rows.hasNext()){
					HashMap<String, String> customLinkInfo = new HashMap<String, String>();
					Row row = rows.nextRow();
					String contentPath = row.getValue("jcr:path").getString();
					if(includeSubfolders == false && !folderPath.equals(contentPath)){
						continue;
					}
					else{
						customLinkInfo.put("cl_cid", getRowValueString(row, "rx:sys_contentid"));
						customLinkInfo.put("cl_sys_title", getRowValueString(row, "rx:sys_title"));
						customLinkInfo.put("cl_override_long_title", getRowValueString(row, "rx:override_title"));
						customLinkInfo.put("cl_override_short_title", getRowValueString(row, "rx:override_short_title"));
						customLinkInfo.put("cl_override_long_desc", getRowValueString(row, "rx:override_long_description"));
						customLinkInfo.put("cl_override_short_desc", getRowValueString(row, "rx:override_short_description"));
						
						//get target
						IPSGuid itemGuid = psoObjFinder.getGuidById(row.getValue("rx:sys_contentid").getString());

						try {
							customLinkNode = psoObjFinder.getNodeByGuid(itemGuid);
						} catch (RepositoryException e2) {
							e2.printStackTrace();
						}
						
						IPSSystemWs systemWebService = PSSystemWsLocator.getSystemWebservice();

						List<PSRelationship> rels = new ArrayList<PSRelationship>();
						PSRelationshipFilter filter = new PSRelationshipFilter();
						//This is going to be the current/edit revision for this content item.
						filter.setOwner(new PSLocator(customLinkNode.getGuid().getUUID()));
						filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
						
						try {
							rels = systemWebService.loadRelationships(filter);
						} catch (PSErrorException e1) {
							e1.printStackTrace();
						}
							
						IPSGuid depGuid = null;
					
						for(PSRelationship rel : rels){
							PSLocator dep =  rel.getDependent();
							if(rel.getProperty(IPSHtmlParameters.SYS_SLOTID).equals(slotid)){		
								depGuid = psoObjFinder.getGuidById(Integer.toString(dep.getId()));
							}
						}
						if(depGuid == null){
							customLinkInfo.put("tgt_sys_title", "");
							customLinkInfo.put("tgt_long_title", "");
							customLinkInfo.put("tgt_short_title", "");
							customLinkInfo.put("tgt_long_desc", "");
							customLinkInfo.put("tgt_short_desc", "");
							customLinkInfo.put("tgt_path", "");
							retList.add(customLinkInfo);
							continue;
						}
						IPSNode depNode = psoObjFinder.getNodeByGuid(depGuid);
						Long ctypeID = depNode.getProperty("rx:sys_contenttypeid").getLong();
						String ctypeName = "";
						try {
							ctypeName = itemDefMgr.contentTypeIdToName(ctypeID);
						} catch (PSInvalidContentTypeException e) {
							e.printStackTrace();
						}
						
						String tgt_qry = "select jcr:path, rx:sys_contentid, rx:sys_title, rx:long_title, rx:short_title, rx:long_description, rx:short_description from rx:"+ctypeName+" where rx:sys_contentid = "+depGuid.getUUID();
						Query tgt_query = null;
						QueryResult tgt_qresults = null;
						
						try {
							tgt_query = contentMgr.createQuery(tgt_qry, Query.SQL);
							if(tgt_query != null){
								tgt_qresults = contentMgr.executeQuery(tgt_query, -1, null, null);
							}
							if(tgt_qresults != null){
								RowIterator tgt_rows = tgt_qresults.getRows();
								while(tgt_rows.hasNext()){
									Row tgt_row = tgt_rows.nextRow();
									String tgtPath = getRowValueString(tgt_row, "jcr:path")+"/"+getRowValueString(tgt_row, "rx:sys_title");
									customLinkInfo.put("tgt_sys_title", getRowValueString(tgt_row, "rx:sys_title"));
									customLinkInfo.put("tgt_long_title", getRowValueString(tgt_row, "rx:long_title"));
									customLinkInfo.put("tgt_short_title", getRowValueString(tgt_row, "rx:short_title"));
									customLinkInfo.put("tgt_long_desc", getRowValueString(tgt_row, "rx:long_description"));
									customLinkInfo.put("tgt_short_desc", getRowValueString(tgt_row, "rx:short_description"));
									customLinkInfo.put("tgt_path", tgtPath);

								}
							}
							
						}
						catch (InvalidQueryException e5) {
							e5.printStackTrace();
						} catch (RepositoryException e5) {
							e5.printStackTrace();
						}
						
						//}	
						retList.add(customLinkInfo);
					}
				}
			}
		} catch (InvalidQueryException e4) {
			e4.printStackTrace();
		} catch (RepositoryException e4) {
			e4.printStackTrace();
		}

		return retList;
}

	public HashMap<String, HashMap<String, String>> report_FieldUsage(String folderCID, String subfolders, String contentType, String dataField){
		boolean includeSubfolders = Boolean.parseBoolean(subfolders);
		PSOObjectFinder psoObjFinder = new PSOObjectFinder();
		PSLocationUtils locUtils = new PSLocationUtils();
		HashMap<String, HashMap<String, String>> retList = new HashMap<String, HashMap<String, String>>();
		IPSGuid folderGuid = null;
		IPSNode folder = null;
		IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
		String folderPath = "";
		IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
		ArrayList<IPSGuid> guidList = new ArrayList<IPSGuid>();
		
		
		
		folderGuid = psoObjFinder.getGuidById(folderCID);

		try {
			folder = psoObjFinder.getNodeByGuid(folderGuid);
			if(folder != null){
				folderPath = locUtils.path(folder);
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		String qry = "select rx:sys_contentid, rx:sys_title, rx:"+dataField+", jcr:path from rx:"+contentType+" where jcr:path like '"+folderPath+"%'";
		Query query = null;
		QueryResult qresults = null;

		try {
			query = contentMgr.createQuery(qry, Query.SQL);
			if(query != null){
				qresults = contentMgr.executeQuery(query, -1, null, null);
			}
			if(qresults != null){
				RowIterator rows = qresults.getRows();
				while(rows.hasNext()){
					HashMap<String, String> fieldUsageInfo = new HashMap<String, String>();
					Row row = rows.nextRow();
					String contentPath = row.getValue("jcr:path").getString()+ "/" +row.getValue("rx:sys_title").getString();
					String itemFPath = cgvTools.getFolderPathFromItemPath(contentPath);
					if(includeSubfolders == false && !folderPath.equals(itemFPath)){
						continue;
					}
					else{
						fieldUsageInfo.put("content_id", row.getValue("rx:sys_contentid").getString());
						fieldUsageInfo.put("sys_title",row.getValue("rx:sys_title").getString());
						IPSGuid itemGuid = gmgr.makeGuid(row.getValue("rx:sys_contentid").getString(), PSTypeEnum.CONTENT);
						guidList.add(itemGuid);
						/*Value dataVal = row.getValue("rx:"+dataField);
						String dataString = "";
						if(dataVal != null){
							dataString = dataVal.getString();
						}
						fieldUsageInfo.put("data_field", dataString);*/
						fieldUsageInfo.put("pretty_url_name", contentPath);
						retList.put(fieldUsageInfo.get("content_id"), fieldUsageInfo);
					}
				}
			}
		} catch (InvalidQueryException e4) {
			e4.printStackTrace();
		} catch (RepositoryException e4) {
			e4.printStackTrace();
		}
		
		List<PSCoreItem> itemList = null;
		try {
			itemList = contentWS.loadItems(guidList, false, false, false, true);
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}
		for(PSCoreItem item : itemList){
			String cid = Integer.toString(item.getContentId());
			String dataString = "";
			PSItemField itemData = item.getFieldByName(dataField);
			IPSFieldValue dataVal = itemData.getValue();
			if(itemData != null){
				try {
					dataString = dataVal.getValueAsString();
				} catch (PSCmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			HashMap<String,String> itemInfo = retList.get(cid);
			itemInfo.put("data_field", dataString);
			retList.put(cid, itemInfo);
			
		}
		
		
		

		return retList;
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
								fieldMap.put("fieldName", fieldName);
								fieldMap.put("fieldControlRef", ftName);
								retMap.put(fieldLabel, fieldMap);
							}
						
						}
					}
				}
	
	
				return retMap;
	
				
		
	}

	public HashMap<String, String> getContentTypeNames(){
		HashMap<String, String> retMap = new HashMap<String, String>();
		PSItemDefManager iDefMgr = PSItemDefManager.getInstance();
		IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
		PSSecurityToken secToken = secWs.getSecurityToken();
		// cTypeIds = iDefMgr.getAllContentTypeIds(305);
		//cTypeIds = iDefMgr.getContentTypeIds(305);
		long[] cTypeIds = iDefMgr.getContentTypeIds(secToken);
		String cTypeName ="";
		String cTypeLabel ="";

		for(long cTypeId : cTypeIds){
			try {
				cTypeName = iDefMgr.contentTypeIdToName(cTypeId);
				cTypeLabel = iDefMgr.contentTypeIdToLabel(cTypeId);

			} catch (PSInvalidContentTypeException e) {
				e.printStackTrace();
			}
			retMap.put(cTypeLabel, cTypeName);
		}
		
		return retMap;
		
	}

	private String getRowValueString(Row row, String valueName){
		Value rVal = null;
		String retString = "";
		try {
			 rVal = row.getValue(valueName);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		if(rVal != null){
			try {
				retString = rVal.getString();
			} catch (ValueFormatException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
		
		return retString;
	}
}
