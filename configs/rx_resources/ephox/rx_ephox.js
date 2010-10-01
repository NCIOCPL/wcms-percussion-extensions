/******************************************************************************
 *
 * [ rx_ephox.js ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 * 
 *
 ******************************************************************************/

/**
 * This file holds the functions for the CMS buttons on the EditLive control.
 */

/**
 * Constant: Holds url for search page.  Called by child popup pages.
 */
var INLINE_SEARCH_PAGE = "../sys_searchSupport/getQuery.html?";

/**
 * Constant: Holds url for return page.  Called by child popup pages.
 */
var INLINE_RETURN_PAGE =  "../sys_searchSupport/getResults.html?";

/**
 * Constant: Holds url for EditLive help directory.
 */
var HELP_DIR =  "../rx_resources/ephox/enduserhelp/";

 /**
  * A flag indicating that this is EditLive. Used by returnVariant.xsl
  */
var isEditLive = true;

/**
 * List of block tags
 */
var ___blockTags = new Array(
    "address", "blockquote", "dd", "dl", "dt", "div", "h1", "h2", "h2", "h3",
    "h4", "h5", "h6", "hr", "ins", "li", "noscript'", "ol", "p", "pre",
    "ul"); 

 /**
  * Global variables needed for the browse dialog
  */
 var ___cBackFunction = null;
 var ___slotId = null;
 var ___bws = null;
 var ___bwsMode = null;
 var __rxroot = "/Rhythmyx"; 
 var ___serverProperties = null;
 var ___selectedContent = null;
 

/**
 * The single editlive instance
 */
 var _editLiveInstance = null;

 /**
  * Currently active EditLive section
  */
  var _activeEditLiveSectionName = "";

  /**
   * Flag indicating the <p> tag is inserted before content
   * used by _insertPTagsPart2
   */
  var _isPreFlag = false;

  var _sectionPostfix = "_ps";

/**
 * A flag indicating, whether or not the current browser type is Netscape. 
 * <code>true</code> if Netscape, false otherwise.
 */
var isNav = false;

/**
 * A flag indicating, whether or not the current browser type is Internet Explorer. 
 * <code>true</code> if Internet Explorer, false otherwise.
 */
var isIE  = false;

/**
 * A flag indicating, whether or not the OS is Macintosh. 
 * <code>true</code> if Macintosh, false otherwise.
 */
var isMac = false;

/**
 * Constant replacement for href attribute.
 */
 var PERC_HREF_ATTR = "___perchref";

/**
 * Constant replacement for src attribute.
 */
 var PERC_SRC_ATTR = "___percsrc";

/**
 * Check the browser and set appropriate member field.  
 * Assume Netscape or IE only.
 */
if (navigator.appName == "Netscape") 
{
   isNav = true;
}
else
{
   isIE = true;
}

/**
 * Check the OS and set appropriate member field.  Note: We will need this 
 * in next upgrade.
 */
if (navigator.platform == "MacPPC")
{
   isMac = true;
}
 

function trim(str)
{
   return str.replace(/^\s+|\s+$/g,"");
}

/**
 * Data Object.  Holds property values written to maintain state.
 */
var dataObject = new Object();
dataObject.returnedValue = "";
dataObject.sEditorName = "";
dataObject.wepSelectedText = "";
dataObject.searchType = "";
dataObject.windowRef = "";
dataObject.editorObject = ""; 


/**
* The array of all editors on the page.  There will be one entry per editor...
*
*  var editor = new Object();
*  editor.objectref = eopObject
*  editor.name = field name
*  editor.inlineLinkSlot = "";
*  editor.inlineImageSlot = "";
*  editor.inlineVariantSlot = ""; 
* 
*/
var _rxAllEditors = new Array();


/** 
* CMS Link function 
* 
*/

/**
 * Creates CMS search box for inline links and CMS Image creation:
 */
function createSearchBox(type) 
{
   
   var meta = _getEditSectionMeta(_activeEditLiveSectionName); 
   var inlineslotid = -1;
   if(type == "rxhyperlink")
	{	
	   inlineslotid = meta.inlineLinkSlot;
	   ___bwsMode = ps.util.BROWSE_MODE_RTE_INLINE_LINK;
	}
   else if(type == "rximage")
	{
	   inlineslotid = meta.inlineImageSlot;
	   ___bwsMode =  ps.util.BROWSE_MODE_RTE_INLINE_IMAGE;
	}
	else if(type == "rxvariant")
	{
		inlineslotid = meta.inlineVariantSlot;
		___bwsMode =  ps.util.BROWSE_MODE_RTE_INLINE;
	}

   dataObject.editorObject = _editLiveInstance; 
   dataObject.searchType = type; 
   dataObject.slotid = inlineslotid; 
   
   _editLiveInstance.GetSelectedText("launchSearchBox");
}

function launchSearchBox(selectedText)
{
   ___selectedContent = selectedText;
   var inlineSearchUsesContentBrowser = ___serverProperties.inlineSearchUsesContentBrowser != undefined
			   ? ___serverProperties.inlineSearchUsesContentBrowser : "true";    
   if(inlineSearchUsesContentBrowser.toLowerCase() != "true")
   {
      launchLegacySearchBox(selectedText);
   }
   else
   {
      var inlineslotid = dataObject.slotid;
      var ctypeid = document.forms["EditForm"].sys_contenttypeid.value;
   
      ___slotId ='["1",null,null,null,null,null,null,"' 
	      + ctypeid + '",null,"' 
	      + inlineslotid + '",null,null,null,null,null]';
      ___cBackFunction = contentBrowserCallback;
      ___bws = window.open(ps.util.CONTENT_BROWSE_URL, "contentBrowerDialog",
		     "resizable=1;status=0,toolbar=0,scrollbars=0,menubar=0,location=0,directories=0,width=750,height=500");   
   }
}

/**
 * callback function called by the browse dialog to handle
 * adding the selected content to EditLive at the cursor selection.
 */
