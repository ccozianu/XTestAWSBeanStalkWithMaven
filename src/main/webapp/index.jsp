<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
	//this.getServletContext().get
	if (session == null ||  session.getAttribute("userInfo") == null) {
		application.getRequestDispatcher("/login.jsp").forward(request, response);
		return;
	}
%>
<html>
<body>
<h2>Hello visitor</h2>
</body>
</html>
