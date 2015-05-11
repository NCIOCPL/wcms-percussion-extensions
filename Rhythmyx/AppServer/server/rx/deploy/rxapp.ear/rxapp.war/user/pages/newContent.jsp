<!DOCTYPE html>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>New Content Report</title>
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
						df.action="newContentExcel.jsp";
					}
					else {
						df.action="newContentHtml.jsp";
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
		<h1>New Content Report</h1>
		<p>Lists content items that were new, including items that were new and also modified within the date range. Does not include PDQ content.</p>
		<form action="" method="post" name="report">
		<input name="sys_contentid" type="hidden" value='<%= request.getParameter("sys_contentid") %>' />
		<div id="report_input">
			<fieldset>
				<label for="startDate">Start date:</label>
				<input id="startDate" name="startDate" value="yyyy-mm-dd" type="text" class="textfield" />
				<label for="endDate">End date:</label>
				<input id="endDate" name="endDate" value="yyyy-mm-dd" type="text" class="textfield" />
			</fieldset>
			<fieldset>
				<label for="type">Content Type:</label>
				<select name="type" id="select">
					<option>All</option>
					<option>cgvAutoRSS</option>
					<option>cgvArticle</option>
					<option>cgvBanner</option>
					<option>cgvBestBetsCategory</option>
					<option>cgvBlogPost</option>
					<option>cgvBlogSeries</option>
					<option>cgvBooklet</option>
					<option>cgvBookletPage</option>
					<option>cgvCancerBulletin</option>
					<option>cgvCancerBulletinPage</option>
					<option>cgvCancerResearch</option>
					<option>cgvCancerTypeHome</option>
					<option>cgvClinicalTrialResult</option>
					<option>cgvContentSearch</option>
					<option>cgvDocTitleBlock</option>
					<option>cgvDrugInfoSummary</option>
					<option>cgvDynamicList</option>
					<option>cgvFactSheet</option>
					<option>cgvFeaturedClinicalTrial</option>
					<option>cgvInfographic</option>
					<option>cgvManualRSS</option>
					<option>cgvMicrositeIndex</option>
					<option>cgvPowerPoint</option>
					<option>cgvPowerPointPage</option>
					<option>cgvPressRelease</option>
					<option>cgvPromoUrl</option>
					<option>cgvSiteFooter</option>
					<option>cgvTileCarousel</option>
					<option>cgvTimelyContentBlock</option>
					<option>cgvTimelyContentFeature</option>
					<option>cgvTopicSearch</option>
					<option>cgvTopicSearchCategory</option>
					<option>gloImage</option>
					<option>gloUtilityImage</option>
					<option>gloVideo</option>
					<option>nciAppModulePage</option>
					<option>nciAppWidget</option>
					<option>nciContentHeader</option>
					<option>nciDocFragment</option>
					<option>nciErrorPage</option>
					<option>nciFile</option>
					<option>nciForm</option>
					<option>nciGeneral</option>
					<option>nciHome</option>
					<option>nciImage</option>
					<option>nciLandingPage</option>
					<option>nciLink</option>
					<option>nciList</option>
					<option>nciSectionNav</option>
					<option>nciTile</option>
				</select>
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
