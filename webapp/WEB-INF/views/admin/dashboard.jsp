<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 获取当前登录用户，以便动态显示姓名
    model.User currentUser = (model.User) session.getAttribute("user");
    String username = "Admin";
    if (currentUser != null && currentUser.getName() != null && !currentUser.getName().trim().isEmpty()) {
        username = currentUser.getName();
    }
    
    // 获取 Controller 传来的提示信息（例如点击了还未开发的功能）
    String notice = (String) request.getAttribute("notice");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Management System</title>
    <style>
        /* 沿用小组统一的 UI 规范 */
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 40px 0;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
        }

        .container {
            background: #fff;
            width: 100%;
            max-width: 800px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 40px 50px;
            box-sizing: border-box;
        }

        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 5px;
            border-bottom: 2px solid #22223b; 
            padding-bottom: 15px;
        }

        .page-subtitle {
            color: #3b5998;
            font-size: 1.1em;
            font-style: italic;
            margin-bottom: 40px;
            margin-top: 10px;
        }

        .welcome-text {
            font-size: 3em;
            font-weight: 700;
            color: #22223b;
            text-align: center;
            margin-bottom: 15px;
        }
        
        .desc-text {
            text-align: center;
            color: #444;
            font-size: 1.2em;
            margin-bottom: 50px;
        }

        .btn-group {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 25px;
        }

        /* 参考草图的横向宽按钮 */
        .action-btn {
            display: block;
            width: 80%;
            padding: 20px 0;
            font-size: 1.5em;
            font-weight: 700;
            color: #22223b;
            background: #fff;
            border: 3px solid #22223b;
            border-radius: 12px;
            text-align: center;
            text-decoration: none;
            transition: all 0.2s ease;
            box-shadow: 0 2px 8px rgba(34,34,59,0.05);
        }

        .action-btn:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-3px);
            box-shadow: 0 6px 16px rgba(34,34,59,0.2);
        }
        
        .notice-box {
            color: #a12626;
            background: #fdeeee;
            border: 1px solid #efb7b7;
            padding: 10px;
            border-radius: 8px;
            text-align: center;
            font-weight: bold;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>

    <div class="container">
        <div class="page-title">Admin management system</div>
        

        <% if (notice != null) { %>
            <div class="notice-box"><%= notice %></div>
        <% } %>

        <div class="welcome-text">Hi, <%= username %></div>
        <div class="desc-text">Welcome to the Admin management system!</div>

        <div class="btn-group">
            <a href="<%= response.encodeURL("AdminController?action=candidate_management") %>" class="action-btn">Candidate Management</a>
            
            <form action="<%= response.encodeURL("logout") %>" method="post" style="width: 80%; display: flex; justify-content: center; margin-top: 20px;">
                <button type="submit" style="background:none; border:none; color: #3b5998; text-decoration: underline; cursor: pointer; font-size: 1.1em;">Log out</button>
            </form>
        </div>
    </div>

</body>
</html>