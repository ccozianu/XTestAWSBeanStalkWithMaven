<%@page import="me.mywiki.sample2.oidc.UserProfile"%>
<%@page import="me.mywiki.sample2.oidc.OidcClientModule"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
	UserProfile visitor= session != null 
						? (UserProfile) session.getAttribute(OidcClientModule.DEFAULT_USERDATA_SESSION_NAME)
	        			: null ;
	if (visitor == null ) {
		application.getRequestDispatcher("/login.jsp").forward(request, response);
		return;
	}
%>
<html>
<body>
<h2>Hello <%= visitor.name %></h2>
<%if (visitor.pictureUrl != null) { %>
	<div><img src="<%= visitor.pictureUrl %>" > </div>
	<div> The site will be updated shortly with more functions. Check back soon.</div>
<% } %> 
<div>
	<textarea readonly cols="50" rows="100" style="visibility: hidden;"><%= session.getAttribute(OidcClientModule.DEFAULT_USERDATA_SESSION_NAME)%></textarea>
</div>
</body>
</html>
