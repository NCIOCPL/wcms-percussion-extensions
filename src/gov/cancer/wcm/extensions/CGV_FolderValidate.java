package gov.cancer.wcm.extensions;
 
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;

import gov.cancer.wcm.util.CGVConstants;
import gov.cancer.wcm.util.CGV_ParentChildManager;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 

/**
 * This Validation extension checks to make sure the sys_title does not contain
 * spaces or special characters for a folder object.
 *
 * Below are the characters allowed:
 * A-Z, a-z, 0-9, "-" "_" "."
 * @author John Walls
 */
public class CGV_FolderValidate implements IPSFieldValidator
{
	private static Log LOGGER = LogFactory.getLog(CGV_TitlePopulate.class);
 
	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
	 */
	public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
	{
	
	    PSExtensionParams ep = new PSExtensionParams(params);

	    String value = ep.getStringParam(0, null, false);
		LOGGER.debug("******INVOKING Folder sys_title validation");
		
		if( Integer.parseInt(value) == 101 ){
			String systitle = request.getParameter("sys_title");
			if( systitle != null ){
				return validateFolder(systitle);
			}
			else
				return true;
		}
		else
		{
			return true;
		}
		

	}
 
	/**
	 * Validate that URL contains only a-b, A-B, 0-9, and "-" and "_"
	 * @param url to validate
	 * @return boolean true if valid
	 */
	private Object validateFolder(String url) {
		if( url.isEmpty() )
			return true;
		String regex = "[A-Za-z0-9\\-\\_\\.]*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		return m.matches();
	}

	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
      //
    }
}
 
