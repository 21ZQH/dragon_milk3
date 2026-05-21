<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    LocalDateTime applicationDeadline = (LocalDateTime) request.getAttribute("applicationDeadline");
    LocalDateTime moModifyDeadline = (LocalDateTime) request.getAttribute("moModifyDeadline");
    Boolean applicationOpenAttr = (Boolean) request.getAttribute("applicationOpen");
    boolean applicationOpen = applicationOpenAttr == null || applicationOpenAttr.booleanValue();
    Boolean reviewStageOpenAttr = (Boolean) request.getAttribute("reviewStageOpen");
    boolean reviewStageOpen = reviewStageOpenAttr != null && reviewStageOpenAttr.booleanValue();
    Boolean moModifyOpenAttr = (Boolean) request.getAttribute("moModifyOpen");
    boolean moModifyOpen = moModifyOpenAttr == null ? true : moModifyOpenAttr.booleanValue();
    Boolean showModifyLockedModalAttr = (Boolean) request.getAttribute("showModifyLockedModal");
    boolean showModifyLockedModal = showModifyLockedModalAttr != null && showModifyLockedModalAttr.booleanValue();
    DateTimeFormatter deadlineFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>MO Dashboard</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/role-style.css">
    <style>
        body { margin: 0 !important; background: #f4f6f8 !important; color: #1f2a44 !important; font-family: Arial, sans-serif !important; }
        .dashboard-header { background: #fff; border-bottom: 1px solid #d7dee8; padding: 18px 32px; display: flex; justify-content: space-between; align-items: center; gap: 18px; }
        .dashboard-brand { color: #1f2a44; font-size: 18px; font-weight: 700; text-decoration: none; }
        .dashboard-nav { display: flex; align-items: center; justify-content: flex-end; gap: 18px; flex-wrap: wrap; }
        .dashboard-nav a, .dashboard-nav button { min-width: 0 !important; min-height: 0 !important; width: auto !important; padding: 0 !important; border: 0 !important; border-radius: 0 !important; background: transparent !important; color: #23395d !important; font-size: 17px !important; font-weight: 700 !important; text-decoration: none !important; box-shadow: none !important; }
        .dashboard-nav a:hover, .dashboard-nav button:hover { color: #1f2a44 !important; background: transparent !important; }
        .dashboard-nav .is-disabled { color: #9ea3b0 !important; cursor: pointer !important; }
        .dashboard-main { max-width: 860px; margin: 32px auto 56px; padding: 0 20px; }
        .dashboard-title { color: #1f2a44 !important; font-size: 34px !important; font-weight: 700 !important; line-height: 1.2 !important; text-align: left !important; margin: 0 0 26px !important; }
        .status-panel { background: #fff; border: 1px solid #d7dee8; border-radius: 8px; padding: 18px 20px; margin-bottom: 18px; }
        .status-title { color: #1f2a44; font-size: 18px; font-weight: 700; margin-bottom: 12px; }
        .status-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
        .status-item { border: 1px solid #e1e6ef; border-radius: 8px; padding: 14px; background: #fbfcfe; }
        .status-label { color: #5f6f85; font-size: 13px; font-weight: 700; text-transform: uppercase; margin-bottom: 8px; }
        .status-value { color: #1f2a44; font-size: 16px; font-weight: 700; margin-bottom: 8px; }
        .status-meta { color: #5f6f85; line-height: 1.45; }
        .pill { display: inline-flex; align-items: center; border-radius: 999px; padding: 5px 10px; font-size: 13px; font-weight: 700; }
        .pill-open { background: #e8f5ee; color: #166534; }
        .pill-closed { background: #fff0f0; color: #b42318; }
        .pill-neutral { background: #eef2ff; color: #23395d; }
        .dashboard-list { display: flex; flex-direction: column; gap: 14px; }
        .dashboard-card { display: block !important; width: 100% !important; min-height: 0 !important; box-sizing: border-box !important; background: #fff !important; border: 1px solid #d7dee8 !important; border-radius: 8px !important; padding: 20px 24px !important; color: inherit !important; text-align: left !important; text-decoration: none !important; box-shadow: none !important; cursor: pointer !important; }
        .dashboard-card:hover { border-color: #23395d !important; background: #fff !important; }
        .dashboard-card-title { color: #1f2a44; font-size: 20px; font-weight: 700; margin-bottom: 8px; }
        .dashboard-card-meta { color: #5f6f85; font-size: 16px; line-height: 1.5; }
        .dashboard-card-disabled, .dashboard-card-disabled:hover { color: inherit !important; border-color: #d7dee8 !important; background: #fff !important; }
        .dashboard-card-disabled .dashboard-card-title { color: #9ea3b0; }
        .modal-overlay { position: fixed !important; inset: 0 !important; background: rgba(0, 0, 0, 0.35) !important; display: flex !important; align-items: center !important; justify-content: center !important; z-index: 999 !important; }
        .hidden { display: none !important; }
        .modal-box { width: 460px !important; max-width: calc(100vw - 40px) !important; background: #fff !important; border: 2px solid #222 !important; border-radius: 12px !important; box-shadow: 0 4px 16px rgba(0,0,0,0.2) !important; padding: 20px !important; }
        .modal-title { color: #2d3651 !important; font-size: 1.25em !important; font-weight: 700 !important; margin-bottom: 10px !important; }
        .modal-text { color: #444 !important; line-height: 1.6 !important; margin-bottom: 16px !important; }
        .modal-actions { display: flex !important; justify-content: center !important; gap: 10px !important; flex-wrap: wrap !important; }
        @media (max-width: 720px) {
            .dashboard-header { align-items: flex-start; flex-direction: column; padding: 16px 20px; }
            .dashboard-nav { justify-content: flex-start; gap: 12px; }
            .dashboard-main { margin-top: 26px; }
            .dashboard-title { font-size: 28px !important; }
            .status-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body data-show-modify-locked-modal="<%= showModifyLockedModal %>">
<header class="dashboard-header">
    <a class="dashboard-brand" href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">MO Management</a>
    <nav class="dashboard-nav">
        <% if (moModifyOpen) { %>
            <a href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>">Publish Recruitment</a>
        <% } else { %>
            <button class="is-disabled" type="button" onclick="openMoModifyLockedModal()">Publish Recruitment</button>
        <% } %>
        <a href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">Personal Centre</a>
        <a href="<%= response.encodeURL("MOclasscontroller?action=logout") %>">Log out</a>
    </nav>
</header>

<main class="dashboard-main">
    <h1 class="dashboard-title">MO Dashboard</h1>
    <section class="status-panel" aria-label="Recruitment status">
        <div class="status-title">Recruitment Timeline</div>
        <div class="status-grid">
            <div class="status-item">
                <div class="status-label">Course Information Deadline</div>
                <div class="status-value"><%= moModifyDeadline == null ? "Not set" : moModifyDeadline.format(deadlineFormatter) %></div>
                <div class="status-meta">
                    <span class="pill <%= moModifyOpen ? "pill-open" : "pill-closed" %>">
                        <%= moModifyOpen ? "Recruitment editing open" : "Recruitment editing closed" %>
                    </span>
                    <div style="margin-top:8px;">
                        <%= moModifyOpen
                                ? "You can still edit drafts and publish recruitment information."
                                : "Course publishing and modification are locked." %>
                    </div>
                </div>
            </div>
            <div class="status-item">
                <div class="status-label">TA Application Deadline</div>
                <div class="status-value"><%= applicationDeadline == null ? "Not set" : applicationDeadline.format(deadlineFormatter) %></div>
                <div class="status-meta">
                    <span class="pill <%= reviewStageOpen ? "pill-neutral" : "pill-open" %>">
                        <%= reviewStageOpen ? "Review stage open" : "TA applications still open" %>
                    </span>
                    <div style="margin-top:8px;">
                        <%= reviewStageOpen
                                ? "You can review submitted TA applications and publish final results."
                                : "Review opens after the TA application deadline passes." %>
                    </div>
                </div>
            </div>
        </div>
        <div style="margin-top:12px;">
            <span class="pill <%= applicationOpen ? "pill-open" : "pill-closed" %>">
                <%= applicationOpen ? "TAs can still submit applications" : "TA submissions are closed" %>
            </span>
        </div>
    </section>
    <div class="dashboard-list">
        <% if (moModifyOpen) { %>
            <a class="dashboard-card" href="<%= response.encodeURL("MOclasscontroller?action=my_project") %>">
                <div class="dashboard-card-title">Publish recruitment</div>
                <div class="dashboard-card-meta">Open assigned courses, edit draft recruitment information, and publish when ready.</div>
            </a>
        <% } else { %>
            <button class="dashboard-card dashboard-card-disabled" type="button" onclick="openMoModifyLockedModal()">
                <div class="dashboard-card-title">Publish recruitment</div>
                <div class="dashboard-card-meta">Course publishing and modification are closed after the MO deadline.</div>
            </button>
        <% } %>

        <a class="dashboard-card" href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">
            <div class="dashboard-card-title">Personal Centre</div>
            <div class="dashboard-card-meta">Manage assigned projects and review TA applications.</div>
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

<script>
    const showModifyLockedModalFlag = document.body.dataset.showModifyLockedModal === "true";

    function openMoModifyLockedModal() {
        document.getElementById("moModifyLockedModal").classList.remove("hidden");
    }

    function closeMoModifyLockedModal() {
        document.getElementById("moModifyLockedModal").classList.add("hidden");
    }

    if (showModifyLockedModalFlag) {
        openMoModifyLockedModal();
    }
</script>
</body>
</html>
