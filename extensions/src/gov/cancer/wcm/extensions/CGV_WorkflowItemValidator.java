package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_ParentChildManager;
import gov.cancer.wcm.util.CGV_RelItem;
import gov.cancer.wcm.util.CGV_TypeNames;
import gov.cancer.wcm.workflow.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.webservices.PSErrorException;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;

public class CGV_WorkflowItemValidator extends PSOAbstractItemValidationExit {
	
	//protected static IPSGuidManager guidManager = null;
	//protected static IPSRxPublisherService publisherService = null;
	
	//protected static CGV_ParentChildManager pcm = null;

	private static Log log = LogFactory.getLog(CGV_WorkflowItemValidator.class);
	
	private static CGV_RelationshipHandlerService relationshipHandlerService;
	private static IPSContentMgr contentManagerService;
	private static IPSGuidManager guidManagerService; 
	private static PSOWorkflowInfoFinder workflowInfoFinder;	
	
	
	/*
	 * This initializes some of the different services
	 */
	static {
		contentManagerService = PSContentMgrLocator.getContentMgr();
		guidManagerService = PSGuidManagerLocator.getGuidMgr();
		relationshipHandlerService = CGV_RelationshipHandlerServiceLocator.getCGV_RelatoinshipHandlerService();
	    workflowInfoFinder = new PSOWorkflowInfoFinder();	    
	}
	
	/**
	 * Initializes a new instance of the CGV_WorkflowItemValidator
	 */
	public CGV_WorkflowItemValidator()
	{
	   super();	   
	}
	   
	/**
	 * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#validateDocs(org.w3c.dom.Document, org.w3c.dom.Document, com.percussion.server.IPSRequestContext, java.lang.Object[])
	 */
	@Override
	protected void validateDocs(Document inputDoc, Document errorDoc,
	      IPSRequestContext req, Object[] params) throws Exception
	{
	    String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
	    Validate.notEmpty(contentid);
	    String transitionid = req.getParameter(IPSHtmlParameters.SYS_TRANSITIONID);
	    Validate.notEmpty(transitionid);
	    String states = params[0].toString();
	    if(!isExclusive(req)) {
	    	setExclusive(req, true);
	    	if(super.matchDestinationState(contentid, transitionid, states))
	    	{
		    	log.debug("Testing if transition of item is allowed, valid state for test");
		    	ContentItemWFValidatorAndTransitioner validator = new ContentItemWFValidatorAndTransitioner(log);	    	
		    	validator.performTest(req, errorDoc);
	    	} 
	    	setExclusive(req, false);
	    }else {
	    	log.debug("Exclusion flag detected");
	    }	
	    
	} 
	
	 /**
	    * set the exclusion flag.
	    * 
	    * @param req the request context of the caller.
	    * @param b the new exclusion value. <code>true</code> means that
	    *           subsequent effects should not interfere with event processing.
	    */
	   protected void setExclusive(IPSRequestContext req, boolean b)
	   {
	      req.setPrivateObject(EXCLUSION_FLAG, b);
	   }

	   /**
	    * tests if the exclusion flag is on.
	    * 
	    * @param req the parent request context.
	    * @return <code>true</code> if the exclusion flag is set.
	    */
	   protected boolean isExclusive(IPSRequestContext req)
	   {
	      Boolean b = (Boolean) req.getPrivateObject(EXCLUSION_FLAG);
	      if (b == null)
	         return false;
	      return b.booleanValue();
	   }
	
	private static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.WorkflowItemValidator.PSExclusionFlag";
	private static final String ERR_FIELD = "TransitionValidation";
	private static final String ERR_FIELD_DISP = "TransitionValidation";
}
