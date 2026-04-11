<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%@ page import="model.ResumeSubmission" %>
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

    Course selectedCourse = (Course) request.getAttribute("selectedCourse");
    String selectedCourseId = (String) request.getAttribute("selectedCourseId");
    if (selectedCourse == null && appliedCourses != null && !appliedCourses.isEmpty()) {
        selectedCourse = appliedCourses.get(0);
    }
    if (selectedCourseId == null && selectedCourse != null) {
        selectedCourseId = selectedCourse.getId();
    }

    Boolean applicationOpenAttr = (Boolean) request.getAttribute("applicationOpen");
    boolean applicationOpen = applicationOpenAttr == null ? true : applicationOpenAttr.booleanValue();

    boolean profileComplete = false;
    if (currentTA != null) {
        String taName = currentTA.getName();
        String taCollege = currentTA.getCollege();
        String taSkill = currentTA.getSkill();
        profileComplete = taName != null && !taName.trim().isEmpty()
                && taCollege != null && !taCollege.trim().isEmpty()
                && taSkill != null && !taSkill.trim().isEmpty();
    }

    Integer selectedStatus = (Integer) request.getAttribute("selectedStatus");
    if (selectedStatus == null && currentTA != null && selectedCourse != null) {
        selectedStatus = currentTA.getResumeStatusForCourse(selectedCourse.getId());
    }
    if (selectedStatus == null) {
        selectedStatus = ResumeSubmission.STATUS_PENDING;
    }

    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");

    boolean isAccepted = selectedStatus == ResumeSubmission.STATUS_APPROVED;
    boolean isRejected = selectedStatus == ResumeSubmission.STATUS_REJECTED;
    boolean isEvaluating = !isAccepted && !isRejected;

    String statusLabel = isEvaluating ? "Evaluating" : (isAccepted ? "Accepted" : "Rejected");
    String terminatedLabel = isEvaluating ? "Awaiting result" : (isAccepted ? "Accepted" : "Rejected");
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
            width: 980px;
            max-width: calc(100vw - 56px);
            margin: 36px auto;
            border: 2px solid #222;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 34px 30px;
        }
        .title {
            text-align: center;
            font-size: 2.9em;
            font-weight: bold;
            color: #253b6e;
            margin-bottom: 24px;
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
        .top-actions {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;
            justify-content: center;
            margin-bottom: 26px;
        }
        .btn {
            display: inline-block;
            padding: 12px 24px;
            border-radius: 8px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: bold;
            font-family: inherit;
            font-size: 1em;
            border: 1px solid #d1d5db;
            cursor: pointer;
        }
        .btn:hover {
            background: #d1d5db;
        }
        .btn-text-disabled {
            color: #9ea3b0;
        }
        .btn-disabled {
            color: #9ea3b0;
            background: #f3f4f6;
            border-color: #d1d5db;
            cursor: not-allowed;
        }
        .btn-danger {
            background: #fff4f4;
            border-color: #f1b4b4;
            color: #a12626;
        }
        .btn-danger:hover {
            background: #fde1e1;
        }
        .btn-danger.btn-disabled,
        .btn-danger.btn-disabled:hover {
            background: #f3f4f6;
            border-color: #d1d5db;
            color: #9ea3b0;
        }
        .section {
            border: 1px solid #c6cedc;
            border-radius: 16px;
            padding: 28px;
            background: #f5f7fc;
        }
        .section-title {
            font-size: 1.95em;
            font-weight: bold;
            color: #1f315d;
            margin-bottom: 22px;
        }
        .chooser-box {
            border: 1px solid #cfd6e4;
            border-radius: 14px;
            padding: 22px;
            background: #eef2fb;
            margin-bottom: 26px;
        }
        .chooser-title {
            font-size: 1.15em;
            font-weight: bold;
            color: #1f315d;
            margin-bottom: 14px;
        }
        .course-pills {
            display: flex;
            gap: 14px;
            flex-wrap: wrap;
        }
        .course-pill {
            display: inline-block;
            padding: 14px 24px;
            border-radius: 999px;
            border: 3px solid #2b2b4f;
            background: #fff;
            color: #1f315d;
            text-decoration: none;
            font-size: 1.05em;
            font-weight: bold;
        }
        .course-pill.active {
            background: #2b2b4f;
            color: #fff;
        }
        .detail-card {
            border: 1px solid #d2d7e2;
            border-radius: 14px;
            padding: 24px 22px;
            background: #fff;
        }
        .course-name {
            font-size: 2em;
            font-weight: bold;
            color: #1f315d;
            margin-bottom: 10px;
        }
        .course-info {
            font-size: 1.15em;
            color: #3d4a63;
            margin-bottom: 22px;
        }
        .action-row {
            display: flex;
            gap: 14px;
            flex-wrap: wrap;
        }
        .empty-state {
            border: 1px dashed #bcc6d8;
            border-radius: 14px;
            padding: 28px 24px;
            background: #fff;
            color: #43506a;
            font-size: 1.05em;
            line-height: 1.7;
        }
        .empty-actions {
            margin-top: 16px;
        }
        .status-pill {
            display: inline-block;
            padding: 12px 18px;
            border-radius: 999px;
            font-size: 1.02em;
            font-weight: bold;
            margin-bottom: 22px;
        }
        .status-pending {
            background: #fff3da;
            color: #9a6700;
            border: 1px solid #f2cc60;
        }
        .status-accepted {
            background: #edf9f0;
            color: #256029;
            border: 1px solid #93d5a0;
        }
        .status-rejected {
            background: #fdeeee;
            color: #a12626;
            border: 1px solid #efb7b7;
        }
        .progress-panel {
            border: 1px solid #dde3f0;
            border-radius: 14px;
            padding: 26px 20px 22px;
            background: #f9fbff;
        }
        .progress-track {
            position: relative;
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 24px;
        }
        .progress-track::before {
            content: "";
            position: absolute;
            left: 72px;
            right: 72px;
            top: 28px;
            height: 3px;
            background: #d3daea;
            z-index: 0;
        }
        .progress-track.complete::before {
            background: linear-gradient(to right, #3b5998 0%, #3b5998 100%);
        }
        .progress-step {
            position: relative;
            z-index: 1;
            width: 50%;
            text-align: center;
        }
        .step-circle {
            width: 56px;
            height: 56px;
            line-height: 56px;
            margin: 0 auto 14px;
            border-radius: 50%;
            border: 3px solid #d3daea;
            background: #fff;
            color: #69748d;
            font-size: 1.1em;
            font-weight: bold;
        }
        .progress-step.active .step-circle {
            border-color: #3b5998;
            color: #3b5998;
        }
        .progress-step.done .step-circle {
            border-color: #3b5998;
            background: #3b5998;
            color: #fff;
        }
        .step-title {
            font-size: 1.28em;
            font-weight: bold;
            color: #1f315d;
            margin-bottom: 6px;
        }
        .step-subtitle {
            color: #556179;
            font-size: 1em;
        }
        .status-summary {
            margin-top: 18px;
            color: #45526c;
            font-size: 1em;
            line-height: 1.7;
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
            justify-content: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        .modal-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 150px;
            height: 44px;
            padding: 0 16px;
            border-radius: 10px;
            background: #e9ecf5;
            color: #2d3651;
            text-decoration: none;
            font-weight: bold;
            font-family: inherit;
            font-size: 0.95em;
            border: 1px solid #d1d5db;
            cursor: pointer;
        }
        .modal-btn:hover {
            background: #d1d5db;
        }
    </style>
</head>
<body data-application-open="<%= applicationOpen %>">
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
                <div class="empty-state">Please log in as TA first.</div>
            <% } else if (appliedCourses == null || appliedCourses.isEmpty()) { %>
                <div class="empty-state">
                    <% if (applicationOpen && profileComplete) { %>
                        You have not applied to any course yet.
                        <div class="empty-actions">
                            <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Find New Jobs</a>
                        </div>
                    <% } else { %>
                        You have not applied to any course yet.
                        <div class="empty-actions">
                            <button class="btn btn-text-disabled" type="button" onclick="openFindNewJobsUnavailableModal()">Find New Jobs</button>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="chooser-box">
                    <div class="chooser-title">Choose an applied course</div>
                    <div class="course-pills">
                        <% for (Course course : appliedCourses) {
                               if (course == null) {
                                   continue;
                               }
                               boolean active = course.getId() != null && course.getId().equals(selectedCourseId);
                        %>
                            <a class="course-pill <%= active ? "active" : "" %>"
                               href="<%= response.encodeURL("TAclasscontroller?action=personal_centre&courseId=" + course.getId()) %>">
                                <%= course.getCourseName() %>
                            </a>
                        <% } %>
                    </div>
                </div>

                <% if (selectedCourse != null) { %>
                    <div class="detail-card">
                        <div class="course-name"><%= selectedCourse.getCourseName() %></div>
                        <div class="course-info"><%= selectedCourse.getJobTitle() %> | <%= selectedCourse.getWorkingHours() %></div>

                        <div class="action-row">
                            <% if (applicationOpen) { %>
                                <a class="btn" href="<%= response.encodeURL("TAclasscontroller?action=go_apply_by_id&courseId=" + selectedCourse.getId()) %>">Modify (Re-upload Resume)</a>
                                <button class="btn btn-danger" type="button" onclick="openWithdrawModal('<%= selectedCourse.getId() %>')">Withdraw</button>
                            <% } else { %>
                                <button class="btn btn-disabled" type="button" onclick="openDeadlinePassedModal()">Modify (Re-upload Resume)</button>
                                <button class="btn btn-danger btn-disabled" type="button" onclick="openDeadlinePassedModal()">Withdraw</button>
                            <% } %>
                        </div>

                        <% if (!applicationOpen) { %>
                            <span class="status-pill <%= isEvaluating ? "status-pending" : (isAccepted ? "status-accepted" : "status-rejected") %>"><%= statusLabel %></span>

                            <div class="progress-panel">
                                <div class="progress-track <%= isEvaluating ? "" : "complete" %>">
                                    <div class="progress-step active">
                                        <div class="step-circle"><%= isEvaluating ? "1" : "✓" %></div>
                                        <div class="step-title">Evaluating</div>
                                        <div class="step-subtitle"><%= isEvaluating ? "Under review" : "Review completed" %></div>
                                    </div>
                                    <div class="progress-step <%= isEvaluating ? "" : "done" %>">
                                        <div class="step-circle"><%= isEvaluating ? "2" : "✓" %></div>
                                        <div class="step-title">Terminated</div>
                                        <div class="step-subtitle"><%= terminatedLabel %></div>
                                    </div>
                                </div>
                                <div class="status-summary">
                                    <% if (isEvaluating) { %>
                                        Your application is currently being evaluated by the MO. Please wait for the final review result.
                                    <% } else if (isAccepted) { %>
                                        Your application has been evaluated and the final result for this course is Accepted.
                                    <% } else { %>
                                        Your application has been evaluated and the final result for this course is Rejected.
                                    <% } %>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            <% } %>
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

    <div id="profileIncompleteModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="profileIncompleteTitle">
        <div class="modal-box">
            <div class="modal-title" id="profileIncompleteTitle">Profile Incomplete</div>
            <div class="modal-text">Your profile information is incomplete. Please complete it first.</div>
            <div class="modal-actions">
                <button type="button" class="modal-btn" onclick="closeProfileIncompleteModal()">OK</button>
                <button type="button" class="modal-btn" onclick="goToProfileCenter()">Go to Profile Centre</button>
            </div>
        </div>
    </div>

    <div id="deadlinePassedModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="deadlinePassedTitle">
        <div class="modal-box">
            <div class="modal-title" id="deadlinePassedTitle">Application Closed</div>
            <div class="modal-text">The application deadline has passed.</div>
            <div class="modal-actions">
                <button type="button" class="modal-btn" onclick="closeDeadlinePassedModal()">OK</button>
            </div>
        </div>
    </div>

    <form id="withdrawForm" action="<%= response.encodeURL("TAclasscontroller") %>" method="post" class="hidden">
        <input type="hidden" name="action" value="withdraw_application" />
        <input type="hidden" id="withdrawCourseId" name="courseId" value="" />
    </form>

    <script>
        const applicationOpenFlag = document.body.dataset.applicationOpen === "true";
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

        function openProfileIncompleteModal() {
            document.getElementById("profileIncompleteModal").classList.remove("hidden");
        }

        function closeProfileIncompleteModal() {
            document.getElementById("profileIncompleteModal").classList.add("hidden");
        }

        function goToProfileCenter() {
            window.location.href = '<%= response.encodeURL("TAclasscontroller?action=profile_center") %>';
        }

        function openFindNewJobsUnavailableModal() {
            if (!applicationOpenFlag) {
                openDeadlinePassedModal();
                return;
            }
            openProfileIncompleteModal();
        }

        function openDeadlinePassedModal() {
            document.getElementById("deadlinePassedModal").classList.remove("hidden");
        }

        function closeDeadlinePassedModal() {
            document.getElementById("deadlinePassedModal").classList.add("hidden");
        }
    </script>
</body>
</html>
