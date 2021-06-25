<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.Authentication" %>
<%
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	Object principal = auth.getPrincipal();
	
	String name = "";
	if(principal != null){
		name = auth.getName();
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>INDEX</title>
</head>
<body>
    <br><br>
    <div>
        <h1>Security</h1><br>
    </div>
    <br><br>
	<sec:authorize access="isAnonymous()">		<!-- 로그인 인증하면 안보이게 됨 -->
	  	<h5><a href='<c:url value="/loginPage"/>'>LOGIN</a> 로그인 해주세요.</h5>
	</sec:authorize>
	<sec:authorize access="isAuthenticated()">
		<h5><%=name %>님, 반갑습니다.</h5>
		<form action="/logout" method="POST">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
			<button type="submit">LOGOUT</button>
		</form>
	</sec:authorize>
    <div>
        <a href='<c:url value="/page"/>' role="button">GUEST</a>
        <a href='<c:url value="/user/page"/>' role="button">USER</a>
        <a href='<c:url value="/member/page"/>' role="button"">MEMBER</a>
        <a href='<c:url value="/admin/page"/>' role="button">ADMIN</a>
    </div>
</body>
</html>