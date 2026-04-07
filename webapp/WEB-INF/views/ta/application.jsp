<%@ page import="model.Course" %>
<%@ page import="model.TA" %>
<%@ page import="model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Course course = (Course) request.getAttribute("selectedCourse");
    String courseIndex = (String) request.getAttribute("courseIndex");
    String error = (String) request.getAttribute("error");
    User currentUser = (User) session.getAttribute("user");
    TA currentTA = null;
    if (currentUser instanceof TA) {
        currentTA = (TA) currentUser;
    }
%>
<html>
<head>
    <title>Application</title>
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
        .error-box {
            margin-bottom: 24px;
            padding: 14px 18px;
            border-radius: 8px;
            background: #fdeeee;
            color: #a12626;
            border: 1px solid #efb7b7;
            text-align: center;
            font-weight: bold;
        }
        .warning-box {
            margin-top: 24px;
            padding: 14px 18px;
            border-radius: 8px;
            background: #fff7e6;
            color: #9a6700;
            border: 1px solid #f2cc60;
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
        .sub-title {
            font-size: 1.15em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 12px;
        }
        .text-input, .text-area, .select-input {
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
            min-height: 180px;
            resize: vertical;
            line-height: 1.7;
        }
        .hint {
            margin-top: 10px;
            color: #666;
            font-size: 0.95em;
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
        <div class="title">Application</div>
        <% if (error != null) { %>
            <div class="error-box"><%= error %></div>
        <% } %>
        <% if (course != null && currentTA != null) { %>
            <div class="detail-box">
                <div class="sub-title">Applying For</div>
                <div><strong>Course:</strong> <%= course.getCourseName() %></div>
                <div><strong>Job Title:</strong> <%= course.getJobTitle() %></div>
            </div>
            <form action="<%= response.encodeURL("TAclasscontroller") %>" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="upload_resume">
                <input type="hidden" name="courseIndex" value="<%= courseIndex %>">

                <div class="detail-box">
                    <div class="sub-title">Upload your resume</div>
                    <label class="label" for="resumeFile">Resume File</label>
                    <input class="text-input" type="file" id="resumeFile" name="resumeFile" accept=".pdf,application/pdf" required>
                    <div class="hint">Only PDF resumes are accepted.</div>
                </div>

                <div class="button-row">
                    <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=show_all_information&courseIndex=" + courseIndex) %>">Back to Details</a>
                    <button class="nav-btn" type="submit">Submit Resume</button>
                </div>
            </form>
        <% } else { %>
            <div class="detail-box">Current course or TA information is unavailable.</div>
            <div class="button-row">
                <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Back to List</a>
            </div>
        <% } %>
    </div>
</body>
</html>
