<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="model.Course" %>
<%@ page import="model.TA" %>
<%@ page import="model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Course> courseList = (List<Course>) request.getAttribute("courseList");
    User currentUser = (User) session.getAttribute("user");
    boolean loggedInAsTa = currentUser instanceof TA;
    boolean hasUnreadReviewUpdate = loggedInAsTa && ((TA) currentUser).hasUnreadReviewUpdates();
    LocalDateTime applicationDeadline = (LocalDateTime) request.getAttribute("applicationDeadline");
    LocalDateTime moModifyDeadline = (LocalDateTime) request.getAttribute("moModifyDeadline");
    Boolean applicationOpenAttr = (Boolean) request.getAttribute("applicationOpen");
    Boolean moModifyOpenAttr = (Boolean) request.getAttribute("moModifyOpen");
    Boolean reviewStageOpenAttr = (Boolean) request.getAttribute("reviewStageOpen");
    boolean applicationOpen = applicationOpenAttr == null || applicationOpenAttr.booleanValue();
    boolean moModifyOpen = moModifyOpenAttr == null || moModifyOpenAttr.booleanValue();
    boolean reviewStageOpen = reviewStageOpenAttr != null && reviewStageOpenAttr.booleanValue();
    DateTimeFormatter deadlineFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>TA Jobs</title>
    <style>
        body { margin: 0; background: #f4f6f8; font-family: Arial, sans-serif; color: #1f2a44; }
        header { background: #fff; border-bottom: 1px solid #d7dee8; padding: 18px 32px; display: flex; justify-content: space-between; align-items: center; }
        nav a { margin-left: 12px; color: #23395d; text-decoration: none; font-weight: bold; }
        .nav-item { position: relative; display: inline-block; }
        .notification-dot {
            position: absolute;
            top: -8px;
            right: -10px;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background: #d92d20;
            border: 2px solid #fff;
            box-shadow: 0 0 0 2px #23395d;
        }
        main { max-width: 860px; margin: 32px auto; }
        .status-panel { background: #fff; border: 1px solid #d7dee8; border-radius: 8px; padding: 18px 20px; margin-bottom: 22px; }
        .status-title { font-size: 18px; font-weight: 700; margin-bottom: 12px; }
        .status-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
        .status-item { border: 1px solid #e1e6ef; border-radius: 8px; padding: 14px; background: #fbfcfe; }
        .status-label { color: #5f6f85; font-size: 13px; font-weight: 700; text-transform: uppercase; margin-bottom: 8px; }
        .status-value { font-size: 16px; font-weight: 700; margin-bottom: 8px; }
        .status-meta { color: #5f6f85; line-height: 1.45; }
        .pill { display: inline-flex; align-items: center; border-radius: 999px; padding: 5px 10px; font-size: 13px; font-weight: 700; }
        .pill-open { background: #e8f5ee; color: #166534; }
        .pill-closed { background: #fff0f0; color: #b42318; }
        .pill-neutral { background: #eef2ff; color: #23395d; }
        .course { display: block; background: #fff; border: 1px solid #d7dee8; border-radius: 8px; padding: 20px; margin-bottom: 14px; text-decoration: none; color: inherit; }
        .course:hover { border-color: #23395d; }
        .name { font-size: 20px; font-weight: bold; margin-bottom: 8px; }
        .meta { color: #5f6f85; }
        @media (max-width: 720px) {
            header { align-items: flex-start; flex-direction: column; gap: 12px; padding: 16px 20px; }
            nav a { margin-left: 0; margin-right: 12px; }
            main { margin: 24px 16px; }
            .status-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<header>
    <strong>TA Recruitment</strong>
    <nav>
        <a href="<%= request.getContextPath() %>/ta">Jobs</a>
        <span class="nav-item">
            <a href="<%= request.getContextPath() %>/TAclasscontroller?action=personal_centre">Personal Center</a>
            <% if (hasUnreadReviewUpdate) { %>
                <span class="notification-dot" aria-hidden="true"></span>
            <% } %>
        </span>
        <% if (loggedInAsTa) { %>
            <a href="<%= request.getContextPath() %>/TAclasscontroller?action=logout">Logout</a>
        <% } else { %>
            <a href="<%= request.getContextPath() %>/ta?action=auth">TA Login/Register</a>
        <% } %>
    </nav>
</header>
<main>
    <h1>Available TA Positions</h1>
    <section class="status-panel" aria-label="Recruitment status">
        <div class="status-title">Recruitment Timeline</div>
        <div class="status-grid">
            <div class="status-item">
                <div class="status-label">TA Application Deadline</div>
                <div class="status-value"><%= applicationDeadline == null ? "Not set" : applicationDeadline.format(deadlineFormatter) %></div>
                <div class="status-meta">
                    <span class="pill <%= applicationOpen ? "pill-open" : "pill-closed" %>">
                        <%= applicationOpen ? "Applications open" : "Applications closed" %>
                    </span>
                    <div style="margin-top:8px;">
                        <%= applicationOpen
                                ? "TAs can submit, modify, or withdraw applications before this deadline."
                                : "TAs can no longer submit, modify, or withdraw applications." %>
                    </div>
                </div>
            </div>
            <div class="status-item">
                <div class="status-label">Course Information Deadline</div>
                <div class="status-value"><%= moModifyDeadline == null ? "Not set" : moModifyDeadline.format(deadlineFormatter) %></div>
                <div class="status-meta">
                    <span class="pill <%= moModifyOpen ? "pill-open" : "pill-closed" %>">
                        <%= moModifyOpen ? "Recruitment editing open" : "Recruitment editing closed" %>
                    </span>
                    <div style="margin-top:8px;">
                        <%= moModifyOpen
                                ? "MOs may still publish or update course recruitment information."
                                : "Course recruitment information is locked for MOs." %>
                    </div>
                </div>
            </div>
        </div>
        <div style="margin-top:12px;">
            <span class="pill <%= reviewStageOpen ? "pill-neutral" : "pill-open" %>">
                <%= reviewStageOpen ? "Review stage open for MOs" : "Review opens after the TA deadline" %>
            </span>
        </div>
    </section>
    <% if (courseList != null && !courseList.isEmpty()) {
        for (int i = 0; i < courseList.size(); i++) {
            Course c = courseList.get(i);
    %>
        <a class="course" href="<%= request.getContextPath() %>/ta?action=detail&courseIndex=<%= i %>">
            <div class="name"><%= c.getCourseName() %></div>
            <div class="meta"><%= c.getJobTitle() %></div>
            <div class="meta">TA Positions: <%= c.getTaPositions() > 0 ? c.getTaPositions() : "Not set" %></div>
        </a>
    <%  }
    } else { %>
        <div class="course">No courses available.</div>
    <% } %>
</main>
</body>
</html>
