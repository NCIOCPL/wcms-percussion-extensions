/**
 * 
 */
package gov.cancer.wcm.util;

/**
 * @author whole
 *
 */
public class CGVConstants {

	 public static String DISPLAY_TITLE_FLD = "long_title";
	 public static String FRIENDLY_URL_FLD = "pretty_url_name";
	 public static String[] MULTI_PAGE_CONTAINER = {"cgvBooklet", "cgvPowerPoint", "cgvCancerBulletin"};
	 public static String[] MULTI_PAGE_PAGES = {"cgvBookletPage", "cgvPowerPointPage", "cgvCancerBulletinPage"};
	 public static String[] TCGA_TYPES = { "tcgaCancerSelected", "tcgaCancerType", "tcgaEvent", "tcgaListPage", "tcgaSeries", "tcgaNewsRelease"};
	 public static String[] CROSS_SITE_TYPES = {"nciGeneral", "nciHome", "nciLandingPage", "nciErrorPage", "nciFile"};
	 public static String[] TOP_CONTENT_TYPE_NAMES = {
		 "cgvBooklet", 
		 "cgvBookletPage", 
		 "cgvCancerBulletin", 
		 "cgvCancerBulletinPage", 
		 "cgvClinicalTrialResult",
		 "cgvContentSearch", 
		 "cgvDrugInfoSummary",
		 "cgvFactSheet",
		 "cgvFeaturedClinicalTrial",
		 "cgvPowerPoint",
		 "cgvPowerPointPage",
		 "cgvPressRelease",
		 "cgvTopicSearchCategory",
		 "nciGeneral",
		 "nciHome",
		 "nciLandingPage",
		 "nciErrorPage",
		 "cgvManualRSS",
		 "cgvAutoRSS",
		 "cgvBestBetsCategory",
		 "nciAppModulePage",
		 };
//TODO: move TOP_CONTENT_TYPE_NAMES to the bean xml file for CGV_OnDemandPublishService
}
