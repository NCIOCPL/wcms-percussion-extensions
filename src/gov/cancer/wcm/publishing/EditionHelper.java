package gov.cancer.wcm.publishing;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

/*
 * Utility class for working with publishing editions.
 */
public class EditionHelper {
	private static final Log log = LogFactory.getLog(EditionHelper.class);
	
	private IPSPublisherService pubSvc = PSPublisherServiceLocator.getPublisherService();
	private PublishingConfiguration pubConfig = PublishingConfigurationLocator.getPublishingConfiguration();

	// Dual-mapping for ways to look up edition information.
	private HashMap<Integer, IPSEdition> editionIDMap = new HashMap<Integer, IPSEdition>();
	private HashMap<String, IPSEdition> editionNameMap = new HashMap<String, IPSEdition>();
	
	public EditionHelper(){
		log.trace("Create EditionHelper");

		// There's a certain temptation to move the edition retrievals into a static block so it only
		// loads once.  The problem is, this requires a restart in order to pick up any new editions.
		// Maybe put it into a static block, and then reload it in the case where an edition isn't
		// found? But that gets tricky when you decide to avoid having the same code twice.
		List<IPSEdition> editionList = pubSvc.findAllEditions("");
		for(IPSEdition edition : editionList){
			String name = edition.getName();
			int id = edition.getGUID().getUUID();
			
			editionIDMap.put(id, edition);
			editionNameMap.put(name, edition);
		}
	}
}
