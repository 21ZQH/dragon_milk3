<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page import="model.Mo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Mo> moList = (List<Mo>) request.getAttribute("moList");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <title>MO Management</title>
    <style>
        body { background: #f7f7f9; font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 34px 0; color: #22223b; }
        .container { width: 1040px; max-width: calc(100vw - 48px); margin: 0 auto; background: #fff; border: 2px solid #22223b; border-radius: 16px; padding: 30px; box-sizing: border-box; }
        .top-line { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 20px; }
        h1 { margin: 0; font-size: 2em; }
        .back-link, .btn { display: inline-block; text-decoration: none; border: 2px solid #22223b; border-radius: 8px; padding: 10px 16px; color: #22223b; font-weight: 700; background: #fff; cursor: pointer; }
        .btn-primary { background: #22223b; color: #fff; }
        .grid { display: grid; grid-template-columns: 0.9fr 1.1fr; gap: 24px; align-items: start; }
        .panel { border: 1px solid #cfd6e4; border-radius: 10px; padding: 20px; background: #fff; }
        h2 { margin-top: 0; font-size: 1.35em; }
        label { display: block; font-weight: 700; margin: 14px 0 7px; }
        input, textarea { width: 100%; box-sizing: border-box; border: 1px solid #c7ccd8; border-radius: 8px; padding: 11px 12px; font-family: inherit; font-size: 1em; }
        textarea { min-height: 120px; resize: vertical; line-height: 1.5; }
        .hint { color: #596579; line-height: 1.5; font-size: 0.95em; margin-top: 8px; }
        .message { margin-bottom: 16px; padding: 12px 16px; border-radius: 8px; font-weight: 700; }
        .success { background: #edf7ed; color: #256029; border: 1px solid #b7dfb9; }
        .error { background: #fdeeee; color: #a12626; border: 1px solid #efb7b7; }
        .mo-card { border: 1px solid #d7ddea; border-radius: 8px; padding: 14px; margin-bottom: 12px; }
        .mo-name { font-weight: 800; font-size: 1.05em; }
        .mo-meta { color: #4b5565; margin: 5px 0 8px; }
        .course-list { margin: 8px 0 0 18px; padding: 0; color: #26364f; }
        @media (max-width: 850px) { .grid { grid-template-columns: 1fr; } }
    </style>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/role-style.css">
</head>
<body>
    <div class="container">
        <div class="top-line">
            <h1>MO Account and Course Assignment</h1>
            <a class="back-link" href="<%= response.encodeURL("AdminController?action=dashboard") %>">Back to Dashboard</a>
        </div>

        <% if (success != null) { %>
            <div class="message success"><%= success %></div>
        <% } %>
        <% if (error != null) { %>
            <div class="message error"><%= error %></div>
        <% } %>

        <div class="grid">
            <div class="panel">
                <h2>Create MO</h2>
                <form action="<%= response.encodeURL("AdminController") %>" method="post">
                    <input type="hidden" name="action" value="create_mo">

                    <label for="name">MO Name</label>
                    <input id="name" name="name" required>

                    <label for="account">Account</label>
                    <input id="account" name="account" required>

                    <label for="password">Password</label>
                    <input id="password" name="password" required>

                    <label for="degree">Degree</label>
                    <input id="degree" name="degree">

                    <label for="college">College</label>
                    <input id="college" name="college">

                    <label for="courseNames">Assigned Courses</label>
                    <textarea id="courseNames" name="courseNames" required placeholder="Software Engineering&#10;Database Systems"></textarea>
                    <div class="hint">Enter one course per line. Existing course names will be reused; new names will be created as unpublished courses.</div>

                    <div style="margin-top:18px;">
                        <button class="btn btn-primary" type="submit">Create MO Account</button>
                    </div>
                </form>
            </div>

            <div class="panel">
                <h2>Existing MOs</h2>
                <% if (moList == null || moList.isEmpty()) { %>
                    <div class="hint">No MO accounts found.</div>
                <% } else {
                    for (Mo mo : moList) {
                %>
                    <div class="mo-card">
                        <div class="mo-name"><%= mo.getName() == null || mo.getName().isBlank() ? "Unnamed MO" : mo.getName() %></div>
                        <div class="mo-meta">Account: <%= mo.getEmail() %></div>
                        <div class="mo-meta"><%= mo.getCollege() == null ? "" : mo.getCollege() %></div>
                        <% if (mo.getOwnedCourses() == null || mo.getOwnedCourses().isEmpty()) { %>
                            <div class="hint">No assigned courses.</div>
                        <% } else { %>
                            <ul class="course-list">
                                <% for (Course course : mo.getOwnedCourses()) { %>
                                    <li>
                                        <%= course.getCourseName() == null || course.getCourseName().isBlank() ? "Untitled Course" : course.getCourseName() %>
                                        - <%= course.isRecruitmentPublished() ? "published" : "not published" %>
                                    </li>
                                <% } %>
                            </ul>
                        <% } %>
                    </div>
                <%  }
                } %>
            </div>
        </div>
    </div>
</body>
</html>

