package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_ParentChildManager;
import gov.cancer.wcm.util.CGV_TopTypeChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;


import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;


/**
 * Queues items and their parents for on-demand publishing
 * @author whole based on mudumby
 *
 */
public class CGV_ParentChildService implements InitializingBean {
	private static final Log log = LogFactory.getLog(CGV_ParentChildService.class);
	private boolean bDebug = true;	//if true print statements to console
//TODO: set bDebug to false when done debugging
	
	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;

	private Map<String,Map<String,Map<String, String>>> transition;

	public Map<String, Map<String, Map<String, String>>> getTransition() {
		return transition;
	}

	public void setTransition(
			Map<String, Map<String, Map<String, String>>> transition) {
		this.transition = transition;
	}

	/**
	 * Initialize service pointers.
	 * 
	 * @param cmgr
	 */
	protected static void initServices() {
		if (rps == null) {
			rps = PSRxPublisherServiceLocator.getRxPublisherService();
			gmgr = PSGuidManagerLocator.getGuidMgr();
			cmgr = PSContentWsLocator.getContentWebservice();
			pcm = new CGV_ParentChildManager();
		}
	}

	public CGV_ParentChildService() {
	}

	/**
	 * Initialize services.
	 * 
	 * @param extensionDef
	 * @param codeRoot
	 * @throws PSExtensionException
	 */
	public void init(IPSExtensionDef extensionDef, File codeRoot)
	throws PSExtensionException {
//		log.debug("Initializing CGV_OnDemandPublishService...");
		if (bDebug) System.out.println("Initializing CGV_ParentChildService...");
	}

	/**
	 * 
	 */
	public void afterPropertiesSet() throws Exception {
		initServices();
	}


	public String getCurrState(int tranID) {
		Map<String,Map<String,String>> m = transition.get("CancerGov Workflow");
		Map<String,String> mm = m.get(Integer.toString(tranID));
		return mm.get("fromState");
	}

	public String getDestState(int tranID) {
		Map<String,Map<String,String>> m = transition.get("CancerGov Workflow");
		Map<String,String> mm = m.get(Integer.toString(tranID));
		return mm.get("toState");
	}
	
	public String getTrigger(int tranID) {
		Map<String,Map<String,String>> m = transition.get("CancerGov Workflow");
		Map<String,String> mm = m.get(Integer.toString(tranID));
		return mm.get("trigger");
	}


}
