<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=translation"); %>
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
	<h2>Starting Folder: <%=folder%></h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>This Page</th>
			<th>Has a Translation Relationship With</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_translation ?}");
		cstmt.setString(1,request.getParameter("sys_contentid"));
		ResultSet rs = cstmt.executeQuery();
		while (rs.next()) {
			String thisun = rs.getString(2);
			String thatun = rs.getString(8);
			out.println("<tr><td>" + thisun + "</td><td>" + thatun + "</td></tr>");
		}
	}
	catch(Exception e) {
		out.println(e.getMessage());
	}
%>
	</table>
	</body>
</html>