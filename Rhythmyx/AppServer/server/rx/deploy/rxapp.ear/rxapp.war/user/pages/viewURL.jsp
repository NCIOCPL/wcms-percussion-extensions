<%@ page import="gov.cancer.wcm.util.ViewURLHelper" %>
<%@ page import="gov.cancer.wcm.util.TemplateUtils" %>
<%@ page import="java.io.IOException" %>

<html>
	<head>
		<title>Live URL for Content Item</title>
		<style type="text/css">
		<%@include file="viewurl.css" %>
		</style>
	</head>
	
	<body>
		<h3>
		<div class="offset">&nbsp;</div>

<% 
		// Get content ID, site ID, filter, and context to generate url
		// TODO: add JS for copy to clipboard button
		// TODO: dynamic ocntexts - 313 for mobile, 304 for desktop
		// TODO: determine filter value according to summary.getContentStateId();
		// TODO: figure out which workflow states should display menu item
		String url = "";
		int cid = Integer.parseInt(request.getParameter("sys_contentid"));
		String path = ViewURLHelper.getPath(cid);
		int sid = Integer.parseInt(ViewURLHelper.getSIDFromGUID(path));
		String filter = request.getParameter("loc_filter");
		int context = Integer.parseInt(request.getParameter("loc_context"));
		
		url = ViewURLHelper.getPublishedURL(sid, cid, filter, context);
		out.println(url);
%>	

		</h3>
	<div class="toolbar">
		<input type="button" value="Close" onclick="window.close();" class="back">
<%		
		if (ViewURLHelper.showCopyButton == true) { 
%>		
		&nbsp;
		<input type="button" value="Copy to Clipboard" class="copy">
<% } %>		
	</div>
	</body>
</html>
