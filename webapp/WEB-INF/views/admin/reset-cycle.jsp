<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reset Recruitment Cycle</title>
    <style>
        body {
            margin: 0;
            background: #fff;
            color: #172554;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .page {
            width: 860px;
            max-width: calc(100vw - 48px);
            margin: 52px auto;
        }
        .top-line {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            margin-bottom: 28px;
        }
        h1 {
            margin: 0;
            font-size: 2.45em;
            color: #253b6e;
        }
        .back-link,
        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 46px;
            padding: 0 20px;
            border: 1px solid #c8d0e0;
            border-radius: 8px;
            background: #e9edf6;
            color: #101a3b;
            text-decoration: none;
            font-weight: 700;
            font-family: inherit;
            font-size: 1em;
            cursor: pointer;
        }
        .btn-danger {
            background: #2b2754;
            border-color: #2b2754;
            color: #fff;
        }
        .panel {
            border: 1px solid #c8d0e0;
            border-radius: 8px;
            padding: 26px;
            background: #fff;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 14px;
            margin: 22px 0;
        }
        .item {
            border: 1px solid #e1e6ef;
            border-radius: 8px;
            background: #f8fafc;
            padding: 16px;
            line-height: 1.55;
        }
        .item strong {
            display: block;
            color: #14285f;
            margin-bottom: 6px;
        }
        .warning {
            border: 1px solid #f2c56b;
            background: #fff8e6;
            color: #7a4d00;
            border-radius: 8px;
            padding: 14px 16px;
            line-height: 1.6;
            margin-bottom: 20px;
        }
        .error {
            border: 1px solid #efb7b7;
            background: #fdeeee;
            color: #a12626;
            border-radius: 8px;
            padding: 12px 16px;
            font-weight: 700;
            margin-bottom: 18px;
        }
        label {
            display: block;
            font-weight: 800;
            margin: 18px 0 8px;
        }
        input {
            width: 100%;
            box-sizing: border-box;
            min-height: 48px;
            border: 1px solid #c8d0e0;
            border-radius: 8px;
            padding: 10px 12px;
            font-family: inherit;
            font-size: 1em;
        }
        .actions {
            display: flex;
            justify-content: center;
            gap: 14px;
            flex-wrap: wrap;
            margin-top: 24px;
        }
        @media (max-width: 720px) {
            .top-line {
                align-items: flex-start;
                flex-direction: column;
            }
            .summary {
                grid-template-columns: 1fr;
            }
            h1 {
                font-size: 2em;
            }
        }
    </style>
</head>
<body>
<main class="page">
    <div class="top-line">
        <h1>Reset Recruitment Cycle</h1>
        <a class="back-link" href="<%= response.encodeURL("AdminController?action=dashboard") %>">Back to Dashboard</a>
    </div>

    <% if (error != null) { %>
        <div class="error"><%= error %></div>
    <% } %>

    <section class="panel">
        <div class="warning">
            This action starts a new yearly recruitment cycle. It cannot be undone from the application UI.
        </div>

        <div class="summary">
            <div class="item">
                <strong>Cleared</strong>
                TA applications, generated application forms, review picks, review results, unread result notifications, and both deadlines.
            </div>
            <div class="item">
                <strong>Kept</strong>
                Admin accounts, MO accounts, TA accounts, MO-course ownership, course names, and the previous recruitment text as draft content.
            </div>
            <div class="item">
                <strong>Course state</strong>
                Every course returns to Draft so MOs can edit and publish again for the new year.
            </div>
            <div class="item">
                <strong>TA state</strong>
                TA accounts remain available, but Personal Centre starts with no active applications.
            </div>
        </div>

        <form method="post" action="<%= response.encodeURL("AdminController") %>">
            <input type="hidden" name="action" value="reset_cycle_confirm">
            <label for="confirmation">Type RESET to confirm</label>
            <input id="confirmation" name="confirmation" autocomplete="off" required>
            <div class="actions">
                <a class="btn" href="<%= response.encodeURL("AdminController?action=dashboard") %>">Cancel</a>
                <button class="btn btn-danger" type="submit">Reset Cycle</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
