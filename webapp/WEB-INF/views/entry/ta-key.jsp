<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Your TA Access Key</title>
    <style>
        body { background: #f4f6f8; font-family: Arial, sans-serif; }
        .box { width: 520px; margin: 80px auto; background: #fff; border: 1px solid #cfd6df; border-radius: 8px; padding: 32px; text-align: center; }
        .key { font-size: 28px; letter-spacing: 2px; background: #eef3fb; padding: 16px; border-radius: 8px; margin: 22px 0; font-weight: bold; color: #1f2a44; }
        a { display: block; padding: 12px; border-radius: 6px; text-decoration: none; font-weight: bold; color: #fff; background: #23395d; }
    </style>
</head>
<body>
<div class="box">
    <h1>Your TA Access Key</h1>
    <p>Please keep this key. Next time you only need this key to log in.</p>
    <div class="key"><%= request.getAttribute("accessKey") %></div>
    <a href="<%= request.getContextPath() %>/ta">Continue to jobs</a>
</div>
</body>
</html>
