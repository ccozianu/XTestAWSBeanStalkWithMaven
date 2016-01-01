<%@page import="me.mywiki.sample2.oidc.impl.OidcSimpleBootstrapFilter"%>
<%@page import="me.mywiki.sample2.oidc.OidcClientModule.Err.ErrorData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<h1>Bummer</h1>
There's been an error processing your request
<% ErrorData error= (ErrorData) request.getAttribute(OidcSimpleBootstrapFilter.OIDC_FILTER_ERROR); %>
<% if (error != null) { %>
	<div> If you contact developers the error code was: <%= "" +error.majorCode +"." + error.minorCode%> </div>
	<div> Error message is:</div>
	<div ><%= error.userMessage %></div>
	<a href="<%= error.nextUrl == null ? "login.jsp" : error.nextUrl %>"> 
		<%= error.nextUrl %>
	</a>
<% } else { %>
<h2 > it's been a really big booboo</h2>
	The software screwed up so badly, that we don't have a root cause about the error, please try again from the login page:
	<a href="login.jsp">Go back to login page</a>
<% } %>
</body>
</html>