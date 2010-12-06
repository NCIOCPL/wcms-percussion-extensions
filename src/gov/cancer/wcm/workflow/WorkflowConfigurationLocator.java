package gov.cancer.wcm.workflow;

import com.percussion.services.PSBaseServiceLocator;

/**
 */
public class WorkflowConfigurationLocator extends PSBaseServiceLocator
{
	   /**
	    * Private constructor 
	    */
	   private WorkflowConfigurationLocator()
	   {
		   
	      
	   }

	   /**
	    * Returns an instance of Service class for publishing.
	    * 
	   
	    * @return instance of CGV_OnDemandPublishService */
	   public static WorkflowConfiguration getWorkflowConfiguration(){ 
		   return (WorkflowConfiguration) getBean(WORKFLOW_CONFIGURATION_BEAN_NAME);
	   }
	   
	   /**
	    * String representing name of the Service class
	    */
	   private static final String WORKFLOW_CONFIGURATION_BEAN_NAME = "WorkflowConfiguration"; 
}