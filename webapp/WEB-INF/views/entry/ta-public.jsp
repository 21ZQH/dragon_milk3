<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Course> courseList = (List<Course>) request.getAttribute("courseList");
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
        main { max-width: 860px; margin: 32px auto; }
        .course { display: block; background: #fff; border: 1px solid #d7dee8; border-radius: 8px; padding: 20px; margin-bottom: 14px; text-decoration: none; color: inherit; }
        .course:hover { border-color: #23395d; }
        .name { font-size: 20px; font-weight: bold; margin-bottom: 8px; }
        .meta { color: #5f6f85; }
    </style>
</head>
<body>
<header>
    <strong>TA Recruitment</strong>
    <nav>
        <a href="<%= request.getContextPath() %>/ta">Jobs</a>
        <a href="<%= request.getContextPath() %>/TAclasscontroller?action=personal_centre">Personal Center</a>
        <a href="<%= request.getContextPath() %>/ta?action=auth">TA Login/Register</a>
    </nav>
</header>
<main>
    <h1>Available TA Positions</h1>
    <% if (courseList != null && !courseList.isEmpty()) {
        for (int i = 0; i < courseList.size(); i++) {
            Course c = courseList.get(i);
    %>
        <a class="course" href="<%= request.getContextPath() %>/ta?action=detail&courseIndex=<%= i %>">
            <div class="name"><%= c.getCourseName() %></div>
            <div class="meta"><%= c.getJobTitle() %> | <%= c.getWorkingHours() %> | <%= c.getSalary() %></div>
        </a>
    <%  }
    } else { %>
        <div class="course">No courses available.</div>
    <% } %>
</main>
</body>
</html>
