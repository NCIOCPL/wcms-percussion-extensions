<%@ page import="gov.cancer.wcm.util.ViewURLHelper" %>
<%@ page import="gov.cancer.wcm.util.TemplateUtils" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<html>
	<head>
		<title>Live Page URL</title>
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
			int ctid = Integer.parseInt(request.getParameter("sys_contenttypeid"));			
			int fid = Integer.parseInt(request.getParameter("sys_folderid"));
			String path = ViewURLHelper.getPath(cid);
			String fPath = (ViewURLHelper.getPath(fid) + "/" + ViewURLHelper.getNode(fid).getName());
			int sid = Integer.parseInt(ViewURLHelper.getSIDFromGUID(path));
			String filter = request.getParameter("loc_filter");
			int context = Integer.parseInt(request.getParameter("loc_context"));
			int mobileContext = Integer.parseInt(request.getParameter("loc_mobile_context"));
			int[] mSites = ViewURLHelper.MOBILE_SITE_IDS;
			int[] errorPages = ViewURLHelper.ERROR_PAGE_CONTENT_IDS;

			// Check if this is a mobile site. If so, use mobile linking context
			for (int i = 0; i < mSites.length; i++ ) {
				if(sid == mSites[i]) {
					context = mobileContext; 
				}
			}
		
			// Check folder paths and generate actual URL. In the event that the content
			// item has been autoshared. If so, use path of current folder 
			String s = ViewURLHelper.getPublishedURL(sid, cid, filter, context);
			if (path.equals(fPath)) {
				url = s;
			}
			else {
				int i = StringUtils.ordinalIndexOf(fPath, "/", 4);
				String prettyPath = fPath.substring(i);
				String prettyURL = s.substring(s.lastIndexOf('/') + 1);
				url = prettyPath + "/" + prettyURL;
			}
			
			// Check to see if this is an error page - output preformatted string if this
			// is the case (error pages do not have a pretty URL)
			for (int j = 0; j < errorPages.length; j++ ) {
				if(ctid == errorPages[j]) {
					String name = ViewURLHelper.getName(cid);
					name = name.substring(0, name.indexOf('['));
					url = ("/PublishedContent/ErrorMessages/" + name.replace(" ", "") + ".html");
					ViewURLHelper.isCopyableURL = true;
				}
			}
						
			if (ViewURLHelper.isCopyableURL) {
				out.println("Live Page URL: <p>");
			}
			out.println(url);
		}
		
		catch (IllegalArgumentException e) {
			out.println("<p>Error retrieving content URL:<p>" + e);
		}
%>	
		</h3>

		<div class="toolbar"><p>			
<%			
			// Check if URL is in a copyable format and the correct content type, otherwise hide "Copy" button.
			if (ViewURLHelper.isCopyableURL) { 
			/* 
			* JS from LMCButton library: http://www.lettersmarket.com/view_blog/a-3-copy_to_clipboard_lmcbutton.html
			* "Copy" button is a cross-browser flash button (.swf file) that copies the displayed URL to the clipboard
			* All necessary javascript is contained in this JSP. The .swf file and "Close" button image are saved in 
			* the images directory.
			*/
%>		
			<script type="text/javascript">
				function isNotEmpty(str) {
				return !((str == undefined) || (str == ''));
				}

				function ShowLMCButton(cliptext, capt, js, furl)
				{
					var params = 'txt=' + encodeURIComponent(cliptext); 
					if (!isNotEmpty(furl)) { furl = "/Rhythmyx/rx_resources/images/viewURL/lmcbutton.swf"; }
					if (isNotEmpty(capt)) { params += '&capt=' + capt; }
					if (isNotEmpty(js)) { params += '&js=' + js; }
					 
					document.write('<object width="40" height="20">');
					document.write(' <param name="movie" value="' + furl + '">');
					document.write(' <PARAM NAME=FlashVars VALUE="' + params + '">');
					document.write(' <embed src="' + furl + '" flashvars="' + params + '"  width="40" height="20"></embed>');
					document.write('</object>');
				}
			</script>			
			<script type="text/javascript"> ShowLMCButton('<%= url %>', 'Copy');  </script>
			&nbsp;
<% 			} %>		
			
			<a href="javascript:window.close();">
				<img src="/Rhythmyx/rx_resources/images/viewURL/jsp_close_button.png" alt="Close" border="0"> 
			</a>
			
		</div>
	</body>
</html>