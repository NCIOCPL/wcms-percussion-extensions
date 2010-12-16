/**
 * 
 */
package gov.cancer.wcm.linkcheck;

import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.utils.jdbc.PSConnectionHelper;

/**
 * This class provides methods for fetching nciLink content items from the Percussion
 * database, and for storing records describing problematic URLs into the database.
 * @author holewr
 *
 */
public class LinkDataAccess {
    private IPSContentMgr contentManager = null;
	
	/**
	 * Constructor with IPSContentMgr
	 * @param contentMgr
	 */
	public LinkDataAccess(IPSContentMgr contentMgr) {
		contentManager = contentMgr;
	}
	
	/**
	 * Get an array of all nciLink content items in the system.
	 * @param path root path to search under
	 * @return array of nciLink items
	 * @throws Exception
	 */
	public ArrayList<LinkItem> getLinkItems(String path) throws Exception {
		ArrayList<LinkItem> itemList = new ArrayList<LinkItem>();
		try {
			String jcrQuery = format(
					"select rx:sys_contentid, rx:sys_title, rx:url from rx:nciLink " +
					"where jcr:path like ''{0}'' OR jcr:path like ''{0}/%''", path);
			log.debug("jcrQuery" + jcrQuery);
			Query q = contentManager.createQuery(jcrQuery, Query.SQL);
			QueryResult results = contentManager.executeQuery(q, -1, null, null);
			RowIterator rows = results.getRows();
			while (rows.hasNext()) {
				LinkItem item = new LinkItem();
				Row row = (Row)rows.next();
				Value urlValue = row.getValue("rx:url");
				String strUrlValue = urlValue.getString();
				item.setUrl(strUrlValue);
				Value idValue = row.getValue("rx:sys_contentid");
				String strIdValue = idValue.getString();
				item.setContentId(strIdValue);
				Value titleValue = row.getValue("rx:sys_title");
				String strTitleValue = titleValue.getString();
				item.setSysTitle(strTitleValue);
				itemList.add(item);
			}
		}
		catch (Exception e) {
			log.error("Failure in executing JCR query: " + e.getLocalizedMessage());
			throw e;
		}
		return itemList;
	}
	
	/**
	 * Store the array of problematic link items into the database
	 * @param itemArray array of iffy links
	 * @return true if success
	 */
	public boolean saveBadLinkItems(ArrayList<LinkItem> itemArray) {
		boolean res = true;
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = PSConnectionHelper.getDbConnection();
		}
		catch (Exception e) {
			log.error("Couldn't get database connection: " + e.getLocalizedMessage());
			return false;
		}
		try {
			String query = "delete from CGVLINKCHECK";
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch (Exception e) {
			log.error("Error clearing CGVLINKCHECK table: " + e.getLocalizedMessage());
			//we don't set res = false, we'll keep going
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (Exception e) {}
			}
		}
		PreparedStatement pstmt = null;;
		try {
			for (LinkItem item : itemArray) {
				String query = "insert into CGVLINKCHECK(contentid, url, message, response, lastupdate) values(?, ?, ?, ?, ?)";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, item.getContentId());
				pstmt.setString(2, item.getUrl());
				pstmt.setString(3, item.getMessage());
				pstmt.setInt(4, item.getResponse());
				pstmt.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
				pstmt.executeUpdate();
			}
		}
		catch (Exception e) {
			log.error("Error inserting data into CGVLINKCHECK table: " + e.getLocalizedMessage());
			res = false;
		}
		finally {
			if (stmt != null) {
				try {
					pstmt.close();
				}
				catch (Exception e) {}
			}
		}
		return res;
	}
	
	/**
	 * Returns true if the CGVLINKCHECK table already exists
	 * @return
	 */
	public boolean reportTableExists() {
		boolean exists = true;
		try {
			Connection conn = PSConnectionHelper.getDbConnection();
			DatabaseMetaData dmd = conn.getMetaData();
			ResultSet rs = dmd.getTables(null, null, "CGVLINKCHECK", null);
			exists = rs.next();
		}
		catch (Exception e) {
			log.error("Error in checking for table existence: " + e.getLocalizedMessage());
		}
		return exists;
	}
	
	/**
	 * Create the CGVLINKCHECK table
	 * @return true if successful
	 */
	public boolean createTable() {
		boolean result = true;
		Statement stmt = null;
		try {
			Connection conn = PSConnectionHelper.getDbConnection();
			String query = "create table CGVLINKCHECK " +
			"(contentid varchar(10) not null, " +
			"url varchar(256), " +
			"message varchar(100), " +
			"response integer, " +
			"lastupdate datetime)";
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch (Exception e) {
			log.error("Error in creating table: " + e.getLocalizedMessage());
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (Exception e) {}
			}
		}
		return result;
	}
	
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(LinkDataAccess.class);

}
