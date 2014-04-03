package gov.cancer.wcm.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.util.IPSHtmlParameters;

public class CGV_SortDatePopulate extends PSBaseServiceLocator implements
		IPSRequestPreProcessor {
	
	private Map<String, String> sortDateRules;
	private static String DEFAULT_SORT_DATE_SOURCE_FIELD = "date_first_published";
	private static String SORT_DATE_FIELD_NAME = "sort_date";
	private PSONodeCataloger nodeCat = new PSONodeCataloger();


	@Override
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		CGV_SortDatePopulate beanObj = (CGV_SortDatePopulate) getBean("CGV_SortDate");
		setSortDateRules(beanObj.getSortDateRules());		
	}
	
	public Map<String, String> getSortDateRules() {
		return sortDateRules;
	}

	public void setSortDateRules(Map<String, String> newSortDateRules) {
		this.sortDateRules = newSortDateRules;
	}
	
	
	/*
	 * This method does a lookup in the map of sort date rules for the 
	 * provided content type.  If the content type is not in the map 
	 * it returns the default field name.
	 */
	public String getSortDateSourceFieldName(String contentTypeName){
		String srcFieldName = sortDateRules.get(contentTypeName);
		if(srcFieldName == null){
			srcFieldName = DEFAULT_SORT_DATE_SOURCE_FIELD;
		}
		return srcFieldName;
	}
	
	
	@Override
	public void preProcessRequest(Object[] params, IPSRequestContext request)
			throws PSAuthorizationException, PSRequestValidationException,
			PSParameterMismatchException, PSExtensionProcessingException {
		
		// check for the existence of both the command and the sortDate field -
		// certain requests (clone specifically) can generate an internal request
		// that lacks many of the expected parameters
		String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
		String SDFContents = request.getParameter(SORT_DATE_FIELD_NAME);
		
		if(command == null || SDFContents == null) {
			return;
		}

		//Check if this is creation rather than an update.  If this is on creation we continue.
		//If this is an update we leave the field alone.
		if(!command.equalsIgnoreCase("modify")){
			return;
		}
		//Check if sortDate field already contains a value.  If it does, do nothing.
		if(SDFContents.length()!=0){
			return;
		}
		//If sort date field does not have a value
		
		//Get Content Type
		//The getRequestPage(Boolean) call returns the part of the url being 
		//called that corresponds to the content type name.
		//The false argument tells it not to return the file extension.
		String contentTypeName = request.getRequestPage(false);
		
		//Get the name of the field that will be the source for the sortDate
		//see the getSortDateSourceFieldName(String) method above
		String srcField = getSortDateSourceFieldName(contentTypeName);
		
		//Check that the source field exists.  If it does not, or does not have a value, return
		
		if(!hasField(contentTypeName, srcField)){
			return;
		}
	
		if(!hasField(contentTypeName, SORT_DATE_FIELD_NAME)){
			return;
		}
		
		//Get value from source field and copy the value to the source date field
		request.setParameter(SORT_DATE_FIELD_NAME, request.getParameterObject(srcField));

	}

	
	/*
	 * This method provides a simple way to see if a content type has a field by name
	 */
	private boolean hasField(String contentTypeName, String fieldName){
		List<String> fieldNameList = new ArrayList<String>();
		try {
			fieldNameList = nodeCat.getFieldNamesForContentType(contentTypeName);
		} catch (NoSuchNodeTypeException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		//Loop through all of the fieldNames of the content type and return
		//true if and only if the field name we are checking is in the list.
		return fieldNameList.contains(fieldName) || fieldNameList.contains("rx:"+fieldName);

		
	}

}
