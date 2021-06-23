<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form"	uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring"	uri="http://www.springframework.org/tags" %>
<html>
<head>
<title>Home</title>
<script src="<c:url value='/webjars/jquery/3.6.0/jquery.min.js'/>"></script>
<script type="text/javaScript" language="javascript" defer="defer">
$(document).ready(function(){
	list();
});
function save(){
	if(!confirm("저장하시겠습니까?")){
		return;
	}
	$.ajax({
		url : "add.do",
		type : "POST",
		data : {"title":$('#title').val(),"content":$('#content').val()}
	}).done(function(data){
		console.log("@@@@@@");
		if(data.returnCode == 'success'){
			console.log('data ==== ', data);
			location.reload();
		}else{
			alert(data.returnDesc);
		}
	}).fail(function(jqXHR, textStatus, errorThrown){
		alert("오류 : "+ errorThrown);
	});
}
function list(){
	$.ajax({
		url: "<c:url value='/list.do'/>",
		processData: false,
		contentType: false,
		method: "GET",
		cache: false,
		data: ''
	}).done(function(data){
		$('#list').children().remove();
		for(var i = 0; i < data.list.length; i++){
			var contents = data.list[i].content;
			contents = contents.replace(/\n/gi, '\\n');
			var txt = "<tr class=\"menu1\" onclick=\"detail('"+data.list[i].id+"','"+data.list[i].title+"','"+contents+"');\">";
			txt += "<td>" + data.list[i].title + "</td>" + "<td>" + "<span style=\"float:right\">"+ data.list[i].date +"</td>";
			txt += "</tr>"
			$('#list').append(txt);
		}
	}).fail(function(jqXHR, textStatus, errorThrown){
		alert("오류:"+errorThrown);
	});
}
function detail(id, title, contents){
	$('#title').val(title);
	$('#content').val(contents);
	$('#id').val(id);
}
function del(){
	console.log($('#id').val());
	$.ajax({
		url : "del.do",
		type : "POST",
		data : {"id" : $('#id').val()}
	}).done(function(data){
		if(data.returnCode == 'success'){
			location.reload();
		}else{
			alert(data.returnDesc);
		}
	}).fail(function(jqXHR, textStatus, errorThrown){
		alert("오류:"+errorThrown);
	});
}
</script>
</head>
<body>
	<form id="form" name="form1" action="">
		<div>
			<label>title </label><input id="title" type="text">
			<input id="id" type="hidden">
		</div>
		<div>
			<label>content </label><input id="content" type="text">
		</div>
	</form>
	<button type="button" onclick="save()">저장</button>
	<button type="button" onclick="mod()">수정</button>
	<button type="button" onclick="del()">삭제</button>
	<div>
		<table>
			<thead>
				<tr>
					<th>게시글 목록</th>
				</tr>
			</thead>
			<tbody id="list">
				<tr>
					<td>1</td>
				</tr>
				<tr>
					<td>2</td>
				</tr>
				<tr>
					<td>3</td>
				</tr>
				<tr>
					<td>4</td>
				</tr>
			</tbody>
		</table>
	</div>
</body>
</html>
