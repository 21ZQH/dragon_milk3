<%@ page import="model.Course" %>
<%@ page import="model.TA" %>
<%@ page import="model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Course course = (Course) request.getAttribute("selectedCourse");
    String courseIndex = (String) request.getAttribute("courseIndex");
    String success = (String) request.getAttribute("success");
    Boolean applicationOpenAttr = (Boolean) request.getAttribute("applicationOpen");
    boolean applicationOpen = applicationOpenAttr == null || applicationOpenAttr;
    User currentUser = (User) session.getAttribute("user");
    TA currentTA = null;
    if (currentUser instanceof TA) {
        currentTA = (TA) currentUser;
    }

    boolean hasApplied = false;
    if (course != null && currentTA != null && course.getId() != null) {
        String resumeDirectory = currentTA.getResumeDirectoryForCourse(course.getId());
        hasApplied = (resumeDirectory != null && !resumeDirectory.isBlank());
    }
%>
<html>
<head>
    <title>Course Details</title>
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
            padding: 20px 18px;
            background: #f5f7fa;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }
        .label {
            font-size: 1.05em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 8px;
        }
        .value {
            color: #444;
            line-height: 1.8;
            white-space: pre-wrap;
            word-break: break-word;
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
            border: 1px solid #d1d5db;
            transition: background 0.2s;
        }
        .nav-btn:hover {
            background: #d1d5db;
        }
        .notice-box {
            margin-top: 18px;
            padding: 14px 18px;
            border-radius: 8px;
            background: #fff7e6;
            color: #9a6700;
            border: 1px solid #f2cc60;
            text-align: center;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="main-box">
        <div class="title">Course Information</div>
        <% if (success != null) { %>
            <div class="success-box"><%= success %></div>
        <% } %>
        <% if (course != null) { %>
            <div class="detail-box">
                <div class="label">Course Name</div>
                <div class="value"><%= course.getCourseName() %></div>
            </div>
            <div class="detail-box">
                <div class="label">Job Title</div>
                <div class="value"><%= course.getJobTitle() %></div>
            </div>
            <div class="detail-box">
                <div class="label">Working Hours</div>
                <div class="value"><%= course.getWorkingHours() %></div>
            </div>
            <div class="detail-box">
                <div class="label">Job Requirement</div>
                <div class="value"><%= course.getJobRequirement() %></div>
            </div>
            <div class="button-row">
                <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Back to List</a>
                <% if (applicationOpen) { %>
                    <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=go_apply&courseIndex=" + courseIndex) %>">
                        <%= hasApplied ? "Modify Resume" : "Go Apply" %>
                    </a>
                <% } %>
            </div>
            <% if (!applicationOpen) { %>
                <div class="notice-box">The application deadline has passed. New applications and resume updates are no longer available.</div>
            <% } %>
        <% } else { %>
            <div class="detail-box">
                <div class="value">Course information is unavailable.</div>
            </div>
            <div class="button-row">
                <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Back to List</a>
            </div>
        <% } %>
    </div>
</body>
</html>
