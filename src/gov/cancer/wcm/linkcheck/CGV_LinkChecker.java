/**
 * 
 */
package gov.cancer.wcm.linkcheck;

import gov.cancer.wcm.util.SiteProtocolConfig;
import gov.cancer.wcm.util.SiteProtocolConfigCollection;
import gov.cancer.wcm.util.SiteProtocolConfigCollectionLocator;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class checks the status returns when accessing URLs
 * @author holewr
 *
 */
public class CGV_LinkChecker {

	/**
	 * Test this URL and return the status code and message
	 * @param url URL to chekc
	 * @param message status message is returned in this string
	 * @return the status code
	 */
	public static LinkResult checkLink(String url) {
		LinkResult linkResult = new LinkResult();
		
		try {
			//if url lacks the host, add it (assume gancer.gov)
			if (!(url.length() > 5 && url.substring(0,4).equalsIgnoreCase("http"))) {
			    SiteProtocolConfigCollection configs = 
		            SiteProtocolConfigCollectionLocator.getSiteProtocolConfigCollection();
		        
		        SiteProtocolConfig config = configs.getProtocolFromSiteName("CancerGov");
		        String siteUrl = config.getUrl();
			    
				url = siteUrl + url;
			}
			URL u = new URL(url);
			HttpURLConnection conn = null;
			conn = (HttpURLConnection)u.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(false);
			conn.setDoOutput(true);
			conn.connect();
			linkResult.setCode(conn.getResponseCode());
			linkResult.setMessage(conn.getResponseMessage());
			//TODO: do we need to do the header analysis that the old one does?
		}
		catch (MalformedURLException e) {
			linkResult.setCode(400);
			log.error("The url " + url + " was malformed");
		}
		catch (Exception e) {
			//what to do here?
			log.error("An error occurred while checking url " + url + " : " + e.getLocalizedMessage());
		}
		return linkResult;
	}
	
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(CGV_LinkChecker.class);

}
