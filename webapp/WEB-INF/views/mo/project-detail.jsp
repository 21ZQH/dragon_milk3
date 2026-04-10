<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Course" %>
<%
    Course course = (Course) request.getAttribute("selectedCourse");
    String courseIndex = (String) request.getAttribute("courseIndex");
    String success = request.getParameter("success");
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

    <div class="notice">// check course detail information</div>

    <% if ("1".equals(success)) { %>
        <div class="success-toast" id="successToast">
            Course information saved successfully.
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
                <button type="button" class="edit-btn" data-target="courseName">Edit</button>
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
                <button type="button" class="edit-btn" data-target="jobTitle">Edit</button>
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
                <button type="button" class="edit-btn" data-target="workingHours">Edit</button>
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
                <button type="button" class="edit-btn" data-target="jobDescription">Edit</button>
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
                <button type="button" class="edit-btn" data-target="jobRequirement">Edit</button>
            </div>
            <textarea
                id="jobRequirement"
                class="textarea-box"
                name="jobRequirement"
                readonly><%= course.getJobRequirement() == null ? "" : course.getJobRequirement() %></textarea>
        </div>

        <div class="save-section">
            <button type="submit" class="save-btn">Save changes</button>
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

<script>
    const editButtons = document.querySelectorAll(".edit-btn");

    editButtons.forEach(button => {
        button.addEventListener("click", function () {
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
</script>

</body>
</html>
