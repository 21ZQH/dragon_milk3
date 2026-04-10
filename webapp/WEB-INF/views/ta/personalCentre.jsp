<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page import="model.TA" %>
<%@ page import="model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User currentUser = (User) session.getAttribute("user");
    TA currentTA = null;
    if (currentUser instanceof TA) {
        currentTA = (TA) currentUser;
    }

    List<Course> appliedCourses = (List<Course>) request.getAttribute("appliedCourses");
    if (appliedCourses == null && currentTA != null) {
        appliedCourses = currentTA.getAppliedClasses();
    }

    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Personal Centre</title>
    <style>
        body {
            background: #f7f7f7;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .main-box {
            background: #fff;
            width: 760px;
            margin: 50px auto;
            border: 2px solid #222;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 34px 30px;
        }
        .title {
            text-align: center;
            font-size: 2em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 22px;
        }
        .msg-success {
            margin-bottom: 18px;
            padding: 12px 16px;
            border-radius: 8px;
            background: #edf7ed;
            color: #256029;
            border: 1px solid #b7dfb9;
            text-align: center;
            font-weight: bold;
        }
        .msg-error {
            margin-bottom: 18px;
            padding: 12px 16px;
            border-radius: 8px;
            background: #fdeeee;
            color: #a12626;
            border: 1px solid #efb7b7;
            text-align: center;
            font-weight: bold;
        }
        .section {
            border: 1px solid #bbb;
            border-radius: 8px;
            margin: 16px 0;
            padding: 18px;
            background: #f5f7fa;
        }
        .section-title {
            font-size: 1.15em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 10px;
        }
        .course-card {
            border: 1px solid #d2d7e2;
            border-radius: 8px;
            padding: 14px;
            margin: 10px 0;
            background: #fff;
        }
        .course-name {
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 6px;
        }
        .course-info {
            color: #444;
            font-size: 1em;
            margin-bottom: 8px;
        }
        .btn-row {
            margin-top: 10px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .btn {
            display: inline-block;
            padding: 9px 16px;
            border-radius: 7px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: bold;
            font-family: inherit;
            font-size: 0.95em;
            border: 1px solid #d1d5db;
            cursor: pointer;
        }
        .btn:hover {
            background: #d1d5db;
        }
        .btn-danger {
            background: #fdeeee;
            border-color: #efb7b7;
            color: #a12626;
        }
        .btn-danger:hover {
            background: #f8d7d7;
        }
        .top-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            justify-content: center;
            margin-bottom: 8px;
        }
        .empty-actions {
            margin-top: 12px;
            display: flex;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        .modal-overlay {
            position: fixed;
            inset: 0;
            background: rgba(0, 0, 0, 0.35);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 999;
        }
        .hidden {
            display: none;
        }
        .modal-box {
            width: 430px;
            max-width: calc(100vw - 40px);
            background: #fff;
            border: 2px solid #222;
            border-radius: 12px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.2);
            padding: 20px;
        }
        .modal-title {
            font-size: 1.25em;
            font-weight: bold;
            color: #2d3651;
            margin-bottom: 10px;
        }
        .modal-text {
            color: #444;
            margin-bottom: 16px;
            line-height: 1.6;
        }
        .modal-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
        form {
            display: inline;
            margin: 0;
        }
    </style>
</head>
<body>
    <div class="main-box">
        <div class="title">Personal Centre</div>

        <% if (success != null) { %>
            <div class="msg-success"><%= success %></div>
        <% } %>
        <% if (error != null) { %>
            <div class="msg-error"><%= error %></div>
        <% } %>

        <div class="top-actions">
            <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=profile_center") %>">Go to Profile Centre</a>
            <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=home") %>">Back Home</a>
        </div>

        <div class="section">
            <div class="section-title">My Applications</div>
            <% if (currentTA == null) { %>
                <div>Please log in as TA first.</div>
            <% } else if (appliedCourses == null || appliedCourses.isEmpty()) { %>
                <div>You have not applied to any course yet.</div>
                <div class="empty-actions">
                    <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Find New Jobs</a>
                </div>
            <% } else {
                for (Course course : appliedCourses) {
                    if (course == null) {
                        continue;
                    }
            %>
                <div class="course-card">
                    <div class="course-name"><%= course.getCourseName() %></div>
                    <div class="course-info">
                        <%= course.getJobTitle() %> | <%= course.getWorkingHours() %>
                    </div>
                    <div class="btn-row">
                        <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=go_apply_by_id&courseId=" + course.getId()) %>">Modify (Re-upload Resume)</a>
                        <button class="btn btn-danger" type="button" onclick="openWithdrawModal('<%= course.getId() %>')">Withdraw</button>
                    </div>
                </div>
            <%  }
               } %>
        </div>
    </div>

    <div id="withdrawModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="withdrawTitle">
        <div class="modal-box">
            <div class="modal-title" id="withdrawTitle">Confirm Withdraw</div>
            <div class="modal-text">Are you sure you want to withdraw this application and delete the uploaded resume file?</div>
            <div class="modal-actions">
                <button type="button" class="btn" onclick="closeWithdrawModal()">Cancel</button>
                <button type="button" class="btn btn-danger" onclick="confirmWithdraw()">Yes, Withdraw</button>
            </div>
        </div>
    </div>

    <form id="withdrawForm" action="<%= response.encodeURL("TAclasscontroller") %>" method="post" class="hidden">
        <input type="hidden" name="action" value="withdraw_application" />
        <input type="hidden" id="withdrawCourseId" name="courseId" value="" />
    </form>

    <script>
        let pendingWithdrawCourseId = "";

        function openWithdrawModal(courseId) {
            pendingWithdrawCourseId = courseId || "";
            document.getElementById("withdrawModal").classList.remove("hidden");
        }

        function closeWithdrawModal() {
            pendingWithdrawCourseId = "";
            document.getElementById("withdrawModal").classList.add("hidden");
        }

        function confirmWithdraw() {
            if (!pendingWithdrawCourseId) {
                closeWithdrawModal();
                return;
            }
            document.getElementById("withdrawCourseId").value = pendingWithdrawCourseId;
            document.getElementById("withdrawForm").submit();
        }
    </script>
</body>
</html>
