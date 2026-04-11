<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>MO Dashboard</title>
    <style>
        /* ================= 1. 页面基础样式 ================= */
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh; /* 确保整个内容在浏览器中垂直居中 */
        }

        /* ================= 2. 中间白色的主外框 ================= */
        .container {
            background: #fff;
            width: 100%;
            /* 【修改点】：将框拉长，这里设置了最大宽度为 850px，你可以改这个数值来调整框的宽度 */
            max-width: 850px; 
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 40px 40px 50px 40px; /* 上、右、下、左的内边距 */
            box-sizing: border-box;
        }

        /* ================= 3. 顶部大标题样式 ================= */
        .title {
            /* 【修改点】：稍微缩小了字号，之前是3em，现在是2.2em */
            font-size: 2.2em; 
            font-weight: 700;
            color: #22223b;
            margin-bottom: 15px;
            text-align: center;
        }

        /* ================= 4. 标题下方的欢迎描述副文本 ================= */
        .desc {
            color: #444;
            font-size: 1.1em;
            text-align: center;
            line-height: 1.6;
            margin-bottom: 45px; /* 控制文字和下方按钮的间距 */
        }
        
        /* ================= 5. 第一行按钮的排版容器 ================= */
        .nav-row {
            display: flex;
            justify-content: center; /* 按钮在行内居中 */
            align-items: center;
            gap: 30px; /* 按钮之间的水平间距，想宽一点就把30px改大 */
            flex-wrap: wrap; 
            width: 100%;
            margin-bottom: 30px; /* 第一行和第二行（Logo）之间的垂直间距 */
        }

        /* ================= 6. 第二行 Logo 的排版容器 ================= */
        .logo-row {
            display: flex;
            justify-content: center; /* 确保 Logo 居中 */
            align-items: center;
            width: 100%;
        }

        /* ================= 7. 每一个独立按钮的具体样式（完全复刻 TA 界面的按钮） ================= */
        .nav-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 200px;  /* 按钮的宽度 */
            height: 80px;  /* 按钮的高度 */
            padding: 0;
            background: #fff;
            border: 3px solid #18192b;
            border-radius: 20px; /* 圆角程度 */
            font-size: 1.2em;
            font-weight: 700;
            color: #18192b;
            text-decoration: none;
            font-family: 'Segoe UI', Arial, sans-serif;
            box-shadow: 0 2px 8px rgba(24,25,43,0.06);
            transition: box-shadow 0.2s, border-color 0.2s;
            cursor: pointer;
            overflow: hidden; 
            text-align: center;
        }
        /* 鼠标悬停在按钮上时的变色效果 */
        .nav-btn:hover {
            box-shadow: 0 4px 16px rgba(24,25,43,0.12);
            border-color: #3b3e5b;
            background: #f5f6fa;
        }

        /* 针对包含图片的 Logo 按钮的特殊设置 */
        .nav-logo img {
            width: 80%;
            height: 80%;
            object-fit: contain; /* 保持图片比例 */
            display: block;
        }

        /* 消除 form 表单自带的边距，以免打乱排版 */
        form {
            margin: 0;
        }
    </style>
</head>
<body>
    <div class="container">
        
        <div class="title">MO management system</div>
        
        <div class="desc">
            Welcome to the MO management system!<br>
            Here you can create new project, manage your profile, and more.
        </div>
        
        <div class="nav-row">
            
            <a class="nav-btn" href="<%= response.encodeURL("MOclasscontroller?action=create_class") %>">
                Create new project
            </a>

            <a class="nav-btn" href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">
                Personal centre
            </a>

            <a class="nav-btn" href="<%= response.encodeURL("MOclasscontroller?action=logout") %>">
                Log out
            </a>

        </div>

        <div class="logo-row">
            <a class="nav-btn nav-logo" href="#">
                <img src="images/logo.png" alt="Logo" />
            </a>
        </div>

    </div>
</body>
</html>