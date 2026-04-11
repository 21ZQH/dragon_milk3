<%@ page import="model.TA" %>
<%@ page import="model.User" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Set" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User currentUser = (User) session.getAttribute("user");
    TA currentTA = null;
    if (currentUser instanceof TA) {
        currentTA = (TA) currentUser;
    }
    String[] skillOptions = {
            "Java", "Python", "C++", "C", "JavaScript",
            "TypeScript", "SQL", "HTML/CSS", "Spring Boot", "Servlet/JSP",
            "Git", "Linux", "Data Structures", "Algorithms", "Machine Learning"
    };
    Set<String> selectedSkills = new LinkedHashSet<String>();
    if (currentTA != null && currentTA.getSkill() != null && !currentTA.getSkill().trim().isEmpty()) {
        selectedSkills.addAll(Arrays.asList(currentTA.getSkill().split("\\s*,\\s*")));
    }
%>
<html>
<head>
    <title>Edit Skill</title>
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
            margin-bottom: 12px;
        }
        .skill-options {
            display: flex;
            flex-wrap: wrap;
            gap: 12px 18px;
        }
        .skill-item {
            min-width: 180px;
            color: #444;
            font-size: 1em;
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
</head>
<body>
    <div class="main-box">
        <div class="title">Edit Skill</div>
        <% if (currentTA != null) { %>
            <form action="<%= response.encodeURL("TAclasscontroller") %>" method="post">
                <input type="hidden" name="action" value="save_personal_information">
                <input type="hidden" name="name" value="<%= currentTA.getName() == null ? "" : currentTA.getName() %>">
                <input type="hidden" name="college" value="<%= currentTA.getCollege() == null ? "" : currentTA.getCollege() %>">

                <div class="detail-box">
                    <label class="label">Skill</label>
                    <div class="skill-options">
                        <% for (String option : skillOptions) { %>
                            <label class="skill-item">
                                <input type="checkbox" name="skill" value="<%= option %>" <%= selectedSkills.contains(option) ? "checked" : "" %>>
                                <%= option %>
                            </label>
                        <% } %>
                    </div>
                </div>

                <div class="button-row">
                    <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=profile_center") %>">Back</a>
                    <button class="nav-btn" type="submit">Save</button>
                </div>
            </form>
        <% } else { %>
            <div class="detail-box">Current TA information is unavailable.</div>
            <div class="button-row">
                <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=personal_centre") %>">Back to Personal Centre</a>
            </div>
        <% } %>
    </div>
</body>
</html>