function contentBrowserCallback(objectId)
{
   var oId = new ps.aa.ObjectId(objectId.toString());
   var type = dataObject.searchType;
   var buff = "";
   var attribs = "";
   var allowTrueInlineTemplates = (___serverProperties.allowTrueInlineTemplates != undefined
			   ? ___serverProperties.allowTrueInlineTemplates : "false").toLowerCase() == "true";

   attribs += (" sys_dependentvariantid=\"" + oId.getTemplateId() + "\"");
   attribs += (" sys_dependentid=\"" + oId.getContentId() + "\"");
   attribs += (" inlinetype=\"" + type + "\"");
   attribs += (" rxinlineslot=\"" + oId.getSlotId() + "\"");
   attribs += (" sys_dependentid=\"" + oId.getContentId() + "\"");
   var sId = oId.getSiteId() != null ? oId.getSiteId() : "";
   var fId = oId.getFolderId() != null ? oId.getFolderId() : "";
   attribs += (" sys_siteid=\"" + sId + "\"");
   attribs += (" sys_folderid=\"" + fId + "\"");

	if(type == "rxhyperlink" || type == "rximage")
	{	
	   var response = ps.io.Actions.getUrl(oId, "CE_LINK");
	   if(response.isSuccess())
      {
         var urlstring = encodeAmpersand(response.getValue().url);
         if(type == "rxhyperlink")
         {
            buff += "<a href=\"" + urlstring + "\"" + attribs + ">";
            buff += ___selectedContent;
            buff += "</a>";
         }
         else
         {
            buff += "<img src=\"" + urlstring + "\"" + attribs + ">";           
         }
         
	   }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }

	}   
	else if(type == "rxvariant" && allowTrueInlineTemplates)
	{
		var response = 
		   ps.io.Actions.getSnippetContent(oId, false, ___selectedContent);
	   if(response.isSuccess())
      {
        var imgSrcStart = "../sys_resources/images/inlinestart.png"; 
        var imgSrcEnd = "../sys_resources/images/inlineend.png"; 
        var imgtag = 
           "<img alt=\"@inlinemarker@\" width=\"9\" height=\"8\" src=\"@@src@@\"/>";
        var theContent = unicode2entity(response.getValue());
        var tagstart = theContent.indexOf("<") + 1;
        var tagend = minIgnoreNegative(
           theContent.indexOf(">", tagstart),
           theContent.indexOf(" ", tagstart));
        var mattribs = "";
        var tagname = theContent.substring(tagstart, tagend);       
        tagname = stringtrim(tagname);
        var prefix = theContent.substring(0,theContent.indexOf(">"));
        var postfix = theContent.substring(theContent.indexOf(">"));
        var isBlock = isBlockTag(tagname);
        if(isBlock)
        {
           attribs += (" class=\"rx_ephox_inlinevariant\"");
        }
        else
        {
           buff += imgtag.replace("@@src@@", imgSrcStart);
        }
		  attribs += (" contenteditable=\"false\"");
        
        
        attribs += (" rxselectedtext=\"" + encodeURIComponent(___selectedContent)+ "\"");
        buff += prefix + attribs + postfix;
        if(!isBlock)
           buff += imgtag.replace("@@src@@", imgSrcEnd);
	   }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }

	}
   else if(type == "rxvariant" && !allowTrueInlineTemplates)
   {
		var response = 
		   ps.io.Actions.getSnippetContent(oId, false, ___selectedContent);
	   if(response.isSuccess())
      {
        attribs += (" style=\"display: inline;\"");
        attribs += (" class=\"rx_ephox_inlinevariant\"");
        attribs += (" contenteditable=\"false\"");
		  attribs += (" rxselectedtext=\"" + encodeURIComponent(___selectedContent)+ "\"");
        buff += ("<div" + attribs + ">\n");
        buff += unicode2entity(response.getValue());
        buff += "\n</div>";         
	   }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }

   }
   formatOutput(buff);
   ___bws.close();
}

