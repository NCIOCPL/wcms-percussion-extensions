package gov.cancer.wcm.workflow;

public class ContentTypeConfig {
	private boolean isTopType;
	private String name;
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return the isTopType
	 */
	public boolean getIsTopType() {
		return this.isTopType;
	}
	
	private ContentTypeConfig(String name, boolean isTopType) {
		this.name = name;
		this.isTopType = isTopType;
	}
}
