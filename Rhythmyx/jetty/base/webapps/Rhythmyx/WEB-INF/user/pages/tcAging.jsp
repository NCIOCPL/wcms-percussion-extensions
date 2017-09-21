<!DOCTYPE html>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Timely Content Aging Report</title>
	<script language="javascript">
		function doReport()
		{
			df=document.forms[0];
			var dateReg = new RegExp("^([0-9]{4})-([0-9]{2})-([0-9]{2})$");
			var start = document.getElementById('startDate').value;
			var end = document.getElementById('endDate').value;
			var matches1 = dateReg.exec(start);
			var matches2 = dateReg.exec(end);
			matches = matches1 != null && matches2 != null;
			if(!matches) {
				alert("Date fields must be in the format yyyy-mm-dd");
			}
			else {
				var valid = true;
				sYear = start.substring(0,4) - 0;
				sMonth = start.substring(5,7) - 1;
				sDay = start.substring(8,10) - 0;
				if ((sYear < 1900 || sYear > 2100) || sMonth > 12 || sDay > 31)
					valid = false;
				eYear = start.substring(0,4) - 0;
				eMonth = start.substring(5,7) - 1;
				eDay = start.substring(8,10) - 0;
				if ((eYear < 1900 || eYear > 2100) || eMonth > 12 || eDay > 31)
					valid = false;
				
				if (!valid)
					alert("Date field has invalid value");
					
				else {
					if (document.getElementById('excel').checked == true) {
						df.action="tcAgingExcel.jsp";
					}
					else {
						df.action="tcAgingHtml.jsp";
					}
					document.report.submit();
				}
			}
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
		<h1>Timely Content Aging Report</h1>
		<p>For the date range entered below, lists every time a particular timely content block was live on the site and in which Timely Content Zone.</p>
		<form action="" method="get" name="report">
		<input name="sys_contentid" type="hidden" value='<%= request.getParameter("sys_contentid") %>' />
		<div id="report_input">
			<fieldset>
				<label for="startDate">Start date:</label>
				<input id="startDate" name="startDate" value="yyyy-mm-dd" type="text" class="textfield" />
				<label for="endDate">End date:</label>
				<input id="endDate" name="endDate" value="yyyy-mm-dd" type="text" class="textfield" />
			</fieldset>
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
