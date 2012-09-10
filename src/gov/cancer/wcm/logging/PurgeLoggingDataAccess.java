/**
 * 
 */
package gov.cancer.wcm.logging;


import gov.cancer.wcm.linkcheck.LinkDataAccess;

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

	public void LogItemState(int contentID){
	
		try {
			Connection conn = PSConnectionHelper.getDbConnection();
			
			String query = format("insert into nci_purgelog(contentid,purgeDate) "
							+"values({0}, getdate())", ""+contentID);
			//Statement stmt = conn.createStatement();
			//stmt.execute(query);

			CallableStatement proc = conn.prepareCall("{call NCI_AddPurgeLogEntry(?)}");
			proc.setInt(1, contentID);
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
            .getLog(LinkDataAccess.class);

}
