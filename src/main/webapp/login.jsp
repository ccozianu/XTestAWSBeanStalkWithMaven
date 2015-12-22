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
	<div> Currently we support google sign in here:
		<form target="./login"  method="post">
			<input type="submit" id="GoogleSigninButton"  value="Go" />
		</form> 
	</div>
	<div style="margin-top: 5em">
		Or you can login with username and password, please contact 
		<a href="ccozianu@acm.org"> Costin Cozianu &lt;ccozianu@acm.org&gt;</a>
		for details.
	</div>
	<form target="./login"  method="post">
		<div>
			<span>Username: </span>			<input type="text" maxlength="20">	
		</div>
		<div>
			<span>password: </span> <input type="password" maxlength="20">
		</div>
	</form>
</body>
</html>