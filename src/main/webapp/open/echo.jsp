<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Echo request parameters</title>
</head>
<body>
<h1>Request </h1>

	<table>
	<thead>
		<tr>
		<th colspan="2">Request parsing</th>
		</tr>
		<tr>
		<th>Field</th>
		<th>Value</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td align="right"> Scheme </td>
			<td align="left"> <%= request.getScheme() %> </td>
		</tr>
		<tr>
			<td align="right"> ServerName</td>
			<td align="left"> <%= request.getServerName() %> </td>	
		</tr>
		<tr>
			<td align="right"> ServerPort </td>
			<td align="left"> <%= request.getServerPort() %> </td>	
		</tr>		
	    <tr>
			<td align="right"> ContextPath </td>
			<td align="left"> <%= request.getContextPath() %> </td>	
		</tr>
	    <tr>
			<td align="right"> ServletPath </td>
			<td align="left"> <%= request.getServletPath() %> </td>	
		</tr>
	</tbody>
	</table>
	
	<table>
	<tbody>
	<%  //begin for
		for (Cookie cookie:request.getCookies()) {
     %>
		<td align="center"> <%= cookie.getDomain() %></td>	
		<td align="right"> <%= cookie.getName() %> </td>
		<td align="left"> <%= cookie.getValue() %> </td>	
    <%  //end for 	
		}
	%>	
	</tbody>
	</table>


</body>
</html>