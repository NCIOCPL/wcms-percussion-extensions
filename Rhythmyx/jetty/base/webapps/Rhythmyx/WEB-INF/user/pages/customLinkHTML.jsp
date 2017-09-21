<%@ page import="gov.cancer.wcm.extensions.CGV_Reports" %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>

<html>
	<head>
		<title>Custom Link Report</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	<body><%
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
	String wo = "not including";
	if(request.getParameter("subfolders").equals("true")){
		wo = "including";
	}
	%>
	<h2>Custom Link Title Report</h2>
	<strong>Folder:</strong> <%=folder%> <%=wo%> subfolders
	</br>
	<div class="toolbar">
		<input type="button" value="Print" onclick="window.print();" class="Print"> 
		<input type="button" value="Close" onclick="window.close();" class="close"> 
		<input type="button" value="Back" onclick="window.history.go(-1);" class="back"> 
	</div>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Custom Link Content ID</th>
			<th>Custom Link System Title</th>
			<th>Target Content Title</th>
			<th>Target Path</th>
			<th>Original Title</th>
			<th>Override Title</th>
			<th>Original Short Title</th>
			<th>Override Short Title</th>
			<th>Original Long Description</th>
			<th>Override Long Description</th>
			<th>Original Short Description</th>
			<th>Override Short Description</th>

		</tr>
		<%
		CGV_Reports reportTools = new CGV_Reports();
		List mapList = reportTools.report_CustomLink(request.getParameter("sys_contentid"),request.getParameter("subfolders"));
		for(int i = 0; i < mapList.size(); i++){
			HashMap fieldMap = (HashMap) mapList.get(i);
			String cl_cid = (String) fieldMap.get("cl_cid");
			String cl_sys_title =(String) fieldMap.get("cl_sys_title");
			String cl_override_long_title =(String) fieldMap.get("cl_override_long_title");
			String cl_override_short_title = (String) fieldMap.get("cl_override_short_title");
			String cl_override_long_desc = (String) fieldMap.get("cl_override_long_desc");
			String cl_override_short_desc = (String) fieldMap.get("cl_override_short_desc");
			
			String tgt_sys_title = (String) fieldMap.get("tgt_sys_title");
			String tgt_long_title = (String) fieldMap.get("tgt_long_title");
			String tgt_short_title = (String) fieldMap.get("tgt_short_title");
			String tgt_long_desc = (String) fieldMap.get("tgt_long_desc");
			String tgt_short_desc = (String) fieldMap.get("tgt_short_desc");
			String tgt_path = (String) fieldMap.get("tgt_path");

			
		%>
		<tr>
			<td><%=cl_cid%></td>
			<td><%=cl_sys_title%></td>
			<td><%=tgt_sys_title%></td>
			<td><%=tgt_path%></td>
			<td><%=tgt_long_title%></td>
			<td><%=cl_override_long_title%></td>
			<td><%=tgt_short_title%></td>
			<td><%=cl_override_short_title%></td>
			<td><%=tgt_long_desc%></td>
			<td><%=cl_override_long_desc%></td>
			<td><%=tgt_short_desc%></td>
			<td><%=cl_override_short_desc%></td>
			
		</tr>
		<%}%>
	</table>
	</body>
</html>
