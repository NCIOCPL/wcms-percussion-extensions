package gov.cancer.wcm.workflow;

import java.util.List;

public class ContentTypesConfigCollection {
	private List<ContentTypeConfig> contentTypeConfigs;
	private ContentTypeConfig defaultConfig;
	
	/**
	 * Gets the content type configuration for a given content type, or
	 * return the default configuration
	 * @return the contentType
	 */
	public ContentTypeConfig getContentTypeOrDefault(String contentTypeName) {
		ContentTypeConfig rtnConfig = defaultConfig;
		
		for (ContentTypeConfig config : contentTypeConfigs) {
			if (contentTypeName.equals(config.getName())) {
				rtnConfig = config;
				continue;
			}
		}
		
		return rtnConfig;
	}
	
	/**
	 * 
	 * @param contentTypeConfigs
	 * @param defaultConfig
	 */
	public ContentTypesConfigCollection(
			List<ContentTypeConfig> contentTypeConfigs,
			ContentTypeConfig defaultConfig
	) {
		this.contentTypeConfigs = contentTypeConfigs;
		this.defaultConfig = defaultConfig;
	}
}
