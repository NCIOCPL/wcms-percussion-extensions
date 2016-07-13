package gov.cancer.wcm.util;

import java.util.List;

public class SiteProtocolConfigCollection {
    private List<SiteProtocolConfig> siteProtocolConfigs;
    private SiteProtocolConfig defaultConfig = null;

    /**
     * Gets the site protocol configuration for a given site ID, or return null
     * if not found
     * 
     * @param siteName
     *            the name of the site in Rhythmyx
     * @return the site protocol configuration, else null if name not found
     */
    public SiteProtocolConfig getProtocolFromSiteName(String siteName) {
        SiteProtocolConfig retConfig = defaultConfig;
        
        for (SiteProtocolConfig config : siteProtocolConfigs) {
            if (siteName.equals(config.getName())) {
                retConfig = config;
                break;
            }
        }

        return retConfig;
    }

    /**
     * Gets the site protocol configuration for a given site ID, or return null
     * if not found
     * 
     * @param siteId
     *            the numeric id of the site in Rhythmyx
     * @return the site protocol configuration, else null if name not found
     */
    public SiteProtocolConfig getProtocolConfigFromSiteId(int siteId) {
        SiteProtocolConfig retConfig = defaultConfig;
        
        for (SiteProtocolConfig config : siteProtocolConfigs) {
            if (siteId == config.getId()) {
                retConfig = config;
                break;
            }
        }

        return retConfig;
    }

    /**
     * 
     * @param contentTypeConfigs
     * @param defaultConfig
     */
    public SiteProtocolConfigCollection(
            List<SiteProtocolConfig> siteProtocolConfigs, String defaultName) {
        this.siteProtocolConfigs = siteProtocolConfigs;
        this.defaultConfig = getProtocolFromSiteName(defaultName);
    }
}
