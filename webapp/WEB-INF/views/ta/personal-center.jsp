<%@ page import="model.TA" %>
<%@ page import="model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User currentUser = (User) session.getAttribute("user");
    TA currentTA = null;
    if (currentUser instanceof TA) {
        currentTA = (TA) currentUser;
    }
    String success = (String) request.getAttribute("success");
%>
<html>
<head>
    <title>Personal Centre</title>
    <style>
        body {
            background: #f7f7f7;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .main-box {
            background: #fff;
            width: 700px;
            margin: 60px auto;
            border: 2px solid #222;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 40px 30px 30px 30px;
        }
        .title {
            text-align: center;
            font-size: 2em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 30px;
        }
        .success-box {
            margin-bottom: 24px;
            padding: 14px 18px;
            border-radius: 8px;
            background: #edf7ed;
            color: #256029;
            border: 1px solid #b7dfb9;
            text-align: center;
            font-weight: bold;
        }
        .detail-box {
            border: 1px solid #bbb;
            border-radius: 8px;
            margin: 18px 0;
            padding: 18px 16px;
            background: #f5f7fa;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }
        .label {
            display: block;
            font-size: 1.05em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 8px;
        }
        .text-input, .text-area {
            width: 100%;
            box-sizing: border-box;
            border: 1px solid #c7ccd8;
            border-radius: 8px;
            padding: 12px 14px;
            font-size: 1em;
            color: #444;
            background: #fff;
        }
        .text-area {
            min-height: 140px;
            resize: vertical;
            line-height: 1.7;
        }
        .button-row {
            display: flex;
            justify-content: center;
            gap: 16px;
            margin-top: 30px;
            flex-wrap: wrap;
        }
        .nav-btn {
            display: inline-block;
            padding: 12px 24px;
            border-radius: 8px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: bold;
            font-family: inherit;
            font-size: 1em;
            border: 1px solid #d1d5db;
            transition: background 0.2s;
            cursor: pointer;
            appearance: none;
            -webkit-appearance: none;
        }
        .nav-btn:hover {
            background: #d1d5db;
        }
    </style>
    <script>
        function goBackOrHome() {
            if (document.referrer && document.referrer.length > 0) {
                history.back();
                return;
            }
            window.location.href = '<%= response.encodeURL("TAclasscontroller?action=view_information") %>';
        }
    </script>
</head>
<body>
    <div class="main-box">
        <div class="title">Personal Centre</div>
        <% if (success != null) { %>
            <div class="success-box"><%= success %></div>
        <% } %>
        <% if (currentTA != null) { %>
            <form action="<%= response.encodeURL("TAclasscontroller") %>" method="post">
                <input type="hidden" name="action" value="save_personal_information">

                <div class="detail-box">
                    <label class="label" for="name">Name</label>
                    <input class="text-input" type="text" id="name" name="name" value="<%= currentTA.getName() == null ? "" : currentTA.getName() %>">
                </div>

                <div class="detail-box">
                    <label class="label" for="email">Email</label>
                    <input class="text-input" type="text" id="email" value="<%= currentTA.getEmail() == null ? "" : currentTA.getEmail() %>" readonly>
                </div>

                <div class="detail-box">
                    <label class="label" for="password">Password</label>
                    <input class="text-input" type="text" id="password" value="<%= currentTA.getPassword() == null ? "" : currentTA.getPassword() %>" readonly>
                </div>

                <div class="detail-box">
                    <label class="label" for="college">Educational Background</label>
                    <input class="text-input" type="text" id="college" name="college" value="<%= currentTA.getCollege() == null ? "" : currentTA.getCollege() %>">
                </div>

                <div class="detail-box">
                    <label class="label" for="skill">Skill</label>
                    <textarea class="text-area" id="skill" name="skill"><%= currentTA.getSkill() == null ? "" : currentTA.getSkill() %></textarea>
                </div>

                <div class="button-row">
                    <button class="nav-btn" type="button" onclick="goBackOrHome()">Back</button>
                    <button class="nav-btn" type="submit">Save</button>
                </div>
            </form>
        <% } else { %>
            <div class="detail-box">Current TA information is unavailable.</div>
            <div class="button-row">
                <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Back to Home</a>
            </div>
        <% } %>
    </div>
</body>
</html>
