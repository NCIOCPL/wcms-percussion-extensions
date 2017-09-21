<!DOCTYPE html>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Translation Relationships Report</title>
	<script language="javascript">
		function doReport()
		{
			df=document.forms[0];
			if (document.getElementById('excel').checked == true) {
				df.action="translationExcel.jsp";
			}
			else {
				df.action="translationHtml.jsp";
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
		<h1>Translation Relationships Report</h1>
		<p>Identifies content items associated via a translation relationship.</p>
		<form action="" method="post" name="report">
		<input name="sys_contentid" type="hidden" value='<%= request.getParameter("sys_contentid") %>' />
		<div id="report_input">
			<fieldset>
				<legend>Report Format:</legend>
				<input id="excel" value="1" type="checkbox" checked="yes" class="checkbox" /><label for="excel">Output as Excel</label>
			</fieldset>
			<div class="form-row buttons">
				<input name="close" onclick="javascript:window.close();" type="button" value="Cancel" class="cancel" />
				<input name="run" onclick="javascript:doReport();" type="button" value="Run" class="submit" />
			</div>
		</div>
		</form>
	</body>
</html>
