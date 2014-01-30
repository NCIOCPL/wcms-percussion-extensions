package gov.cancer.wcm.extensions.Newsletter;

import java.io.File;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;


/**
 * Wraps a configurable list of folder names to create (arranged by site)
 * into a configurable java bean.
 *
 */
public class NCI_NewsletterFolderService implements InitializingBean {
	private static final Log log = LogFactory.getLog(NCI_NewsletterFolderService.class);

	private Map<String, Map<String,String>> folderNames;	//mapping with the folders and the slots
	private String aggroWidgetTemplate;		//Name of the Sn Template aggro widgets use to go into the slots
	private String landingPageTemplate;		//Name of the Sn template used in the navLandingPageSlot
	private List<String> navonTranstions;	//List of transitions to make a navon public
	private List<String> pageTranstions;	//List of transitions to make a page public
	
	public List<String> getNavonTranstions() {
		return navonTranstions;
	}

	public void setNavonTranstions(List<String> navonTranstions) {
		this.navonTranstions = navonTranstions;
	}

	public List<String> getPageTranstions() {
		return pageTranstions;
	}

	public void setPageTranstions(List<String> pageTranstions) {
		this.pageTranstions = pageTranstions;
	}

	public String getAggroWidgetTemplate() {
		return aggroWidgetTemplate;
	}

	public void setAggroWidgetTemplate(String aggroWidgetTemplate) {
		this.aggroWidgetTemplate = aggroWidgetTemplate;
	}

	public String getLandingPageTemplate() {
		return landingPageTemplate;
	}

	public void setLandingPageTemplate(String landingPageTemplate) {
		this.landingPageTemplate = landingPageTemplate;
	}

	public Map<String, Map<String,String>> getFolderNames() {
		return folderNames;
	}

	public void setFolderNames(Map<String, Map<String,String>> folderNames) {
		this.folderNames = folderNames;
	}
	
	/**
	 * Returns a list of strings (folder) to create, based
	 * on the "site" passed in.
	 * @param site - Name of the site to get the folders for.
	 * @return
	 */
	public Map<String,String> getFolderList(String site){
		return folderNames.get(site);
	}

	/**
	 * Initialize service pointers.
	 * 
	 * @param cmgr
	 */
	protected static void initServices() {
	}

	public NCI_NewsletterFolderService() {

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
		log.debug("Initializing NCI_NewsletterFolderService...");
	}


	/**
	 * 
	 */
	public void afterPropertiesSet() throws Exception {
		initServices();
	}


}