function stringtrim(str)
{
   return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

function launchLegacySearchBox(selectedText)
   {
   var inlineslotid = dataObject.slotid;
  
   //Update the inlinelinksearch form elements
   document.inlinelinkssearch.action = INLINE_SEARCH_PAGE;
   document.inlinelinkssearch.inlinetext.value = selectedText;
   document.inlinelinkssearch.inlineslotid.value = inlineslotid;
   document.inlinelinkssearch.inlinetype.value = dataObject.searchType;
   //Open an empty window.
   var w = "";
   if(isNav)
   {
      dataObject.windowRef = window.open("", "searchitems", 
      "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1," + 
      "resizable=1,width=400,height=400,screenX=220,screenY=220");
   } 
   else
   {
      dataObject.windowRef =  window.open("", "searchitems", 
      "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1," + 
      "resizable=1,width=400,height=400, left=220, top=220");
   }   
   //Submit the inlinelinksearch form to this window
   document.inlinelinkssearch.submit();

   dataObject.windowRef.focus();
  
}

/**
 * Clean up dataObject.  Set all properties to "" for the next call. 
 */
function cleanUp()
{
   dataObject.returnedValue = "";
   dataObject.sEditorName = "";
   dataObject.wepSelectedText = "";
   dataObject.searchType = "";
   dataObject.windowRef = "";

}

/**
 * Formats the output then pastes into the control.
 * Netscape 4.7 & 6.23 compliant.  
 * @param returnedHTML is a string and can be <code>null</code>
 */
function formatOutput(returnedHTML)
{   
   dataObject.searchType = "";
   //alert("Returned HTML is: " + returnedHTML); 
   var ephox = dataObject.editorObject;
   ephox.InsertHTMLAtCursor(encodeURIComponent(returnedHTML));  
}

function minIgnoreNegative(a, b)
{
   if(a == -1)
      return b;
   if(b == -1)
      return a;
   return Math.min(a,b);
}

/**
 * Determines if tag is a block tag.
 * @param tagname cannot be <code>null</code> or empty.
 * @return <code>true</code> if a block tag.
 */
 function isBlockTag(tagname)
 {
    if(tagname == null || tagname.length == 0)
       alert("tagname cannot be null or empty.");
    var len = ___blockTags.length;
    for(i = 0; i < len; i++)
    {
       if(___blockTags[i].toLowerCase() == tagname.toLowerCase())
          return true;
    }
      return false;
 }

/**
 * Builds a url from the parameters given. 
 * @param oStr -  is the text in the browser.  May be <code>null</code>.
 * @param str - is the url text.  May be <code>null</code>.
 */
function buildUrl(oStr, str)
{  
   if(!isStringValid(str))
   {
      return;
   }

   var link = "<A HREF=\""+ str +"\">"
   
   if(!isStringValid(oStr))
   {
     link += str;
   }  
   else 
   {
      link += oStr;
   }            
    
   link += "</A>";   

   var ephox = dataObject.editorObject;
   ephox.InsertHTMLAtCursor(encodeURIComponent(link));  
}  
  
/**
 * Does str pass our test for valid.  Returns <code>true</code> if 
 * it does.
 * @param str - string to be tested. May be <code>null</code>.
 */
function isStringValid(str)
{      
   if(str == null || str.length == 0 || str == "http://")
   {
      return false;
   }

   return true;
}



/**
 * Creates link to non Rhythmyx managed element.
 * @param sEditorName - must not be <code>null</code> or empty.
 */
function createExternalReference()
{ 
   dataObject.editorObject = _editLiveInstance; 
   _editLiveInstance.GetSelectedText("launchPromptBox");
}

/**
 * Inserts an empty <p> tag set either before or after the
 * existing content.
 * @param sEditorName - must not be <code>null</code> or empty.
 * @param isPreContent - determines if the P tags come before
 * or after the existing content.
 */
function _insertPTags(isPreContent)
{    
   _isPreFlag = isPreContent;
   _editLiveInstance.GetBody("_insertPTagsPart2");
}

function _insertPTagsPart2(selectedText)
{
   var ptags = "<p></p>";
   var modifedContent = "";
   if(isPreContent)
      modifiedContent = ptags + selectedText;
   else
      modifiedContent = selectedText + ptags;
   _editLiveInstance.setBody(encodeURIComponent(modifiedContent));
   
   
}



function launchPromptBox(selectedText)
{
   var str=prompt("Enter link location (e.g. http://www.yahoo.com):", "http:\/\/");
   buildUrl(selectedText, str );
}
// A list of allowed elements, these are defined from HTML 4.01
var allowed = new Array("sub","sup","small","big","em","b","i","tt","strong","dfn","code",
"samp","kbd","var","cite","abbr","acronym","img","object","br","script","map","q","span","bdo","font");

var skipFirst = new Array("p","div");

// Anchor tags only allow certain elements to be nested inside them. This function
// determines if the selectedHtml only has allowable elements. The list of allowable
// elements is listed in the "allowed" variable above.
//
// There are some exceptions. If the first element found is one of the "skipFirst"
// elements listed, those are allowed for that special case. This allows a user to 
// do what looks like a valid selection that really includes (incorrectly) the div
// or paragraph tags. In this circumstance, ektron will fix the results.
function isStringValidForLink(selectedHtml)
{
	var i;
	var len = selectedHtml.length;

	// Handle some special cases.. notably Ektron can return <p>content</p> or <div>content</div> when
	// the user believes they've just selected regular text or an image. Skip these starting cases
	var first = true;

	for(i = 0; i < len; i++)
	{
		// If we find an element that is not in the allowed array, then return false
		if (selectedHtml.charAt(i) == '<')
		{
			// Find the end delimiter or space
			var e, j;
			for(e = i + 1; e < len; e++)
			{
				var ch = selectedHtml.charAt(e);
				if (ch == '>' || ch == ' ')
				{
					break;
				}
			}
			var el = selectedHtml.substring(i + 1, e).toLowerCase();
			var inarray = false;
			if (first)
			{
				first = false;
				for(j = 0; j < skipFirst.length; j++)
				{
					if (skipFirst[j] == el)
					{
						inarray = true;
					}
				}
			}
			if (el.length > 0 && el.charAt(0) == '/')
			{
				inarray = true; // Closing tag, ignore
			}
			for(j = 0; j < allowed.length && inarray == false; j++)
			{
				if (allowed[j] == el)
				{
					inarray = true;
				}
			}
			if (! inarray)
			{
				return false;
			}
			i = e;
		}
	}

	return true;
}

function RxEphoxHelp()
{
   var lang = document.forms['EditForm'].sys_lang.value;
   var langCode = lang.substr(0, 2);
   var helpLang = "english";
   
   /* Uncomment when we implement help in other languages
   if(langCode == "cs") helpLang = "czech";
   if(langCode == "fr") helpLang = "french";
   if(langCode == "de") helpLang = "german";
   if(langCode == "it") helpLang = "italian";
   if(langCode == "ko") helpLang = "korean";
   if(langCode == "es") helpLang = "spanish";
   */
   var helpURL = HELP_DIR + helpLang + "/userhelphome.htm";
   
   var nWin = window.open(helpURL, "ephoxHelp", "height=600; width=800; menubar=0; status=0; toolbar=0");
   nWin.focus();
   
}


function isEphoxControlDirty()
{   
    var ephoxApplet = _getEditLiveApplet();
	if(ephoxApplet == null || ephoxApplet == undefined)
		return false;
    return ephoxApplet.isDirty();  
}


/*
 Retrieve the editlive doc. Also checks to see
 if it should be considered empty and returns it as really empty
 instead of <p>&#160</p>.
*/
function getEphoxDocument(sEditorName)
{
   
   var doc = _editLiveInstance.getContentForEditableSection(
	    _getSectionDivNameByFieldName(sEditorName));
   if(rxIsEditLiveDocEmpty(doc))
	   return "";
   return doc;
}

/*
 Returns the content between the <body> tags in 
 the passed in Ephox document. 
*/
function rxGetEphoxBodyContent(doc)
{
   var lDoc = doc.toLowerCase();
   var sBodyPos = lDoc.indexOf("<body");
   if(sBodyPos == -1)
	   return doc;
   var eBodyPos = lDoc.indexOf(">", sBodyPos + 5);
   if(eBodyPos == -1) 
	   return doc;
   var cBodyPos = lDoc.lastIndexOf("</body>");
   if(cBodyPos == -1)
	   return doc;
   return doc.substring(eBodyPos + 1, cBodyPos);
}

/*
 Checks to see if Editlive should be considered empty.
 Is empty if the body contains:
 <p></p>
 <p>&#160;</p>
 <p>&nbsp;</p>
 &nbsp;
 &#160;
*/
function rxIsEditLiveDocEmpty(doc)
{
	var body = 
		trim(rxGetEphoxBodyContent(doc) + "").toLowerCase();
    var emptyStatements = 
		[
		   "",
	       "<p><p>",
		   "<p>&#160;</p>",
		   "<p>&nbsp;</p>",
		   "&nbsp;",
		   "&#160;"
		];
     for(idx in emptyStatements)
	{
       if(body == emptyStatements[idx])
		   return true;
	}
	return false;

}
 
/*
  Disables the submit button on the CE
 */
function rxEphoxDisableSubmit()
{
   var submitButton = document.getElementById("rxCESubmit");
   if(submitButton != null && submitButton != undefined)
      submitButton.disabled = true;
}

/*
 Tracks the number of ephox controls that completed
 init and if all are inited then enables the submit
 button
 */
var _initedEditLiveControls = 0; 
function rxEphoxHandleEditorInitComplete()
{
   ++_initedEditLiveControls;
   if(_rxAllEditors.length == _initedEditLiveControls)
   {
      var submitButton = document.getElementById("rxCESubmit");
      if(submitButton != null && submitButton != undefined)
         submitButton.disabled = false;
   }
} 

/**
 *  Used to initialize the single editlive instance. This should only
 *  called after the end FORM tag that encloses the editable div's.
 */
function _initializeEditLiveInstance()
{
   _initializeServerProperties();
   var allowTrueInlineTemplates = (___serverProperties.allowTrueInlineTemplates != undefined
			   ? ___serverProperties.allowTrueInlineTemplates : "false").toLowerCase() == "true";
   _editLiveInstance = new EditLiveJava("_editLiveInstance", "100%", "100%"); 
   _editLiveInstance.setDownloadDirectory("../rx_resources/ephox/editlivejava");
   _editLiveInstance.setStyles(escape("div.rx_ephox_inlinevariant {border: solid #c0c0c0 1px;}"));
   _editLiveInstance.setMinimumJREVersion("1.4.2");
   _editLiveInstance.setAutoSubmit(false);
   _editLiveInstance.setOnInitComplete("rxEphoxHandleEditorInitComplete");
   _editLiveInstance.addParam("allowTrueInlineTemplates", 
      allowTrueInlineTemplates ? "true" : "false");
   __addAllEditLivePlugins();
   //_editLiveInstance.disableObviousEditableSections(); // Uncomment to disable the mouse over pencil
   // Add all editable DIV's
   for(var i=0; i < _rxAllEditors.length; i++)
   {
      _editLiveInstance.addEditableSection(_getSectionDivNameByFieldName(_rxAllEditors[i].name));
   }
   
}

/**
 * Grabs the server properties from the server and caches them
 */
function _initializeServerProperties()
{
   if(___serverProperties == null)
   {
      var response = ps.io.Actions.getServerProperties();
	   if(response.isSuccess())
      {
         ___serverProperties = response.getValue();    
	   }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }
   }
}

