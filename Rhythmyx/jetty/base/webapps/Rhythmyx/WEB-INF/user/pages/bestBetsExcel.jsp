<%	response.setHeader("Content-type","application/vnd.ms-excel");
	response.setHeader("Content-disposition","inline;filename=bestBets"); %>
<%@ page import="com.percussion.utils.jdbc.PSConnectionHelper" %>
<%@ page import="gov.cancer.wcm.extensions.CGV_Reports" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>

<html>
	<head>
		<title>Best Bets Category Audit</title>
		<style type="text/css">
		<%@include file="reportsout.css" %>
		</style>
	</head>
	<body>
	<h2>Best Bets Category Audit</h2>
	<div class="toolbar">
	<input type="button" value="Print" onclick="window.print();" class="Print"> <input type="button" value="Close" onclick="window.close();" class="close"> <input type="button" value="Back" onclick="window.history.go(-1);" class="back"> </div>
	<table cellspacing="2" cellpadding="0" border="1">
		<tr>
			<th>Best Bet Grouping</th>
			<th>Best Bet Category</th>
			<th>Best Bet List Item</th>
			<th>Best Bet List Item Path</th>
			<th>Best Bet List Item Content Type</th>
		</tr>
<%


	CGV_Reports reportTools = new CGV_Reports();
	HashMap reportMap = reportTools.report_BestBets();
	Object[] gkeys = reportMap.keySet().toArray();
	Arrays.sort(gkeys);
	for(int i = 0; i<gkeys.length; i++){
		HashMap groupingMap = (HashMap)reportMap.get(gkeys[i]);
		Object[] ckeys = groupingMap.keySet().toArray();
		Arrays.sort(ckeys);
		String groupingName = (String)gkeys[i];
		for(int j = 0; j<ckeys.length; j++){
			String categoryName = (String)ckeys[j];
			HashMap categoryMap = (HashMap)groupingMap.get(ckeys[j]);
			Object[] likeys = categoryMap.keySet().toArray();
			for(int k = 0; k<likeys.length; k++){
				HashMap listItemMap = (HashMap)categoryMap.get(likeys[k]);
				String liName = (String)listItemMap.get("name");
				String liURL = (String)listItemMap.get("url");
				String liContentType = (String)listItemMap.get("contentType");
				%><td><%=groupingName%></td>
				  <td><%=categoryName%></td>
				  <td><%=liName%></td>
				  <td><%=liURL%></td>
				  <td><%=liContentType%></td>
				  </tr><%  
			}
			if(likeys.length == 0){
				%><td><%=groupingName%></td>
				  <td><%=categoryName%></td>
				  <td></td>
				  <td></td>
				  <td></td>
				  </tr><%
			}
		}

	}
%>
	</table>
	</body>
</html>
