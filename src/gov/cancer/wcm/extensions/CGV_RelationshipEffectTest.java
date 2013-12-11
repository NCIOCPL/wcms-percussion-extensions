package gov.cancer.wcm.extensions;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.error.PSRuntimeException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.relationship.effect.PSEffectUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.webservices.PSErrorException;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * This a test of relationship effects and can go away at some point.
 * @author bpizzillo
 *
 */
@PSHandlesEffectContext(required={PSEffectContext.PRE_WORKFLOW})
public class CGV_RelationshipEffectTest extends PSEffect {

	private static Log log = LogFactory.getLog(CGV_WorkflowItemValidator.class);


	@Override
	public void attempt(
			Object[] params, 
			IPSRequestContext request,
			IPSExecutionContext context, 
			PSEffectResult result
	)
	throws PSExtensionProcessingException, PSParameterMismatchException {
		result.setSuccess();
	}

	@Override
	public void recover(
			Object[] params, 
			IPSRequestContext request,
			IPSExecutionContext context, 
			PSExtensionProcessingException processException,
			PSEffectResult result
	) throws PSExtensionProcessingException {
		// TODO Auto-generated method stub

	}

	@Override
	public void test(
			Object[] params, 
			IPSRequestContext request,
			IPSExecutionContext context, 
			PSEffectResult result
	)
	throws PSExtensionProcessingException, PSParameterMismatchException 
	{
		//This will get called for any changes to a relationship,
		//however, we only want to fire for isPreWorkflow
		if (!context.isPreWorkflow()) {
			result.setWarning(
					"This effect is active only during relationship workflow");
			return;
		}


		//Checks to see if we have already processed this relationship.

		boolean doesExclusiveExist = true;
		if(request.getPrivateObject(EXCLUSION_FLAG) != null){
			doesExclusiveExist = true;
		}
		else{
			doesExclusiveExist = false;
		}

		if(doesExclusiveExist){
			result.setWarning(
					"The exclusive flag exists for the transition.");
			return;
		}

		/**
		 * request... get initiator and transition, pass into our stuff.
		 * 
		 * Private object, exclusive: check workflow states, see if the exclusive is in the execution context
		 * if exclusive is flagged, we dont need to call this.
		 * CALL: wfvalidator and transitioner
		 * if exclusive, dont call effect
		 * Does PRIVATE exclusive object exist???, setexclusive, isexclusive.
		 * 
		 */

		//		PSRelationship currRel = context.getCurrentRelationship();		
		//		if (context.getProcessedRelationships() != null) {
		//			for(Object processedRel : context.getProcessedRelationships()) {
		//				
		//				PSLocator currOwner = currRel.getOwner();
		//				PSLocator processedOwner = ((PSRelationship)processedRel).getOwner();
		//				PSLocator currDep = currRel.getDependent();
		//				PSLocator processedDep = ((PSRelationship)processedRel).getDependent();
		//				
		//				if (
		//					(currOwner.getId() == processedOwner.getId() &&	currDep.getId() == processedDep.getId())
		//					|| (currOwner.getId() == processedDep.getId() 
		//							&& currDep.getId() == processedOwner.getId() 
		//							&& currRel.getConfig().getType() == ((PSRelationship)processedRel).getConfig().getType()
		//					)
		//				) {
		//					result.setWarning("Skip: already processed same owner/dependent.");
		//					return;
		//				}			
		//			}			
		//		}		

		String wfAction = request.getParameter("WFAction", "").trim();
		if ((wfAction == null) || (wfAction.length() == 0))
		{
			result.setWarning("No WFAction?");
			return;
		}

		System.out.println("The exclusive flag does not exist. Calling the transitioner and validator workflow code.");
		Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
		ContentItemWFValidatorAndTransitioner validator = new ContentItemWFValidatorAndTransitioner(log);
//		if(!ContentItemWFValidatorAndTransitioner.isExclusive(request)){
		if(request.getParameter(NCI_EFFECT_FLAG) != "true"){
			ContentItemWFValidatorAndTransitioner.setExclusive(request, true);
			try {
				validator.performTest(request, errorDoc);
				if(docHasErrors(errorDoc)){
					result.setError("Could not transition.  See log for more info.");
				}
				else{
					result.setSuccess();
				}
			} catch (PSException e) {
				result.setError(e);
				e.printStackTrace();
			} catch (PSErrorException e) {
				result.setError(e.getErrorMessage());
				e.printStackTrace();
			}
			ContentItemWFValidatorAndTransitioner.setExclusive(request, false);
		}

		//result.setSuccess();
		//result.setWarning("Finished running the effect");
		return;
	}

	private static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.WorkflowItemValidator.PSExclusionFlag";
	private static final String NCI_EFFECT_FLAG = "gov.cancer.wcm.extensions.WorkflowItemValidator.NCI_EFFECT_FLAG";
	
	private boolean docHasErrors(Document errorDoc){
	     Element root = errorDoc.getDocumentElement();
	      if(root == null)
	      {
	         return false;
	      }
	      PSXmlTreeWalker w = new PSXmlTreeWalker(root);
	      Element e = w.getNextElement(PSItemErrorDoc.ERROR_FIELD_SET_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
	      if(e == null)
	      {
	         return false;
	      }
	      e = w.getNextElement(PSItemErrorDoc.ERROR_FIELD_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
	      if(e == null)
	      {
	         return false;
	      }
	      return true; 
	}

}
