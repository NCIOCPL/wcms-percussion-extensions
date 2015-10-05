<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Tile Aging Report</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	<body>
	<h2>Tile Aging Report</h2>
	<div class="toolbar">
	<input type="button" value="Print" onclick="window.print();" class="Print"> <input type="button" value="Close" onclick="window.close();" class="close"> <input type="button" value="Back" onclick="window.history.go(-1);" class="back"> </div>
	<p>Date Range: <%= request.getParameter("startDate") %> - <%= request.getParameter("endDate") %> </p>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content ID</th>
			<th>Content Title</th>
			<th>Where Published</th>
			<th>Date Published to Live Site</th>
			<th>Date Removed from Live Site</th>
			<th>Item Path</th>
			<th>Path to Content Item</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		String contentID = request.getParameter("sys_contentid");
		//If we pass in the contentid of the site, the report doesn't return data - at site level must pass in null
		if (contentID.equals("305"))
			contentID = null;
		CallableStatement cstmt = conn.prepareCall("{call percReport_TileAging ?, ?, ?}");
		cstmt.setString(1,request.getParameter("startDate"));
		cstmt.setString(2,request.getParameter("endDate"));
		cstmt.setString(3,contentID);
		ResultSet rs = cstmt.executeQuery();
		String lastTitle = "";
		while (rs.next()) {
			String id = rs.getString(1);
			String title = rs.getString(2);
			String displayTitle = title.equals(lastTitle) ? "" : title;
			String where = rs.getString(5);
			String path = rs.getString(6);
			String published = rs.getString(7);
			published = published.substring(0,10);
			String removed = rs.getString(8);
			if (removed != null)
				removed = removed.substring(0,10);
			else
				removed = "";
			out.println("<tr><td>" + id + "</td><td>" + displayTitle + "</td><td>" + where + "</td><td>" + published + "</td><td>" + removed + "</td><td>" + path + "</td><td>" + path + "</td></tr>");
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
