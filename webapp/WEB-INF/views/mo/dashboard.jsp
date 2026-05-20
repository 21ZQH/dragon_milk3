<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Boolean moModifyOpenAttr = (Boolean) request.getAttribute("moModifyOpen");
    boolean moModifyOpen = moModifyOpenAttr == null ? true : moModifyOpenAttr.booleanValue();
    Boolean moProfileCompleteAttr = (Boolean) request.getAttribute("moProfileComplete");
    boolean moProfileComplete = moProfileCompleteAttr == null ? true : moProfileCompleteAttr.booleanValue();
    Boolean showModifyLockedModalAttr = (Boolean) request.getAttribute("showModifyLockedModal");
    boolean showModifyLockedModal = showModifyLockedModalAttr != null && showModifyLockedModalAttr.booleanValue();
    Boolean showProfileIncompleteModalAttr = (Boolean) request.getAttribute("showProfileIncompleteModal");
    boolean showProfileIncompleteModal = showProfileIncompleteModalAttr != null && showProfileIncompleteModalAttr.booleanValue();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>MO Dashboard</title>
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
            margin: 0 0 26px !important;
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
            color: inherit !important;
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
<body data-show-modify-locked-modal="<%= showModifyLockedModal %>"
      data-show-profile-incomplete-modal="<%= showProfileIncompleteModal %>">
<header class="dashboard-header">
    <a class="dashboard-brand" href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">MO Management</a>
    <nav class="dashboard-nav">
        <% if (moModifyOpen && moProfileComplete) { %>
            <a href="<%= response.encodeURL("MOclasscontroller?action=create_class") %>">Publish Recruitment</a>
        <% } else if (!moModifyOpen) { %>
            <button class="is-disabled" type="button" onclick="openMoModifyLockedModal()">Publish Recruitment</button>
        <% } else { %>
            <button class="is-disabled" type="button" onclick="openProfileIncompleteModal()">Publish Recruitment</button>
        <% } %>
        <a href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">Personal Centre</a>
        <a href="<%= response.encodeURL("MOclasscontroller?action=logout") %>">Log out</a>
    </nav>
</header>

<main class="dashboard-main">
    <h1 class="dashboard-title">MO Dashboard</h1>
    <div class="dashboard-list">
        <% if (moModifyOpen && moProfileComplete) { %>
            <a class="dashboard-card" href="<%= response.encodeURL("MOclasscontroller?action=create_class") %>">
                <div class="dashboard-card-title">Publish recruitment</div>
                <div class="dashboard-card-meta">Create TA recruitment information for courses assigned by Admin.</div>
            </a>
        <% } else if (!moModifyOpen) { %>
            <button class="dashboard-card dashboard-card-disabled" type="button" onclick="openMoModifyLockedModal()">
                <div class="dashboard-card-title">Publish recruitment</div>
                <div class="dashboard-card-meta">Course publishing and modification are closed after the MO deadline.</div>
            </button>
        <% } else { %>
            <button class="dashboard-card dashboard-card-disabled" type="button" onclick="openProfileIncompleteModal()">
                <div class="dashboard-card-title">Publish recruitment</div>
                <div class="dashboard-card-meta">Complete your MO profile before publishing recruitment information.</div>
            </button>
        <% } %>

        <a class="dashboard-card" href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">
            <div class="dashboard-card-title">Personal Centre</div>
            <div class="dashboard-card-meta">Edit your profile, manage assigned projects, and review TA applications.</div>
        </a>

        <a class="dashboard-card" href="<%= response.encodeURL("MOclasscontroller?action=logout") %>">
            <div class="dashboard-card-title">Log out</div>
            <div class="dashboard-card-meta">Leave the MO workspace and return to the MO login page.</div>
        </a>
    </div>
</main>

<div id="moModifyLockedModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="moModifyLockedTitle">
    <div class="modal-box">
        <div class="modal-title" id="moModifyLockedTitle">Course Modification Closed</div>
        <div class="modal-text">The deadline for MO to publish or modify recruitment information has passed.</div>
        <div class="modal-actions">
            <button type="button" class="modal-btn" onclick="closeMoModifyLockedModal()">OK</button>
        </div>
    </div>
</div>

<div id="profileIncompleteModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="profileIncompleteTitle">
    <div class="modal-box">
        <div class="modal-title" id="profileIncompleteTitle">Complete Your Profile</div>
        <div class="modal-text">Please complete your personal information before publishing or modifying recruitment information.</div>
        <div class="modal-actions">
            <button type="button" class="modal-btn" onclick="closeProfileIncompleteModal()">OK</button>
            <a class="modal-btn" href="<%= response.encodeURL("MOclasscontroller?action=profile_center") %>">Go to Profile</a>
        </div>
    </div>
</div>

<script>
    const showModifyLockedModalFlag = document.body.dataset.showModifyLockedModal === "true";
    const showProfileIncompleteModalFlag = document.body.dataset.showProfileIncompleteModal === "true";

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

    if (showModifyLockedModalFlag) {
        openMoModifyLockedModal();
    }

    if (showProfileIncompleteModalFlag) {
        openProfileIncompleteModal();
    }
</script>
</body>
</html>
