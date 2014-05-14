/**
 *	Simple object to contain a user-oriented summary of information about a single content item. 
 */
package gov.cancer.wcm.privateArchive;

/**
 * @author learnb
 *
 */
public class ContentSummary {
	private String _title;
	private String _workflowState;
	private int _contentId;
	
	@Override
	public String toString(){
		return _title + " (" + _contentId + ") " + _workflowState;
	}
	
	public String getTitle(){
		return _title;
	}
	
	public void setTitle(String title){
		_title = title;
	}
	
	public String getWorkflowState(){
		return _workflowState;
	}
	
	public void setWorkflowState(String workflowState){
		_workflowState = workflowState;
	}
	
	public int getContentId(){
		return _contentId;
	}
	
	public void setContentId(int contentId){
		_contentId = contentId;
	}
	
	public ContentSummary(String title, String workflowState, int contentId){
		_title = title;
		_workflowState = workflowState;
		_contentId = contentId;
	}
}
