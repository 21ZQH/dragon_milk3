<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.TA" %>
<%@ page import="model.Course" %>
<%
    // 从 Controller 获取传递过来的 TA 列表
    List<TA> taList = (List<TA>) request.getAttribute("taList");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Candidate Management</title>
    <style>
        /* 沿用统一的 UI 规范 */
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

        .candidate-name {
            font-size: 1.6em;
            font-weight: 700;
            color: #22223b;
        }

        .candidate-email {
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

        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin-bottom: 20px;
        }

        .info-item {
            font-size: 1.05em;
            color: #444;
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
    </style>
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
                for (TA ta : taList) {
                    // 获取已申请的课程数量
                    List<Course> appliedCourses = ta.getAppliedClasses();
                    int appliedCount = (appliedCourses != null) ? appliedCourses.size() : 0;
                    boolean isFull = appliedCount >= 3;
        %>
            <div class="candidate-card">
                <div class="card-header">
                    <div class="candidate-name">
                        <%= ta.getName() != null && !ta.getName().isEmpty() ? ta.getName() : "Unknown Name" %>
                        <span class="candidate-email">(<%= ta.getEmail() %>)</span>
                    </div>
                    <div class="quota-badge <%= isFull ? "full" : "" %>">
                        Applied Courses: <%= appliedCount %> / 3
                    </div>
                </div>

                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">College:</span> 
                        <%= ta.getCollege() != null && !ta.getCollege().isEmpty() ? ta.getCollege() : "N/A" %>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Skill:</span> 
                        <%= ta.getSkill() != null && !ta.getSkill().isEmpty() ? ta.getSkill() : "N/A" %>
                    </div>
                </div>

                <div class="course-section-title">Current Applications:</div>
                <% if (appliedCount > 0) { %>
                    <ul class="applied-course-list">
                        <% for (Course course : appliedCourses) { %>
                            <li class="applied-course-item">
                                <strong><%= course.getCourseName() %></strong> 
                                <span><%= course.getJobTitle() %></span>
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
</div>

</body>
</html>