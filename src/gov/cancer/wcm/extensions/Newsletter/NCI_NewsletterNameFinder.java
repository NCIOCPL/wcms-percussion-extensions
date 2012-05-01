package gov.cancer.wcm.extensions.Newsletter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import gov.cancer.wcm.extensions.jexl.CGV_AssemblyTools;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;

public class NCI_NewsletterNameFinder extends PSJexlUtilBase implements IPSJexlExpression{
	
	CGV_AssemblyTools aTools = new CGV_AssemblyTools();
	IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();

	@IPSJexlMethod(description = "Gets the Newsletter name from the Section name of the navon <folderLevels> levels above it", params = {
			@IPSJexlParam(name = "path", description = "The folder path of the current item"),
			@IPSJexlParam(name = "folderLevels", description = "the number of levels the navon with the Newsletter name is above the item")})
	public String getNewsletterName(String path, int folderLevels){
		while(folderLevels > 0){
			try {
				path = aTools.getParentFromPath(path);
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
			folderLevels -= 1;
		}
		
		String navtitle = "";
		String qry = "select rx:nav_title, jcr:path from rx:rffNavon where jcr:path = '"+ path + "'";
		Query query = null;
		QueryResult qresults = null;
		
			try {
				query = contentMgr.createQuery(qry, Query.SQL);

				if(query != null){
					qresults = contentMgr.executeQuery(query, -1, null, null);
				}
				if(qresults != null){
					RowIterator rows = qresults.getRows();
					if(rows.hasNext()){
						Row row = rows.nextRow();
						Value navtitleval = row.getValue("rx:nav_title");
						if(navtitleval != null){
							navtitle = navtitleval.getString();
						}
					}
				}
			} catch (InvalidQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		
		
		
		return navtitle;
	}

	@IPSJexlMethod(description = "Gets the Newsletter title from the long_title of the newsletter issue", params = {
			@IPSJexlParam(name = "path", description = "The folder path of the current item"),
			@IPSJexlParam(name = "folderLevels", description = "the number of levels the navon with the Newsletter name is above the item")
			})
	public String getIssueTitle(String path, int folderLevels){
		while(folderLevels > 0){
			try {
				path = aTools.getParentFromPath(path);
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
			folderLevels -= 1;
		}
		
		String newsletterTitle = "";
		String qry = "select rx:long_title, jcr:path from rx:genNewsletterDetails where jcr:path = '"+ path + "'";
		Query query = null;
		QueryResult qresults = null;
		
			try {
				query = contentMgr.createQuery(qry, Query.SQL);

				if(query != null){
					qresults = contentMgr.executeQuery(query, -1, null, null);
				}
				if(qresults != null){
					RowIterator rows = qresults.getRows();
					if(rows.hasNext()){
						Row row = rows.nextRow();
						Value newsletterTitleVal = row.getValue("rx:long_title");
						if(newsletterTitleVal != null){
							newsletterTitle = newsletterTitleVal.getString();
						}
					}
				}
			} catch (InvalidQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		
		
		
		return newsletterTitle;
	}
}
