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
    <meta charset="UTF-8">
    <title>Personal Centre</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/role-style.css">
    <style>
        body {
            margin: 0 !important;
            background: #f4f6f8 !important;
            color: #1f2a44 !important;
            font-family: Arial, sans-serif !important;
        }

        .dashboard-header {
            background: #fff;
            border-bottom: 1px solid #d7dee8;
            padding: 18px 32px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 18px;
        }

        .dashboard-brand {
            color: #1f2a44;
            font-size: 18px;
            font-weight: 700;
            text-decoration: none;
        }

        .dashboard-nav {
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 18px;
            flex-wrap: wrap;
        }

        .dashboard-nav a,
        .dashboard-nav button {
            min-width: 0 !important;
            min-height: 0 !important;
            width: auto !important;
            padding: 0 !important;
            border: 0 !important;
            border-radius: 0 !important;
            background: transparent !important;
            color: #23395d !important;
            font-size: 17px !important;
            font-weight: 700 !important;
            text-decoration: none !important;
            box-shadow: none !important;
        }

        .dashboard-nav a:hover,
        .dashboard-nav button:hover {
            color: #1f2a44 !important;
            background: transparent !important;
        }

        .dashboard-nav .is-disabled {
            color: #9ea3b0 !important;
            cursor: pointer !important;
        }

        .dashboard-main {
            max-width: 860px;
            margin: 32px auto 56px;
            padding: 0 20px;
        }

        .dashboard-title {
            color: #1f2a44 !important;
            font-size: 34px !important;
            font-weight: 700 !important;
            line-height: 1.2 !important;
            text-align: left !important;
            margin: 0 0 8px !important;
        }

        .dashboard-welcome {
            color: #5f6f85;
            font-size: 16px;
            margin: 0 0 26px;
        }

        .dashboard-list {
            display: flex;
            flex-direction: column;
            gap: 14px;
        }

        .dashboard-card {
            display: block !important;
            width: 100% !important;
            min-height: 0 !important;
            box-sizing: border-box !important;
            background: #fff !important;
            border: 1px solid #d7dee8 !important;
            border-radius: 8px !important;
            padding: 20px 24px !important;
            color: inherit !important;
            text-align: left !important;
            text-decoration: none !important;
            box-shadow: none !important;
            cursor: pointer !important;
        }

        .dashboard-card:hover {
            border-color: #23395d !important;
            background: #fff !important;
        }

        .dashboard-card-title {
            color: #1f2a44;
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .dashboard-card-meta {
            color: #5f6f85;
            font-size: 16px;
            line-height: 1.5;
        }

        .dashboard-card-disabled,
        .dashboard-card-disabled:hover {
            border-color: #d7dee8 !important;
            background: #fff !important;
        }

        .dashboard-card-disabled .dashboard-card-title {
            color: #9ea3b0;
        }

        .modal-overlay {
            position: fixed !important;
            inset: 0 !important;
            background: rgba(0, 0, 0, 0.35) !important;
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            z-index: 999 !important;
        }

        .hidden {
            display: none !important;
        }

        .modal-box {
            width: 460px !important;
            max-width: calc(100vw - 40px) !important;
            background: #fff !important;
            border: 2px solid #222 !important;
            border-radius: 12px !important;
            box-shadow: 0 4px 16px rgba(0,0,0,0.2) !important;
            padding: 20px !important;
        }

        .modal-title {
            color: #2d3651 !important;
            font-size: 1.25em !important;
            font-weight: 700 !important;
            margin-bottom: 10px !important;
        }

        .modal-text {
            color: #444 !important;
            line-height: 1.6 !important;
            margin-bottom: 16px !important;
        }

        .modal-actions {
            display: flex !important;
            justify-content: center !important;
            gap: 10px !important;
            flex-wrap: wrap !important;
        }

        @media (max-width: 720px) {
            .dashboard-header {
                align-items: flex-start;
                flex-direction: column;
                padding: 16px 20px;
            }

            .dashboard-nav {
                justify-content: flex-start;
                gap: 12px;
            }

            .dashboard-main {
                margin-top: 26px;
            }

            .dashboard-title {
                font-size: 28px !important;
            }
        }
    </style>
</head>
<body>
<header class="dashboard-header">
    <a class="dashboard-brand" href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">MO Management</a>
    <nav class="dashboard-nav">
        <a href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">Dashboard</a>
        <a href="<%= response.encodeURL("MOclasscontroller?action=profile_center") %>">Profile</a>
        <a href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>">My Project</a>
        <% if (reviewStageOpen) { %>
            <a href="<%= response.encodeURL("MOclasscontroller?action=review_candidates") %>">Review</a>
        <% } else { %>
            <button class="is-disabled" type="button" onclick="openReviewLockedModal()">Review</button>
        <% } %>
        <a href="<%= response.encodeURL("MOclasscontroller?action=logout") %>">Log out</a>
    </nav>
</header>

<main class="dashboard-main">
    <h1 class="dashboard-title">Personal Centre</h1>
    <p class="dashboard-welcome">Hi, <%= username %></p>

    <div class="dashboard-list">
        <a href="<%= response.encodeURL("MOclasscontroller?action=profile_center") %>" class="dashboard-card">
            <div class="dashboard-card-title">Edit personal information</div>
            <div class="dashboard-card-meta">Update your name, educational background, and department profile.</div>
        </a>

        <a href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>" class="dashboard-card">
            <div class="dashboard-card-title">My project</div>
            <div class="dashboard-card-meta">Open your assigned courses, inspect recruitment details, and edit published information before the MO deadline.</div>
        </a>

        <% if (reviewStageOpen) { %>
            <a href="<%= response.encodeURL("MOclasscontroller?action=review_candidates") %>" class="dashboard-card">
                <div class="dashboard-card-title">Review applications</div>
                <div class="dashboard-card-meta">Review submitted TA application forms, save picks, and publish final results.</div>
            </a>
        <% } else { %>
            <button type="button" class="dashboard-card dashboard-card-disabled" onclick="openReviewLockedModal()">
                <div class="dashboard-card-title">Review applications</div>
                <div class="dashboard-card-meta">Review opens after the TA application deadline has passed.</div>
            </button>
        <% } %>
    </div>
</main>

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