/**
 * Handles some setup when an editable DIV section is clicked.
 */
function _onclickEditLiveSection(name, locale, isReadOnly)
{
   _activeEditLiveSectionName = name;
   var meta = _getEditSectionMeta(name);
   _editLiveInstance.setLocale(locale.toUpperCase().substr(0,2));
   _editLiveInstance.setConfigurationFile(meta.config);
   _editLiveInstance.setReadOnly(isReadOnly);
}

/**
 * Places a processing instruction at the current cursor location that is 
 * understood by the pagination mechanism of assembly.
 */
function RxEphoxInsertPageBreak()
{
   _editLiveInstance.InsertHTMLAtCursor("<?pageBreak?>");
}

/**
 * Callback to launch the inline link search
 */
function RxEphoxInlineLink()
{
   createSearchBox("rxhyperlink"); 
}

/**
 * Callback to launch the inline image search
 */
function RxEphoxImageLink()
{
   createSearchBox("rximage"); 
}

/**
 * Callback to launch the inline variant search
 */
function RxEphoxVariantLink()
{   
   createSearchBox("rxvariant");
}

/**
 * Callback to insert <p> tags at the top of the current
 * loaded document.
 */
function RxEphoxInsertPTagsA()
{
   _insertPTags(true); 
}

/**
 * Callback to insert <p> tags at the bottom of the current
 * loaded document.
 */
function RxEphoxInsertPTagsB()
{
   _insertPTags(false); 
}

/**
 * Get the meta data object for the specified section
 */
function _getEditSectionMeta(sectionName)
{
   
   for(var i=0; i < _rxAllEditors.length; i++)
     {
       if(_rxAllEditors[i].name == sectionName)
          return(_rxAllEditors[i]);
     }
   alert("Editor section meta data for [" + sectionName + "] not found!!!!"); 
   return("");  
}

/**
 * Returns the div section name from the field name.
 */
function _getSectionDivNameByFieldName(fieldName)
{
   return fieldName + _sectionPostfix;
}
/**
 * Activates a section as specified by its index in the page
 */
function _activateEditLiveSection(index)
{
     var name = _rxAllEditors[index].name;
	 var div = document.getElementById(_getSectionDivNameByFieldName(name));
	 div.onclick();
}

/**
 * Gets a reference to the EditLive Applet instance.
 */
function _getEditLiveApplet()
{
   return PSGetApplet(self, "_editLiveInstance_elj");
}

/*
  Loops through the EditLive editor array and moves the controls contents
  into each appropriate hidden field.
*/
function rxEphoxPreSubmit()
{
   var editorName = "";
   var ephoxApplet = null;
   var hiddenField = null;
   var sectionName = null;
   var div = null;
   for(var i=0; i < _rxAllEditors.length; i++)
   {
      editorName = _rxAllEditors[i].name;
      sectionName =  _getSectionDivNameByFieldName(editorName);
      

      //Get hidden field and set value
      hiddenField = document.getElementById(editorName);
      if(hiddenField == null || hiddenField == undefined)
      {
         alert("Unable to retrieve EditLive hidden field: " + editorName + ".");
      }
      else
      {
         var eContent = getEphoxDocument(editorName);
         hiddenField.value = eContent;
      }
      //Get editlive div section and clear it. We do this so any html fields
      //in the div will not get added to the form on submit.
      div = document.getElementById(sectionName);
      if(div == null || div == undefined)
      {
         alert("Unable to retrieve EditLive div section: " + sectionName + ".");
      }
      else
      {
         div.innerHTML = "";
      }
   }  
}

