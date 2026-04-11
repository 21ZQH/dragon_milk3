<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Set MO Course Modification Deadline</title>
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
            max-width: 820px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 36px 40px 40px 40px;
            box-sizing: border-box;
        }

        .top-line {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #22223b;
            padding-bottom: 15px;
            margin-bottom: 30px;
        }

        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin: 0;
        }

        .back-link {
            text-decoration: none;
            color: #22223b;
            font-weight: 600;
            border: 2px solid #22223b;
            padding: 10px 18px;
            border-radius: 12px;
            background: #fff;
            transition: all 0.2s ease;
        }

        .back-link:hover {
            background: #22223b;
            color: #fff;
            box-shadow: 0 4px 10px rgba(34,34,59,0.12);
        }

        .desc-box {
            background: #fcfcfe;
            border: 2px solid #e2e5ef;
            border-radius: 16px;
            padding: 20px 22px;
            margin-bottom: 28px;
            color: #444;
            line-height: 1.6;
            font-size: 1.05em;
        }

        .form-card {
            border: 2px solid #22223b;
            border-radius: 16px;
            padding: 30px 28px;
            background: #fcfcfe;
        }

        .form-row {
            margin-bottom: 24px;
        }

        .form-label {
            display: block;
            font-size: 1.1em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 10px;
        }

        .form-input {
            width: 100%;
            box-sizing: border-box;
            padding: 14px 16px;
            font-size: 1em;
            border: 2px solid #cfd3df;
            border-radius: 12px;
            outline: none;
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
            background: #fff;
        }

        .form-input:focus {
            border-color: #22223b;
            box-shadow: 0 0 0 4px rgba(34,34,59,0.08);
        }

        .deadline-input {
            padding: 16px 18px;
            font-size: 1.15em;
            min-height: 58px;
            border: 3px solid #22223b;
            border-radius: 18px;
        }

        .hint-text {
            margin-top: 8px;
            color: #666;
            font-size: 0.95em;
            line-height: 1.6;
        }

        .btn-row {
            display: flex;
            justify-content: center;
            margin-top: 30px;
        }

        .save-btn {
            min-width: 220px;
            padding: 16px 24px;
            font-size: 1.2em;
            font-weight: 700;
            color: #22223b;
            background: #fff;
            border: 3px solid #22223b;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
            box-shadow: 0 2px 8px rgba(34,34,59,0.05);
        }

        .save-btn:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(34,34,59,0.2);
        }
    </style>
</head>
<body>

<div class="container">
    <div class="top-line">
        <h1 class="page-title">Set the deadline for MO to modify the course information</h1>
        <a class="back-link" href="<%= response.encodeURL("AdminController?action=dashboard") %>">Back to Dashboard</a>
    </div>

    <div class="desc-box">
        Please set the deadline for MO to modify the course information.
        After this deadline passes, MOs will no longer be allowed to edit course details.
    </div>

    <div class="form-card">
        <% if (request.getAttribute("success") != null) { %>
            <div style="margin-bottom: 18px; padding: 12px 16px; border-radius: 10px; background: #eaf7ea; color: #256029; border: 1px solid #b7dfb9;">
                <%= request.getAttribute("success") %>
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div style="margin-bottom: 18px; padding: 12px 16px; border-radius: 10px; background: #fdeaea; color: #9f2d2d; border: 1px solid #e6b8b8;">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <% if (request.getAttribute("savedMoDeadline") != null) { %>
            <div style="margin-bottom: 18px; padding: 12px 16px; border-radius: 10px; background: #eef2ff; color: #22223b; border: 1px solid #cfd7ff;">
                Current saved deadline: <%= request.getAttribute("savedMoDeadline") %>
            </div>
        <% } %>

        <form action="<%= response.encodeURL("AdminController") %>" method="post">
            <input type="hidden" name="action" value="save_mo_deadline">

            <div class="form-row">
                <label class="form-label" for="deadlineDate">MO Course Modification Deadline Date</label>
                <input
                        class="form-input deadline-input"
                        type="date"
                        id="deadlineDate"
                        name="deadlineDate"
                        required>
                <div class="hint-text">
                    Enter the closing date for MO course information modification.
                </div>
            </div>

            
            <div class="form-row">
                <label class="form-label" for="deadlineTime">MO Course Modification Deadline Time</label>
                <input
                        class="form-input deadline-input"
                        type="time"
                        id="deadlineTime"
                        name="deadlineTime"
                        required>
                <div class="hint-text">
                    Set the exact closing time. After this time, MOs can no longer edit the course information.
                </div>
            </div>

            <div class="btn-row">
                <button type="submit" class="save-btn">Save Deadline</button>
            </div>
        </form>
    </div>
</div>

</body>
</html>
