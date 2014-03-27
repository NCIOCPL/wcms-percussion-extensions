/**
 * 
 */
package gov.cancer.wcm.logging;


import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;

import com.percussion.utils.jdbc.PSConnectionHelper;

/**
 * Manages the data access layer for Purge Logging.
 *  
 * @author learnb
 *
 */
public class PurgeLoggingDataAccess {

	/** Record the details of a single Percussion content item.
	 * 
	 * @param contentID Percussion content ID of the content item being purged. 
	 * @param itemTitle sys_title of the content item being purged.
	 * @param purgedBy User ID performing the purge
	 * @param workflowState Workflow state at time of purge.
	 * @param folderPaths Path(s) within Percussion where the content item was stored.
	 */
	public void LogItemState(int contentID,
				String itemTitle,
				String purgedBy,
				String workflowState,
				List<String> folderPaths){
	
		try {
			// We're going to put this in Percussion's database,
			// so we let Percussion create the connection.
			Connection conn = PSConnectionHelper.getDbConnection();

			String folderParam = joinStrings(folderPaths, ',');
			
			CallableStatement proc = conn.prepareCall("{call NCI_AddPurgeLogEntry(?,?,?,?,?)}");
			proc.setInt("@PurgedContentID", contentID);
			proc.setString("@itemTitle", itemTitle);
			proc.setString("@purgeBy", purgedBy);
			proc.setString("@workflowState", workflowState);
			proc.setString("@folderPathList", folderParam);
			proc.execute();
			conn.close();
		} catch (Exception e) {
			log.error("Couldn't get database connection: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Utility method to combine the elements of a List<String> into
	 * a single delimited String object.
	 * 
	 * @param stringList Collection of String objects to join.
	 * @param separator	The delimiter between successive values.
	 * @return a String made by appending the individual strings
	 * contained in stringList.  If stringList contains one or zero entries,
	 * the return value does not contain separator.  If stringList contains
	 * zero entries, the empty string is returned. 
	 */
	private String joinStrings(List<String> stringList, char separator){

		StringBuilder sb = new StringBuilder();
		
		boolean sbHasContents = false;
		for(String entry : stringList){
			if(sbHasContents){
				sb.append(separator);
			}
			sb.append(entry);
			sbHasContents = true;
		}
		
		return sb.toString();
	}
	
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(PurgeLoggingDataAccess.class);

}
