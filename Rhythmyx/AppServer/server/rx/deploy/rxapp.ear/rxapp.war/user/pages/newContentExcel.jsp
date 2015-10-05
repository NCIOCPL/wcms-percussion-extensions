<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=newContent"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>New Content</title>
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
	<h2>New Content</h2>
	<h2>Starting Folder: <%=folder%></h2>
	<p>Date Range: <%= request.getParameter("startDate") %> - <%= request.getParameter("endDate") %> </p>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content Type</th>
			<th>Title</th>
			<th>PrettyURL</th>
			<th>Date Created</th>
			<th>Last Modified Date</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_newContent ?, ?, ?, ?}");
		cstmt.setString(1,request.getParameter("type"));
		cstmt.setString(2,request.getParameter("startDate"));
		cstmt.setString(3,request.getParameter("endDate"));
		cstmt.setString(4,request.getParameter("sys_contentid"));
		ResultSet rs = cstmt.executeQuery();
		String lastType = "";
		while (rs.next()) {
			String type = rs.getString(3);
			String displayType = type.equals(lastType) ? "" : type;
			String title = rs.getString(2);
			String url = rs.getString(4);
			String created = rs.getString(5);
			String modified = rs.getString(6);
			if (url != null)
				url = url.replace("CancerGov", "http://www.cancer.gov");
			else
				url = "";
			created = created.substring(0,10);
			modified = modified.substring(0,10);
			out.println("<tr><td>" + displayType + "</td><td>" + title + "</td><td><a href=\"" + url + "\">" + url + "</a></td><td>" + created + "</td><td>" + modified + "</td></tr>");
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