/**
 * Method to set the ephox content.
 * IE innerHTML converts the relative urls associated with href and src attributes 
 * to absolute. This method will rename these attributes before setting the content
 * on the ephox div and revert them back through dom afterwards.
 * We may miss some of these renamed attributes, if they are part of comments.
 * rxEphoxPreSubmit will handle those missing attributes.
 *
 * @param divElemName name of the div element that will represent the ephox in 
 * inactive mode.
 * @param ephoxContent the content of ephox.
 *
 */
function setEphoxInitialContent(divElemName, ephoxContent)
{
   if(!ephoxContent || ephoxContent.length < 1)
      return;
   if($.browser.msie)
   {
      var regExH = new RegExp(" href=","gi");
      var regExS = new RegExp(" src=","gi");
      ephoxContent = ephoxContent.replace(regExH," "+ PERC_HREF_ATTR + "=" );
      ephoxContent = ephoxContent.replace(regExS," "+ PERC_SRC_ATTR + "=" );
      $("#" + divElemName).html(ephoxContent);
      var nodes = document.getElementById(divElemName).getElementsByTagName("a");
      for(i=0;i<nodes.length;i++)
      {
         var curNode = nodes[i];
         var attr = curNode.getAttribute(PERC_HREF_ATTR);
         if(attr != null)
         {
            var inh = curNode.innerHTML;
            curNode.setAttribute("href", attr);
            curNode.removeAttribute(PERC_HREF_ATTR);
            curNode.innerHTML = inh;
         }
      }
      nodes = document.getElementById(divElemName).getElementsByTagName("img");
      _replaceSrcTag(nodes);
       nodes = document.getElementById(divElemName).getElementsByTagName("input");
      _replaceSrcTag(nodes);
      nodes = document.getElementById(divElemName).getElementsByTagName("div");
      _replaceSrcTag(nodes);
       nodes = document.getElementById(divElemName).getElementsByTagName("embed");
      _replaceSrcTag(nodes);
   }
   else
   {
      $("#" + divElemName).html(ephoxContent);
   }
}

// Converts unicode chars into the hexidecimal reference
// entity
function unicode2entity(str)
{
   var sPos = 0;
   var ePos = -1;
   var result = "";

   while((sPos = str.indexOf("%u", sPos)) != -1)
   {
      if(ePos == -1)
      {
          ePos = 0;
          result += str.substring(ePos, sPos);
      }
      else if(sPos - ePos > 0)
      {
          result += str.substring(ePos + 1, sPos);
      }
      result += "&#x" + str.substr(sPos + 2, 4) + ";";
      sPos += 5;
      ePos = sPos;

   } 
   result += str.substring(ePos + 1);

   return result;
}
            
          
function encodeAmpersand(urlstring)
{
   var re = new RegExp("&amp;","g");
   urlstring = urlstring.replace(re,"&");
   var re1 = new RegExp("&","g");
   urlstring = urlstring.replace(re1,"&amp;");
   return urlstring;
}
/**
 * Method to set the ephox content.
 * IE innerHTML converts the relative urls associated with href and src attributes 
 * to absolute. This method will rename these attributes before setting the content
 * on the ephox div and revert them back through dom afterwards.
 * We may miss some of these renamed attributes, if they are part of comments.
 * rxEphoxPreSubmit will handle those missing attributes.
 *
 * @param divElemName name of the div element that will represent the ephox in 
 * inactive mode.
 * @param ephoxContent the content of ephox.
 *
 */
function setEphoxInitialContent(divElemName, ephoxContent)
{
   if(!ephoxContent || ephoxContent.length < 1)
      return;
   if($.browser.msie)
   {
      var regExH = new RegExp(" href=","gi");
      var regExS = new RegExp(" src=","gi");
      ephoxContent = ephoxContent.replace(regExH," "+ PERC_HREF_ATTR + "=" );
      ephoxContent = ephoxContent.replace(regExS," "+ PERC_SRC_ATTR + "=" );
      $("#" + divElemName).html(ephoxContent);
      var nodes = document.getElementById(divElemName).getElementsByTagName("a");
      for(i=0;i<nodes.length;i++)
      {
         var curNode = nodes[i];
         var attr = curNode.getAttribute(PERC_HREF_ATTR);
         if(attr != null)
         {
            var inh = curNode.innerHTML;
            curNode.setAttribute("href", attr);
            curNode.removeAttribute(PERC_HREF_ATTR);
            curNode.innerHTML = inh;
         }
      }
      nodes = document.getElementById(divElemName).getElementsByTagName("img");
      _replaceSrcTag(nodes);
       nodes = document.getElementById(divElemName).getElementsByTagName("input");
      _replaceSrcTag(nodes);
      nodes = document.getElementById(divElemName).getElementsByTagName("div");
      _replaceSrcTag(nodes);
       nodes = document.getElementById(divElemName).getElementsByTagName("embed");
      _replaceSrcTag(nodes);
   }
   else
   {
      // Convert pageBreak PI to an XML element that will work in 
      // FireFox. This will be converted back to the PI format by 
      // a EditLive custom plugin when the content is loaded.
      var regExPb = new RegExp("<[ ]*\\?pageBreak[ ]*\\?>", "g");
      ephoxContent = ephoxContent.replace(regExPb, "<pageBreak/>");

      $("#" + divElemName).html(ephoxContent);
   }
}
/********************************************************************
*
* The code from here on is Cancer.gov customization
* @author holewr
*
***********************************************************************/
var cGovReq;			//global request object
var cGovOriginalData;	//data from editor window
var cGovMassagedData;	//editor data after preprocessing
var cGovUniqueId;		//id unique within this page
var cGovEditorName;		//name of editor
var cGovStatusWindow;	//window for status display
var cGovCheckboxWindow	//window for term checkboxes
var cGovCRConst = "&#x000d;";	//carriage return substitute
var cGovLFConst = "&#x000a;";	//line feed substitute
/**
* The following variables should be set from a config file
*/
var cGovWebServiceURL = 'http://verdi.nci.nih.gov/u/glossify/';	//url of glossifier web service
//var cGovWebServiceURL = 'http://pdqupdate.cancer.gov/u/glossify/';	//url of glossifier web service
var cGovPreviewURL = "www.cancer.gov";	//dictionary preview URL

/**
 * Method to fetch Ephox content, glossify it, and reset it into the editor.
 * This method is a custom cancer.gov method
 * 
 */
