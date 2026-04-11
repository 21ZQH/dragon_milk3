<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    model.User currentUser = (model.User) session.getAttribute("user");
    String username = "Guest";
    if (currentUser != null && currentUser.getName() != null && !currentUser.getName().trim().isEmpty()) {
        username = currentUser.getName();
    }

    Boolean reviewStageOpenAttr = (Boolean) request.getAttribute("reviewStageOpen");
    boolean reviewStageOpen = reviewStageOpenAttr == null ? true : reviewStageOpenAttr.booleanValue();
    boolean autoOpenReviewLocked = "1".equals(request.getParameter("reviewLocked"));
%>
<!DOCTYPE html>
<html>
<head>
    <title>Personal Centre</title>
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
            max-width: 800px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 40px 50px;
            box-sizing: border-box;
        }

        .top-line {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 10px;
        }

        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin: 0;
            border-bottom: 2px solid #22223b;
            padding-bottom: 15px;
            flex: 1 1 auto;
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

        .welcome-text {
            font-size: 3em;
            font-weight: 700;
            color: #22223b;
            text-align: center;
            margin: 40px 0 50px;
        }

        .btn-group {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 25px;
        }

        .action-btn {
            display: block;
            width: 80%;
            padding: 20px 0;
            font-size: 1.5em;
            font-weight: 700;
            color: #22223b;
            background: #fff;
            border: 3px solid #22223b;
            border-radius: 12px;
            text-align: center;
            text-decoration: none;
            transition: all 0.2s ease;
            box-shadow: 0 2px 8px rgba(34,34,59,0.05);
            cursor: pointer;
            font-family: inherit;
        }

        .action-btn:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-3px);
            box-shadow: 0 6px 16px rgba(34,34,59,0.2);
        }

        .action-btn.locked {
            color: #aeb4c3;
            border-color: #aeb4c3;
            background: #fff;
            box-shadow: none;
        }

        .action-btn.locked:hover {
            background: #fff;
            color: #aeb4c3;
            transform: none;
            box-shadow: none;
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
            width: 520px;
            max-width: calc(100vw - 40px);
            background: #fff;
            border: 2px solid #22223b;
            border-radius: 16px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.18);
            padding: 28px 26px;
        }

        .modal-title {
            font-size: 1.8em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 18px;
        }

        .modal-text {
            color: #444;
            font-size: 1.1em;
            line-height: 1.7;
            margin-bottom: 26px;
        }

        .modal-actions {
            display: flex;
            justify-content: center;
        }

        .modal-btn {
            min-width: 160px;
            padding: 14px 26px;
            border-radius: 12px;
            border: 3px solid #22223b;
            background: #fff;
            color: #22223b;
            font-size: 1.2em;
            font-weight: 700;
            cursor: pointer;
        }

        .modal-btn:hover {
            background: #22223b;
            color: #fff;
        }
    </style>
</head>
<body>

    <div class="container">
        <div class="top-line">
            <div class="page-title">Personal Centre</div>
            <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">Back</a>
        </div>

        <div class="welcome-text">Hi, <%= username %></div>

        <div class="btn-group">
            <a href="<%= response.encodeURL("MOclasscontroller?action=profile_center") %>" class="action-btn">Edit personal information</a>
            <a href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>" class="action-btn">My project</a>

            <% if (reviewStageOpen) { %>
                <a href="<%= response.encodeURL("MOclasscontroller?action=review_candidates") %>" class="action-btn">Review</a>
            <% } else { %>
                <button type="button" class="action-btn locked" onclick="openReviewLockedModal()">Review</button>
            <% } %>
        </div>
    </div>

    <div id="reviewLockedModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="reviewLockedTitle">
        <div class="modal-box">
            <div class="modal-title" id="reviewLockedTitle">Review Not Available</div>
            <div class="modal-text">The application deadline has not passed yet.</div>
            <div class="modal-actions">
                <button type="button" class="modal-btn" onclick="closeReviewLockedModal()">OK</button>
            </div>
        </div>
    </div>

    <script>
        function openReviewLockedModal() {
            document.getElementById("reviewLockedModal").classList.remove("hidden");
        }

        function closeReviewLockedModal() {
            document.getElementById("reviewLockedModal").classList.add("hidden");
        }

        <% if (autoOpenReviewLocked) { %>
        openReviewLockedModal();
        <% } %>
    </script>

</body>
</html>
