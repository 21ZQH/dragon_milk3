<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 获取当前登录用户，以便动态显示姓名
    model.User currentUser = (model.User) session.getAttribute("user");
    String username = "Guest";
    if (currentUser != null && currentUser.getName() != null && !currentUser.getName().trim().isEmpty()) {
        username = currentUser.getName();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Personal Centre</title>
    <style>
        /* 页面基础背景和居中排版 */
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

        /* 主容器白板 */
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

        /* 顶部标题栏 */
        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 5px;
            border-bottom: 2px solid #22223b; 
            padding-bottom: 15px;
        }

        /* 蓝色的副文本说明 */
        .page-subtitle {
            color: #3b5998;
            font-size: 1.1em;
            font-style: italic;
            margin-bottom: 40px;
            margin-top: 10px;
        }

        /* 欢迎语样式：Hi, xxx */
        .welcome-text {
            font-size: 3em;
            font-weight: 700;
            color: #22223b;
            text-align: center;
            margin-bottom: 50px;
        }

        /* 纵向排列的按钮组容器 */
        .btn-group {
            display: flex;
            flex-direction: column; /* 规定子元素纵向排列 */
            align-items: center;    /* 居中对齐 */
            gap: 25px;              /* 按钮之间的垂直间距 */
        }

        /* 纵向大按钮样式 */
        .action-btn {
            display: block;
            width: 80%; /* 按钮宽度占据容器的 80%，显得非常大气 */
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

        /* 悬浮时的反色和上浮效果 */
        .action-btn:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-3px);
            box-shadow: 0 6px 16px rgba(34,34,59,0.2);
        }
    </style>
</head>
<body>

    <div class="container">
        
        <div class="page-title">Personal Centre</div>
        

        <div class="welcome-text">Hi, <%= username %></div>

        <div class="btn-group">
            
            <a href="#" class="action-btn">Edit personal information</a>

            <a href="#" class="action-btn">My project</a>

            <a href="#" class="action-btn">Review</a>

        </div>

    </div>

</body>
</html>