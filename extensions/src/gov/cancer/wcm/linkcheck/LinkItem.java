/**
 * 
 */
package gov.cancer.wcm.linkcheck;

/**
 * Data container for link items and http response status
 * @author holewr
 *
 */
public class LinkItem {
	private String contentId;
	private String sysTitle;
	private String url;
	private String message;	//response message, i.e. "OK"
	private int response;	//response code, i.e. 400
	
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getSysTitle() {
		return sysTitle;
	}
	public void setSysTitle(String sysTitle) {
		this.sysTitle = sysTitle;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getResponse() {
		return response;
	}
	public void setResponse(int response) {
		this.response = response;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
