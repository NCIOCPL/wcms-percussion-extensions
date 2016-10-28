package gov.cancer.wcm.util;

public class SiteProtocolConfig {
	
	private int id;
	private String name;
	private String hostname;
	private boolean isHttps;
	
	/* static defines */
	private static String PROTOCOL_HTTP = "http://";
	private static String PROTOCOL_HTTPS = "https://";

	/**
	 * Retrieves the site ID of the configuration.
	 * @return An int representing the Rhythmyx Site ID.
	 */
	public int getId(){
		return this.id;
	}
	
	/**
	 * Returns the site name of the configuration.
	 * @return A string matching the name under //Sites in Rhythmyx.
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Gets the hostname (without protocol)
	 * @return A string containing only the hostname.
	 */
	public String getHostname() {
		return this.hostname;
	}
	
	/**
	 * Indicates if the site is using the HTTPS protocol.
	 * @return true for HTTPS, else false for HTTP.
	 */
	public boolean isHttps() {
		return this.isHttps;
	}
	
	/**
	 * Gets the appropriate protocol string for the site.
	 * @return a String containing either the HTTPS or HTTP protocol prefix.
	 */
	public String getProtocol() {
		if(isHttps()) {
			return PROTOCOL_HTTPS;
		}
		else {
			return PROTOCOL_HTTP;
		}
	}
	
	/**
	 * Gets the complete url for the site, including protocol.
	 * @return A String containing the protocol and hostname.
	 */
	public String getUrl() {
		return getProtocol() + getHostname();
	}
	
	/**
	 * Constructor for a SiteProtocolConfig.
	 * @param id int site id
	 * @param name String site name
	 * @param hostname String site hostname
	 * @param isHttps boolean site HTTPS flag
	 */
	private SiteProtocolConfig(int id, String name, String hostname, boolean isHttps) {
		this.id = id;
		this.name = name;
		this.hostname = hostname;
		this.isHttps = isHttps;
	}
}
