<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
%>
    <script>
        alert("<%= error %>");
        window.location.href = "start.html";
    </script>
<%
        return;
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %>
<%@ page import="model.TA" %>
<%
    User currentUser = (User) session.getAttribute("user");
    TA currentTA = null;
    if (currentUser instanceof TA) {
        currentTA = (TA) currentUser;
    }

    String username = (String) session.getAttribute("username");
    if ((username == null || username.trim().isEmpty()) && currentTA != null
            && currentTA.getName() != null && !currentTA.getName().trim().isEmpty()) {
        username = currentTA.getName().trim();
    }
    if (username == null || username.trim().isEmpty()) {
        username = "Guest";
    }

    Boolean applicationOpenAttr = (Boolean) request.getAttribute("applicationOpen");
    boolean applicationOpen = applicationOpenAttr == null ? true : applicationOpenAttr.booleanValue();
    Boolean showDeadlineModalAttr = (Boolean) request.getAttribute("showDeadlineModal");
    boolean showDeadlineModal = showDeadlineModalAttr != null && showDeadlineModalAttr.booleanValue();

    boolean profileComplete = false;
    boolean hasUnreadReviewUpdate = false;
    if (currentTA != null) {
        String taName = currentTA.getName();
        String taCollege = currentTA.getCollege();
        String taSkill = currentTA.getSkill();
        profileComplete = taName != null && !taName.trim().isEmpty()
                && taCollege != null && !taCollege.trim().isEmpty()
                && taSkill != null && !taSkill.trim().isEmpty();
        hasUnreadReviewUpdate = currentTA.hasUnreadReviewUpdates();
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>Main Page for TA</title>
    <style>
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 0;
        }
        .container {
            background: #fff;
            max-width: 600px;
            margin: 48px auto;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 32px 32px 32px 32px;
        }
        .nav-row {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 20px; 
            flex-wrap: wrap; 
            margin: 40px 0 32px 0;
            width: 100%;
            box-sizing: border-box;
        }

        .nav-cell {
            flex: 1;
            border-right: 2px solid #22223b;
            background: #fff;
            font-size: 1.2em;
            font-weight: 700;
            color: #22223b;
            text-align: center;
            padding: 18px 0;
            font-family: 'Segoe UI', Arial, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .nav-cell:last-child {
            border-right: none;
        }
        .nav-logo img {
            height: 32px;
        }
        .nav-link, .nav-btn {
            color: #22223b;
            text-decoration: none;
            background: none;
            border: none;
            font-family: inherit;
            font-size: inherit;
            font-weight: inherit;
            cursor: pointer;
            padding: 0;
        }
        .title {
            font-size: 2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 24px;
            text-align: center;
        }
        .welcome {
            font-size: 1.1em;
            color: #22223b;
            margin-bottom: 12px;
            text-align: center;
        }
        .desc {
            color: #444;
            margin-bottom: 32px;
            font-size: 1em;
            text-align: center;
        }
        .main-buttons button {
            width: 100%;
            font-size: 1.15em;
            font-weight: 700;
            margin: 12px 0;
            padding: 14px 0;
            border-radius: 8px;
            border: none;
            background: #e9edfa;
            color: #22223b;
            cursor: pointer;
            transition: background 0.2s;
        }
        .main-buttons button:hover {
            background: #bfc8e6;
        }
        .nav-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 170px;
            height: 80px;
            padding: 0;
            background: #fff;
            border: 3px solid #18192b;
            border-radius: 20px;
            font-size: 1.3em;
            font-weight: 700;
            color: #18192b;
            text-decoration: none;
            font-family: 'Segoe UI', Arial, sans-serif;
            box-shadow: 0 2px 8px rgba(24,25,43,0.06);
            transition: box-shadow 0.2s, border-color 0.2s;
            cursor: pointer;
            overflow: hidden; 
        }

        .nav-logo img {
            width: 100%;
            height: 100%;
            object-fit: contain;
            display: block;
        }

        .nav-btn:hover {
            box-shadow: 0 4px 16px rgba(24,25,43,0.12);
            border-color: #3b3e5b;
            background: #f5f6fa;
        }

        .nav-btn-disabled {
            color: #9ea3b0;
        }

        .nav-item {
            position: relative;
            display: inline-flex;
        }

        .notification-dot {
            position: absolute;
            top: 10px;
            right: 12px;
            width: 14px;
            height: 14px;
            border-radius: 50%;
            background: #d92d20;
            border: 2px solid #fff;
            box-shadow: 0 0 0 2px #18192b;
            z-index: 2;
        }

        .notification-tooltip {
            position: absolute;
            bottom: calc(100% + 12px);
            left: 50%;
            transform: translateX(-50%);
            background: #22223b;
            color: #fff;
            padding: 10px 14px;
            border-radius: 10px;
            font-size: 0.92em;
            font-weight: 600;
            white-space: nowrap;
            box-shadow: 0 4px 12px rgba(0,0,0,0.18);
            opacity: 0;
            visibility: hidden;
            transition: opacity 0.2s ease;
            pointer-events: none;
            z-index: 3;
        }

        .notification-tooltip::after {
            content: "";
            position: absolute;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            border-width: 8px 7px 0 7px;
            border-style: solid;
            border-color: #22223b transparent transparent transparent;
        }

        .nav-item.has-notification:hover .notification-tooltip {
            opacity: 1;
            visibility: visible;
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
            width: 460px;
            max-width: calc(100vw - 40px);
            background: #fff;
            border: 3px solid #18192b;
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.2);
            padding: 22px;
        }

        .modal-title {
            font-size: 1.2em;
            font-weight: bold;
            color: #18192b;
            margin-bottom: 12px;
        }

        .modal-text {
            color: #444;
            line-height: 1.6;
            margin-bottom: 20px;
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
            min-width: 130px;
            height: 48px;
            padding: 0 18px;
            background: #fff;
            border: 3px solid #18192b;
            border-radius: 14px;
            color: #18192b;
            font-size: 1em;
            font-weight: 700;
            cursor: pointer;
        }

        .modal-btn:hover {
            background: #f5f6fa;
        }

       
        form {
            margin: 0;
        }
    </style>
