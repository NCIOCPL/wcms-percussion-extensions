<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Translation Relationships</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	<body>
<%
	String folder = "";
	Connection cn = null;
	try {
		cn = PSConnectionHelper.getDbConnection();
		String query = "select f.ParentID,f.foldername from dbo.folder f where f.id = ?";
		PreparedStatement pstmt = cn.prepareStatement(query);
		pstmt.setString(1,request.getParameter("sys_contentid"));
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			folder = rs.getString(2);
		}
	}
	catch(Exception e) {
		out.println("Error in retrieving data");
	}
%>
	<h2>Translation Relationships</h2>
	<h1>Starting Folder: <%=folder%></h1>
	<div class="toolbar">
	<input type="button" value="Print" onclick="window.print();" class="Print"> <input type="button" value="Close" onclick="window.close();" class="close"> <input type="button" value="Back" onclick="window.history.go(-1);" class="back"> </div>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content ID</th>
			<th>Content Title</th>
			<th>Has a Translation Relationship With</th>
			<th>Item Path</th>
			<th>Path to Content Item</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_translation ?}");
		cstmt.setString(1,request.getParameter("sys_contentid"));
		ResultSet rs = cstmt.executeQuery();
		while (rs.next()) {
			String id = rs.getString(1);
			String title = rs.getString(2);
			String path = rs.getString(4);
			String relatedItem = rs.getString(9);
			out.println("<tr><td>" + id + "</td><td>" + title + "</td><td>" + relatedItem + "</td><td>" + path + "</td><td>" + path + "</td></tr>");
		}
	}
	catch(Exception e) {
		out.println(e.getMessage());
	}
%>
	</table>
	</body>
</html>
