<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Echo request parameters</title>
</head>
<body>
<html>
<head><title>My First Stripe</title></head>
<body>
<h1>Stripes Calculator</h1>
 
Hi, I'm the Stripes Calculator. I can only do addition. Maybe, some day, a nice programmer
will come along and teach me how to do other things?
 
<%-- beanclass="me.mywiki.sample2.stripes.CalculatorActionBean" focus="" --%> 
<stripes:form beanclass="me.mywiki.sample2.stripes.EncryptionActions" focus="">
    <table>
        <tr>
            <td>Text to encrypt:</td>
            <td><stripes:text name="textToEncrypt"/></td>
        </tr>
        <tr>
            <td>Result:</td>
            <td>${actionBean.result}</td>
        </tr>
    </table>
</stripes:form>
</body>
</html>
</body>
</html>