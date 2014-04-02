package gov.cancer.wcm.privateArchive;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.error.PSException;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.IPSWebServicesErrors;
import com.percussion.server.webservices.PSWebServicesRequestHandler;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;

import gov.cancer.wcm.workflow.validators.RequireUIWarningIgnoreCondition;

/*
 * CRUFT ALERT!!!!  THIS CLASS DUPLICATES gov.cancer.wcm.workflow.PercussionWFTransition.java
 */

public class PrivateArchiveWFTransition {

	public PrivateArchiveWFTransition(){
		
	}

	public static void transitionItem(int id, String trigger, String comment, 
			List<String> addhocUsers) throws PSErrorException 
			{ 
		PSWebServicesRequestHandler ws = PSWebServicesRequestHandler 
		.getInstance(); 

		PSRequest req = (PSRequest) PSRequestInfo 
		.getRequestInfo(PSRequestInfo.KEY_PSREQUEST); 

		HashMap oldParams = req.getParameters(); 
		req.setParameters(new HashMap()); 

		String addhocList = null; 
		// concatenate a list of user names with ';' delimiter 
		if (addhocUsers != null && (!addhocUsers.isEmpty())) 
		{ 
			StringBuffer users = new StringBuffer(); 
			for (int i = 0; i < addhocUsers.size(); i++) 
			{ 
				if (i > 0) 
					users.append(";"); 
				users.append(addhocUsers.get(i)); 
			} 
			addhocList = users.toString(); 
		} 

		try 
		{ 
			req.setParameter(IPSHtmlParameters.SYS_CONTENTID, Integer 
					.toString(id)); 
			req.setParameter(RequireUIWarningIgnoreCondition.UI_WARNING_TRIGGER_FLAG, "T"); 
			ws.transitionItem(req, trigger, comment, addhocList); 
		} 
		catch (PSException e) 
		{ 
			int errorCode = IPSWebserviceErrors.FAILED_TRANSITION_ITEM; 
			PSErrorException error = new PSErrorException(errorCode,
					PSWebserviceErrors.createErrorMessage(errorCode, trigger, id), 
					ExceptionUtils.getFullStackTrace(e)); 
			throw error; 
		} 
		finally 
		{ 
			req.setParameters(oldParams); 
		} 
			} 

	public void transitionItem(PSRequest request, String transition, 
			String comment, String adhocList) throws PSException 
			{ 
		String path = getContentEditorURL(request); 

		request.setParameter(IPSHtmlParameters.SYS_COMMAND, "workflow"); 
		request.setParameter("WFAction", transition); 

		// the will get around the fact that it "needs" a 
		// WFAction as a name, we put the transition(Id) there and over 
		// in the exit we check for a numeric value, if it is numeric, 
		// we call a different constructor 

		if (comment != null && comment.trim().length() > 0) 
			request.setParameter("commenttext", comment); 

		if (adhocList != null && adhocList.trim().length() > 0) 
			request.setParameter("sys_wfAdhocUserList", adhocList); 

		resetValidationError(request); 
		PSInternalRequest iReq = makeInternalRequest(request, path); 
		checkValidationError(iReq.getRequest(), path); 
			} 


	/** 
	 * Get the content editor url for a specific content item. 
	 * 
	 * @param request the original request, assumes that <code>sys_contentid 
	 *    </code> is within the parameter list of the request 
	 * 
	 * @return the url where the content type editor is located, used for 
	 *    internal requests, never <code>null</code> 
	 * 
	 * @throws PSInternalRequestCallException 
	 * @throws PSException if the content type id cannot be found 
	 */ 
	protected String getContentEditorURL(PSRequest request) 
	throws PSInternalRequestCallException, PSException 
	{ 
		PSInternalRequest ir = 
			PSServer.getInternalRequest( 
					CONTENT_EDITOR_CATALOGER, 
					request, 
					null, 
					true); 
		if (ir == null) 
		{ 
			throw new PSException( 
					IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED, 
					CONTENT_EDITOR_CATALOGER); 
		} 
		else 
		{ 
			// make the internal request and extract the URL from the result XML 
			Element root = ir.getResultDoc().getDocumentElement(); 
			NodeList nl = root.getChildNodes(); 
			for (int i = 0; i < nl.getLength(); i++) 
			{ 
				Node node = nl.item(i); 
				String name = PSXMLDomUtil.getUnqualifiedNodeName(node); 
				if (name.equalsIgnoreCase("query")) 
				{ 
					String url = PSXMLDomUtil.getElementData(node); 
					if (url != null && url.trim().length() != 0) 
					{ 
						return url; 
					} 
				} 
			} 
		} 
		Object args[] = new Object[2]; 
		args[0] = request.getParameter(IPSHtmlParameters.SYS_CONTENTID); 
		args[1] = request.getParameter(IPSHtmlParameters.SYS_REVISION); 

		// content id not found 
		throw new PSException( 
				IPSWebServicesErrors.WEB_SERVICE_CONTENT_ITEM_NOT_FOUND, 
				args); 
	} 




