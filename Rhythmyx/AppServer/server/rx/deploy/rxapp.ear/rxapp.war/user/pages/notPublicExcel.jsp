<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=notPublic"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Content That Is Not Public</title>
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
	<h2>Content That Is Not Public</h2>
	<h2>Starting Folder: <%=folder%></h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content ID</th>
			<th>Content Title</th>
			<th>Workflow State</th>
			<th>Pretty URL</th>
			<th>Content Type</th>
			<th>Item Path</th>
			<th>Path to Content Item</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		CallableStatement cstmt = conn.prepareCall("{call percReport_neverpublic ?, ?}");
		cstmt.setString(1,request.getParameter("sys_contentid"));
		cstmt.setString(2,request.getParameter("allFolders"));
		ResultSet rs = cstmt.executeQuery();
		String lastType = "";
		while (rs.next()) {
			String id = rs.getString(1);
			String type = rs.getString(2);
			String displayType = type.equals(lastType) ? "" : type;
			String url = rs.getString(3);
			String path = rs.getString(4);
			String title = rs.getString(5);
			String state = rs.getString(6);
			
			if (url != null)
				url = url.replace("CancerGov", "http://www.cancer.gov");
			else
				url = "";
			out.println("<tr><td>" + id + "</td><td>" + title + "</td><td>" + state + "</td><td><a href=\"" + url + "\">" + url + "</a></td><td>" + displayType + "</td><td>" + path + "</td><td>" + path + "</td></tr>");
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
