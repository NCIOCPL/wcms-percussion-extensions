<%@ page import="gov.cancer.wcm.extensions.CGV_Reports" %>
<%@ page import="java.util.*" %>


<html>
	<head>
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>Field Usage Report</title>
		<script language="javascript">
		function cTypeChanged(newVal){
			var fid = <%= request.getParameter("sys_contentid") %>;
			location.href='fieldUsage.jsp?sys_contentid='+fid+'&content_type='+newVal;
			return true;
		}
		function doReport()
		{
			df=document.forms[0];	
			var fid = <%= request.getParameter("sys_contentid") %>;
			var ctypeDD = document.getElementById("contentType");
			var ctype = ctypeDD.options[ctypeDD.selectedIndex].value;
			var fieldDD = document.getElementById('field');
			var fieldval = fieldDD.options[fieldDD.selectedIndex].value;
			var fvsplit = fieldval.split(",");
			var field = fvsplit[0];
			var scopeDD = document.getElementById('fieldScope');
			var scope = scopeDD.options[scopeDD.selectedIndex].text;
			var subfolders = document.getElementById('allFolders').checked;
			var fieldType = fvsplit[1];
			if (document.getElementById('excel').checked == true) {
				df.action="fieldUsageExcel.jsp?sys_contentid="+fid+"&content_type="+ctype+"&field="+field+"&subfolders="+subfolders+"&scope="+scope+"&field_type="+fieldType;
			}
			else {	
				df.action="fieldUsageHTML.jsp?sys_contentid="+fid+"&content_type="+ctype+"&field="+field+"&subfolders="+subfolders+"&scope="+scope+"&field_type="+fieldType;
			}
			document.report.submit();
		}
		</script>
		<style type="text/css">
			<%@include file="reports.css" %>
		</style>	
		<!--[if lt IE 9]>
	<style>
		fieldset legend {
			line-height: 1.2em;
			margin-top: -12px;
			margin-bottom: 4px;
		}
	</style>
	<![endif]-->

	<!--[if lt IE 8]>
	<style>
		.form-row.buttons input {
			padding: 4px 0;
		}
	</style>
	<![endif]-->
	</head>
	<body onload="javascript:self.focus();">
	<h1>Field Usage Report</h1>
	<p>A report that shows activity for a Content Type/Data Field in Cancer.gov
	
	<h2>INPUTS:</h2>
	<form action="" method="post" name="report">
	<input name="sys_contentid" type="hidden" value='<%= request.getParameter("sys_contentid") %>' />
	<fieldset>
		<label for="contentType">Content Type:</label>
		<select name="contentType" id="contentType" onChange="cTypeChanged(this.value)">
		<%
			CGV_Reports cgvReports = new CGV_Reports();
			HashMap cTypeMap = cgvReports.getContentTypeNames();
			Object[] ckeys = cTypeMap.keySet().toArray();
			Arrays.sort(ckeys);
			for(int j = 0; j<ckeys.length; j++){
				String ckey = (String)ckeys[j];
				String sel = "";
				if(request.getParameter("content_type") == null){
					if(j == 0){
						sel = "selected";
					}
				}
				else if(cTypeMap.get(ckey).equals(request.getParameter("content_type"))){
						sel = "selected";
				}
			%>
			<p><%=cTypeMap.get(ckey).equals(request.getParameter("content_type"))%></p>
			<option value=<%=cTypeMap.get(ckey)%> <%=sel%>><%=ckey%></option>
			<%}%>
		</select>
		</br>
		<label for="field">Data Field:</label>
		<select name="field" id="field">
		<%
		String ctypeName = request.getParameter("content_type");
		if(request.getParameter("content_type") ==null){
			ctypeName = (String)cTypeMap.get(ckeys[0]);
			}
		HashMap fieldMap = cgvReports.getDataFieldNames(ctypeName);
		Object[] keys = fieldMap.keySet().toArray();
		Arrays.sort(keys);
		for(int i = 0; i<keys.length; i++){
			String sel = "";
			String key = (String)keys[i];
			HashMap subMap = (HashMap) fieldMap.get(key);
			if(i==0){sel = " selected";}
		%>
		<option value=<%= subMap.get("fieldName") %>,<%=subMap.get("fieldControlRef")%><%=sel%>><%= subMap.get("fieldLabel") %></option>
		<%}%>
		</select>
		</br>
		<label for="fieldScope">Field Scope:</label>
		<select name="fieldScope" id="fieldScope">
			<option value=0 selected>Empty</option>
			<option value=1>Not Empty</option>
			<option value=2>All</option>
		</select>
		</br>
		<label for="allFolders">Include All Subfolders: </label>
		<input id="allFolders" value="1" type="checkbox" class="checkbox" />
		<label for="allFolders">Yes</label>
		</br>
		<label for="excel">Report Format: </label>
		<input id="excel" value="1" type="checkbox" checked class="checkbox" />
		<label for="excel">Output as Excel</label>
	</fieldset>			
		<div class="form-row buttons">
			<input name="close" onclick="javascript:window.close();" type="button" value="Cancel" class="cancel" />
			<input name="run" onclick="javascript:doReport();" type="button" value="Run" class="submit" />
		</div>
		</form>
	</body>
	
</html>
