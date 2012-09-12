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
	 */
	public void LogItemState(int contentID,
				String itemTitle,
				String purgedBy,
				String workflowState){
	
		try {
			// We're going to put this in Percussion's database,
			// so we let Percussion create the connection.
			Connection conn = PSConnectionHelper.getDbConnection();
			
			CallableStatement proc = conn.prepareCall("{call NCI_AddPurgeLogEntry(?,?,?,?)}");
			proc.setInt("@PurgedContentID", contentID);
			proc.setString("@itemTitle", itemTitle);
			proc.setString("@purgeBy", purgedBy);
			proc.setString("@workflowState", workflowState);
			proc.execute();
			conn.close();
		} catch (Exception e) {
			log.error("Couldn't get database connection: " + e.getLocalizedMessage());
		}
	}
	
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(PurgeLoggingDataAccess.class);

}
