<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 分页参数
    int pageSize = 3; // 每页显示3个
    
    int page_1 = 1;
    String pageParam = request.getParameter("page");
    if (pageParam != null) {
        try {
            page_1 = Integer.parseInt(pageParam);
            if (page_1 < 1) page_1 = 1;
        } catch (Exception e) {
            page_1 = 1;
        }
    }
    List<Course> courseList = (List<Course>)session.getAttribute("courseList");
    int total = (courseList == null) ? 0 : courseList.size();
    int totalPages = (int)Math.ceil((double)total / pageSize);
    if (page_1 > totalPages) page_1 = totalPages;
    int start = (page_1 - 1) * pageSize;
    int end = Math.min(start + pageSize, total);
%>
<html>
<head>
    <title>Find a job</title>
    <style>
        body {
            background: #f7f7f7;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .main-box {
            background: #fff;
            width: 500px;
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
        .course-list {
            margin-top: 10px;
        }
        .course-box {
            border: 1px solid #bbb;
            border-radius: 8px;
            margin: 18px 0;
            padding: 18px 16px;
            background: #f5f7fa;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }
        .course-link {
            display: block;
            color: inherit;
            text-decoration: none;
        }
        .course-link:hover .course-box {
            border-color: #2d3651;
            box-shadow: 0 4px 14px rgba(45,54,81,0.12);
        }
        .course-name {
            font-size: 1.2em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 8px;
        }
        .course-info {
            color: #444;
            font-size: 1em;
        }
        .pagination {
            text-align: center;
            margin-top: 20px;
        }
        .pagination a, .pagination span {
            display: inline-block;
            margin: 0 8px;
            padding: 6px 16px;
            border-radius: 6px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: bold;
            border: 1px solid #d1d5db;
            transition: background 0.2s;
        }
        .pagination a:hover {
            background: #d1d5db;
        }
        .pagination .current {
            background: #2d3651;
            color: #fff;
            border: 1px solid #2d3651;
        }
    </style>
</head>
<body>
    <div class="main-box">
        <div class="title">Find a job</div>
        <div class="course-list">
            <%
                if (courseList != null && total > 0) {
                    for (int i = start; i < end; i++) {
                        Course c = courseList.get(i);
            %>
                <a class="course-link" href="<%= response.encodeURL("TAclasscontroller?action=show_all_information&courseIndex=" + i) %>">
                    <div class="course-box">
                        <div class="course-name"><%= c.getCourseName() %></div>
                        <div class="course-info">
                            <%= c.getJobTitle() %> | <%= c.getWorkingHours() %>
                            <% if (c.getSalary() != null) { %> | <%= c.getSalary() %> <% } %>
                        </div>
                    </div>
                </a>
            <%
            
                    }
                } else {
            %>
                <div class="course-box">No courses available.</div>
            <%
                }
            %>
        </div>
        <div class="pagination">
            <% if (page_1 > 1) { %>
                <a href="<%= response.encodeURL("TAclasscontroller?action=view_information&page=" + (page_1 - 1)) %>">&laquo; Prev</a>
            <% } %>
            <% for (int p = 1; p <= totalPages; p++) { %>
                <% if (p == page_1) { %>
                    <span class="current"><%= p %></span>
                <% } else { %>
                    <a href="<%= response.encodeURL("TAclasscontroller?action=view_information&page=" + p) %>"><%= p %></a>
                <% } %>
            <% } %>
            <% if (page_1 < totalPages) { %>
                <a href="<%= response.encodeURL("TAclasscontroller?action=view_information&page=" + (page_1 + 1)) %>">Next &raquo;</a>
            <% } %>
        </div>
    </div>
</body>
</html>
