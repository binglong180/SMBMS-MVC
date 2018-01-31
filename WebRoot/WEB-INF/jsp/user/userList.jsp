<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>用户列表</title>
</head>
<body>
	<form action="${pageContext.request.contextPath }/user/list"
		method="get">
		<input type="text" name="userName" value=""> <input
			type="submit" value="提交">
	</form>
	<c:forEach var="user" items="${queryUserList}">
		<div>用户姓名：=====》${user.userName} 用户密码：=====》${user.userPassword}
			用户地址：=====》${user.address}</div>
	</c:forEach>
</body>
</html>