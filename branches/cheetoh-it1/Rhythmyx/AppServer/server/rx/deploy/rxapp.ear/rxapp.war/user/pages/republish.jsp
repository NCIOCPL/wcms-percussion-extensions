<%@ page import="gov.cancer.wcm.publishing.CGV_OnDemandPublishService" %>
<%@ page import="gov.cancer.wcm.publishing.CGV_OnDemandPublishServiceLocator" %>


<html>
	<head>
		<title>Processing POD Request</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	
	<body>
		<h3> 
		Your request is being processed. Please wait a moment...
<% 
		int contentId = Integer.parseInt(request.getParameter("sys_contentid"));
		
		// Push selected content to Publish on Demand
		CGV_OnDemandPublishService svc = CGV_OnDemandPublishServiceLocator.getCGV_OnDemandPublishService();
		svc.publishOnDemand(contentId);	

        // Redirect to the Rhythmyx refresh page. Method sendRedirect() sends a temporary redirect response to the client using the specified URL and clears the buffer
        String url = String.format("/Rhythmyx/sys_cxSupport/redirectrefresh.html?sys_contentid=%d&refreshHint=none", contentId);
		response.sendRedirect(url);
%>
		</h3>
		<div class="toolbar">
			<input type="button" value="Close" onclick="window.close();" class="close">
		</div>
	</body>
</html>
