package gov.cancer.wcm.workflow;

public class ContentTypeConfig {
	private boolean isTopType;
	private boolean requiresParentNavonsPublic;
	
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
	
	public boolean getRequiresParentNavonsPublic(){
		return this.requiresParentNavonsPublic;
	}
	
	
	private ContentTypeConfig(String name, boolean isTopType, boolean requiresParentNavonsPublic) {
		this.name = name;
		this.isTopType = isTopType;
		this.requiresParentNavonsPublic = requiresParentNavonsPublic;
	}
}
