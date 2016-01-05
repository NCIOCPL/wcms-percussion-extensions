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
			<th>English Content ID</th>
			<th>English Content Title</th>
			<th>English Pretty URL</th>
			<th>English Item Path</th>
			<th>English Content Type</th>
			<th>Has a Translation Relationship With Content ID</th>
			<th>Has a Translation Relationship With Content Title</th>
			<th>Has a Translation Relationship With Pretty URL</th>
			<th>Has a Translation Relationship With Item Path</th>
			<th>Has a Translation Relationship With Content Type</th>

		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_translation ?}");
		cstmt.setString(1,request.getParameter("sys_contentid"));
		ResultSet rs = cstmt.executeQuery();
		while (rs.next()) {
			String englishid = rs.getString(2);
			String englishtitle = rs.getString(3);
			String englishurl = rs.getString(4);
			String englishpath = rs.getString(5);
			String englishcontenttype = rs.getString(1);
			String spanishid = rs.getString(9);
			String spanishtitle = rs.getString(10);
			String spanishurl = rs.getString(11);
			String spanishpath = rs.getString(12);
			String spanishcontenttype = rs.getString(16);
			out.println("<tr><td>" + englishid + "</td><td>" + englishtitle + "</td><td>" + englishurl + "</td><td>" + englishpath + "</td><td>"+ englishcontenttype  + "</td><td>" + spanishid + "</td><td>" + spanishtitle + "</td><td>" + spanishurl + "</td><td>" + spanishpath + "</td><td>"+  spanishcontenttype + "</td></tr>");
		}
	}
	catch(Exception e) {
		out.println(e.getMessage());
	}
%>
	</table>
	</body>
</html>
