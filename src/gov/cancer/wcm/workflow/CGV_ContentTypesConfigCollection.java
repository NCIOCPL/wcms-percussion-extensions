package gov.cancer.wcm.workflow;

import java.util.Map;

public class CGV_ContentTypesConfigCollection {
	private Map<String, CGV_ContentTypeConfig> contentTypes;

	/**
	 * @param contentTypes the contentTypes to set
	 */
	public void setContentTypes(Map<String, CGV_ContentTypeConfig> contentTypes) {
		this.contentTypes = contentTypes;
	}

	/**
	 * @return the contentType
	 */
	public CGV_ContentTypeConfig getContentType(String contentTypeName) {
		return contentTypes.get(contentTypeName);
	}
	
	
}
