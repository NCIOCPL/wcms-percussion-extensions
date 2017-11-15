<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils,com.percussion.servlets.*,javax.security.auth.login.LoginException"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
<%
         String locale = PSI18nUtils.getSystemLanguage();
         pageContext.setAttribute("locale", locale);		 
%>
<!DOCTYPE HTML PUBLIC "-//W3C//Dtd HTML 4.0 Transitional//EN">
<html>
<head>
<title>${rxcomp:i18ntext('jsp_login@Rhythmyx Login',locale)}</title>
<script language="javascript">
      function setCursor() {
        // leave focus in username
        cmd = document.getElementById("j_username");
        cmd.focus();
      }
	</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="sys_resources/css/rxcx.css" type="text/css"
	media="screen" />
<%
		boolean hasError = true;
		//Code to authenticate user using SSO headers.
		String redirectTo = null;
		if ("SiteMinder".equals(request.getHeader("Auth-Type"))) {			
			
			String user = request.getHeader("SM_USER");			
			
			if (user != null) {				
				//Authenticate User
				try {
					HttpSession localHttpSession = request.getSession(true);
					HttpServletRequest request2 = PSSecurityFilter.authenticate(request, response, null, null);
					redirectTo = (String)localHttpSession.getAttribute("RX_REDIRECT_URL");
					if (redirectTo == null)
						redirectTo = "/Rhythmyx/index.jsp";

					localHttpSession.removeAttribute("RX_REDIRECT_URL");
					hasError = false;
				} catch (LoginException ex) {
				}				
			}
		} else {
			hasError = false;
		}

		 String username = request.getParameter("j_username");
		 String password = request.getParameter("j_password");
		 String error = request.getParameter("j_error");

		 if (username == null)
			username = "";
		 if (password == null)
			password = "";
		
		if (hasError) {
			error = "Access Denied";
		}
%>

<%
	if (redirectTo != null) {
%>
<script type="text/javascript">
	window.location="<%=redirectTo%>";
</script>
<%	} %>

</head>
<body onload="setCursor()">
<table class="RxLogin" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td colspan="2">
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td width="25"><img height="25"
					src="rx_resources/images/${locale}/rhythmyx_login_topleft.gif"
					width="25"></td>
				<td class="rhythmyx_login_topbkgd"><img height="25"
					src="rx_resources/images/${locale}/blank-pixel.gif" width="25"></td>
				<td width="25"><img height="25"
					src="rx_resources/images/${locale}/rhythmyx_login_topright.gif"
					width="25"></td>
			</tr>
		</table>
		</td>
		<td class="RightShadow"><img
			src="rx_resources/images/${locale}/shadow-topright.gif" width="9"
			height="25" /></td>
	</tr>
	<tr>
		<td colspan="2" class="BannerCell"><img height="50"
			src="rx_resources/images/${locale}/rhythmyx_login_banner.jpg"
			width="516"></td>
		<td class="RightShadow">&nbsp;</td>
	</tr>
	<tr>
		<td class="grayBKGD" colspan="2">
		<form id="loginform" method="post" class="Login-ConsoleForm" 
			  enctype="multipart/form-data" >
		<table cellspacing="0" cellpadding="0" border="0" width="384"
			style="margin-left:56px; margin-right:56;">
			<tr>
				<%
				         if (error != null)
				         {
				%>
				<%=error%>
				<%
				}
				%>
			</tr>
			<tr>
				<%
					if (!hasError) {
				%>
				<td width="382" bgcolor="#e8e3da">
				<table cellspacing="1" cellpadding="0" width="100%" border="0">
					<tr class="whiteBKGD" valign="middle">
						<td align="right"><span class="fieldName">${rxcomp:i18nhtml('jsp_login@User name',locale)}</span></td>
						<td>&nbsp; <input id="j_username"
							accessKey="${rxcomp:i18nmnemonic('jsp_login@User name',locale)}"
							name="j_username" value="<%= username %>" tabindex="1"></td>
						<td rowspan="2" class="button" align="center"><input
							type="image" height="17" alt="Logon" title="Logon" width="62"
							src="rx_resources/images/${locale}/login.gif" border="0"
							name="Logon" tabindex="3"></td>
					</tr>
					<tr class="whiteBKGD" valign="middle">
						<td align="right"><span class="fieldName">${rxcomp:i18nhtml('jsp_login@Password',locale)}</td>
						<td>&nbsp; <input
							accessKey="${rxcomp:i18nmnemonic('jsp_login@Password',locale)}"
							type="password" name="j_password" value="<%= password %>"
							tabindex="2"></td>
					</tr>
				</table>
				</td>
				<% } %>
			</tr>
		</table>
		</form>
		</td>
		<td class="RightShadow">&nbsp;</td>
	</tr>
	<tr>
		<td colspan="2" class="BottomShadow">&nbsp;</td>
		<td><img
			src="rx_resources/images/${locale}/shadow-bottomright.gif" width="9"
			height="9"></td>
	</tr>
</table>
<div class="copyright">&copy; Copyright Percussion Software 2009</div>
</body>
</html>