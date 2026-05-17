<%@ page import="model.ApplicationForm" %>
<%@ page import="model.Course" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Course course = (Course) request.getAttribute("selectedCourse");
    String courseIndex = (String) request.getAttribute("courseIndex");
    ApplicationForm form = (ApplicationForm) request.getAttribute("applicationForm");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
    Boolean applicationOpenAttr = (Boolean) request.getAttribute("applicationOpen");
    boolean applicationOpen = applicationOpenAttr == null || applicationOpenAttr.booleanValue();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Application Form</title>
    <style>
        body {
            margin: 0;
            background: #fff;
            color: #222;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .page {
            width: 980px;
            max-width: calc(100vw - 48px);
            margin: 34px auto;
        }
        .title {
            font-size: 2.1em;
            font-weight: 700;
            color: #253b6e;
            margin-bottom: 8px;
        }
        .subtitle {
            color: #4b5565;
            margin-bottom: 22px;
            line-height: 1.6;
        }
        .message {
            margin-bottom: 18px;
            padding: 12px 16px;
            border-radius: 8px;
            font-weight: 700;
        }
        .success {
            background: #edf7ed;
            color: #256029;
            border: 1px solid #b7dfb9;
        }
        .error {
            background: #fdeeee;
            color: #a12626;
            border: 1px solid #efb7b7;
        }
        .course-box, .form-box, .feedback-box {
            border: 1px solid #cfd6e4;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 18px;
            background: #fff;
        }
        .course-row {
            line-height: 1.8;
            color: #334155;
        }
        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }
        .field {
            margin-bottom: 16px;
        }
        .field.full {
            grid-column: 1 / -1;
        }
        label {
            display: block;
            font-weight: 700;
            color: #26364f;
            margin-bottom: 7px;
        }
        input, textarea {
            width: 100%;
            box-sizing: border-box;
            border: 1px solid #c7ccd8;
            border-radius: 8px;
            padding: 11px 12px;
            font-size: 1em;
            font-family: inherit;
            color: #222;
            background: #fff;
        }
        textarea {
            min-height: 118px;
            resize: vertical;
            line-height: 1.55;
        }
        .feedback-box textarea {
            background: #fffaf0;
            border-color: #e8ca82;
        }
        .button-row {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            justify-content: flex-end;
            margin-top: 20px;
        }
        .btn {
            display: inline-block;
            padding: 12px 22px;
            border-radius: 8px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: 700;
            border: 1px solid #d1d5db;
            cursor: pointer;
            font-size: 1em;
            font-family: inherit;
        }
        .btn-primary {
            background: #253b6e;
            color: #fff;
            border-color: #253b6e;
        }
        .btn-disabled {
            background: #f3f4f6;
            color: #9ea3b0;
            cursor: not-allowed;
        }
        @media (max-width: 760px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="page">
        <div class="title">Standard Application Form</div>
        <div class="subtitle">
            Review and edit the generated form before submitting. This form is specific to your account and this job.
        </div>

        <% if (success != null) { %>
            <div class="message success"><%= success %></div>
        <% } %>
        <% if (error != null) { %>
            <div class="message error"><%= error %></div>
        <% } %>

        <% if (course != null && form != null) { %>
            <div class="course-box">
                <div class="course-row"><strong>Course:</strong> <%= course.getCourseName() %></div>
                <div class="course-row"><strong>Job Title:</strong> <%= course.getJobTitle() %></div>
                <div class="course-row"><strong>Requirement:</strong> <%= course.getJobRequirement() %></div>
            </div>

            <form action="<%= response.encodeURL("TAclasscontroller") %>" method="post">
                <input type="hidden" name="courseId" value="<%= course.getId() %>">
                <div class="form-box">
                    <div class="grid">
                        <div class="field">
                            <label for="applicantName">Name</label>
                            <input id="applicantName" name="applicantName" value="<%= form.getApplicantName() == null ? "" : form.getApplicantName() %>">
                        </div>
                        <div class="field">
                            <label for="email">Email</label>
                            <input id="email" name="email" value="<%= form.getEmail() == null ? "" : form.getEmail() %>">
                        </div>
                        <div class="field full">
                            <label for="education">Education</label>
                            <textarea id="education" name="education"><%= form.getEducation() == null ? "" : form.getEducation() %></textarea>
                        </div>
                        <div class="field full">
                            <label for="skills">Skills</label>
                            <textarea id="skills" name="skills"><%= form.getSkills() == null ? "" : form.getSkills() %></textarea>
                        </div>
                        <div class="field full">
                            <label for="relevantExperience">Relevant Experience</label>
                            <textarea id="relevantExperience" name="relevantExperience"><%= form.getRelevantExperience() == null ? "" : form.getRelevantExperience() %></textarea>
                        </div>
                        <div class="field full">
                            <label for="projectExperience">Project Experience</label>
                            <textarea id="projectExperience" name="projectExperience"><%= form.getProjectExperience() == null ? "" : form.getProjectExperience() %></textarea>
                        </div>
                        <div class="field full">
                            <label for="courseFit">Course Fit</label>
                            <textarea id="courseFit" name="courseFit"><%= form.getCourseFit() == null ? "" : form.getCourseFit() %></textarea>
                        </div>
                    </div>
                </div>

                <div class="feedback-box">
                    <label for="feedback">AI Feedback</label>
                    <textarea id="feedback" name="feedback"><%= form.getFeedback() == null ? "" : form.getFeedback() %></textarea>
                </div>

                <div class="button-row">
                    <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=show_all_information&courseIndex=" + courseIndex) %>">Back to Details</a>
                    <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=personal_centre") %>">Personal Centre</a>
                    <% if (applicationOpen) { %>
                        <button class="btn" type="submit" name="action" value="save_application_form">Save Draft</button>
                        <button class="btn btn-primary" type="submit" name="action" value="submit_application_form">Submit Application</button>
                    <% } else { %>
                        <button class="btn btn-disabled" type="button">Application Closed</button>
                    <% } %>
                </div>
            </form>
        <% } else { %>
            <div class="course-box">Application form is unavailable.</div>
            <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Back to List</a>
        <% } %>
    </div>
</body>
</html>
