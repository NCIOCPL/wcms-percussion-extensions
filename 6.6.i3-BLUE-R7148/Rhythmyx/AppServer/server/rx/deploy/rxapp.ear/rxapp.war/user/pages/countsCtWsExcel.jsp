<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=countsCtWs"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Counts by Content Type of Workflow State</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
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
	<h2>Counts by Content Type of Workflow State</h2>
	<h2>Starting Folder: <%=folder%></h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content Type</th>
			<th>Workflow State</th>
			<th>Count</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_contentcount ?, ?, ?}");
		cstmt.setString(1,request.getParameter("sys_contentid"));
		cstmt.setString(2,request.getParameter("allFolders"));
		cstmt.setString(3,request.getParameter("type"));
		ResultSet rs = cstmt.executeQuery();
		String lastType = "";
		while (rs.next()) {
			String type = rs.getString(1);
			String displayType = type.equals(lastType) ? "" : type;
			String state = rs.getString(2);
			String count = rs.getString(3);
			out.println("<tr><td>" + displayType + "</td><td>" + state + "</td><td>" + count + "</td></tr>");
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
