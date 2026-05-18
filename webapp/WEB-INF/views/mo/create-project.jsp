<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Course> courseList = (List<Course>) request.getAttribute("courseList");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Publish Recruitment</title>
    <style>
        body { background: #f7f7f9; font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 40px 0; }
        .container { background: #fff; width: 100%; max-width: 820px; margin: 0 auto; border-radius: 20px; border: 2px solid #22223b; box-shadow: 0 4px 16px rgba(0,0,0,0.12); padding: 40px 50px; box-sizing: border-box; }
        .top-line { display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap; margin-bottom: 28px; }
        .page-title { font-size: 2.1em; font-weight: 700; color: #22223b; margin: 0; }
        .back-link { text-decoration: none; color: #22223b; font-weight: 700; border: 2px solid #22223b; padding: 10px 16px; border-radius: 12px; }
        .form-group { margin-bottom: 24px; }
        .form-label { display: block; font-size: 1.15em; font-weight: 700; color: #22223b; margin-bottom: 9px; }
        .form-control { width: 100%; padding: 14px; font-size: 1.05em; font-family: inherit; color: #22223b; background: #fff; border: 2px solid #22223b; border-radius: 8px; box-sizing: border-box; }
        textarea.form-control { resize: vertical; min-height: 130px; line-height: 1.6; }
        .submit-section { text-align: center; margin-top: 34px; }
        .publish-btn { background: #22223b; color: #fff; font-size: 1.15em; font-weight: 700; border: none; border-radius: 12px; padding: 14px 44px; cursor: pointer; }
        .empty-box { border: 2px dashed #c7ccd8; border-radius: 12px; padding: 28px; color: #4b5565; background: #fafafa; line-height: 1.6; }
        .hint { color: #5b667a; line-height: 1.6; margin: -10px 0 24px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="top-line">
            <div class="page-title">Publish recruitment</div>
            <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">Back</a>
        </div>

        <div class="hint">Choose one of the courses assigned by Admin, then publish the TA recruitment information for that course.</div>

        <% if (courseList == null || courseList.isEmpty()) { %>
            <div class="empty-box">No assigned courses are available. Please contact Admin to assign courses to your account.</div>
        <% } else { %>
            <form action="<%= response.encodeURL("MOclasscontroller") %>" method="post">
                <input type="hidden" name="action" value="publish_course">

                <div class="form-group">
                    <label class="form-label" for="courseIndex">Assigned Course</label>
                    <select id="courseIndex" name="courseIndex" class="form-control" required>
                        <% for (int i = 0; i < courseList.size(); i++) {
                            Course course = courseList.get(i);
                        %>
                            <option value="<%= i %>">
                                <%= course.getCourseName() == null || course.getCourseName().isBlank() ? "Untitled Course" : course.getCourseName() %>
                                <%= course.isRecruitmentPublished() ? " (published)" : " (not published)" %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="jobTitle">Job Title</label>
                    <input type="text" id="jobTitle" name="jobTitle" class="form-control" placeholder="e.g. Teaching Assistant" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="workingHours">Working Hours</label>
                    <input type="text" id="workingHours" name="workingHours" class="form-control" placeholder="e.g. 10 hours/week" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="jobDescription">Job Description</label>
                    <textarea id="jobDescription" name="jobDescription" class="form-control" placeholder="Details..."></textarea>
                </div>

                <div class="form-group">
                    <label class="form-label" for="jobRequirement">Job Requirement</label>
                    <textarea id="jobRequirement" name="jobRequirement" class="form-control" placeholder="Details..."></textarea>
                </div>

                <div class="submit-section">
                    <button type="submit" class="publish-btn">Publish recruitment</button>
                </div>
            </form>
        <% } %>
    </div>
</body>
</html>