function cGovEphoxGlossify()
{
	cGovUniqueId = 0;
	cGovEditorName = _rxAllEditors[0].name;
	// Get the content from the editor using an existing function
	cGovOriginalData = _editLiveInstance.getContentForEditableSection(_getSectionDivNameByFieldName(cGovEditorName));
	// Get glossification info from web service and apply it
	cGovEphoxDoGlossify(cGovOriginalData);
}

/**
* Send the glossification request to the web service
*
*/
function cGovEphoxDoGlossify(data) {
	// Pre-process the string to encode cr and lf and flag previous glossify results
	cGovMassagedData = cGovPrepareStr(data);
	// Wrap in CDATA so markup doesn't hose everything
	var safeData = "<![CDATA[" + cGovMassagedData + "]]>";
	// XML SOAP command for web service
	var soapCommand = "<?xml version=\"1.0\"?>" +
		"<soap:Envelope " +
		"xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
		"xmlns:xsd='http://www.w3.org/2001/XMLSchema' " +
		"xmlns:soap='http://schemas.xmlsoap.org/soap/envelope' " +
		"soap:encodingStyle='http://schemas.xmlsoap.org/soap/encoding'>" +
		"<soap:Body xmlns:m='cips.nci.nih.gov/cdr'>" +
			"<m:glossify>" +
				"<m:fragment>" + safeData + "</m:fragment>" +
				"<m:dictionaries>" +
					"<m:string>Cancer.gov</m:string>" +
				"</m:dictionaries>" +
				"<m:languages>" +
					"<m:string>en</m:string>" +
				"</m:languages>" +
			"</m:glossify>" +
		"</soap:Body>" +
		"</soap:Envelope>";
	// Get request object depending on browser
	if (window.XMLHttpRequest) {
		try {
			cGovReq = new XMLHttpRequest();
		}
		catch (e) {
			cGovReq = false;
		}
	}
	else if (window.ActiveXObject) {
		try {
			cGovReq = new ActiveXObject("Msxml2.XMLHTTP");
		}
		catch (e) {
			try {
				cGovReq = new ActiveXObject("Microsoft.XMLHTTP");
			}
			catch (e) {
				cGovReq = false;
			}
		}
	}
	if (cGovReq) {
		//Display status window
		cGovStatusWindow = window.open("","","height=480,width=640");
		cGovStatusWindow.document.write('<html><head><title>GlossifyDocumentPrep</title>');
		cGovStatusWindow.document.write('<style type="text/css">H2 {COLOR: #333366; FONT-FAMILY: Trebuchet MS, Tahoma, Verdana, Arial, sans-serif; FONT-SIZE: 12px; FONT-WEIGHT: bold; LINE-HEIGHT: 14px}</style>');
		cGovStatusWindow.document.write('<script language="javascript" type="text/javascript">');
		cGovStatusWindow.document.write("var prg_width = 200;");
		cGovStatusWindow.document.write("function progress() {");
        cGovStatusWindow.document.write("var node = document.getElementById('progress');");
        cGovStatusWindow.document.write("var w = node.style.width.match(/\\d+/);");
		cGovStatusWindow.document.write("if (w == prg_width)");
        cGovStatusWindow.document.write("w = 0;");
		cGovStatusWindow.document.write("node.style.width = parseInt(w) + 5 + 'px';");
        cGovStatusWindow.document.write("}");
        cGovStatusWindow.document.write("setInterval(progress, 250);");		
		cGovStatusWindow.document.write('</script></head>');
		cGovStatusWindow.document.write('<body><div><div style="border: 1px solid black; width:200px; height:10px;"><div id="progress" style="height:10px; width:0px; background-color:red;"/></div></div>');
		cGovStatusWindow.document.write("<h2>Processing document, please wait.........</h2></body></html>");

		// Do this on completion of asynchronous call
		cGovReq.onreadystatechange = cGovProcessReqChange;

		// Open request and set its headers
		cGovReq.open("POST", cGovWebServiceURL, true);
		cGovReq.setRequestHeader("Content-Type", "application/soap+xml");
		cGovReq.setRequestHeader("SOAPAction", "cips.nci.nih.gov/cdr/glossify");
		// Send the request
		cGovReq.send(soapCommand);
		return true;
	}
	else {
		alert("Your browser does not appear to support this functionality.");
	}
	return false;
}

/**
* Class containing values of each Term returned from the service
*
*/
function termObject() {
	this.start = 0;
	this.length = 0;
	this.docId = "";
	this.dictionary = "";
	this.language = "";
	this.first = true;
}

/**
* Completion callback
*
*/
function cGovProcessReqChange() {
	if (cGovReq.readyState == 4 && cGovReq.status == 200) {
//alert("got response");
		cGovStatusWindow.close();
		//Web service transaction has completed, parse the response
		var env = cGovReq.responseXML.getElementsByTagName("soap:Envelope");
		var body = env[0].getElementsByTagName("soap:Body");
		var resp = body[0].getElementsByTagName("glossifyResponse");
		var glossifyResult = resp[0].getElementsByTagName("glossifyResult");
		var terms = glossifyResult[0].getElementsByTagName("Term");
		// Put the terms values into an array
		var termsArray = cGovBuildTermsArray(terms);
		cGovMassagedData = cGovBuildCBDisplayString(cGovMassagedData, termsArray);
		
		// Set up HTML window with javascript and checkboxed text
		var cGovCheckboxWindow=window.open("","","height=480,width=640");
		cGovCheckboxWindow.document.write('<html><head>');
		cGovCheckboxWindow.document.write('<style type="text/css">H2 {COLOR: #333366; FONT-FAMILY: Trebuchet MS, Tahoma, Verdana, Arial, sans-serif; FONT-SIZE: 12px; FONT-WEIGHT: bold; LINE-HEIGHT: 14px}</style>');
		cGovCheckboxWindow.document.write('</head>');
		cGovCheckboxWindow.document.write('<script language="Javascript">');
		cGovCheckboxWindow.document.write('function returnChecks() {');
		cGovCheckboxWindow.document.write('	var checkArray = [];');
		cGovCheckboxWindow.document.write('	if (document.Glossify.terms != null) {');
		cGovCheckboxWindow.document.write('		boxes = document.Glossify.terms.length;');
		cGovCheckboxWindow.document.write('		if (boxes == null) {');
		cGovCheckboxWindow.document.write('			if (document.Glossify.terms.checked) {');
		cGovCheckboxWindow.document.write('				checkArray.push(document.Glossify.terms.value);');
		cGovCheckboxWindow.document.write('			}');
		cGovCheckboxWindow.document.write('		}');
		cGovCheckboxWindow.document.write('		else {');
		cGovCheckboxWindow.document.write('			for (i=0;i<boxes;i++) {');
		cGovCheckboxWindow.document.write('				if (document.Glossify.terms[i].checked) {');
		cGovCheckboxWindow.document.write('					checkArray.push(document.Glossify.terms[i].value);');
		cGovCheckboxWindow.document.write('				}');
		cGovCheckboxWindow.document.write('			}');
		cGovCheckboxWindow.document.write('		}');
		cGovCheckboxWindow.document.write('	}');
		cGovCheckboxWindow.document.write('window.opener.submitter(checkArray);');
		cGovCheckboxWindow.document.write('window.close();');
		cGovCheckboxWindow.document.write('}\n</script>');
		cGovCheckboxWindow.document.write('<body>');
		cGovCheckboxWindow.document.write('<form name="Glossify" id="Glossify" onSubmit="returnChecks();return(false)">');
		cGovCheckboxWindow.document.write('<h2>Please check/uncheck the word(s) you want glossified</h2>');
		cGovCheckboxWindow.document.write('<hr>\n' + cGovMassagedData + '<hr>\n');
		cGovCheckboxWindow.document.write('<input type="submit" value="Submit Changes">');
		cGovCheckboxWindow.document.write('</form>\n</body>\n</html>');
	}
}

