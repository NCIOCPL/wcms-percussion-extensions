<!DOCTYPE html>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Counts by Content Type and Workflow State</title>
	<script language="javascript">
		function doReport()
		{
			df=document.forms[0];
			if (document.getElementById('excel').checked == true) {
				df.action="countsCtWsExcel.jsp";
			}
			else {
				df.action="countsCtWsHtml.jsp";
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
		<h1>Counts by Content Type and Workflow State</h1>
		<p>Provides a count of content types by workflow state for selected folders and content types.</p>
		<form action="" method="post" name="report">
		<input name="sys_contentid" type="hidden" value='<%= request.getParameter("sys_contentid") %>' />
		<div id="report_input">
			<fieldset>
				<legend>Include all subfolders?</legend>
				<input id="subFolders" name="allFolders" value="1" type="checkbox" checked="yes" class="checkbox" /><label for="subFolders">Yes</label>
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
					<option>pdqCancerInfoSummary</option>
					<option>pdqCancerInfoSummaryLink</option>
					<option>pdqCancerInfoSummaryPage</option>
					<option>pdqDrugInfoSummary</option>
					<option>pdqMediaFile</option>
					<option>pdqMediaLink</option>
					<option>pdqTableSection</option>
					<option>PDQQueueViewer</option>
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
