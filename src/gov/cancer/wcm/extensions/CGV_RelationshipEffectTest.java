package gov.cancer.wcm.extensions;

import gov.cancer.wcm.workflow.ContentItemWFValidatorAndTransitioner;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.error.PSRuntimeException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.effect.PSEffectUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.webservices.PSErrorException;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * This a test of relationship effects and can go away at some point.
 * @author bpizzillo
 *
 */
public class CGV_RelationshipEffectTest extends PSEffect {
	
	private static Log log = LogFactory.getLog(CGV_WorkflowItemValidator.class);


	@Override
	public void attempt(
			Object[] params, 
			IPSRequestContext request,
			IPSExecutionContext context, 
			PSEffectResult result
	)
			throws PSExtensionProcessingException, PSParameterMismatchException 
	{
		// TODO Auto-generated method stub
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
//		Boolean b = null;
		try {
			if(request.getPrivateObject(EXCLUSION_FLAG) != null){
				doesExclusiveExist = true;
			}
			else{
				doesExclusiveExist = false;
			}
		} catch (PSRuntimeException e) {
			doesExclusiveExist = false;
			e.printStackTrace();
		}

//		if (b == null){
//			doesExclusiveExist = false;
//		}
//		else{
//			doesExclusiveExist = true;
//		}

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
		
		//PSRelationship currRel = context.getCurrentRelationship();		

	    String wfAction = request.getParameter("WFAction", "").trim();
	    if ((wfAction == null) || (wfAction.length() == 0))
	    {
	      result.setWarning("No WFAction?");
	      return;
	    }
	    
	    System.out.println("The exclusive flag does not exist. Calling the transitioner and validator workflow code.");
	    Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
    	ContentItemWFValidatorAndTransitioner validator = new ContentItemWFValidatorAndTransitioner(log);
    	ContentItemWFValidatorAndTransitioner.setExclusive(request, true);
	    try {
			validator.performTest(request, errorDoc);
	    } catch (PSException e) {
	    	e.printStackTrace();
		} catch (PSErrorException e) {
			e.printStackTrace();
		}
		ContentItemWFValidatorAndTransitioner.setExclusive(request, false);
	    
	    result.setSuccess();
		//result.setError("This is an error");
	}
	
	private static final String EXCLUSION_FLAG =  "gov.cancer.wcm.extensions.WorkflowItemValidator.PSExclusionFlag";

}
