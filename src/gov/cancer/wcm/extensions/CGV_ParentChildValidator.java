package gov.cancer.wcm.extensions;

import gov.cancer.wcm.util.CGV_ParentChildManager;
import gov.cancer.wcm.util.CGV_RelItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.error.PSException;
import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;

/**
 */
public class CGV_ParentChildValidator extends PSOAbstractItemValidationExit {

	

	protected static IPSGuidManager gmgr = null;
	protected static IPSRxPublisherService rps = null;
	protected static IPSContentWs cmgr = null;
	protected static CGV_ParentChildManager pcm = null;

	private static Log log = LogFactory.getLog(CGV_ParentChildValidator.class);
	private static CGV_RelationshipHandlerService rhs;
	private static PSOWorkflowInfoFinder winfo;
	   /**
	    * 
	    */
	   public CGV_ParentChildValidator()
	   {
	      super();
	      initServices();
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
	    	 performTest(req,errorDoc);
	      } 
	      setExclusive(req, false);
	      }else {
	    	  log.debug("Exclusion flag detected");
	      }
	   }


	   
	   /**
	    * Method performTest.
	    * @param request IPSRequestContext
	    * @param errorDoc Document
	    * @throws PSException
	    */
	   public void performTest(IPSRequestContext request,Document errorDoc)
		throws PSException {
		   initServices();
			// TODO Auto-generated method stub

			log.debug("Parent/child Validator: Calling extension...");

			String transition = request.getParameter("sys_transitionid");
			String currCID = request.getParameter("sys_contentid");
			 int id = Integer.parseInt(currCID);
			
			
			     Set<CGV_RelItem> items;
				try {
					items = rhs.findRelations(Collections.singletonList(id));
				
			     Set<CGV_RelItem> transitionItems = new HashSet<CGV_RelItem>();
			     Set<CGV_RelItem> noTransitionItems = new HashSet<CGV_RelItem>();
				 
			     CGV_RelItem currItem=null;
			     
			     
				for(CGV_RelItem item : items) {
					if (item.getId()==id) {
						currItem = item;
					} else 	if (item.isWfFollow()) {
						transitionItems.add(item);
					} else  {
						noTransitionItems.add(item);
					}	
					
				
					item.initStatus();
					log.debug("Found item "+item);
		       }
				
			
				
			    PSState destState = winfo.findDestinationState(currCID, transition);
				if (rhs.preventAncestorPull(currItem, destState))  {
					//Preventing child item from pulling parent,  just check if children are in correct state for move.
					noTransitionItems.addAll(transitionItems);
					transitionItems.clear();
				}
			    String wfName = winfo.findWorkflow(currItem.getWfId()).getName();
			    
				boolean transitionOk = true;
				transitionOk =  transitionOk && rhs.checkNavonState(items ,errorDoc);
				log.debug("valid child state transitionOk="+transitionOk);
				transitionOk =  transitionOk && rhs.isValidChildState(wfName, destState, noTransitionItems ,errorDoc);
				log.debug("valid child state transitionOk="+transitionOk);
				transitionOk =  transitionOk &&rhs.checkCheckoutState(transitionItems ,errorDoc);
				log.debug("check checkout state transitionOk="+transitionOk);
				transitionOk =  transitionOk && rhs.checkTransition(wfName, destState, currItem,transitionItems ,errorDoc);
				
				log.debug("Checks completed with transitionOk="+transitionOk);
				
				
				} catch (PSErrorException e) {
					PSItemErrorDoc.addError(errorDoc, ERR_FIELD, ERR_FIELD_DISP, "Error getting related ids for item with id {0}", new Object[]{id});	
				}
			
			
		

		}	


		
	
		/**
		 * Method canModifyStyleSheet.
		 * @return boolean
		 * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
		 */
		public boolean canModifyStyleSheet() {
			return false;
		}

		private static void initServices() {
			if (rps == null) {
				rhs = CGV_RelationshipHandlerServiceLocator.getCGV_RelatoinshipHandlerService();
			    winfo = new PSOWorkflowInfoFinder();
			}
		}

		
		
		/**
		 * Method getRelatedItemIds.
		 * @param contentid int
		 * @return List<Integer>
		 */
		List<Integer> getRelatedItemIds(int contentid) {
			List<Integer> rels = new ArrayList<Integer>();
			
			return rels;
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
		private static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.ParentChildValidator.PSExclusionFlag";
		private static final String ERR_FIELD = "TransitionValidation";
		private static final String ERR_FIELD_DISP = "TransitionValidation";
		 
		

}
