package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Random;

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
	private static Log LOGGER = LogFactory.getLog(CGV_TitlePopulate.class);
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
		String displaytitle = request.getParameter(CGVConstants.DISPLAY_TITLE_FLD);
		LOGGER.debug("******INVOKING titlePopulate");
		String sysTitle = modifyTitle(displaytitle);
		request.setParameter("sys_title", sysTitle);
	}

	/**
	 * Add a random number to the end of the title
	 * @param displayTitle the title to modify
	 * @return String modified title
	 */
	private static String modifyTitle(String displayTitle) {
		int randNum=get4DigitRandomNumber();
		String sysTitle=displayTitle+"[#"+randNum+"]";
		return sysTitle;
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
