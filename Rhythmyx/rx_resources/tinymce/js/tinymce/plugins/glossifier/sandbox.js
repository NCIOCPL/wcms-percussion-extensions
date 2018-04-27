/**
 * plugin.js
 * Custom plugin for CancerGov WYSIWYG glossifier
 *
 * Copyright, Moxiecode Systems AB
 * Released under LGPL License.
 *
 * License: http://www.tinymce.com/license
 * Contributing: http://www.tinymce.com/contributing
 */
 
/*********************************
* Global variables               *
**********************************/
var cGovReq;			//global request object
var cGovOriginalData;	//data from editor window
var cGovMassagedData;	//editor data after preprocessing
var cGovUniqueId = 0;		//id unique within this page
var cGovEditorName;		//name of editor
var cGovStatusWindow;	//window for status display
var cGovCheckboxWindow	//window for term checkboxes
var cGovCRConst = "&#x000d;";	//carriage return substitute
var cGovLFConst = "&#x000a;";	//line feed substitute
var cGovWebServiceURL = '/GlossifierProxy/services/GlossifierProxy.GlossifierSoap/';	//url of glossifier web service
var cGovPreviewURL = "www.cancer.gov";	//dictionary preview URL
var cGovElementPrefix = "ns1";
var cGovSoapPrefix = "soapenv";
var cGovSoapNameSpace = "http:?/?/?schemas.xmlsoap.org/?soap/?envelope/";
var cGovWSNameSpace = "cancer.gov/glossproxy";
var cGovSoapMethod = "cancer.gov/glossproxy/glossify";
var cGovIsEnglish = true;
var cGovLanguage = document.documentElement.lang.split('-')[0];
var _glossifyEditor;
var myElement;
var myCheckArray;
// global tinymce = true


