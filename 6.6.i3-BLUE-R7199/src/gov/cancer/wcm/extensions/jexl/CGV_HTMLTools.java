package gov.cancer.wcm.extensions.jexl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;

public class CGV_HTMLTools extends PSJexlUtilBase implements IPSJexlExpression{
	private static Log log = LogFactory.getLog(CGV_HTMLTools.class);
	//private static IPSContentMgr cmgr = null;

	public CGV_HTMLTools() {
		super();
	}

	
	@IPSJexlMethod(description = "Returns up to the first two paragraphs of text for a HTML string. This assumes that you have gotten this from a HTML field.", 
			params = {
				@IPSJexlParam(name = "content", description = "The content to get paragraphs from.")})
	public String getFirstParagraphs(String content) 
	{
		String rtnContent = "";
		
		try {
			rtnContent = extractFirstParagraphs(content);
		} catch (Exception ex) {
		}
		
		return rtnContent;
	}	

	/***
	 * Extracts the first two paragraph tags from a string of HTML.  The tags
	 * must be non-empty and next to each other otherwise one is returned.
	 * @param content The HTML to extract the first two tags from
	 * @return The paragraph tags, or an empty string if none were found.
	 */
	private String extractFirstParagraphs(String content) {
        Source html = new Source(stripEmptyContainerTags(stripImgTags(content)));
        OutputDocument toReturn = new OutputDocument(html);
        StringBuilder rtnString = new StringBuilder();
        
        List<Element> elemList = html.getChildElements();
        
        int first_good_p_pos = -1;
        
        //Loop through the elements finding the first couple of P tags,
        //following our business rules of course.
        for (int i = 0; i< elemList.size(); i++) {
            Element elem = elemList.get(i);
            
            //If it is a p tag then it is something we could be looking for...
            if (elem.getName() == "p") {                
                if (!elem.isEmpty() && !elem.isWhiteSpace()) {                                        
                    
                    //Set the first p tag position, or 
                    //bail on the loop as we are done.
                    if (first_good_p_pos == -1) {
                        rtnString.append(elem.toString());
                        first_good_p_pos = i;                        
                    } else if ((first_good_p_pos+1) == i) {
                        //If this is the next good P tag, then 
                        //add it.
                        rtnString.append(elem.toString());
                    } else {
                        //This is not a candidate for the next P tag,
                        //quit looping as we have seen enough.
                        break;
                    }
                }                
            }                        
        }
        
        return rtnString.toString();		
	}

	/**
	 * This method will strip image tags (img) from a string.
	 * @param content The content to strip the images from
	 * @return The cleaned content.
	 */
    private String stripImgTags(String content) {
        Source html = new Source(content);
        OutputDocument toReturn = new OutputDocument(html);
        for (Element imgElem : html.getAllElements("img")) {
            toReturn.remove(imgElem);        
        }
        
        return toReturn.toString();
    }

    /**
     * Strips empty P and Div tags from a string.
     * @param content The content to strip the empty containers from
     * @return The cleaned content.
     */
    private String stripEmptyContainerTags(String content) {
        Source html = new Source(content);
        OutputDocument toReturn = new OutputDocument(html);
        for (Element elem : html.getChildElements()) {
            if ((elem.getName() == "p" || elem.getName() == "div") && (elem.isEmpty() || elem.isWhiteSpace())) {
                toReturn.remove(elem);                
            }
        }
        
        return toReturn.toString();
    }		

}