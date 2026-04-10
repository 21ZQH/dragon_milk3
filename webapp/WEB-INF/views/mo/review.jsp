<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Course" %>
<%@ page import="model.ResumeSubmission" %>
<%@ page import="model.TA" %>
<%@ page import="java.net.URLEncoder" %>
<%
    Course course = (Course) request.getAttribute("selectedCourse");
    String courseIndex = (String) request.getAttribute("courseIndex");
    boolean reviewPublished = course != null && course.isReviewPublished();
    String saved = request.getParameter("saved");
    String published = request.getParameter("published");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Review Applications</title>
    <style>
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 30px 20px;
        }

        .container {
            max-width: 1180px;
            margin: 0 auto;
            background: #fff;
            border: 2px solid #22223b;
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            padding: 30px;
            box-sizing: border-box;
        }

        .top-bar {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 18px;
        }

        .title {
            margin: 0;
            font-size: 2em;
            color: #22223b;
        }

        .subtitle {
            margin: 8px 0 0 0;
            color: #3b5998;
            font-size: 1em;
        }

        .top-links {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .back-link {
            text-decoration: none;
            color: #22223b;
            font-weight: 600;
            border: 2px solid #22223b;
            padding: 10px 16px;
            border-radius: 10px;
            background: #fff;
            transition: all 0.2s ease;
        }

        .back-link:hover {
            background: #22223b;
            color: #fff;
        }

        .summary-bar {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            margin-bottom: 20px;
        }

        .summary-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 14px;
            border-radius: 999px;
            border: 1px solid #d5d7ea;
            background: #f4f5fb;
            color: #22223b;
            font-weight: 600;
        }

        .summary-pill.published {
            background: #e8f7ec;
            border-color: #95d3a8;
            color: #1d6b2a;
        }

        .summary-pill.pending {
            background: #fff4df;
            border-color: #efcd80;
            color: #8a5a00;
        }

        .message {
            margin-bottom: 18px;
            padding: 12px 16px;
            border-radius: 10px;
            font-weight: 600;
        }

        .message.saved {
            background: #eef4ff;
            color: #1c4f9b;
            border: 1px solid #b8cff5;
        }

        .message.published {
            background: #eaf8ec;
            color: #1d6b2a;
            border: 1px solid #9dd3a5;
        }

        .table-wrap {
            overflow-x: auto;
            border: 1px solid #d9d9e3;
            border-radius: 10px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background: #fff;
        }

        th, td {
            border: 1px solid #e5e5ee;
            padding: 12px;
            text-align: left;
            vertical-align: top;
        }

        th {
            background: #f2f3fb;
            color: #1f2140;
            font-size: 0.95em;
        }

        tbody tr:hover {
            background: #fafafe;
        }

        .pick-cell {
            width: 70px;
            text-align: center;
        }

        .resume-link {
            color: #2f5aa8;
            font-weight: 600;
            text-decoration: none;
        }

        .resume-link:hover {
            text-decoration: underline;
        }

        .status-badge {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 999px;
            font-size: 0.9em;
            font-weight: 700;
        }

        .status-pending {
            background: #fff4df;
            color: #8a5a00;
        }

        .status-approved {
            background: #e8f7ec;
            color: #1d6b2a;
        }

        .status-rejected {
            background: #fdecec;
            color: #9c2020;
        }

        .empty {
            color: #666;
            text-align: center;
            padding: 18px;
        }

        .actions {
            margin-top: 18px;
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            flex-wrap: wrap;
        }

        .primary-btn,
        .secondary-btn {
            border-radius: 10px;
            padding: 10px 18px;
            font-size: 0.95em;
            font-weight: 700;
            cursor: pointer;
        }

        .secondary-btn {
            border: 2px solid #22223b;
            background: #fff;
            color: #22223b;
        }

        .primary-btn {
            border: 2px solid #22223b;
            background: #22223b;
            color: #fff;
        }

        .secondary-btn:hover {
            background: #f1f2f9;
        }

        .primary-btn:hover {
            background: #30325f;
            border-color: #30325f;
        }

        .readonly-note {
            margin-top: 18px;
            padding: 14px 16px;
            border-radius: 10px;
            background: #f6f7fb;
            border: 1px solid #d7daec;
            color: #333;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="top-bar">
            <div>
                <h1 class="title">Review Applications</h1>
                <p class="subtitle">
                    <%= course == null ? "Course not found." : ("Course: " + course.getCourseName()) %>
                </p>
            </div>
            <div class="top-links">
                <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=project_detail&courseIndex=" + (courseIndex == null ? "" : courseIndex)) %>">Back to Project</a>
                <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>">My Project</a>
            </div>
        </div>

        <% if (course != null) { %>
            <div class="summary-bar">
                <div class="summary-pill">
                    Applicants:
                    <span><%= course.getTaApplicants().size() %></span>
                </div>
                <div class="summary-pill">
                    Picked:
                    <span id="pickedCount"><%= course.getPickedApplicantEmails().size() %></span>
                </div>
                <div class="summary-pill <%= reviewPublished ? "published" : "pending" %>">
                    <%= reviewPublished ? "Published" : "Pending Review" %>
                </div>
            </div>

            <% if ("1".equals(saved)) { %>
                <div class="message saved">Pick selections saved successfully.</div>
            <% } %>
            <% if ("1".equals(published)) { %>
                <div class="message published">Review has been published. This page is now read-only.</div>
            <% } %>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th class="pick-cell">Pick</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>College</th>
                            <th>Skill</th>
                            <th>Resume</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            boolean hasVisibleApplicant = false;
                            for (int i = 0; i < course.getTaApplicants().size(); i++) {
                                TA applicant = course.getTaApplicants().get(i);
                                if (applicant == null) {
                                    continue;
                                }
                                boolean picked = course.isApplicantPicked(applicant.getEmail());
                                if (reviewPublished && !picked) {
                                    continue;
                                }
                                hasVisibleApplicant = true;
                                Integer status = applicant.getResumeStatusForCourse(course.getId());
                                int statusValue = status == null ? ResumeSubmission.STATUS_PENDING : status.intValue();
                                String statusLabel = statusValue == ResumeSubmission.STATUS_APPROVED
                                        ? "Approved"
                                        : statusValue == ResumeSubmission.STATUS_REJECTED ? "Rejected" : "Pending";
                                String statusClass = statusValue == ResumeSubmission.STATUS_APPROVED
                                        ? "status-approved"
                                        : statusValue == ResumeSubmission.STATUS_REJECTED ? "status-rejected" : "status-pending";
                        %>
                        <tr>
                            <td class="pick-cell">
                                <% if (reviewPublished) { %>
                                    <input type="checkbox" checked disabled />
                                <% } else { %>
                                    <input
                                        type="checkbox"
                                        class="pick-checkbox"
                                        name="pickedEmail"
                                        value="<%= applicant.getEmail() == null ? "" : applicant.getEmail() %>"
                                        form="saveReviewForm"
                                        <%= picked ? "checked" : "" %> />
                                <% } %>
                            </td>
                            <td><%= applicant.getName() == null || applicant.getName().isBlank() ? "Unnamed TA" : applicant.getName() %></td>
                            <td><%= applicant.getEmail() == null ? "" : applicant.getEmail() %></td>
                            <td><%= applicant.getCollege() == null || applicant.getCollege().isBlank() ? "-" : applicant.getCollege() %></td>
                            <td><%= applicant.getSkill() == null || applicant.getSkill().isBlank() ? "-" : applicant.getSkill() %></td>
                            <td>
                                <% if (applicant.getResumeDirectoryForCourse(course.getId()) != null
                                        && !applicant.getResumeDirectoryForCourse(course.getId()).isBlank()
                                        && applicant.getEmail() != null
                                        && !applicant.getEmail().isBlank()) { %>
                                    <a
                                        class="resume-link"
                                        href="<%= response.encodeURL("MOclasscontroller?action=view_resume&courseIndex="
                                                + courseIndex + "&applicantEmail="
                                                + URLEncoder.encode(applicant.getEmail(), "UTF-8")) %>"
                                        target="_blank">
                                        Open Resume
                                    </a>
                                <% } else { %>
                                    -
                                <% } %>
                            </td>
                            <td><span class="status-badge <%= statusClass %>"><%= statusLabel %></span></td>
                        </tr>
                        <% } %>
                        <% if (!hasVisibleApplicant) { %>
                        <tr>
                            <td colspan="7" class="empty">
                                <%= reviewPublished ? "No picked applicant has been published for this course." : "No applicant data found for this course." %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <% if (!reviewPublished) { %>
                <div class="actions">
                    <form id="saveReviewForm" method="post" action="<%= response.encodeURL("MOclasscontroller") %>">
                        <input type="hidden" name="action" value="save_review_picks" />
                        <input type="hidden" name="courseIndex" value="<%= courseIndex == null ? "" : courseIndex %>" />
                        <button type="submit" class="secondary-btn">Save Changes</button>
                    </form>

                    <form id="publishReviewForm" method="post" action="<%= response.encodeURL("MOclasscontroller") %>">
                        <input type="hidden" name="action" value="publish_review" />
                        <input type="hidden" name="courseIndex" value="<%= courseIndex == null ? "" : courseIndex %>" />
                        <button type="submit" class="primary-btn">Publish</button>
                    </form>
                </div>
            <% } else { %>
                <div class="readonly-note">
                    This review has already been published. Only the accepted applicants remain visible, and the review result can no longer be edited.
                </div>
            <% } %>
        <% } else { %>
            <div class="readonly-note">Course information is unavailable.</div>
        <% } %>
    </div>

    <script>
        (function () {
            var saveForm = document.getElementById('saveReviewForm');
            var publishForm = document.getElementById('publishReviewForm');
            var pickCount = document.getElementById('pickedCount');
            var checkboxes = document.querySelectorAll('.pick-checkbox');

            function syncSelections() {
                if (!saveForm || !publishForm) {
                    return;
                }

                publishForm.querySelectorAll('input[name="pickedEmail"]').forEach(function (node) {
                    node.parentNode.removeChild(node);
                });

                var selectedCount = 0;
                checkboxes.forEach(function (checkbox) {
                    if (!checkbox.checked) {
                        return;
                    }
                    selectedCount++;
                    var hiddenField = document.createElement('input');
                    hiddenField.type = 'hidden';
                    hiddenField.name = 'pickedEmail';
                    hiddenField.value = checkbox.value;
                    publishForm.appendChild(hiddenField);
                });

                if (pickCount) {
                    pickCount.textContent = selectedCount;
                }
            }

            checkboxes.forEach(function (checkbox) {
                checkbox.addEventListener('change', syncSelections);
            });

            syncSelections();
        })();
    </script>
</body>
</html>
