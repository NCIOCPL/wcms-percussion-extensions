package gov.cancer.wcm.extensions;

import java.io.File;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
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
import com.percussion.workflow.PSWorkFlowUtils;

/**
 * This a test of relationship effects and can go away at some point.
 * @author bpizzillo
 *
 */
public class CGV_RelationshipEffectTest extends PSEffect {


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
		//Dunno if this is needed, but it is in AA_Mandatory...
		PSRelationship currRel = context.getCurrentRelationship();		
		if (context.getProcessedRelationships() != null) {
			for(Object processedRel : context.getProcessedRelationships()) {
				
				PSLocator currOwner = currRel.getOwner();
				PSLocator processedOwner = ((PSRelationship)processedRel).getOwner();
				PSLocator currDep = currRel.getDependent();
				PSLocator processedDep = ((PSRelationship)processedRel).getDependent();
				
				if (
					(currOwner.getId() == processedOwner.getId() &&	currDep.getId() == processedDep.getId())
					|| (currOwner.getId() == processedDep.getId() 
							&& currDep.getId() == processedOwner.getId() 
							&& currRel.getConfig().getType() == ((PSRelationship)processedRel).getConfig().getType()
					)
				) {
					result.setWarning("Skip: already processed same owner/dependent.");
				}			
			}			
		}
				
		
	    String wfAction = request.getParameter("WFAction", "").trim();
	    if ((wfAction == null) || (wfAction.length() == 0))
	    {
	      result.setWarning("No WFAction?");
	      return;
	    }
	    
	    PSLocator initiator = null;
	    
	    //Find who started this
	    if (context.getActivationEndPoint() == context.RS_ENDPOINT_OWNER) {
	    	initiator = currRel.getOwner();
	    } else {
	    	initiator = currRel.getDependent();
	    }
	    
	    result.setSuccess();
		//result.setError("This is an error");
	}

}
