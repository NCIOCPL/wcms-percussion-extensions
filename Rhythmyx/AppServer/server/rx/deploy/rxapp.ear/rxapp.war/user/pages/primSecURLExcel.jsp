<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=primSecURLs"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Primary with Secondary URLs of Public Content</title>
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
	<h2>Primary with Secondary URLs of Public Content</h2>
	<h2>Starting Folder: <%=folder%></h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Title</th>
			<th>Pretty URL</th>
			<th>Secondary URL</th>
			<th>Secondary URL 2</th>
			<th>Secondary URL 3</th>
			<th>Secondary URL 4</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_secondaryURL ?, ?, ?}");
		cstmt.setString(1,request.getParameter("sys_contentid"));
		cstmt.setString(2,request.getParameter("allFolders"));
		cstmt.setString(3,null);
		ResultSet rs = cstmt.executeQuery();
		while (rs.next()) {
			String title = rs.getString(2);
			String url = rs.getString(3);
			String sec1 = rs.getString(4);
			String sec2 = rs.getString(5);
			String sec3 = rs.getString(6);
			String sec4 = rs.getString(7);
			if (url != null)
				url = url.replace("CancerGov", "http://www.cancer.gov");
			else
				url = "";
			out.println("<tr><td>" + title + "</td><td><a href=\"" + url + "\">" + url + "</a></td><td>" + sec1 + "</td><td>" + sec2 + "</td><td>" + sec3 + "</td><td>" + sec4 + "</td></tr>");
		}
	}
	catch(Exception e) {
		out.println("Error in retrieving data");
	}
%>
	</table>
	</body>
</html>
