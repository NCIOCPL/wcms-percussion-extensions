<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=sharedContent"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Shared Content Items</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	<body>
	<h2>Shared Content Items</h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content Type</th>
			<th>Title</th>
			<th>Parent</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		String contentID = request.getParameter("sys_contentid");
		//If we pass in the contentid of the site, the report doesn't return data - at site level must pass in null
		if (contentID.equals("305"))
			contentID = null;
		CallableStatement cstmt = conn.prepareCall("{call percReport_sharedcontent ?}");
		cstmt.setString(1,contentID);
		ResultSet rs = cstmt.executeQuery();
		String lastType = "";
		String lastTitle = "";
		while (rs.next()) {
			boolean isUrl = false;
			String type = rs.getString(2);
			String displayType = type.equals(lastType) ? "" : type;
			String title = rs.getString(3);
			String displayTitle = title.equals(lastTitle) ? "" : title;
			String parent = rs.getString(6);
			if (parent != null) {
				if (parent.contains("CancerGov")) {
					parent = parent.replace("CancerGov", "http://www.cancer.gov");
					isUrl = true;
				}
			}
			else
				parent = "";
			if (isUrl)
				out.println("<tr><td>" + displayType + "</td><td>" + displayTitle + "</td><td><a href=\"" + parent + "\">" + parent + "</a></td></tr>");
			else
				out.println("<tr><td>" + displayType + "</td><td>" + displayTitle + "</td><td>" + parent + "</td></tr>");
			lastType = type;
			lastTitle = title;
		}
	}
	catch(Exception e) {
		out.println("Error in retrieving data");
	}
%>
	</table>
	</body>
</html>
