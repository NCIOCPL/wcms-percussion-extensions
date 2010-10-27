package gov.cancer.wcm.util;

import gov.cancer.wcm.extensions.CGV_RelationshipHandlerService;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.util.logging.resources.logging;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;


/**
 */
public class CGV_RelItem{
	private static Log log = LogFactory.getLog(CGV_RelItem.class);
	private static IPSCmsContentSummaries sum;
	private static IPSWorkflowService wfService;
	private int id;
	private int wfState;
	private int wfId;
	private String wfStateName;
	private String wfName;
	private String checkedOutTo;
	private boolean isShared;
	private int upLevel=0;
	private int downLevel=0;
	private boolean upComplete;
	private boolean downComplete;
	private boolean wfFollow;
	private boolean statusInitialized;
	private boolean hasPubicRevision;
	private int communityid;
	private Set<Integer> parents;
	private Set<Integer> children;
	
	
	/**
	 * Constructor for CGV_RelItem.
	 * @param id int
	 */
	public CGV_RelItem(int id) {
		this.id=id;
	}
	/**
	 * Method getUpLevel.
	 * @return int
	 */
	public int getUpLevel() {
		return upLevel;
	}
	/**
	 * Method setUpLevel.
	 * @param upLevel int
	 */
	public void setUpLevel(int upLevel) {
		this.upLevel = upLevel;
	}
	/**
	 * Method getDownLevel.
	 * @return int
	 */
	public int getDownLevel() {
		return downLevel;
	}
	/**
	 * Method setDownLevel.
	 * @param downLevel int
	 */
	public void setDownLevel(int downLevel) {
		this.downLevel = downLevel;
	}
	/**
	 * Method getId.
	 * @return int
	 */
	public int getId() {
		return id;
	}
	/**
	 * Method setId.
	 * @param id int
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * Method getWfState.
	 * @return int
	 */
	public int getWfState() {
		initStatus();
		return wfState;
	}
	
	/**
	 * Method getWfId.
	 * @return int
	 */
	public int getWfId() {
		initStatus();
		return wfId;
	}
	
	/**
	 * Method getCheckedOutTo.
	 * @return String
	 */
	public String getCheckedOutTo() {
		initStatus();
		return checkedOutTo;
	}
	
	/**
	 * Method isShared.
	 * @return boolean
	 */
	public boolean isShared() {
		return isShared;
	}
	/**
	 * Method setShared.
	 * @param isShared boolean
	 */
	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}
	
	/**
	 * Method hashCode.
	 * @return int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	/**
	 * Method equals.
	 * @param obj Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CGV_RelItem other = (CGV_RelItem) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	

	/**
	 * Method setParents.
	 * @param parents Set<Integer>
	 */
	public void setParents(Set<Integer> parents) {
		this.parents = parents;
	}
	/**
	 * Method toString.
	 * @return String
	 */

	@Override
	public String toString() {
		return "CGV_RelItem [id=" + id + ", wfState=" + wfState + ", wfId="
				+ wfId + ", wfStateName=" + wfStateName + ", wfName=" + wfName
				+ ", checkedOutTo=" + checkedOutTo + ", isShared=" + isShared
				+ ", upLevel=" + upLevel + ", downLevel=" + downLevel
				+ ", upComplete=" + upComplete + ", downComplete="
				+ downComplete + ", wfFollow=" + wfFollow
				+ ", statusInitialized=" + statusInitialized
				+ ", hasPubicRevision=" + hasPubicRevision + ", communityid="
				+ communityid + ", parents=" + parents + ", children="
				+ children + "]";
	}
	/**
	 * Method getParents.
	 * @return Set<Integer>
	 */
	public Set<Integer> getParents() {
		return parents;
	}
	/**
	 * Method setChildren.
	 * @param children Set<Integer>
	 */
	public void setChildren(Set<Integer> children) {
		this.children = children;
	}
	/**
	 * Method getChildren.
	 * @return Set<Integer>
	 */
	public Set<Integer> getChildren() {
		return children;
	}
	/**
	 * Method isTop.
	 * @return boolean
	 */
	public boolean isTop() {
		return (parents!=null && parents.size()==0);
	}
	/**
	 * Method isUpComplete.
	 * @return boolean
	 */
	public boolean isUpComplete() {
		return upComplete;
	}
	/**
	 * Method setUpComplete.
	 * @param upComplete boolean
	 */
	public void setUpComplete(boolean upComplete) {
		this.upComplete = upComplete;
	}
	/**
	 * Method isDownComplete.
	 * @return boolean
	 */
	public boolean isDownComplete() {
		return downComplete;
	}
	/**
	 * Method setDownComplete.
	 * @param downComplete boolean
	 */
	public void setDownComplete(boolean downComplete) {
		this.downComplete = downComplete;
	}
	/**
	 * Method setWfFollow.
	 * @param wfFollow boolean
	 */
	public void setWfFollow(boolean wfFollow) {
		this.wfFollow = wfFollow;
	}
	/**
	 * Method isWfFollow.
	 * @return boolean
	 */
	public boolean isWfFollow() {
		return wfFollow;
	}

	public void initStatus() {
	  if (!statusInitialized) {
		  resetStatus();
	  }		
	}
	
	private static void initServices() {
		if (sum == null) {
			sum = PSCmsContentSummariesLocator.getObjectManager();
			wfService = PSWorkflowServiceLocator.getWorkflowService();
		}
	}
	
	public void resetStatus() {
		   initServices();
		   log.debug("resetting item status");
		   PSComponentSummary summary = sum.loadComponentSummary(id);
		   this.wfState = summary.getContentStateId();
		   this.wfId = summary.getWorkflowAppId();		   
		  
		   PSWorkflow workflow = wfService.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW,this.wfId));
		   PSState state = wfService.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,this.wfState),
				   	new PSGuid(PSTypeEnum.WORKFLOW,this.wfId));
		   this.wfStateName=state.getName();
		   this.wfName=workflow.getName();
		   this.communityid=summary.getCommunityId();
		   this.checkedOutTo = summary.getCheckoutUserName();
		   if (summary.getPublicRevision() > 0) this.hasPubicRevision=true;
		   statusInitialized=true;
		   log.debug("done resetting item status");
	}
	/**
	 * Method getCommunityid.
	 * @return int
	 */
	public int getCommunityid() {
		initStatus();
		return communityid;
	}
	/**
	 * Method hasPubicRevision.
	 * @return boolean
	 */
	public boolean hasPubicRevision() {
		initStatus();
		return hasPubicRevision;
	}
	/**
	 * Method getWfStateName.
	 * @return String
	 */
	public String getWfStateName() {
		return wfStateName;
	}
	/**
	 * Method getWfName.
	 * @return String
	 */
	public String getWfName() {
		return wfName;
	}
	/**
	 * Method setWfState.
	 * @param wfState int
	 */
	public void setWfState(int wfState) {
		this.wfState = wfState;
	}
	
}
