<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=unusedContent"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Unused Content Items</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	<body>
	<h2>Unused Content Items</h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content Type</th>
			<th>Title</th>
			<th>Last Modified Date</th>
			<th>Workflow State</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		String contentID = request.getParameter("sys_contentid");
		//If we pass in the contentid of the site, the report doesn't return data - at site level must pass in null
		if (contentID.equals("305"))
			contentID = null;
		CallableStatement cstmt = conn.prepareCall("{call percReport_unusedcontent ?}");
		cstmt.setString(1,contentID);
		ResultSet rs = cstmt.executeQuery();
		String lastType = "";
		while (rs.next()) {
			String type = rs.getString(3);
			String displayType = type.equals(lastType) ? "" : type;
			String title = rs.getString(2);
			String modified = rs.getString(6);
			modified = modified.substring(0,10);
			String state = rs.getString(4);
			out.println("<tr><td>" + displayType + "</td><td>" + title + "</td><td>" + modified + "</td><td>" + state + "</td></tr>");
			lastType = type;
		}
	}
	catch(Exception e) {
		out.println("Error in retrieving data");
	}
%>
	</table>
	</body>
</html>