/*********************************
* CGov Percussion functions      *
**********************************/
/**
* Send the glossification request to the web service
*/
function cGovTinyMCEGlossify(data) {
	// Pre-process the string to encode cr and lf and flag previous glossify results
	cGovMassagedData = cGovPrepareStr(data);
	// Wrap in CDATA so markup doesn't hose everything
	var safeData = '<![CDATA[' + cGovMassagedData + ']]>';
	// XML SOAP command for web service
	var soapCommand = "<?xml version=\"1.0\"?>" +
		"<soapenv:Envelope " +
		"xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' " +
		"xmlns:m='" + cGovWSNameSpace + "'> " +
		"<soapenv:Header/>" +
		"<soapenv:Body>" +
			"<m:glossify>" +
				"<m:fragment>" + safeData + "</m:fragment>" +
				"<m:dictionaries>" +
					"<m:string>Cancer.gov</m:string>" +
				"</m:dictionaries>" +
				"<m:languages>" +
					"<m:string>" + cGovLanguage + "</m:string>" +
				"</m:languages>" +
			"</m:glossify>" +
		"</soapenv:Body>" +
		"</soapenv:Envelope>";
		cGovReq = new XMLHttpRequest();

		var loadingHtml = (
			'<!DOCTYPE html>' +
			'<html>' +
			  '<head>' +
				'<title>GlossifyDocumentPrep</title>' +
				'<style type="text/css">H2 {COLOR: #333366; FONT-FAMILY: Trebuchet MS, Tahoma, Verdana, Arial, sans-serif; FONT-SIZE: 12px; FONT-WEIGHT: bold; LINE-HEIGHT: 14px}</style>' +
				'<script language="javascript" type="text/javascript">' +
				  'var prg_width = 200;' +
				  'function progress() {' +
					'var node = document.getElementById("progress");' +
					'var w = node.style.width.match(/\\d+/);' +
					'if (w == prg_width) {' +
						'w = 0;' +
					'}' +
					'node.style.width = parseInt(w) + 5 + "px";' +
				  '}' +
				  'setInterval(progress, 250);' +
				'</script>' +
			  '</head>' +
			  '<body>' +
				'<div>' +
				'<div style="border: 1px solid black; width:200px; height:10px;">' +
				  '<div id="progress" style="height:10px; width:0px; background-color:red;"/></div>' +
				'</div>' +
				'<h2>Processing document, please wait...</h2>' +
			  '</body>' +
			'</html>'
		);		
		
		/** draw html into body **/
		myElement.firstChild.src = 'data:text/html;charset=utf-8,' + encodeURIComponent(loadingHtml);
		
		console.log("== Debug myelement.firstChild 1 ==");
		console.log(myElement.firstChild);
		console.log("== End debug myelement.firstChild 1 ==");
		
		// Do this on completion of asynchronous call
		cGovReq.onreadystatechange = cGovProcessReqChange;

		// Open request and set its headers
		cGovReq.open("POST", cGovWebServiceURL, true);
		//cGovReq.setRequestHeader("Accept-Charset", "utf-8");
		cGovReq.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
		cGovReq.setRequestHeader("SOAPAction", cGovSoapMethod);
		// Send the request
		cGovReq.send(soapCommand);
		
		return true;	
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
* Attempts to retrieve elements in a namespace, using a variety of browser-specific methods
*
*/
function getElementsByTagNameNS(parent, namespace, alias, tagname) {
	elements = parent.getElementsByTagName(alias + ":" + tagname);
	if(elements.length == 0){
		elements = parent.getElementsByTagName(tagname);
	}
	if(elements.length == 0){
		elements = parent.getElementsByTagNameNS(namespace, tagname);
	}
	return elements;
}

/**
* Completion callback
*
*/
function cGovProcessReqChange() {

	if (cGovReq.readyState == 4 && cGovReq.status == 200) {
		
		//Web service transaction has completed, parse the response
		var env = getElementsByTagNameNS(cGovReq.responseXML, cGovSoapNameSpace, cGovSoapPrefix, "Envelope");
		var body = getElementsByTagNameNS(env[0], cGovSoapNameSpace, cGovSoapPrefix, "Body");
		var resp = getElementsByTagNameNS(body[0], cGovWSNameSpace, cGovElementPrefix, "glossifyResponse");
		var glossifyResult = getElementsByTagNameNS(resp[0], cGovWSNameSpace, cGovElementPrefix, "glossifyResult");
		var terms = getElementsByTagNameNS(glossifyResult[0], cGovWSNameSpace, cGovElementPrefix, "Term");

		// Put the terms values into an array
		var termsArray = cGovBuildTermsArray(terms);
		cGovMassagedData = cGovBuildCBDisplayString(cGovMassagedData, termsArray);

		
		
		var preventClicksOnLinksScript = (
			'<script>' +
				'document.addEventListener && document.addEventListener("click", function(e) {' +
					'for (var elm = e.target; elm; elm = elm.parentNode) {' +
						'if (elm.nodeName === "A") {' +
							'e.preventDefault();' +
						'}' +
					'}' +
				'}, false);' +
			'</script> '
		);		
		
		var checkboxHtml = (
				  '<!DOCTYPE html>' +
				  '<html><head>' + 
				  '<style type="text/css">H2 {COLOR: #333366; FONT-FAMILY: Trebuchet MS, Tahoma, Verdana, Arial, sans-serif; FONT-SIZE: 12px; FONT-WEIGHT: bold; LINE-HEIGHT: 14px}</style>' + 
				  '</head>' + 
				  '<body>' +
				  '<h2>Please check/uncheck the word(s) you want glossified</h2>' +
				  '<hr>' + cGovMassagedData + '<hr>' +
				  '</body>' +
				  '</html>'
		);
		
		/** Overwrite 'loading...' html with checkboxes **/
		myElement.firstChild.src = 'data:text/html;charset=utf-8,' + encodeURIComponent(checkboxHtml);

		var doc = new DOMParser().parseFromString(cGovMassagedData, 'text/html');		
		var inputElements = doc.querySelectorAll("input");
		
		
		console.log("== Debug our new collection ==");
		console.log(doc);
		console.log(inputElements);
		console.log(myCheckArray);
		myCheckArray = setCheckArray(inputElements);
		console.log(myCheckArray);
		console.log("== End debug our new collection ==");
		
		
		
	}
	
	
}

function setCheckArray(inputElements) {
	// TODO: pick up checked items
	var checkArray = [];
	if (inputElements != null) {
		var boxes = inputElements.length;
		if (boxes == null) {
			if (inputElements.checked) {
				checkArray.push(inputElements.value);
			}
		} 
		else {
			for (i = 0; i < boxes; i++) {
				console.log('== each element '  + inputElements[i]);				
				console.log(inputElements[i]);				
				if (inputElements[i].checked) {
					checkArray.push(inputElements[i].value);
				}
			}
		}
	}
	return checkArray;
}
		
function testSubmit() {
	//TODO: delete dictionary preview URL once we add it
	//	if (!cGovCheckboxWindow.closed)
	//		cGovCheckboxWindow.close();
		checkArray = [ 3, 2 ];		  
		cGovMassagedData = '<p><input type=checkbox name=terms value=5><a __newterm="5" class="definition" href="/Common/PopUps/popDefinition.aspx?id=CDR0000045333&version=Patient&language=English" onclick="javascript:popWindow("defbyid","CDR0000045333&version=Patient&language=English"); return false;"><font __type="glossifyTemp" style="background-color: #ffff00;">cancer</font></a> <input type=checkbox name=terms value=4><a __newterm="4" class="definition" href="/Common/PopUps/popDefinition.aspx?id=CDR0000535555&version=Patient&language=English" onclick="javascript:popWindow("defbyid","CDR0000535555&version=Patient&language=English"); return false;"><font __type="glossifyTemp" style="background-color: #ffff00;">ipilimumab</font></a> <input type=checkbox name=terms value=3><a __newterm="3" class="definition" href="/Common/PopUps/popDefinition.aspx?id=CDR0000767747&version=Patient&language=English" onclick="javascript:popWindow("defbyid","CDR0000767747&version=Patient&language=English"); return false;"><font __type="glossifyTemp" style="background-color: #ffff00;">nivolumab</font></a> <input type=checkbox name=terms value=2><a __newterm="2" class="definition" href="/Common/PopUps/popDefinition.aspx?id=CDR0000045134&version=Patient&language=English" onclick="javascript:popWindow("defbyid","CDR0000045134&version=Patient&language=English"); return false;"><font __type="glossifyTemp" style="background-color: #ffff00;">kaposi sarcoma</font></a></p>'
		
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

		return finalText;
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
	// _editLiveInstance.setContentForEditableSection(_getSectionDivNameByFieldName(cGovEditorName), finalText);
	_glossifyEditor.setContent(finalText);
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
//alert("terms count = " + terms.length);
	for (i=0;i<terms.length;i++) {
	// fetch the values for each Term returned by the service
		var term = new termObject();
		var start = getElementsByTagNameNS(terms[i], cGovWSNameSpace, cGovElementPrefix, "start");
		term.start = start[0].childNodes[0].nodeValue;
		var len = getElementsByTagNameNS(terms[i], cGovWSNameSpace, cGovElementPrefix, "length");
		term.length = len[0].childNodes[0].nodeValue;
		var docId = getElementsByTagNameNS(terms[i], cGovWSNameSpace, cGovElementPrefix, "docId");
		term.docId = docId[0].childNodes[0].nodeValue;
		var dictionary = getElementsByTagNameNS(terms[i], cGovWSNameSpace, cGovElementPrefix, "dictionary");
		term.dictionary = dictionary[0].childNodes[0].nodeValue;
		var language = getElementsByTagNameNS(terms[i], cGovWSNameSpace, cGovElementPrefix, "language");
		term.language = language[0].childNodes[0].nodeValue;
		var first = getElementsByTagNameNS(terms[i], cGovWSNameSpace, cGovElementPrefix, "firstOccurrence");
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
		var language;
		// Determine if we need to highlight the term
		//Build the newterm string
		if (cGovLanguage == "es")
			language = "Spanish";
		else
			language = "English";
		//alert(language);
		data = firstPart + "<input type=checkbox name=terms value=" + cGovUniqueId + "><a __newterm=\"" + cGovUniqueId + "\" class=\"definition\" href=\"/Common/PopUps/popDefinition.aspx?id=" + tobj.docId + "&version=Patient&language=" + language + "\" onclick=\"javascript:popWindow('defbyid','" + tobj.docId + "&version=Patient&language=" + language + "'); return false;\">" + text + "</a>" + lastPart;
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
	var rxDef1 = new RegExp("<a\\s+(class=\"definition\".+?>.+?</a>)");
	var rxDict2 = new RegExp("<a\\s+(onclick=\"javascript:popWindow.+?href=.+?dictionary/db_alpha.aspx.+?</a>)","i");
	var rxDef2 = new RegExp("<a\\s+(onclick=\"javascript:popWindow.+?href=.+?popDefinition.aspx.+?</a>)");
	var rxDef3 = new RegExp("<a\\s+(href=\"/Common/PopUps/popDefinition.aspx.+?</a>)");
	tempData = cGovDoRegExp1(rxDict2, tempData);
	tempData = cGovDoRegExp1(rxDef2, tempData);
	tempData = cGovDoRegExp1(rxDef1, tempData);
	tempData = cGovDoRegExp1(rxDef3, tempData);
	for (var i=0;i<tempData.length;i++) {
		var c = tempData.charAt(i);
		if (c == "\n") {
			result += cGovLFConst;
		}
		else if (c == "\r") {
			result += cGovCRConst;
		}
		else if (c == "”") {	//right doulbe quote
			result += "&#148;";
		}
		else if (c == "—") {	//em dash
			result += "&#151;";
		}
		else if (c == "–") {	//en dash
			result += "&#150;";
		}
		else if (c == "Á") {	//A accent - for some reason WS chokes on this
			result += "&#193;";
		}
		else if (c == "Í") {	//I accent
			result += "&#205;";
		}
		else {
			result += c;
		}
	}
//alert("result = " + result);
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
//alert("target = " + target);
			if (target == null) {
				done = true;
			}
			else {
				offset += target.index;
				var iDed = cGovAddUniqueID(target[1]);
				result = result.replace(target[0],iDed);
//alert("result = " + result);
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
	var fontTag;
	var existingTermsArray = [];
	while (!done) {
		var temp = data.substr(offset);
		var tempStart = data.substr(0,offset);
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
				if (!found) {
					//this term is new to us so process it and store in the known list
					existingTermsArray.push(target[1].toLowerCase());
					//assign font to the term
					fontTag = '<font __type=\"glossifyTemp\" style=\"background-color: #ffff00;\">' + target[1] + '</font>';
					temp = temp.replace(target[1], fontTag);
					offset += target.index + target[0].length + fontTag.length - target[1].length;
				}
				else {
					offset += target.index + target[0].length;
				}
				//reassemble the string
				data = tempStart + temp;
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

/**
* Pass the correct Spanish characters to soap
*
*/
function fixSpanish(editorContent, entity, character) {
	var fixedSpanish = editorContent.split(entity).join(character);
	return fixedSpanish;
}




/**
 * plugin.js
 *
 * Released under LGPL License.
 * Copyright (c) 1999-2015 Ephox Corp. All rights reserved
 *
 * License: http://www.tinymce.com/license
 * Contributing: http://www.tinymce.com/contributing
 */

/*global tinymce:true */

tinymce.PluginManager.add('glossifier', function(editor) {
	var settings = editor.settings;
	var allContent;

	editor.addCommand('openGlossifier', function() {

		allContent = editor.getContent();

		editor.windowManager.open({
			title: 'Glossifier tool',
			width: parseInt(editor.getParam("plugin_preview_width", "650"), 10),
			height: parseInt(editor.getParam("plugin_preview_height", "500"), 10),
			html: '<iframe src="javascript:\'\'" frameborder="0"></iframe>',
			buttons: [{
					//TODO: hide this on loading screen
					text: 'Submit changes', 
					onclick: function() {
				
						var myCont = testSubmit();
						alert(myCont);	
                        editor.setContent(myCont);					
				}},
				{
					text: 'Close',
					onclick: function() {
                        this.parent().parent().close();
				}}
			],
			onPostRender: function() {
				var previewHtml, loadingHtml, headHtml = '';

				headHtml += '<base href="' + editor.documentBaseURI.getURI() + '">';

				tinymce.each(editor.contentCSS, function(url) {
					headHtml += '<link type="text/css" rel="stylesheet" href="' + editor.documentBaseURI.toAbsolute(url) + '">';
				});

				var bodyId = settings.body_id || 'tinymce';
				if (bodyId.indexOf('=') != -1) {
					bodyId = editor.getParam('body_id', '', 'hash');
					bodyId = bodyId[editor.id] || bodyId;
				}

				var bodyClass = settings.body_class || '';
				if (bodyClass.indexOf('=') != -1) {
					bodyClass = editor.getParam('body_class', '', 'hash');
					bodyClass = bodyClass[editor.id] || '';
				}
				
				// Fire off legacy glossifier
				/// TODO: close window upon clicking 'submit'
				/// TODO: to set the editor value, do "editor.setContent(my_glossified_string);"
				/// TODO: make sure spanish works
				myElement = this.getEl('body');
				cGovTinyMCEGlossify(allContent);

			}
		});
	});

	editor.addButton('glossifier', {
		icon: 'glossifier" style="background-image:url(\'../rx_resources/tinymce/images/glossify.gif\');"',
		title : 'Glossify',
		cmd : 'openGlossifier'
	});

	editor.addMenuItem('glossifier', {
		icon: 'glossifier" style="background-image:url(\'../rx_resources/tinymce/images/glossify.gif\');"',
		text : 'Glossify',
		cmd : 'openGlossifier',
		context: 'tools'
	});
});
