package gov.cancer.wcm.extensions;
 
import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import gov.cancer.wcm.util.CGVConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 

/**
 * This Validation extension checks to make sure the pretty
 * URL shall not allow for encoded characters for the Published 
 * Page MetaData shared Field.
 *
 * Below are the characters allowed:
 * A-Z, a-z, 0-9, "-" "_" "."
 * @author John Walls
 */
public class CGV_URLEncoding implements IPSFieldValidator
{
	private static Log LOGGER = LogFactory.getLog(CGV_TitlePopulate.class);
 
	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
	 */
	public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
	{
  
		
		LOGGER.debug("******INVOKING friendlyURLencoding validation");
		  
		String friendlyURL = request.getParameter(CGVConstants.FRIENDLY_URL_FLD);
		if( friendlyURL != null ){
			return validatePrettyUrl(friendlyURL);
		}
		else
			return true;
	}
 
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
      //
    }

	/**
	 * Validate that URL contains only a-b, A-B, 0-9, and "-" and "_"
	 * @param url to validate
	 * @return boolean true if valid
	 */
	private static boolean validatePrettyUrl(String url) {
		if( url.isEmpty() )
			return true;
		String regex = "[A-Za-z0-9\\-\\_\\.]*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		return m.matches();
	}
}
 
