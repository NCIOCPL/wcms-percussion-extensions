/**
 * Field validator for use with folders to check that the sys_title
 * does not cause a conflict with a specified field in content items.  
 */
package gov.cancer.wcm.extensions;

import java.io.File;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;

/**
 * @author learnb
 *
 */
public class CGV_FolderNameUniqueValidator implements IPSFieldValidator {

	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
	 */
	@Override
	public Object processUdf(Object[] arg0, IPSRequestContext arg1)
			throws PSConversionException {

		// Get the folder's name.
		// Get the field we want to compare it to.
		// Search for content items which use that field and have the
		// field set to the name the folder wants to use.
		// Were any rows found?
		// Yes -- Fail.
		// No -- Success.
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
	 */
	@Override
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		// TODO Auto-generated method stub

	}

}
