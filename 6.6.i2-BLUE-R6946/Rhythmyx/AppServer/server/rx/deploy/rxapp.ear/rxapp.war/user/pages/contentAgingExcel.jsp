<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=contentAging"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<html>
	<head>
		<title>Content Aging Report</title>
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
	<h2>Content Aging Report</h2>
	<h2>Starting Folder: <%=folder%></h2>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content Type</th>
			<th>Content Title</th>
			<th>Primary Pretty URL</th>
			<th>Last Modified Date</th>
			<th>Posted Date</th>
			<th>Revised Date</th>
			<th>Review Date</th>
		</tr>
<%
	Connection conn = null;
	try {
		conn = PSConnectionHelper.getDbConnection();
		String contentID = request.getParameter("sys_contentid");
		CallableStatement cstmt = conn.prepareCall("{call percReport_contentaging ?, ?}");
		cstmt.setString(1,contentID);
		cstmt.setString(2,request.getParameter("allFolders"));
		ResultSet rs = cstmt.executeQuery();
		String lastType = "";
		while (rs.next()) {
			String type = rs.getString(1);
			String displayType = type.equals(lastType) ? "" : type;
			String title = rs.getString(2);
			String url = rs.getString(3);
			if (url != null)
				url = url.replace("CancerGov", "http://www.cancer.gov");
			else
				url = "";
			String modified = rs.getString(4);
			String posted = rs.getString(5);
			String revised = rs.getString(6);
			String reviewed = rs.getString(7);
			if (modified != null)
				modified = modified.substring(0,10);
			else
				modified = "";
			if (posted != null)
				posted = posted.substring(0,10);
			else
				posted = "";
			if (revised != null)
				revised = revised.substring(0,10);
			else
				revised = "";
			if (reviewed != null)
				reviewed = reviewed.substring(0,10);
			else
				reviewed = "";
			out.println("<tr><td>" + displayType + "</td><td>" + title + "</td><td><a href=\"" + url + "\">" + url + "</a></td><td>" + posted + "</td><td>" + revised + "</td><td>" + reviewed + "</td></tr>");
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
