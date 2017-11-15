<!DOCTYPE html>
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
      <title>Custom Link Report</title>
	  <script language="javascript">
		function doReport()
		{
			df=document.forms[0];
			var subfolders = document.getElementById('allFolders').checked;
			var cid  = <%= request.getParameter("sys_contentid") %>;
			if (document.getElementById('excel').checked == true) {
				df.action="customLinkExcel.jsp?subfolders="+subfolders+"&sys_contentid="+cid;
			}
			else {
				df.action="customLinkHTML.jsp?subfolders="+subfolders+"&sys_contentid="+cid;
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
	  <h1>Custom Link Report</h1>
	  <p>A report that inventories Custom Link Title activity within a chosen folder</p>
      <form action="" method="post" name="report">
	  <input name="sys_contentid" type="hidden" value='<%= request.getParameter("sys_contentid") %>' />
			<fieldset>
				<legend>Custom Link Report:</legend>
				<label for="allFolders">Include All Subfolders: </label>
				<input id="allFolders" value="1" type="checkbox" class="checkbox" />
				<label for="allFolders">Yes</label>
				</br>
				<label for="excel">Report Format: </label>
				<input id="excel" value="1" type="checkbox" checked="yes" class="checkbox" />
				<label for="excel">Output as Excel</label>
			</fieldset>			
			<div class="form-row buttons">
				<input name="close" onclick="javascript:window.close();" type="button" value="Cancel" class="cancel" />
				<input name="run" onclick="javascript:doReport();" type="button" value="Run" class="submit" />
			</div>
      </form>
   </body>
</html>
