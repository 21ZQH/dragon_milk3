<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Boolean moModifyOpenAttr = (Boolean) request.getAttribute("moModifyOpen");
    boolean moModifyOpen = moModifyOpenAttr == null ? true : moModifyOpenAttr.booleanValue();
    Boolean showModifyLockedModalAttr = (Boolean) request.getAttribute("showModifyLockedModal");
    boolean showModifyLockedModal = showModifyLockedModalAttr != null && showModifyLockedModalAttr.booleanValue();
%>
<!DOCTYPE html>
<html>
<head>
    <title>MO Dashboard</title>
    <style>
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        .container {
            background: #fff;
            width: 100%;
            max-width: 850px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 40px 40px 50px 40px;
            box-sizing: border-box;
        }

        .title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 15px;
            text-align: center;
        }

        .desc {
            color: #444;
            font-size: 1.1em;
            text-align: center;
            line-height: 1.6;
            margin-bottom: 45px;
        }

        .nav-row {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 30px;
            flex-wrap: wrap;
            width: 100%;
            margin-bottom: 30px;
        }

        .logo-row {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
        }

        .nav-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 200px;
            height: 80px;
            padding: 0;
            background: #fff;
            border: 3px solid #18192b;
            border-radius: 20px;
            font-size: 1.2em;
            font-weight: 700;
            color: #18192b;
            text-decoration: none;
            font-family: 'Segoe UI', Arial, sans-serif;
            box-shadow: 0 2px 8px rgba(24,25,43,0.06);
            transition: box-shadow 0.2s, border-color 0.2s, background 0.2s;
            cursor: pointer;
            overflow: hidden;
            text-align: center;
        }

        .nav-btn:hover {
            box-shadow: 0 4px 16px rgba(24,25,43,0.12);
            border-color: #3b3e5b;
            background: #f5f6fa;
        }

        .nav-btn-disabled {
            color: #9ea3b0;
            background: #fff;
        }

        .nav-btn-disabled:hover {
            border-color: #18192b;
            background: #fff;
            box-shadow: 0 2px 8px rgba(24,25,43,0.06);
        }

        .nav-logo img {
            width: 80%;
            height: 80%;
            object-fit: contain;
            display: block;
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
            font-weight: 700;
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
<body data-show-modify-locked-modal="<%= showModifyLockedModal %>">
    <div class="container">
        <div class="title">MO management system</div>

        <div class="desc">
            Welcome to the MO management system!<br>
            Here you can create new project, manage your profile, and more.
        </div>

        <div class="nav-row">
            <% if (moModifyOpen) { %>
                <a class="nav-btn" href="<%= response.encodeURL("MOclasscontroller?action=create_class") %>">
                    Create new project
                </a>
            <% } else { %>
                <button class="nav-btn nav-btn-disabled" type="button" onclick="openMoModifyLockedModal()">
                    Create new project
                </button>
            <% } %>

            <a class="nav-btn" href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">
                Personal centre
            </a>

            <a class="nav-btn" href="<%= response.encodeURL("MOclasscontroller?action=logout") %>">
                Log out
            </a>
        </div>

        <div class="logo-row">
            <a class="nav-btn nav-logo" href="#">
                <img src="images/logo.png" alt="Logo" />
            </a>
        </div>
    </div>

    <div id="moModifyLockedModal" class="modal-overlay hidden" role="dialog" aria-modal="true" aria-labelledby="moModifyLockedTitle">
        <div class="modal-box">
            <div class="modal-title" id="moModifyLockedTitle">Course Modification Closed</div>
            <div class="modal-text">The deadline for MO to create or modify course information has passed.</div>
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
