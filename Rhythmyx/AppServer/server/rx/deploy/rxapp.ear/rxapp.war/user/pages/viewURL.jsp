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
		// TODO: display secondary URLs
		// Get content ID, site ID, filter, and context to generate url
		String url = "";

		try {
			int cid = Integer.parseInt(request.getParameter("sys_contentid"));
			String path = ViewURLHelper.getPath(cid);
			int sid = Integer.parseInt(ViewURLHelper.getSIDFromGUID(path));
			String filter = request.getParameter("loc_filter");
			int context = Integer.parseInt(request.getParameter("loc_context"));
			int mobileContext = Integer.parseInt(request.getParameter("loc_mobile_context"));
			int[] mSites = ViewURLHelper.MOBILE_SITE_IDS;

			// Check if this is a mobile site. If so, use mobile linking context
			for (int i = 0; i < mSites.length; i++ ) {
				if(sid == mSites[i]) {
					context = mobileContext; 
				}
			}
		
			// Generate actual URL
			url = ViewURLHelper.getPublishedURL(sid, cid, filter, context);
			if (ViewURLHelper.isCopyableURL) {
				out.println("Live site URL: <p>");
			}
			out.println(url);
		}
		
		catch (IllegalArgumentException e) {
			out.println("<p>Error retrieving content URL:<p>" + e);
		}
%>	
		</h3>

		<div class="toolbar">
		<input type="button" value="Close" onclick="window.close();" class="back">
<%		
		// TODO: add JS for copy to clipboard - button commented out for now
		// if (ViewURLHelper.isCopyableURL) { 
%>		
		<!-- &nbsp;
		<input type="button" value="Copy to Clipboard" class="copy"> -->
<% 		// } %>		
		</div>
	</body>
</html>
