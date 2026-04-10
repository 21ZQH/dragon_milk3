<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Profile Centre</title>
    <style>
        body {
            background: #f7f7f7;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .box {
            background: #fff;
            width: 620px;
            margin: 60px auto;
            border: 2px solid #222;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 34px 28px;
            text-align: center;
        }
        .title {
            font-size: 1.8em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 16px;
        }
        .desc {
            color: #444;
            margin-bottom: 24px;
            line-height: 1.8;
        }
        .btn {
            display: inline-block;
            padding: 10px 18px;
            border-radius: 8px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: bold;
            border: 1px solid #d1d5db;
        }
        .btn:hover {
            background: #d1d5db;
        }
    </style>
</head>
<body>
    <div class="box">
        <div class="title">Profile Centre</div>
        <div class="desc">
            This page is reserved for profile management features (for example, changing password and personal information).
            <br/>
            You can implement the full editing form here later.
        </div>
        <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=personal_centre") %>">Back to Personal Centre</a>
    </div>
</body>
</html>
