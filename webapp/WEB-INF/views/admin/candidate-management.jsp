<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.TA" %>
<%@ page import="model.Course" %>
<%
    // 从 Controller 获取传递过来的 TA 列表
    List<TA> taList = (List<TA>) request.getAttribute("taList");
    int taPageSize = 5;
    int taPage = 1;
    try {
        taPage = Integer.parseInt(request.getParameter("taPage"));
    } catch (Exception ignored) {
        taPage = 1;
    }
    int taTotal = taList == null ? 0 : taList.size();
    int taTotalPages = Math.max(1, (int) Math.ceil(taTotal / (double) taPageSize));
    taPage = Math.max(1, Math.min(taPage, taTotalPages));
    int taStart = (taPage - 1) * taPageSize;
    int taEnd = Math.min(taStart + taPageSize, taTotal);
%>
<!DOCTYPE html>
<html>
<head>
    <title>Candidate Management</title>
    <style>
        
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
            max-width: 900px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 36px 40px 40px 40px;
            box-sizing: border-box;
        }

        .top-line {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #22223b;
            padding-bottom: 15px;
            margin-bottom: 25px;
        }

        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin: 0;
        }

        .back-link {
            text-decoration: none;
            color: #22223b;
            font-weight: 600;
            border: 2px solid #22223b;
            padding: 10px 18px;
            border-radius: 12px;
            background: #fff;
            transition: all 0.2s ease;
        }

        .back-link:hover {
            background: #22223b;
            color: #fff;
            box-shadow: 0 4px 10px rgba(34,34,59,0.12);
        }

        .candidate-list {
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        /* 候选人信息卡片 */
        .candidate-card {
            border: 2px solid #22223b;
            border-radius: 16px;
            padding: 24px;
            background: #fcfcfe;
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
            border-bottom: 1px dashed #ccc;
            padding-bottom: 10px;
        }

        .candidate-email-main {
            font-size: 1.6em;
            font-weight: 700;
            color: #22223b;
            word-break: break-word;
        }

        .candidate-key {
            color: #666;
            font-size: 0.9em;
            margin-left: 10px;
            font-weight: normal;
        }

        /* 申请进度条标识 */
        .quota-badge {
            background: #e9ecf5;
            color: #22223b;
            padding: 6px 12px;
            border-radius: 8px;
            font-weight: 700;
            border: 1px solid #d1d5db;
        }

        .quota-badge.full {
            background: #fdeeee;
            color: #a12626;
            border-color: #efb7b7;
        }

        .info-label {
            font-weight: 700;
            color: #22223b;
        }

        .course-section-title {
            font-weight: 700;
            color: #22223b;
            margin-bottom: 10px;
            font-size: 1.1em;
        }

        .applied-course-list {
            list-style-type: none;
            padding: 0;
            margin: 0;
        }

        .applied-course-item {
            background: #fff;
            border: 1px solid #ddd;
            padding: 10px 15px;
            border-radius: 8px;
            margin-bottom: 8px;
            font-size: 0.95em;
            color: #333;
            display: flex;
            justify-content: space-between;
            align-items: center; 
        }

        .empty-box {
            border: 2px dashed #bbb;
            border-radius: 14px;
            padding: 40px 24px;
            text-align: center;
            color: #666;
            background: #fafafa;
            font-size: 1.1em;
        }

        .pager {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 8px;
            margin-top: 22px;
        }

        .pager a, .pager span {
            border: 1px solid #cfd6e4;
            border-radius: 8px;
            padding: 7px 11px;
            text-decoration: none;
            color: #22223b;
            font-weight: 700;
            background: #fff;
        }

        .pager .current {
            background: #22223b;
            color: #fff;
            border-color: #22223b;
        }

        .pager .disabled {
            color: #9aa3b2;
            background: #f3f5fa;
        }

        .pager-info {
            color: #596579;
            font-size: 0.94em;
            margin-top: 16px;
        }
    </style>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/role-style.css">
</head>
<body>

<div class="container">
    <div class="top-line">
        <h1 class="page-title">Candidate Management</h1>
        <a class="back-link" href="<%= response.encodeURL("AdminController?action=dashboard") %>">Back to Dashboard</a>
    </div>

    <div class="candidate-list">
        <%
            if (taList != null && !taList.isEmpty()) {
                for (int i = taStart; i < taEnd; i++) {
                    TA ta = taList.get(i);
                    // 获取已申请的课程数量
                    List<Course> appliedCourses = ta.getAppliedClasses();
                    int appliedCount = (appliedCourses != null) ? appliedCourses.size() : 0;
                    
        %>
            <div class="candidate-card">
                <div class="card-header">
                    <div class="candidate-email-main">
                        <%= ta.getEmail() %>
                        <span class="candidate-key">Key: <%= ta.getPassword() == null || ta.getPassword().isBlank() ? "N/A" : ta.getPassword() %></span>
                    </div>
                    <div class="quota-badge">
                        Applied Courses: <%= appliedCount %>
                    </div>
                </div>

                <div class="course-section-title">Applied Courses:</div>
                <% if (appliedCount > 0) { %>
                    <ul class="applied-course-list">
                        <% for (Course course : appliedCourses) { %>
                            <li class="applied-course-item">
                                <div>
                                    <strong><%= course.getCourseName() %></strong> 
                                    <span>(<%= course.getJobTitle() %>)</span>
                                </div>
                            </li>
                        <% } %>
                    </ul>
                <% } else { %>
                    <div style="color: #888; font-style: italic; font-size: 0.95em;">No courses applied yet.</div>
                <% } %>
            </div>
        <%
                }
            } else {
        %>
            <div class="empty-box">No candidates found in the system.</div>
        <%
            }
        %>
    </div>
    <% if (taList != null && !taList.isEmpty()) { %>
        <div class="pager-info">Showing <%= taStart + 1 %>-<%= taEnd %> of <%= taTotal %> candidate(s).</div>
        <div class="pager">
            <% if (taPage > 1) { %>
                <a href="<%= response.encodeURL("AdminController?action=candidate_management&taPage=" + (taPage - 1)) %>">Previous</a>
            <% } else { %>
                <span class="disabled">Previous</span>
            <% } %>
            <% for (int pageNumber = 1; pageNumber <= taTotalPages; pageNumber++) { %>
                <% if (pageNumber == taPage) { %>
                    <span class="current"><%= pageNumber %></span>
                <% } else { %>
                    <a href="<%= response.encodeURL("AdminController?action=candidate_management&taPage=" + pageNumber) %>"><%= pageNumber %></a>
                <% } %>
            <% } %>
            <% if (taPage < taTotalPages) { %>
                <a href="<%= response.encodeURL("AdminController?action=candidate_management&taPage=" + (taPage + 1)) %>">Next</a>
            <% } else { %>
                <span class="disabled">Next</span>
            <% } %>
        </div>
    <% } %>
</div>

</body>
</html>
