package gov.cancer.wcm.extensions;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.util.IPSHtmlParameters;

public class CGV_MimeTypePopulate extends PSBaseServiceLocator implements
	IPSRequestPreProcessor {
	
    private Map<String, String> mimeTypeRules;
    private static final Log log = LogFactory.getLog(CGV_MimeTypePopulate.class);

    
	@Override
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		CGV_MimeTypePopulate beanObj = (CGV_MimeTypePopulate) getBean("CGV_MimeTypes"); 
		setMimeTypeRules(beanObj.getMimeTypeRules());
    	log.debug("CGV_MimeTypePopulate: end of init()");

	}

    public void setMimeTypeRules(Map<String, String> newMimeTypeRules){
    	this.mimeTypeRules = newMimeTypeRules;
    }
    
    public Map<String, String> getMimeTypeRules(){
    	return mimeTypeRules;
    }
    
	@Override
	public void preProcessRequest(Object[] params, IPSRequestContext request)
			throws PSAuthorizationException, PSRequestValidationException,
			PSParameterMismatchException, PSExtensionProcessingException {
		
		String extensionFieldName = params[0].toString();
		String mimeTypeFieldName = params[1].toString();
		log.debug("CGV_MimeTypePopulate: extField- " + extensionFieldName + ", mimeField- "+ mimeTypeFieldName);
		log.debug("CGV_MimeTypePopulate: ext- " + request.getParameter(extensionFieldName) + ", mime- "+ request.getParameter(mimeTypeFieldName));

        String cmd = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
        if (cmd != null && cmd.equalsIgnoreCase("modify")) {
        	log.debug("CGV_MimeTypePopulate: inside modify");

        	String fileExtension = request.getParameter(extensionFieldName);
        	if(mimeTypeRules.containsKey(fileExtension)){
            	log.debug("CGV_MimeTypePopulate: bean contains file extension");
        		request.setParameter(mimeTypeFieldName, mimeTypeRules.get(fileExtension));
        		log.debug("CGV_MimeTypePopulate: ext- " + request.getParameter(mimeTypeFieldName));
        		return;
        	}
        }
		// TODO Auto-generated method stub
		
	}



	
}