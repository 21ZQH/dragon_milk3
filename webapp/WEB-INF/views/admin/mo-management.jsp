<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page import="model.Mo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Mo> moList = (List<Mo>) request.getAttribute("moList");
    List<Course> generatedCourseDrafts = (List<Course>) request.getAttribute("generatedCourseDrafts");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
    int moPageSize = 5;
    int moPage = 1;
    try {
        moPage = Integer.parseInt(request.getParameter("moPage"));
    } catch (Exception ignored) {
        moPage = 1;
    }
    int moTotal = moList == null ? 0 : moList.size();
    int moTotalPages = Math.max(1, (int) Math.ceil(moTotal / (double) moPageSize));
    moPage = Math.max(1, Math.min(moPage, moTotalPages));
    int moStart = (moPage - 1) * moPageSize;
    int moEnd = Math.min(moStart + moPageSize, moTotal);
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
        .pager { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-top: 16px; }
        .pager a, .pager span { border: 1px solid #cfd6e4; border-radius: 8px; padding: 7px 11px; text-decoration: none; color: #22223b; font-weight: 700; background: #fff; }
        .pager .current { background: #22223b; color: #fff; border-color: #22223b; }
        .pager .disabled { color: #9aa3b2; background: #f3f5fa; }
        .pager-info { color: #596579; font-size: 0.94em; margin-top: 10px; }
        .draft-table-wrap { margin: 0 0 18px; overflow-x: auto; border: 1px solid #cfd6e4; border-radius: 10px; }
        .draft-table { width: 100%; border-collapse: collapse; font-size: 0.95em; }
        .draft-table th, .draft-table td { border-bottom: 1px solid #e1e6ef; padding: 11px 12px; text-align: left; vertical-align: top; }
        .draft-table th { background: #f3f5fa; font-weight: 800; color: #22223b; white-space: nowrap; }
        .draft-table tr:last-child td { border-bottom: none; }
        .status-pill { display: inline-block; border-radius: 999px; padding: 4px 10px; background: #fff7e6; color: #9a6400; border: 1px solid #f2cf7a; font-weight: 700; white-space: nowrap; }
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
        <% if (generatedCourseDrafts != null && !generatedCourseDrafts.isEmpty()) { %>
            <div class="draft-table-wrap">
                <table class="draft-table">
                    <thead>
                        <tr>
                            <th>Course</th>
                            <th>Job Title</th>
                            <th>Job Description</th>
                            <th>Job Requirement</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Course course : generatedCourseDrafts) { %>
                            <tr>
                                <td><%= course.getCourseName() == null || course.getCourseName().isBlank() ? "Untitled Course" : course.getCourseName() %></td>
                                <td><%= course.getJobTitle() == null || course.getJobTitle().isBlank() ? "Teaching Assistant" : course.getJobTitle() %></td>
                                <td><%= course.getJobDescription() == null || course.getJobDescription().isBlank() ? "Not generated" : course.getJobDescription() %></td>
                                <td><%= course.getJobRequirement() == null || course.getJobRequirement().isBlank() ? "Not generated" : course.getJobRequirement() %></td>
                                <td><span class="status-pill"><%= course.isRecruitmentPublished() ? "Published" : "Draft" %></span></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>

        <div class="grid">
            <div class="panel">
                <h2>Create MO</h2>
                <form action="<%= response.encodeURL("AdminController") %>" method="post">
                    <input type="hidden" name="action" value="create_mo">

                    <label for="account">Account</label>
                    <input id="account" name="account" required>

                    <label for="password">Password</label>
                    <input id="password" name="password" required>

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
                    for (int i = moStart; i < moEnd; i++) {
                        Mo mo = moList.get(i);
                %>
                    <div class="mo-card">
                        <div class="mo-name"><%= mo.getEmail() == null || mo.getEmail().isBlank() ? "Unnamed MO" : mo.getEmail() %></div>
                        <div class="mo-meta">Account: <%= mo.getEmail() %></div>
                        <div class="mo-meta">Password: <%= mo.getPassword() == null || mo.getPassword().isBlank() ? "N/A" : mo.getPassword() %></div>
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
                %>
                    <div class="pager-info">Showing <%= moStart + 1 %>-<%= moEnd %> of <%= moTotal %> MO account(s).</div>
                    <div class="pager">
                        <% if (moPage > 1) { %>
                            <a href="<%= response.encodeURL("AdminController?action=manage_mo&moPage=" + (moPage - 1)) %>">Previous</a>
                        <% } else { %>
                            <span class="disabled">Previous</span>
                        <% } %>
                        <% for (int pageNumber = 1; pageNumber <= moTotalPages; pageNumber++) { %>
                            <% if (pageNumber == moPage) { %>
                                <span class="current"><%= pageNumber %></span>
                            <% } else { %>
                                <a href="<%= response.encodeURL("AdminController?action=manage_mo&moPage=" + pageNumber) %>"><%= pageNumber %></a>
                            <% } %>
                        <% } %>
                        <% if (moPage < moTotalPages) { %>
                            <a href="<%= response.encodeURL("AdminController?action=manage_mo&moPage=" + (moPage + 1)) %>">Next</a>
                        <% } else { %>
                            <span class="disabled">Next</span>
                        <% } %>
                    </div>
                <%
                } %>
            </div>
        </div>
    </div>
</body>
</html>

