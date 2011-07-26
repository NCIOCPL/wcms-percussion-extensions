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
import com.percussion.util.IPSHtmlParameters;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import gov.cancer.wcm.util.CGV_Logger;

/**
 * This Preprocessor extension populates the sys_title field by appending a 4 digit number
 * to the value in the display title and updates the value in last modified user field.
 * 
 * The NCI_TitlePopulate extends PSDefaultExtension class and 
 * implements IPSRequestPreProcessor interface.
 * 
 * @author Manvinder Singh modified by whole
 *
 */
public class CGV_TitlePopulate extends PSDefaultExtension implements
		IPSRequestPreProcessor {
	private static Logger LOGGER = CGV_Logger.getLogger(CGV_TitlePopulate.class);
	private static IPSContentWs cws = null;
	
	public CGV_TitlePopulate() {
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
		
        String cmd = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
        if (cmd != null && cmd.equalsIgnoreCase("modify")) {
		
			String fieldName = CGVConstants.DISPLAY_TITLE_FLD;
			
			if (params.length > 0 && StringUtils.isNotBlank(params[0].toString())) {
				fieldName = params[0].toString();
			}
			String displaytitle = request.getParameter(fieldName);
			
			LOGGER.debug("******INVOKING titlePopulate on " + displaytitle);
			
	
			//Pattern q = Pattern.compile("\\[#[0-9]{4}\\]");
			Pattern q = Pattern.compile(".*\\[#[0-9]{4}\\]");
			//Pattern q = Pattern.compile(displaytitle + "\\[#[0-9]{4}\\]");
			
			if(request.getParameter(fieldName) != null && request.getParameter("sys_title") != null){
				//Matcher m = q.matcher(displaytitle);
				Matcher m = q.matcher(request.getParameter("sys_title"));
				//Check to see if the sys_title already has [#XXXX] appended.
				String newTitle = "";
				if( !m.matches() ){	
					newTitle = modifyTitle(displaytitle, false, "");
				}
				else{
					String numbers = request.getParameter("sys_title").substring(
							request.getParameter("sys_title").length()-7, request.getParameter("sys_title").length());
					newTitle = modifyTitle(displaytitle, true, numbers);
				}
				request.setParameter("sys_title", newTitle);
			}
        }
	}

	/**
	 * Add a random number to the end of the title
	 * @param displayTitle the title to modify
	 * @param keepNumbers - should we keep the four numbers on the end of the title?
	 * @return String modified title
	 */
	private static String modifyTitle(String displayTitle, boolean keepNumbers, String numbers) {
		if(displayTitle != null){
			if(displayTitle.length() >= 248 ){
				displayTitle = displayTitle.substring(0, 247);
			}
		}
		int randNum=get4DigitRandomNumber();
		if(!keepNumbers){
			return displayTitle+"[#"+randNum+"]";
		}
		else{
			return displayTitle+numbers;
		}
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

	private static int get4DigitRandomNumber() {
		Random rand = new Random();
		int max = 9999;
		int min = 1000;
		int randomNum = rand.nextInt(max - min + 1) + min;
		return randomNum;
	}
}
