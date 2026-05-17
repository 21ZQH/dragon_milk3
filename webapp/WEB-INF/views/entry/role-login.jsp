<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String role = (String) request.getAttribute("role");
    String title = (String) request.getAttribute("title");
    String action = "Admin".equals(role) ? "LoginAdmin" : "LoginMo";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= title %></title>
    <style>
        body { background: #f4f6f8; font-family: Arial, sans-serif; margin: 0; }
        .box { width: 420px; margin: 80px auto; background: #fff; border: 1px solid #cfd6df; border-radius: 8px; padding: 32px; }
        h1 { margin: 0 0 24px; color: #1f2a44; text-align: center; }
        input { width: 100%; box-sizing: border-box; padding: 12px; margin: 10px 0; border: 1px solid #b9c2cf; border-radius: 6px; font-size: 16px; }
        button, a { display: block; width: 100%; box-sizing: border-box; text-align: center; padding: 12px; margin-top: 14px; border-radius: 6px; text-decoration: none; font-weight: bold; }
        button { border: 0; background: #23395d; color: #fff; cursor: pointer; }
        a { color: #23395d; background: #e8edf5; }
        .hint { color: #5f6f85; font-size: 14px; text-align: center; margin-top: 16px; }
    </style>
</head>
<body>
<div class="box">
    <h1><%= title %></h1>
    <form action="<%= request.getContextPath() %>/account" method="post">
        <input type="hidden" name="action" value="<%= action %>">
        <input type="email" name="email" placeholder="Email" required>
        <input type="password" name="password" placeholder="Password" required>
        <button type="submit">Log in</button>
    </form>
    <div class="hint">Built-in accounts only. Registration is disabled for <%= role %>.</div>
</div>
<script>
    const error = new URLSearchParams(window.location.search).get('error');
    if (error) alert(error);
</script>
</body>
</html>
