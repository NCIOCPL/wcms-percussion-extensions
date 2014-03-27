package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.*;
import org.apache.log4j.Logger;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * Populates the Navon's URL Folder Name with it's system title (minus special chars)
 * 
 * @author John Walls
 *
 */
public class CGV_NavonUrlPopulate extends PSDefaultExtension implements
		IPSRequestPreProcessor {
	//private static Logger LOGGER = CGV_Logger.getLogger(CGV_NavonUrlPopulate.class);
	private static IPSContentWs cws = null;
	
	public CGV_NavonUrlPopulate() {
		super();
		initServices();
	}

	/**
	 * This Preprocessor extension populates the sys_title field by appending a 4 digit number
 	 * to the value in the display title and update the value in last modified user field.
 	 * 
	 */
	public void preProcessRequest(Object[] params, IPSRequestContext request)
			throws PSAuthorizationException, PSRequestValidationException,
			PSParameterMismatchException, PSExtensionProcessingException {
		String systitle = request.getParameter("sys_title");
		String pretty = request.getParameter("pretty_url_folder_name");
		if(pretty.isEmpty() || pretty.length() == 0){
				request.setParameter("pretty_url_folder_name", systitle);
		}
		
//		String displaytitle = request.getParameter("sys_title");
//		//LOGGER.debug("******INVOKING titlePopulate");
//
//		Pattern q = Pattern.compile("[A-Za-z0-9\\-\\_\\.]*");
//		if(request.getParameter("sys_title") != null){
//			if(request.getParameter("pretty_url_folder_name") == null){
//				Matcher m = q.matcher(displaytitle);
//				if( !m.matches() ){
//
//					request.setParameter("pretty_url_folder_name", modifyTitle(displaytitle));
//				}
//				else{
//					request.setParameter("pretty_url_folder_name", displaytitle);
//				}
//			}
//		}
	}

	public boolean canModifyStyleSheet() {
		// TODO Auto-generated method stub
		return false;
	}

	private static void initServices() {
		if (cws == null) {
			cws = PSContentWsLocator.getContentWebservice();
		}
	}

}