/**
* Callback called when popup window is submitted
* Gets array of checked checkbox values, modifies the text to go into the editor
*
*/
function submitter(checkArray) {
//TODO: delete dictionary preview URL once we add it
//	if (!cGovCheckboxWindow.closed)
//		cGovCheckboxWindow.close();
	var rxCheckBox = new RegExp("<input type=checkbox name=terms.+?value=.+?>");
	var rxFixLinks = new RegExp("<a __(?:new|old)term=\"(.+?)\"(.+?)>(.+?)</a>");
	var rxKillFonts = new RegExp("<font __type=\"glossifyTemp\".+?>(.+?)</font>");
	var rxFixCRs = new RegExp(cGovCRConst);
	var rxFixLFs = new RegExp(cGovLFConst);
	var finalText = cGovStripCheckboxes(rxCheckBox, cGovMassagedData);
	finalText = cGovFixFinalLinks(rxFixLinks, finalText, checkArray);
	finalText = cGovKillFonts(rxKillFonts, finalText);
	finalText = cGovFixCRLF(rxFixCRs, finalText, "\r");
	finalText = cGovFixCRLF(rxFixLFs, finalText, "\n");
	//finish up by restoring text to editor
	_editLiveInstance.setContentForEditableSection(_getSectionDivNameByFieldName(cGovEditorName), finalText);
}

/**
* Strip checkbox markup from the final text
*
*/
function cGovStripCheckboxes(theRegExp, theText) {
	var done = false;
	var offset = 0;
	while (!done) {
		var temp = theText.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
			var target = theRegExp.exec(temp);
			if (target == null) {
				done = true;
			}
			else {
				offset += target.index;
				theText = theText.replace(target[0],"");
			}
		}
	}
//alert("end of cGovStripCheckboxes, theText= " + theText);
	return theText;
}

/**
* Fix up the links in the final text
*
*/
function cGovFixFinalLinks(theRegExp, theText, checkArray) {
	var done = false;
	var offset = 0;
	while (!done) {
		var temp = theText.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
//alert("temp= " + temp + " length= " + temp.length);
			var target = theRegExp.exec(temp);
//alert("ran regexp");
			if (target == null) {
//alert("done"); 
				done = true;
			}
			else {
				offset += target.index;
//alert("target[0]= " + target[0]);
//alert("target[1]= " + target[1]);
//alert("target[2]= " + target[2]);
//alert("target[3]= " + target[3]);
				var good = false;
				for (i=0;i<checkArray.length;i++) {
					if (checkArray[i] == target[1]) {
						good = true;
						break;
					}
				}
				if (good) {
				//user checked this one
					theText = theText.replace(target[0], "<a " + target[2] + ">" + target[3] + "</a>");
				}
				else {
				//user didn't check this one
					theText = theText.replace(target[0], target[3]);
					offset += target[3].length;
				}
			}
		}
	}
//alert("end of cGovFixFinalLinks, theText= " + theText);
	return theText;
}

/**
* Strip the special font markup from the final text
*
*/
function cGovKillFonts(theRegExp, theText) {
	var done = false;
	var offset = 0;
	while (!done) {
		var temp = theText.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
			var target = theRegExp.exec(temp);
			if (target == null) {
				done = true;
			}
			else {
				offset += target.index;
				theText = theText.replace(target[0], target[1]);
			}
		}
	}
	return theText;
}

/**
* Change CR and LF codes back to CRLF
*
*/
function cGovFixCRLF(theRegExp, theText, replacement) {
	var done = false;
	var offset = 0;
	while (!done) {
		var temp = theText.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
			var target = theRegExp.exec(temp);
			if (target == null) {
				done = true;
			}
			else {
				offset += target.index;
				theText = theText.replace(target[0], replacement);
			}
		}
	}
	return theText;
	
}

/**
* Build array of values from fetched terms
*
*/
function cGovBuildTermsArray(terms) {
	var termsArray = [];	//array of term values
	for (i=0;i<terms.length;i++) {
	// fetch the values for each Term returned by the service
		var term = new termObject();
		var start = terms[i].getElementsByTagName("start");
		term.start = start[0].childNodes[0].nodeValue;
		var len = terms[i].getElementsByTagName("length");
		term.length = len[0].childNodes[0].nodeValue;
		var docId = terms[i].getElementsByTagName("docId");
		term.docId = docId[0].childNodes[0].nodeValue;
		var dictionary = terms[i].getElementsByTagName("dictionary");
		term.dictionary = dictionary[0].childNodes[0].nodeValue;
		var language = terms[i].getElementsByTagName("language");
		term.language = language[0].childNodes[0].nodeValue;
		var first = terms[i].getElementsByTagName("firstOccurrence");
		term.first = first[0].childNodes[0].nodeValue;
		termsArray[i] = term;
	}
	return termsArray;
}

