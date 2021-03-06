<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="xdi2.core.properties.XDI2Properties" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>XDI Parser</title>
<script type="text/javascript" src="tabber.js"></script>
<link rel="stylesheet" target="_blank" href="style.css" TYPE="text/css" MEDIA="screen">
</head>
<body>
	<div id="imgtop"><img id="imgtopleft" src="images/xdi2-topleft.png"><a href="http://projectdanube.org/"><img id="imgtopright" src="images/xdi2-topright.png"></a></div>
	<div id="main">
	<div class="header">
	<span id="appname"><img src="images/app20b.png"> XDI Parser</span>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<a href="index.jsp">&gt;&gt;&gt; Other Apps...</a><br>
	This is version <%= XDI2Properties.properties.getProperty("project.version") %> <%= XDI2Properties.properties.getProperty("project.build.timestamp") %>, Git commit <%= XDI2Properties.properties.getProperty("git.commit.id").substring(0,6) %> <%= XDI2Properties.properties.getProperty("git.commit.time") %>.
	</div>

	<% if (request.getAttribute("error") != null) { %>
			
		<p style="font-family: monospace; white-space: pre; color: red;"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "" %></p>

	<% } %>

	<form action="XDIParser" method="post" accept-charset="UTF-8">

		<table><tr>
		<td>Parse:</td>
		<td>

		<!--
		<select name="rulename" style="width: 200px">
		
		<% for (String rule : (String[]) request.getAttribute("rules")) { %>
			<option class="input" name="rulename" value="<%= rule %>" <%= rule.equals(request.getAttribute("rulename")) ? "selected" : "" %>><%= rule %></option>
		<% } %>
		</select>
		-->
		&nbsp;
		<input type="text" class="input" name="input" style="width: 500px" value="<%= request.getAttribute("input") != null ? ((String) request.getAttribute("input")).replace("\"", "&quot;") : "" %>">
		&nbsp;
		<!--
		<input type="radio" name="parser" value="manual" <%= "manual".equals(request.getAttribute("parser")) ? "checked" : "" %>>manual (doesn't use ABNF)
		&nbsp;
		<input type="radio" name="parser" value="aparse" <%= "aparse".equals(request.getAttribute("parser")) ? "checked" : "" %>>aParse
		&nbsp;
		<input type="radio" name="parser" value="apg" <%= "apg".equals(request.getAttribute("parser")) ? "checked" : "" %>>APG
		-->
		<input type="hidden" name="parser" value="manual">
		&nbsp;
		<input type="submit" value="Go!">

		</td>
		</tr></table>

	</form>

	<br><div class="line"></div>

	<div class="tabber">

	<% if ("aparse".equals(request.getAttribute("parser"))) { %>

	<% if (request.getAttribute("output1") != null) { %>
	    <div class="tabbertab">
		<h2>aParse Tree</h2>
		<div class="result"><pre><%= request.getAttribute("output1") %></pre></div><br>
		</div>
	<% } %>

	<% if (request.getAttribute("output2") != null) { %>
	    <div class="tabbertab">
		<h2>aParse Stack</h2>
		<div class="result"><pre><%= request.getAttribute("output2") %></pre></div><br>
		</div>
	<% } %>

	<% if (request.getAttribute("output3") != null) { %>
	    <div class="tabbertab">
		<h2>aParse Xml</h2>
		<div class="result"><pre><%= request.getAttribute("output3") %></pre></div><br>
		</div>
	<% } %>

	<% if (request.getAttribute("output4") != null) { %>
	    <div class="tabbertab">
		<h2>aParse Counts</h2>
		<div class="result"><pre><%= request.getAttribute("output4") %></pre></div><br>
		</div>
	<% } %>
	
	<% } %>

	<% if ("apg".equals(request.getAttribute("parser"))) { %>

	<% if (request.getAttribute("output5") != null) { %>
	    <div class="tabbertab">
		<h2>APG Result</h2>
		<div class="result"><pre><%= request.getAttribute("output5") %></pre></div><br>
		</div>
	<% } %>
	
	<% if (request.getAttribute("output6") != null) { %>
	    <div class="tabbertab">
		<h2>APG Statistics</h2>
		<div class="result"><pre><%= request.getAttribute("output6") %></pre></div><br>
		</div>
	<% } %>
	
	<% if (request.getAttribute("output7") != null) { %>
	    <div class="tabbertab">
		<h2>APG Trace</h2>
		<div class="result"><pre><%= request.getAttribute("output7") %></pre></div><br>
		</div>
	<% } %>
	
	<% } %>

	<% if ("manual".equals(request.getAttribute("parser"))) { %>

	<% if (request.getAttribute("output8") != null) { %>
	    <div class="tabbertab">
		<h2>Node Types</h2>
		<div class="result"><pre><%= request.getAttribute("output8") %></pre></div><br>
		</div>
	<% } %>
	
	<% } %>

	</div>

	</div>	
</body>
</html>

