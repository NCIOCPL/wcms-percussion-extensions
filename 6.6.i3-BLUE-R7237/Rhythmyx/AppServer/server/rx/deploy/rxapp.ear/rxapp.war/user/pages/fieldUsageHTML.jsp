<%@ page import="gov.cancer.wcm.extensions.CGV_Reports" %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>

<html>
	<head>
		<title>Field Usage Report</title>
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
	String wo = "without";
	if(request.getParameter("subfolders").equals("true")){
		wo = "with";
	}
	%>
	<h2>Field Usage Report</h2>
	<ul style="list-style-type:none;">
		<li><strong>Folder:</strong> <%=folder%> <%=wo%> subfolders</li>
		<li><strong>Content Type:</strong> <%=request.getParameter("content_type")%></li>
		<li><strong>Data Field:</strong> <%=request.getParameter("field")%></li>
		<li><strong>Field Scope:</strong> <%=request.getParameter("scope")%></li>
	</ul>
	<div class="toolbar">
		<input type="button" value="Print" onclick="window.print();" class="Print"> 
		<input type="button" value="Close" onclick="window.close();" class="close"> 
		<input type="button" value="Back" onclick="window.history.go(-1);" class="back"> 
	</div>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Content ID</th>
			<th>Content Title</th>
			<th>Path to Content Item</th>
			<th>Field Content</th>
		</tr>
		<%
		CGV_Reports cgvReports = new CGV_Reports();
		Map mapList = cgvReports.report_FieldUsage(request.getParameter("sys_contentid"),request.getParameter("subfolders"), request.getParameter("content_type"), request.getParameter("field"));
		Object[] mapKeys = mapList.keySet().toArray();
		for(int i = 0; i < mapKeys.length; i++){
			HashMap fieldMap = (HashMap) mapList.get(mapKeys[i]);
			String cid = (String) fieldMap.get("content_id");
			String sys_title =(String) fieldMap.get("sys_title");
			String content =(String) fieldMap.get("data_field");
			String scope = request.getParameter("scope");
			String path = (String) fieldMap.get("pretty_url_name");
			if(scope.equals("Empty") && !content.equals("")){
				continue;
			}
			else if(scope.equals("Not Empty") && content.equals("")){
				continue;
			}
			String prettyContent = content;
			String fieldType = request.getParameter("field_type");
			boolean empty = content.equals("");
				if(fieldType.equals("sys_CalendarSimple")){}
				else if(fieldType.equals("sys_DropDownSingle")){
					if(empty){
						prettyContent = "Unselected";
					}
					}
				else if(fieldType.equals("sys_EditBox")){
					}
				else if(fieldType.equals("sys_EditLive")){
					prettyContent="Not Empty";
					if(empty){
						prettyContent="Empty";
					}
					}
				else if(fieldType.equals("sys_EditLiveDynamic")){
					prettyContent="Not Empty";
					if(empty){
						prettyContent="Empty";
					}
					}
				else if(fieldType.equals("sys_FileWord")){
					}
				else if(fieldType.equals("sys_HtmlEditor")){
					}
				else if(fieldType.equals("sys_RadioButtons")){
					if(empty){
						prettyContent="Unselected";
					}
					}
				else if(fieldType.equals("sys_SingleCheckBox")){
					prettyContent = "Checked";
					if(empty || content=="false"){
						prettyContent = "Unchecked";
					}
					}
				else if(fieldType.equals("sys_TextArea")){			
					}
				else if(fieldType.equals("sys_VariantDropDown")){
					}
				else if(fieldType.equals("sys_CheckBoxGroup")){
					}					
				else if(fieldType.equals("sys_CheckBoxTree")){
					}			
				else if(fieldType.equals("sys_DropDownMultiple")){
					}

		%>
		<tr>
			<td><%=cid%></td>
			<td><%=sys_title%></td>
			<td><%=path%></td>
			<td><%=prettyContent%></td>
		</tr>
		<%}%>
	</table>
	</body>
</html>