	/** 
	 * Check the validation error that is registered in the given request. 
	 * 
	 * @param request The request that may contains the validation error, 
	 *    it may not be <code>null</code>. 
	 * 
	 * @param path the resource path that is used for the request, it may 
	 *    not be <code>null</code> or empty. 
	 *     
	 * @throws PSCmsException if an validation error has occured. 
	 */ 
	protected void checkValidationError(PSRequest request, String path) 
	throws PSCmsException 
	{ 
		if (request == null) 
			throw new IllegalArgumentException("request may not be null"); 
		if (path == null || path.trim().length() == 0) 
			throw new IllegalArgumentException("path may not be null or empty"); 

		String validateError = request.getParameter( 
				IPSHtmlParameters.SYS_VALIDATION_ERROR); 
		if (validateError != null && validateError.trim().length() > 0) 
		{ 
			PSConsole.printMsg(getClass().getName(), 
					IPSCmsErrors.VALIDATION_ERROR, 
					new Object[] {path, validateError}); 
			throw new PSCmsException(IPSCmsErrors.VALIDATION_ERROR, 
					new Object[] {path, validateError}); 
		} 
	} 

	/** 
	 * Helper function to execute an internal request. 
	 * 
	 * @param request the original request object, assumed not <code>null</code> 
	 * @param path the application and resource location of the action to be 
	 *    executed by the system, assumed not <code>null</code> 
	 * 
	 * @return PSInternalRequest the internal request that was generated, never 
	 *    <code>null</code>, may contain a modified request object 
	 * 
	 * @throws PSException if the internal request is not created 
	 */ 
	protected static PSInternalRequest makeInternalRequest( 
			PSRequest request, 
			String path) 
	throws PSException 
	{ 
		PSInternalRequest iReq = 
			PSServer.getInternalRequest(path, request, null, true); 
		if (iReq == null) 
		{ 
			throw new PSException( 
					IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED, 
					path); 
		} 
		handleOverrideCommunity(request);       
		iReq.performUpdate(); 

		return iReq; 
	} 

	/** 
	 * Checks to see if the override community param is set in 
	 * the request and if so overrides to that community. 
	 * @param request the request, cannot be <code>null</code>. 
	 * @throws PSException when encountering any error. 
	 */ 
	protected static void handleOverrideCommunity(PSRequest request) 
	throws PSException 
	{ 
		if(null == request) 
			throw new IllegalArgumentException("Request cannot be null."); 

		String usersComm = (String)request.getUserSession().getPrivateObject( 
				IPSHtmlParameters.SYS_COMMUNITY); 
		if(usersComm == null) 
			usersComm = "";   

		// call verify community only attempting to override 
		String commOverride = request.getParameter( 
				IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID); 
		if (commOverride != null && commOverride.trim().length() > 0 && 
				!usersComm.trim().equals(commOverride.trim())) 
		{ 
			PSServer.verifyCommunity(request); 
		} 
	} 

	/** 
	 * Reset the validation error in the given request. This is used 
	 * in conjuction with the {@link #checkValidationError(PSRequest)}. 
	 * 
	 * @param request The request object, it may not be <code>null</code>. 
	 */   
	protected void resetValidationError(PSRequest request) 
	{ 
		if (request == null) 
			throw new IllegalArgumentException("request may not be null"); 

		request.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR, null); 
	} 

	/** 
	 * Name of the internal resource used to catalog content editor URLs. 
	 */ 
	private static final String CONTENT_EDITOR_CATALOGER = 
		"sys_psxContentEditorCataloger/getUrl.xml"; 

}



