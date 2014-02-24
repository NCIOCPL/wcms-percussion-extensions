<%@ page import="com.percussion.pso.utils.RxRequestUtils" 
%><%@ page import="gov.cancer.wcm.privateArchive.PrivateArchiveManager" 
%><%@ page import="gov.cancer.wcm.privateArchive.PrivateArchiveException" 
%><%@ page import="gov.cancer.wcm.privateArchive.ContentSummary" 
%><%@ page import="gov.cancer.wcm.util.JSPUtil" 
%><%@ page import="java.lang.StringBuilder" 
%><%@ page import="java.util.List" %><%!
public String getFormattedNumber(int number) {
	String[] words = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };
	String output;
	
	if( number >= 0 && number < words.length )
		output = words[number];
	else
		output = String.format("%,d", number);
	
	return output;
}
%><%
	int[] contentIDs = JSPUtil.GetIntParameterList(request.getParameterMap(), "sys_contentid");
	int targetID = contentIDs[0];
	
	String errorDisplay = null;
	String errorDetails = null;

	// The entry point for all Private Archive middle-tier work.
	PrivateArchiveManager mgr = new PrivateArchiveManager();

	// For a POST request, perform the desired workflow action, and then redirect to the refresh page.
	String method = request.getMethod();
	if( method.equals("POST") ) {
	
		try {
			// Attempt the transition
			mgr.performArchiveTransition(targetID);
			
			// Redirect to Percussion's page refresh.
			String url = String.format("/Rhythmyx/sys_cxSupport/redirectrefresh.html?sys_contentid=%d&refreshHint=none", targetID);
			response.sendRedirect(url);
		} catch (PrivateArchiveException e) {
			errorDisplay = e.getMessage();
		}
	}

	// For non-POST requests, or if there's an error, display a page.
	List<ContentSummary> parentItems = mgr.getParentItems(targetID);

%><!DOCTYPE html>
<html>
   <head>
      <title>Move to Private Archive</title>
		<link href="/Rhythmyx/sys_resources/css/templates.css" rel="stylesheet">
		<script type="text/javascript" src="/Rhythmyx/sys_resources/js/jquery/jquery-1.5.2.js"></script>
		<script type="text/javascript">
			$(document).ready(function () {
				$("#submitButton").bind("click", function(event) {
					if(!confirm("Do you want to continue?")) {
						event.preventDefault();
						event.stopPropagation();
					}
				});
			});
		</script>
	</head>
   <body class="backgroundcolor" leftmargin="0" marginheight="0" marginwidth="0" topmargin="0">
<% if( errorDisplay == null ) { %>
	<h2>Warning</h2>
	<p>The item you are about to Private Archive has <%= getFormattedNumber(parentItems.size()) %> items linking to it.
		If you continue, these links will be disabled.</p>
	<form id="paForm" method="POST">
		<input type="hidden" id="sys_contentid" value="<%= targetID %>" />
		<input type="hidden" id="__RequireUIWarningIgnoreCondition_warnedByUI" value="Y" />
		<input type="submit" id="submitButton" value="Move to Private Archive" />
	</form>
<% } else { %>
	<h2>Error</h2>
	<p><%= errorDisplay %>
<% } %>
	</body>
</html>
