/**
 * 
 */
package gov.cancer.wcm.publishing;

import com.percussion.utils.guid.IPSGuid;

/**
 * @author doylejd
 *
 */
public class PublishItem {
	public IPSGuid folderGuid;
	public IPSGuid itemGuid;
	public int contentID;
	
	public PublishItem(IPSGuid folderGuid, IPSGuid itemGuid, int contentID){
		this.folderGuid = folderGuid;
		this.itemGuid = itemGuid;
		this.contentID = contentID;
	}
}
