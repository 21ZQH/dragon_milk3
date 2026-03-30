<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
%>
    <script>
        alert("<%= error %>");
        window.location.href = "start.html";
    </script>
<%
        return;
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String username = (String) session.getAttribute("username");
    if (username == null) {
        username = "Guest";
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>Main Page for TA</title>
    <style> 
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 0;
        }
        .container {
            background: #fff;
            max-width: 600px;
            margin: 48px auto;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 32px 32px 32px 32px;
        }
        .nav-row {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 20px; 
            flex-wrap: wrap; 
            margin: 40px 0 32px 0;
            width: 100%;
            box-sizing: border-box;
        }

        .nav-cell {
            flex: 1;
            border-right: 2px solid #22223b;
            background: #fff;
            font-size: 1.2em;
            font-weight: 700;
            color: #22223b;
            text-align: center;
            padding: 18px 0;
            font-family: 'Segoe UI', Arial, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .nav-cell:last-child {
            border-right: none;
        }
        .nav-logo img {
            height: 32px;
        }
        .nav-link, .nav-btn {
            color: #22223b;
            text-decoration: none;
            background: none;
            border: none;
            font-family: inherit;
            font-size: inherit;
            font-weight: inherit;
            cursor: pointer;
            padding: 0;
        }
        .title {
            font-size: 2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 24px;
            text-align: center;
        }
        .welcome {
            font-size: 1.1em;
            color: #22223b;
            margin-bottom: 12px;
            text-align: center;
        }
        .desc {
            color: #444;
            margin-bottom: 32px;
            font-size: 1em;
            text-align: center;
        }
        .main-buttons button {
            width: 100%;
            font-size: 1.15em;
            font-weight: 700;
            margin: 12px 0;
            padding: 14px 0;
            border-radius: 8px;
            border: none;
            background: #e9edfa;
            color: #22223b;
            cursor: pointer;
            transition: background 0.2s;
        }
        .main-buttons button:hover {
            background: #bfc8e6;
        }
        .nav-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 170px;
            height: 80px;
            padding: 0;
            background: #fff;
            border: 3px solid #18192b;
            border-radius: 20px;
            font-size: 1.3em;
            font-weight: 700;
            color: #18192b;
            text-decoration: none;
            font-family: 'Segoe UI', Arial, sans-serif;
            box-shadow: 0 2px 8px rgba(24,25,43,0.06);
            transition: box-shadow 0.2s, border-color 0.2s;
            cursor: pointer;
            overflow: hidden; 
        }

        .nav-logo img {
            width: 100%;
            height: 100%;
            object-fit: contain; /* 保持图片比例，充满按钄1�7 */
            display: block;
        }

        .nav-btn:hover {
            box-shadow: 0 4px 16px rgba(24,25,43,0.12);
            border-color: #3b3e5b;
            background: #f5f6fa;
        }

       
        form {
            margin: 0;
        }
    </style>
</head>
<body>
    <div class="container">
       
        <div class="title">TA Recruitment System</div>
        <div class="welcome">Hi, <%= username %></div>
        <div class="desc">
            Welcome to the TA management system! Here you can find job opportunities, manage your profile, and more.
        </div>
         <div class="nav-row">
          
        <a class="nav-btn" href="findJob.jsp">Find a Job</a>
        <a class="nav-btn" href="personalCentre.jsp">Personal centre</a>
        <form action="logout" method="post" style="display:inline;">
            <button class="nav-btn" type="submit">Log out</button>
        </form>
        <a class="nav-btn nav-logo" href="#">
            <img src="images/logo.png" alt="Logo" />
        </a>
            </div>
        </div>
    </div>
</body>
</html>
