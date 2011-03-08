package gov.cancer.wcm.util;

import java.util.List;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;

/**
 */
public class CGV_RelationshipConfig {
	private String name;
	private int maxUp=1;
	private int maxDown=1;
	private boolean wfFollow;
	private List<Long> parentTypes;
	private List<Long> childTypes;
	private List<String> relationshipNames;
	private List<String> parentRels;
	private List<String> childRels;
	/**
	 * Method getMaxUp.
	 * @return int
	 */
	public int getMaxUp() {
		return maxUp;
	}
	/**
	 * Method setMaxUp.
	 * @param maxUp int
	 */
	public void setMaxUp(int maxUp) {
		this.maxUp = maxUp;
	}
	/**
	 * Method getMaxDown.
	 * @return int
	 */
	public int getMaxDown() {
		return maxDown;
	}
	/**
	 * Method setMaxDown.
	 * @param maxDown int
	 */
	public void setMaxDown(int maxDown) {
		this.maxDown = maxDown;
	}
	/**
	 * Method isWfFollow.
	 * @return boolean
	 */
	public boolean isWfFollow() {
		return wfFollow;
	}
	/**
	 * Method setWfFollow.
	 * @param wfFollow boolean
	 */
	public void setWfFollow(boolean wfFollow) {
		this.wfFollow = wfFollow;
	}
	/**
	 * Method getParentTypes.
	 * @return List<String>
	 * @throws PSInvalidContentTypeException
	 */
	public List<String> getParentTypes() throws PSInvalidContentTypeException {
		return (parentTypes==null) ? null : CGV_TypeNames.getTypeNames(parentTypes);
	}
	/**
	 * Method getParentTypeIds.
	 * @return List<Long>
	 * @throws PSInvalidContentTypeException
	 */
	public List<Long> getParentTypeIds() throws PSInvalidContentTypeException {
		return parentTypes;
	}
	/**
	 * Method setParentTypes.
	 * @param parentTypes List<String>
	 * @throws PSInvalidContentTypeException
	 */
	public void setParentTypes(List<String> parentTypes) throws PSInvalidContentTypeException {
		this.parentTypes = (parentTypes==null) ? null : CGV_TypeNames.getTypeIds(parentTypes);
	}
	/**
	 * Method getChildTypes.
	 * @return List<String>
	 * @throws PSInvalidContentTypeException
	 */
	public List<String> getChildTypes() throws PSInvalidContentTypeException {
		return (childTypes==null) ? null : CGV_TypeNames.getTypeNames(childTypes);
	}
	/**
	 * Method getChildTypeIds.
	 * @return List<Long>
	 * @throws PSInvalidContentTypeException
	 */
	public List<Long> getChildTypeIds() throws PSInvalidContentTypeException {
		return childTypes;
	}
	/**
	 * Method setChildTypes.
	 * @param childTypes List<String>
	 * @throws PSInvalidContentTypeException
	 */
	public void setChildTypes(List<String> childTypes) throws PSInvalidContentTypeException {
		this.childTypes = (childTypes==null) ? null :  CGV_TypeNames.getTypeIds(childTypes);
	}
	
	
	/**
	 * Method getRelationshipNames.
	 * @return List<String>
	 */
	public List<String> getRelationshipNames() {
		return relationshipNames;
	}
	/**
	 * Method setRelationshipNames.
	 * @param relationshipNames List<String>
	 */
	public void setRelationshipNames(List<String> relationshipNames) {
		this.relationshipNames = relationshipNames;
	}
	/**
	 * Method getParentRels.
	 * @return List<String>
	 */
	public List<String> getParentRels() {
		return parentRels;
	}
	/**
	 * Method setParentRels.
	 * @param parentRels List<String>
	 */
	public void setParentRels(List<String> parentRels) {
		this.parentRels = parentRels;
	}
	/**
	 * Method getChildRels.
	 * @return List<String>
	 */
	public List<String> getChildRels() {
		return childRels;
	}
	/**
	 * Method setChildRels.
	 * @param childRels List<String>
	 */
	public void setChildRels(List<String> childRels) {
		this.childRels = childRels;
	}
	/**
	 * Method setName.
	 * @param name String
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Method getName.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	
	
}
