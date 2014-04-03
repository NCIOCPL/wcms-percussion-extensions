package gov.cancer.wcm.extensions.Newsletter;
import com.percussion.services.PSBaseServiceLocator;

/**
 *	 
 *	This class represents the factory design pattern.  It returns an instantiated instance of 
 *	NCI_NewsletterFolderService.
 *
 * 	@author John Walls
 *
 * @version $Revision: 1.0 $
 */
public class NCI_NewsletterFolderLocator extends PSBaseServiceLocator
{
   /**
    * Private constructor 
    */
   private NCI_NewsletterFolderLocator()
   {
   }

   /**
    * Returns an instance of Service class for publishing.
    * 
   
    * @return instance of NCI_NewsletterFolderService */
   public static NCI_NewsletterFolderService getNCI_NewsletterFolderService(){ 
	   
	   System.out.println("DEBUG: Getting the bean...");
	   return (NCI_NewsletterFolderService) getBean(NCINEWSLETTERFOLDERSERVICEBEAN);
   }
   
   /**
    * String representing name of the Service class
    */
   private static final String NCINEWSLETTERFOLDERSERVICEBEAN = "NCI_NewsletterFolderService"; 
}
