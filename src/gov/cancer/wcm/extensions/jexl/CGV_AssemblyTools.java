package gov.cancer.wcm.extensions.jexl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyItem;

public class CGV_AssemblyTools extends PSJexlUtilBase implements IPSJexlExpression{

	private static Log log = LogFactory.getLog(CGV_AssemblyTools.class);
	
	public CGV_AssemblyTools() {
		super();
	}
	
	@IPSJexlMethod(description = "gets the parent path given a path", params = {
			@IPSJexlParam(name = "path", description = "the path") })
	public String getParentFromPath(String path)
			throws RepositoryException 
	{
		Validate.notNull(path, "The path parameter cannot be null");		
		Validate.notEmpty(path, "The path parameter cannot be empty");
		Validate.isTrue(path.contains("/"), "The path parameter does not contain a / and therefore is an invalid path.");
		Validate.isTrue((!path.equals("/") && !path.equals("//")) , "Since the path is the root (/), there is no parent");
		
		if (path.endsWith("/"))
			path = path.substring(0,path.length()-2);
	
		if (path.lastIndexOf("/") == 0)
			path = "/";
		else
			path = path.substring(0, path.lastIndexOf("/"));
				
		return path;
	}

}
