<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form"	uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring"	uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec"	uri="http://www.springframework.org/security/tags" %>
<html>
<head>
	<!-- csrf토큰  -->
<meta id="_csrf" name="_csrf" content="${_csrf.token}" />
<meta id="_csrf_header" name="_csrf_header" content="${_csrf.headerName}" /> 
	
<title>Home</title>
	
<script src="<c:url value='/webjars/jquery/3.6.0/jquery.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/paginationjs/2.1.4/pagination.min.js"></script>		<!-- 페이징처리를 위해 pagination.js 사용 -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/paginationjs/2.1.4/pagination.css"/><!-- 페이지 css디자인 -->

<script type="text/javaScript" language="javascript" defer="defer">
$(document).ready(function(){
	list();
});
function save(){
	if(!confirm("저장하시겠습니까?")){
		return;
	}
	var header = $("meta[name = '_csrf_header']").attr('content');
	var token = $("meta[name= '_csrf']").attr('content');
	$.ajax({
		url : "<c:url value='/add.do'/>",
		type : "POST",
		beforeSend : function(xhr){
			xhr.setRequestHeader(header,token);
		},
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
	var header = $("meta[name = '_csrf_header']").attr('content');
	var token = $("meta[name= '_csrf']").attr('content');
	$.ajax({
		url: "<c:url value='/list.do'/>",
		processData: false,
		contentType: false,
		method: "GET",
		cache: false,
		beforeSend : function(xhr){
			xhr.setRequestHeader(header,token);
		},
		data: ''
	}).done(function(data){
		$('#list').children().remove();
		$(function(){
			let container = $('#pagination');
			var contents;
			var array = new Array();
			for(var i = 0; i < data.list.length; i++){
				contents = data.list[i].content;
				contents = contents.replace(/\n/gi, '\\n');
				txt = "<tr class=\"menu1\" onclick=\"detail('"+data.list[i].id+"','"+data.list[i].title+"','"+contents+"');\">";
				txt += "<td>" + data.list[i].title + "</td>" + "<td>" + "<span style=\"float:right\">"+ data.list[i].date +"</td>";
				txt += "</tr>"
				$('#list').append(txt);
				array.push({name:txt});				//dataSource로 전달하기 위해 array에 담아준다.
			}
			container.pagination({
				dataSource: function(done){			//array에 {name: ~~ }방식으로 담은 것을 dataSource에 넣어줘야 게시글 출력
					var result = [];
					for(var i = 0; i < array.length; i++){
						result.push(array[i]);
					}
					done(result)
				},pageSize:5,						//페이지당 게시글 5개씩
				callback: function (data, pagination){
					var dataHtml = '<tr>';
					$.each(data,function(index, item){
						dataHtml += item.name;
					});
					dataHtml += '</tr>';
					
					$("#list").html(dataHtml);
				}
			})
		})
		
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
	if(!confirm("삭제하시겠습니까?")){
		return;
	}
	var header = $("meta[name = '_csrf_header']").attr('content');
	var token = $("meta[name= '_csrf']").attr('content');
	$.ajax({
		url : "<c:url value='/del.do'/>",
		type : "POST",
		beforeSend : function(xhr){
			xhr.setRequestHeader(header,token);
		},
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
function mod(){
	console.log($('#id').val());
	if(!confirm("수정하시겠습니까?")){
		return;
	}
	var header = $("meta[name = '_csrf_header']").attr('content');
	var token = $("meta[name= '_csrf']").attr('content');
	$.ajax({
		url : "<c:url value='/mod.do'/>",
		type : "POST",
		beforeSend : function(xhr){
			xhr.setRequestHeader(header,token);
		},
		data : {"id":$('#id').val(),"title":$('#title').val(),"content":$('#content').val()}
	}).done(function(data){
		console.log("@@@@@@");
		if(data.returnCode == 'success'){
			console.log('data ==== ', data);
			location.reload();
		}else if(data.returnCode == 'null'){
			alert(data.returnDesc);
		}else{
			alert(data.returnDesc);
		}
	}).fail(function(jqXHR, textStatus, errorThrown){
		alert("오류 : "+ errorThrown);
	});
}
</script>
</head>
<body>
	<form id="form" name="form1" action="">
		<div>
			<label>title </label><input id="title" type="text">
			<input id="id" type="hidden">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
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
					<td>게시글1_test</td>
				</tr>
				<tr>
					<td>게시글2_test</td>
				</tr>
				<tr>
					<td>게시글3_test</td>
				</tr>
				<tr>
					<td>게시글4_test</td>
				</tr>
			</tbody>
		</table>
		<div id="pagination"></div>
	</div>
</body>
</html>
