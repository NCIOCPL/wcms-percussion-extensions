package gov.cancer.wcm.extensions.jexl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.jexl.PSOQueryTools;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;

public class CGV_AssemblyTools extends PSJexlUtilBase implements IPSJexlExpression{

	private static Log log = LogFactory.getLog(CGV_AssemblyTools.class);
	private static IPSContentMgr cmgr = null;
	
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
	
	@IPSJexlMethod(description = "Returns the number of pages for a dynamic auto slot", params = {
		@IPSJexlParam(name = "path", description = "The entire JSR query") })
	public int pagerCount(String path)
			throws RepositoryException 
	{
		if(cmgr == null)
		{
			cmgr = PSContentMgrLocator.getContentMgr();
		}
	
		
		
		//Query q = cmgr.createQuery(path, Query.SQL);
		PSOQueryTools pso = new PSOQueryTools();
		//QueryResult r =  cmgr.executeQuery(q, -1, null, null);
		//List<Map<String, Value>> eq = new ArrayList<Map<String, Value>>();
		return pso.executeQuery(path, -1, null, null).size();
		
		

	}

}
