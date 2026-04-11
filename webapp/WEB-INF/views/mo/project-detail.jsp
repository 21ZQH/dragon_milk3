<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Course" %>
<%
    Course course = (Course) request.getAttribute("selectedCourse");
    String courseIndex = (String) request.getAttribute("courseIndex");
    String success = request.getParameter("success");
    String error = (String) request.getAttribute("error");
    Object moModifyDeadline = request.getAttribute("moModifyDeadline");
    Boolean moModifyOpenAttr = (Boolean) request.getAttribute("moModifyOpen");
    boolean moModifyOpen = moModifyOpenAttr == null ? true : moModifyOpenAttr.booleanValue();
    Boolean moProfileCompleteAttr = (Boolean) request.getAttribute("moProfileComplete");
    boolean moProfileComplete = moProfileCompleteAttr == null ? true : moProfileCompleteAttr.booleanValue();
    Boolean showProfileIncompleteModalAttr = (Boolean) request.getAttribute("showProfileIncompleteModal");
    boolean showProfileIncompleteModal = showProfileIncompleteModalAttr != null && showProfileIncompleteModalAttr.booleanValue();
    boolean canModifyProject = moModifyOpen && moProfileComplete;
%>
<!DOCTYPE html>
<html>
<head>
    <title>Project Detail</title>
    <style>
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 40px 0;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
        }

        .container {
            background: #fff;
            width: 100%;
            max-width: 900px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 36px 40px 40px 40px;
            box-sizing: border-box;
            position: relative;
        }

        .top-line {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 12px;
            border-bottom: 2px solid #22223b;
            padding-bottom: 12px;
        }

        .page-title {
            font-size: 2em;
            font-weight: 700;
            color: #22223b;
            margin: 0;
            word-break: break-word;
        }

        .back-link {
            display: inline-block;
            text-decoration: none;
            color: #22223b;
            font-weight: 600;
            border: 2px solid #22223b;
            padding: 10px 16px;
            border-radius: 12px;
            background: #fff;
            transition: all 0.2s ease;
        }

        .back-link:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-1px);
            box-shadow: 0 4px 10px rgba(34,34,59,0.12);
        }

        .notice {
            color: #3b5998;
            font-size: 1em;
            font-style: italic;
            margin-top: 8px;
            margin-bottom: 24px;
        }

        .success-toast {
            position: sticky;
            top: 0;
            z-index: 100;
            margin-bottom: 20px;
            padding: 14px 18px;
            border-radius: 12px;
            background: #eaf8ec;
            color: #1d6b2a;
            border: 1.5px solid #9dd3a5;
            font-weight: 700;
            box-shadow: 0 4px 12px rgba(29,107,42,0.10);
            animation: fadeIn 0.25s ease;
        }

        .error-box {
            margin-bottom: 20px;
            padding: 14px 18px;
            border-radius: 12px;
            background: #fdeaea;
            color: #9f2d2d;
            border: 1.5px solid #e6b8b8;
            font-weight: 700;
        }

        .deadline-box {
            margin-bottom: 20px;
            padding: 12px 16px;
            border-radius: 12px;
            background: #eef2ff;
            color: #22223b;
            border: 1.5px solid #cfd7ff;
            font-weight: 600;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(-8px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .section {
            margin-top: 28px;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            margin-bottom: 10px;
            flex-wrap: wrap;
        }

        .section-title {
            font-size: 1.4em;
            font-weight: 700;
            color: #22223b;
            margin: 0;
        }

        .edit-btn {
            display: inline-block;
            padding: 8px 18px;
            border-radius: 10px;
            border: 2px solid #22223b;
            color: #22223b;
            font-weight: 600;
            background: #fff;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .edit-btn:hover {
            background: #22223b;
            color: #fff;
        }

        .edit-btn.disabled,
        .edit-btn.disabled:hover {
            background: #f3f4f6;
            border-color: #d1d5db;
            color: #9ea3b0;
            cursor: not-allowed;
            box-shadow: none;
            transform: none;
        }

        .edit-btn.active {
            background: #22223b;
            color: #fff;
        }

        .input-box,
        .textarea-box {
            width: 100%;
            box-sizing: border-box;
            border: 2px solid #22223b;
            border-radius: 12px;
            padding: 16px;
            font-size: 1.05em;
            font-family: inherit;
            background: #fcfcfe;
            transition: all 0.2s ease;
        }

        .textarea-box {
            min-height: 180px;
            resize: vertical;
            line-height: 1.7;
        }

        .input-box[readonly],
        .textarea-box[readonly] {
            background: #f5f6fa;
            color: #333;
            cursor: not-allowed;
        }

        .input-box.editable,
        .textarea-box.editable {
            background: #fff;
            box-shadow: 0 0 0 3px rgba(59, 89, 152, 0.08);
        }

        .save-section {
            margin-top: 36px;
            text-align: center;
        }

        .save-btn {
            display: inline-block;
            padding: 14px 40px;
            font-size: 1.15em;
            font-weight: 700;
            color: #fff;
            background: #22223b;
            border: none;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .save-btn:hover {
            background: #30325f;
            transform: translateY(-1px);
        }

        .save-btn.disabled,
        .save-btn.disabled:hover {
            background: #f3f4f6;
            color: #9ea3b0;
            cursor: not-allowed;
            transform: none;
        }

        .empty-box {
            border: 2px dashed #bbb;
            border-radius: 14px;
            padding: 40px 24px;
            text-align: center;
            color: #666;
            background: #fafafa;
            font-size: 1.1em;
            margin-top: 24px;
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
<body>

<div class="container">
    <div class="top-line">
        <h1 class="page-title">
            <%= course != null && course.getCourseName() != null && !course.getCourseName().trim().isEmpty()
                    ? course.getCourseName()
                    : "Project Detail" %>
        </h1>
        <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>">Back</a>
    </div>

    <!-- <div class="notice">// check course detail information</div> -->

    <% if ("1".equals(success)) { %>
        <div class="success-toast" id="successToast">
            Course information saved successfully.
        </div>
    <% } %>

    <% if (error != null) { %>
        <div class="error-box">
            <%= error %>
        </div>
    <% } %>

    <% if (moModifyDeadline != null) { %>
        <div class="deadline-box">
            MO course modification deadline: <%= moModifyDeadline %>
        </div>
    <% } %>

    <%
        if (course != null) {
    %>
    <form action="<%= response.encodeURL("MOclasscontroller") %>" method="post">
        <input type="hidden" name="action" value="save_course_changes">
        <input type="hidden" name="courseIndex" value="<%= courseIndex %>">

        <div class="section">
            <div class="section-header">
                <h2 class="section-title">Course Name</h2>
                <button type="button"
                        class="edit-btn <%= canModifyProject ? "" : "disabled" %>"
                        data-target="courseName"
                        data-locked="<%= !canModifyProject %>"
                        data-lock-reason="<%= !moModifyOpen ? "deadline" : (!moProfileComplete ? "profile" : "") %>">Edit</button>
            </div>
            <input
                id="courseName"
                class="input-box"
                type="text"
                name="courseName"
                value="<%= course.getCourseName() == null ? "" : course.getCourseName() %>"
                readonly
                required>
        </div>

        <div class="section">
            <div class="section-header">
                <h2 class="section-title">Job Title</h2>
                <button type="button"
                        class="edit-btn <%= canModifyProject ? "" : "disabled" %>"
                        data-target="jobTitle"
                        data-locked="<%= !canModifyProject %>"
                        data-lock-reason="<%= !moModifyOpen ? "deadline" : (!moProfileComplete ? "profile" : "") %>">Edit</button>
            </div>
            <input
                id="jobTitle"
                class="input-box"
                type="text"
                name="jobTitle"
                value="<%= course.getJobTitle() == null ? "" : course.getJobTitle() %>"
                readonly
                required>
        </div>

        <div class="section">
            <div class="section-header">
                <h2 class="section-title">Working Hours</h2>
                <button type="button"
                        class="edit-btn <%= canModifyProject ? "" : "disabled" %>"
                        data-target="workingHours"
                        data-locked="<%= !canModifyProject %>"
                        data-lock-reason="<%= !moModifyOpen ? "deadline" : (!moProfileComplete ? "profile" : "") %>">Edit</button>
            </div>
            <input
                id="workingHours"
                class="input-box"
                type="text"
                name="workingHours"
                value="<%= course.getWorkingHours() == null ? "" : course.getWorkingHours() %>"
                readonly
                required>
        </div>

        <div class="section">
            <div class="section-header">
                <h2 class="section-title">Job Description</h2>
                <button type="button"
                        class="edit-btn <%= canModifyProject ? "" : "disabled" %>"
                        data-target="jobDescription"
                        data-locked="<%= !canModifyProject %>"
                        data-lock-reason="<%= !moModifyOpen ? "deadline" : (!moProfileComplete ? "profile" : "") %>">Edit</button>
            </div>
            <textarea
                id="jobDescription"
                class="textarea-box"
                name="jobDescription"
                readonly><%= course.getJobDescription() == null ? "" : course.getJobDescription() %></textarea>
        </div>

        <div class="section">
            <div class="section-header">
                <h2 class="section-title">Job Requirement</h2>
                <button type="button"
                        class="edit-btn <%= canModifyProject ? "" : "disabled" %>"
                        data-target="jobRequirement"
                        data-locked="<%= !canModifyProject %>"
                        data-lock-reason="<%= !moModifyOpen ? "deadline" : (!moProfileComplete ? "profile" : "") %>">Edit</button>
            </div>
            <textarea
                id="jobRequirement"
                class="textarea-box"
                name="jobRequirement"
                readonly><%= course.getJobRequirement() == null ? "" : course.getJobRequirement() %></textarea>
        </div>

        <div class="save-section">
            <% if (canModifyProject) { %>
                <button type="submit" class="save-btn">Save changes</button>
            <% } else if (!moModifyOpen) { %>
                <button type="button" class="save-btn disabled" onclick="openMoModifyLockedModal()">Save changes</button>
            <% } else { %>
                <button type="button" class="save-btn disabled" onclick="openProfileIncompleteModal()">Save changes</button>
            <% } %>
        </div>
    </form>
    <%
        } else {
    %>
        <div class="empty-box">Course information is unavailable.</div>
    <%
        }
    %>
</div>

<div id="moModifyLockedModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="moModifyLockedTitle">
    <div class="modal-box">
        <div class="modal-title" id="moModifyLockedTitle">Course Modification Closed</div>
        <div class="modal-text">The deadline for MO to modify course information has passed.</div>
        <div class="modal-actions">
            <button type="button" class="modal-btn" onclick="closeMoModifyLockedModal()">OK</button>
        </div>
    </div>
</div>

<div id="profileIncompleteModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="profileIncompleteTitle">
    <div class="modal-box">
        <div class="modal-title" id="profileIncompleteTitle">Complete Your Profile</div>
        <div class="modal-text">Please complete your personal information before creating or modifying course projects.</div>
        <div class="modal-actions">
            <button type="button" class="modal-btn" onclick="closeProfileIncompleteModal()">OK</button>
            <a class="modal-btn" href="<%= response.encodeURL("MOclasscontroller?action=profile_center") %>">Go to Profile</a>
        </div>
    </div>
</div>

<script>
    const editButtons = document.querySelectorAll(".edit-btn");

    editButtons.forEach(button => {
        button.addEventListener("click", function () {
            if (this.dataset.locked === "true") {
                if (this.dataset.lockReason === "profile") {
                    openProfileIncompleteModal();
                } else {
                    openMoModifyLockedModal();
                }
                return;
            }

            const targetId = this.getAttribute("data-target");
            const field = document.getElementById(targetId);

            if (!field) return;

            field.removeAttribute("readonly");
            field.classList.add("editable");
            this.classList.add("active");
            field.focus();

            if (field.tagName === "TEXTAREA" || field.tagName === "INPUT") {
                const value = field.value;
                field.value = "";
                field.value = value;
            }
        });
    });

    function openMoModifyLockedModal() {
        document.getElementById("moModifyLockedModal").classList.remove("hidden");
    }

    function closeMoModifyLockedModal() {
        document.getElementById("moModifyLockedModal").classList.add("hidden");
    }

    function openProfileIncompleteModal() {
        document.getElementById("profileIncompleteModal").classList.remove("hidden");
    }

    function closeProfileIncompleteModal() {
        document.getElementById("profileIncompleteModal").classList.add("hidden");
    }

    const successToast = document.getElementById("successToast");
    if (successToast) {
        setTimeout(() => {
            successToast.style.transition = "opacity 0.4s ease, transform 0.4s ease";
            successToast.style.opacity = "0";
            successToast.style.transform = "translateY(-8px)";
            setTimeout(() => {
                if (successToast.parentNode) {
                    successToast.parentNode.removeChild(successToast);
                }
            }, 400);
        }, 2500);
    }

    <% if (showProfileIncompleteModal) { %>
    openProfileIncompleteModal();
    <% } %>
</script>

</body>
</html>
