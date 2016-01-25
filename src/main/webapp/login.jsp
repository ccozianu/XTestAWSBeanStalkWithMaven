<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
<style>
	input[type=text] {
	    padding:5px; 
	    border:2px solid #ccc; 
	    -webkit-border-radius: 5px;
	    border-radius: 5px;
	}
	
	input[type=text]:focus {
	    border-color:#333;
	}
	
	input[type=submit] {
	    padding:5px 15px; 
	    background:#ccc; 
	    border:0 none;
	    cursor:pointer;
	    -webkit-border-radius: 5px;
	    border-radius: 5px; 
	}
</style>
</head>
<body>

	<h1>Hello, visitor, please sign in.</h1>
	<div> Currently we support the following sign in option google (initial prototy, only G+ sign):
		<form action="<%= "action/login" %>"  method="post">
		<div>
			<label> 
				<input type="radio" name="loginOption" value="OpenIDConnect_google" checked>
			</label>
			</div>
			<div>
			<input type="submit" id="loginAction"  value="Login" />
			</div>
		</form> 
	</div>
	<div style="margin-top: 5em">
		Or you can login with username and password, please contact 
		<a href="mailto:ccozianu@acm.org"> Costin Cozianu &lt;ccozianu@acm.org&gt;</a>
		for details.
	</div>
	<form target="./login"  method="post">
		<div>
			<label>Username: 			<input type="text" maxlength="20"> </label>	
		</div>
		<div>
			<label>password: <input type="password" maxlength="20"></label>
		</div>
	</form>
</body>
</html>