</head>
<body data-application-open="<%= applicationOpen %>" data-show-deadline-modal="<%= showDeadlineModal %>">
    <div class="container">
       
        <div class="title">TA Recruitment System</div>
        <div class="welcome">Hi, <%= username %></div>
        <div class="desc">
            Welcome to the TA management system! Here you can find job opportunities, manage your profile, and more.
        </div>
         <div class="nav-row">
        <% if (applicationOpen && profileComplete) { %>
            <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=view_information") %>">Find a Job</a>
        <% } else { %>
            <button class="nav-btn nav-btn-disabled" type="button" onclick="openFindJobUnavailableModal()">Find a Job</button>
        <% } %>

        <div class="nav-item <%= hasUnreadReviewUpdate ? "has-notification" : "" %>">
            <a class="nav-btn" href="<%= response.encodeURL("TAclasscontroller?action=personal_centre") %>">Personal Centre</a>
            <% if (hasUnreadReviewUpdate) { %>
                <span class="notification-dot" aria-hidden="true"></span>
                <span class="notification-tooltip">Review result updated</span>
            <% } %>
        </div>
        <form action="<%= response.encodeURL("TAclasscontroller") %>" method="post" style="display:inline;">
            <input type="hidden" name="action" value="logout">
            <button class="nav-btn" type="submit">Log out</button>
        </form>

        <a class="nav-btn nav-logo" href="#">
            <img src="images/logo.png" alt="Logo" />
        </a>
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

    <script>
        const applicationOpenFlag = document.body.dataset.applicationOpen === "true";
        const showDeadlineModalFlag = document.body.dataset.showDeadlineModal === "true";

        function openFindJobUnavailableModal() {
            if (!applicationOpenFlag) {
                openDeadlinePassedModal();
                return;
            }
            openProfileIncompleteModal();
        }

        function openProfileIncompleteModal() {
            document.getElementById("profileIncompleteModal").classList.remove("hidden");
        }

        function closeProfileIncompleteModal() {
            document.getElementById("profileIncompleteModal").classList.add("hidden");
        }

        function openDeadlinePassedModal() {
            document.getElementById("deadlinePassedModal").classList.remove("hidden");
        }

        function closeDeadlinePassedModal() {
            document.getElementById("deadlinePassedModal").classList.add("hidden");
        }

        function goToProfileCenter() {
            window.location.href = '<%= response.encodeURL("TAclasscontroller?action=profile_center") %>';
        }

        if (showDeadlineModalFlag) {
            openDeadlinePassedModal();
        }
    </script>
</body>
</html>
