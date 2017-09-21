<%@ page import="gov.cancer.wcm.publishing.PODQueue" %>
<%@ page import="gov.cancer.wcm.publishing.PODWork" %>
<%@ page import="gov.cancer.wcm.publishing.PublishItem" %>
<%@ page import="gov.cancer.wcm.publishing.PODPublisher" %>
<%@ page import="java.util.*" %>


<jsp:useBean id="queue" class="gov.cancer.wcm.publishing.PODQueue" scope="request">
	<jsp:setProperty name="queue" property="*" />
</jsp:useBean>
 <!-- < % 
		
		//try{
		//	PODWork p = (PODWork) queue.publishingQueue.element();	
		//}
		//catch (NoSuchElementException e) {
		//	out.println("There are no publishing jobs waiting in the Queue");
		//}
			
		% -->
<html>
	<head>
		<title>Publishing on Demand Queue Viewer</title>
		
	</head>
	<body> 
		<script>  
			function showHide(obj){  
				var tbody = obj.parentNode.parentNode.parentNode.getElementsByTagName("tbody")[0];  
				var old = tbody.style.display;  
				tbody.style.display = (old == "none"?"":"none");  
			}  
		</script>  
        <h1>
      	Publishing on Demand Queue Viewer
		</h1>
		
		<style type="text/css">  
			th{text-align: left; cursor: pointer;back;
    font-size: 0.9em;}  
			table tbody tr td{padding-left: 15px;}  
		</style>  
		<h2>Publishing on Demand Queue Viewer</h2>
		<!-- <table >
			<tr>  
				<td>  -->
				<table cellspacing="3" cellpadding="10" border="1">  
						<thead>  
							<tr>  
								<th>WorkID</th> 
								<th>Edition ID</th>
								<th>Transitioning Item</th>
								<th>Transitioning User</th>
								<th># Items in Job</th>
								<th>Time in Queue</th>
							</tr>  
						</thead>  
						<tbody>  
				<%
					Iterator i = queue.getPublishingQueue().iterator();
					while(i.hasNext()) {
						PODWork work = (PODWork) i.next();
				%>
					
						<tr>
							<td><% out.println(work.getWorkID());%></td>
							<td><% out.println(work.getEdition()); %> </td>
							<td><% out.println(work.getTransitionItemID() + " - " + work.getTransitionItemName()); %> </td>
							<td><% out.println(work.getTransitionUser()); %> </td>
							<td><% out.println(work.getItems().size()); %> </td>
							<td><% out.println((work.timeWaiting().intValue()/1000) + " seconds"); %></td>
						</tr>
						
				<% } %>
				</tbody>  
					</table>  
					
				<!-- </td>  
			</tr>  
		</table>  -->
		
		
	</body>
</html>