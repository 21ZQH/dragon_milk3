<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>TA Access</title>
    <style>
        body { background: #f4f6f8; font-family: Arial, sans-serif; margin: 0; }
        .wrap { max-width: 900px; margin: 60px auto; display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
        .box { background: #fff; border: 1px solid #cfd6df; border-radius: 8px; padding: 28px; }
        h1 { grid-column: 1 / -1; text-align: center; color: #1f2a44; margin: 0 0 8px; }
        h2 { color: #1f2a44; margin-top: 0; }
        input { width: 100%; box-sizing: border-box; padding: 12px; margin: 10px 0; border: 1px solid #b9c2cf; border-radius: 6px; font-size: 16px; }
        button, a { display: block; width: 100%; box-sizing: border-box; text-align: center; padding: 12px; margin-top: 14px; border-radius: 6px; text-decoration: none; font-weight: bold; }
        button { border: 0; background: #23395d; color: #fff; cursor: pointer; }
        a { color: #23395d; background: #e8edf5; }
        .hint { color: #5f6f85; font-size: 14px; }
    </style>
</head>
<body>
<div class="wrap">
    <h1>TA Access</h1>
    <div class="box">
        <h2>Register</h2>
        <form action="<%= request.getContextPath() %>/account" method="post">
            <input type="hidden" name="action" value="RegisterTA">
            <input type="email" name="email" placeholder="BUPT email (@bupt.edu.cn)" required>
            <button type="submit">Create access key</button>
        </form>
        <p class="hint">Only @bupt.edu.cn email addresses can register.</p>
    </div>
    <div class="box">
        <h2>Log in</h2>
        <form action="<%= request.getContextPath() %>/account" method="post">
            <input type="hidden" name="action" value="LoginTA">
            <input type="password" name="password" placeholder="Access key" required>
            <button type="submit">Log in with key</button>
        </form>
        <p class="hint">Use the key generated when you registered.</p>
    </div>
    <a href="<%= request.getContextPath() %>/ta">Back to jobs</a>
</div>
<script>
    const error = new URLSearchParams(window.location.search).get('error');
    if (error) alert(error);
</script>
</body>
</html>
