<%@page import="me.mywiki.sample2.oidc.OidcClientModule"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
	//this.getServletContext().get
	if (session == null ||  session.getAttribute(OidcClientModule.DEFAULT_USERDATA_SESSION_NAME) == null) {
		application.getRequestDispatcher("/login.jsp").forward(request, response);
		return;
	}
%>
<html>
<body>
<h2>Hello visitor:</h2>
<div>
	<textarea readonly cols="50" rows="100" style="visibility: hidden;"><%= session.getAttribute(OidcClientModule.DEFAULT_USERDATA_SESSION_NAME)%></textarea>
</div>
</body>
</html>