/**
* Build the string with formatting and checkboxes for use in the term selection window
*
*/
function cGovBuildCBDisplayString(data, termsArray) {
	// Go through the terms array in reverse order, insert terms links into massaged data with unique ids
	for (i=termsArray.length - 1;i>=0;i--) {
		var tobj = termsArray[i];
		cGovUniqueId++;
		var firstPart = data.substr(0, parseInt(tobj.start));
		var text = data.substr(parseInt(tobj.start), parseInt(tobj.length));
		var lastPart = data.substr(parseInt(tobj.start) + parseInt(tobj.length));
		// Determine if we need to highlight the term
		//Build the newterm string
		data = firstPart + "<input type=checkbox name=terms value=" + cGovUniqueId + "><a __newterm=\"" + cGovUniqueId + "\" class=\"definition\" href=\"/Common/PopUps/popDefinition.aspx?id=" + tobj.docId + "&version=Patient&language=English\" onclick=\"javascript:popWindow('defbyid','" + tobj.docId + "&version=Patient&language=English'); return false;\">" + text + "</a>" + lastPart;
	}
	var rxNewOldterm = new RegExp("<a __(?:new|old)term=\".+?>(.+?)</a>");
	data = cGovDoRegExpFonts(rxNewOldterm, data);
	// add checkbox (checked) to __oldterms
	rxOldterm = new RegExp("<a __oldterm=\"(.+?)\"");
	data = cGovDoRegExpAddChecks(rxOldterm, data);
//alert("final data for display = \n" + data);
	return data;
}

/**
* Prepare the data for sending to web service. Replace cr and lf with code, mark old web service
* provided URLs with __oldterm
*
*/
function cGovPrepareStr(data) {
//alert("In cGovPrepareStr, data= " + data);
	var tempData = data;
	var result="";
	// These expressions look for two specific styles of links and then change them from
	//	<a whatever> to <a __oldterm>
	//	<a href="/dictionary/db_alpha.aspx?expand=s#symptom" onclick="javascript:popWindow('definition','symptom'); return false;">symptoms</a>
	//	<a class="definition" href="/Common/PopUps/popDefinition.aspx?term=bone marrow&amp;version=Patient&amp;language=English" onclick="javascript:popWindow('definition','bone marrow&amp;version=Patient&amp;language=English');  return(false);">bone marrow</a>
	// The most complete patterns (from the old Admin Tool code) don't work here:
	//	<a\s+(href=\"/dictionary/db_alpha.aspx\?expand=.+?>.+?</a>)
	//	<a\\s+(class=\"definition\".+?>.+?</a>)
	// The following two expressions expect the links to be in a particular order. The second set expect
	// a different order. No matter what order they are in in the editor, they always seem to come back the
	// second way. If this turns out to not be the case, we'll have to run the first two expressions as well
	// as the second.
//	var rxDict1 = new RegExp("<a\\s+(href=.+dictionary/db_alpha.aspx.+</a>)","i");
//	var rxDef1 = new RegExp("<a\\s+(class=\"definition\".+?>.+?</a>)");
	var rxDict2 = new RegExp("<a\\s+(onclick=\"javascript:popWindow.+?href=.+?dictionary/db_alpha.aspx.+?</a>)","i");
	var rxDef2 = new RegExp("<a\\s+(onclick=\"javascript:popWindow.+?href=.+?popDefinition.aspx.+?</a>)");
	tempData = cGovDoRegExp1(rxDict2, tempData);
	tempData = cGovDoRegExp1(rxDef2, tempData);
	for (var i=0;i<tempData.length;i++) {
		var c = tempData.charAt(i);
		if (c == "\n") {
			result += cGovLFConst;
		}
		else if (c == "\r") {
			result += cGovCRConst;
		}
		else {
			result += c;
		}
	}
	return result;
}

/**
* Called for each regex, does the actual work of finding and editing the target link
*
*/
function cGovDoRegExp1(theRegExp, result) {
	var done = false;
	var offset = 0;
	while (!done) {
		var temp = result.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
			var target = theRegExp.exec(temp);
			if (target == null) {
				done = true;
			}
			else {
				offset += target.index;
				var iDed = cGovAddUniqueID(target[1]);
				result = result.replace(target[0],iDed);
				offset += iDed.length;
			}
		}
	}
	return result;
}

/**
* Run regex and add checkboxes to links marked with "__oldterm"
*
*/
function cGovDoRegExpAddChecks(theRegExp, data) {
	var done = false;
	var offset = 0;
	while (!done) {
		var temp = data.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
			var target = theRegExp.exec(temp);
			if (target == null) {
				done = true;
			}
			else {
				offset += target.index;
				var addition = "<input type=checkbox name=terms checked value=" + target[1] + ">" + target[0];
				data = data.replace(target[0], addition);
				offset += addition.length;
			}
		}
	}
	return data;
}

/**
* Add highlighting font to first occurence of each term
*
*/
function cGovDoRegExpFonts(theRegExp, data) {
	var done = false;
	var offset = 0;
	var existingTermsArray = [];
	while (!done) {
		var temp = data.substr(offset);
		if (temp == null) {
			done = true;
		}
		else {
			var target = theRegExp.exec(temp);
			if (target == null) {
				done = true;
			}
			else {
				var found = false;
				for (j=0;j<existingTermsArray.length;j++) {
					if (existingTermsArray[j] == target[1].toLowerCase()) {
						found = true;
						break;
					}
				}
				offset += target.index;
				if (!found) {
					existingTermsArray.push(target[1].toLowerCase());
					data = data.replace(target[1], '<font __type=\"glossifyTemp\" style=\"background-color: #ffff00;\">' + target[1] + '</font>');
				}
				offset += target[0].length;
			}
		}
	}
	return data;
}
/**
* Add the __oldterm= to the old links
*
*/
function cGovAddUniqueID(data) {
	cGovUniqueId++;
	var uniqueID = "<a __oldterm=\"" + cGovUniqueId + "\" " + data;
	return uniqueID;
}

/*
TODO: add preview URL to dictionary for the checkbox window, remove it afterwards (see line 131 of GlossifyDocument.aspx.cs) (ask Bryan about this - can't find it in configs)
TODO: configure css, fonts, javascripts, etc. (mostly on CDE site)
TODO: on perc sites, need ../nciadmin.css stylesheet for status window (currently required styles are on the page because it's not finding the css file)
TODO: add configuration of URLs, etc. in external file
*/