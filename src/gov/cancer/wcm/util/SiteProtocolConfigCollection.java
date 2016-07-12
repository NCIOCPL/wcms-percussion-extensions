package gov.cancer.wcm.util;

import java.util.List;

public class SiteProtocolConfigCollection {
    private List<SiteProtocolConfig> siteProtocolConfigs;

    /**
     * Gets the site protocol configuration for a given site ID, or return null
     * if not found
     * 
     * @param siteName
     *            the name of the site in Rhythmyx
     * @return the site protocol configuration, else null if name not found
     */
    public SiteProtocolConfig getProtocolFromSiteName(String siteName) {
        for (SiteProtocolConfig config : siteProtocolConfigs) {
            if (siteName.equals(config.getName())) {
                return config;
            }
        }

        return null;
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
        for (SiteProtocolConfig config : siteProtocolConfigs) {
            if (siteId == config.getId()) {
                return config;
            }
        }

        return null;
    }

    /**
     * 
     * @param contentTypeConfigs
     * @param defaultConfig
     */
    public SiteProtocolConfigCollection(
            List<SiteProtocolConfig> siteProtocolConfigs) {
        this.siteProtocolConfigs = siteProtocolConfigs;
    }
}
