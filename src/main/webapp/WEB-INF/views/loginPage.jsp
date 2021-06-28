<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.Authentication" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login</title>
</head>
<body>
    <h3>Login with Username and Password</h3>
   
    <form name="f" action="/login" method="POST">
        <table>
            <tbody>
                <tr>
                    <td>User:</td>
                    <td><input type="text" name="loginId" value="" placeholder="Id"></td>
                </tr>
                <tr>
                    <td>Password:</td>
                    <td><input type="password" name="loginPwd" placeholder="Password"></td>
                </tr>
                <tr>
                    <td colspan="2"><button type="submit">Sign In</button></td>
                    <td><input name="${_csrf.parameterName}" type="hidden" value="${_csrf.token}"/></td>
                </tr>
            </tbody>
        </table>
       	<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION}">
    		<font color="red">
        		<p>Your login attempt was not successful due to <br/>
            	${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}</p>
        		<c:remove var="SPRING_SECURITY_LAST_EXCEPTION" scope="session"/>
    		</font>
		</c:if>
    </form>
</body>
</html>