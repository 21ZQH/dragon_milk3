<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    model.User currentUser = (model.User) session.getAttribute("user");
    String username = "Admin";
    if (currentUser != null && currentUser.getName() != null && !currentUser.getName().trim().isEmpty()) {
        username = currentUser.getName();
    }

    String notice = (String) request.getAttribute("notice");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Management System</title>
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

        .dashboard-nav a {
            color: #23395d !important;
            font-size: 17px !important;
            font-weight: 700 !important;
            text-decoration: none !important;
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
            box-sizing: border-box !important;
            background: #fff !important;
            border: 1px solid #d7dee8 !important;
            border-radius: 8px !important;
            padding: 20px 24px !important;
            color: inherit !important;
            text-align: left !important;
            text-decoration: none !important;
            box-shadow: none !important;
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

        .notice-box {
            margin-bottom: 18px !important;
            padding: 12px 16px !important;
            border-radius: 8px !important;
            background: #fdeeee !important;
            color: #a12626 !important;
            border: 1px solid #efb7b7 !important;
            text-align: center !important;
            font-weight: 700 !important;
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
    <a class="dashboard-brand" href="<%= response.encodeURL("AdminController?action=dashboard") %>">Admin Management</a>
    <nav class="dashboard-nav">
        <a href="<%= response.encodeURL("AdminController?action=manage_mo") %>">MO Accounts</a>
        <a href="<%= response.encodeURL("AdminController?action=candidate_management") %>">Candidates</a>
        <a href="<%= response.encodeURL("AdminController?action=logout") %>">Log out</a>
    </nav>
</header>

<main class="dashboard-main">
    <h1 class="dashboard-title">Admin Dashboard</h1>
    <p class="dashboard-welcome">Hi, <%= username %></p>

    <% if (notice != null) { %>
        <div class="notice-box"><%= notice %></div>
    <% } %>

    <div class="dashboard-list">
        <a href="<%= response.encodeURL("AdminController?action=manage_mo") %>" class="dashboard-card">
            <div class="dashboard-card-title">MO Account and Course Assignment</div>
            <div class="dashboard-card-meta">Create MO accounts and assign course ownership.</div>
        </a>

        <a href="<%= response.encodeURL("AdminController?action=candidate_management") %>" class="dashboard-card">
            <div class="dashboard-card-title">Candidate Management</div>
            <div class="dashboard-card-meta">View registered TA candidates, their profiles, applications, and resumes.</div>
        </a>

        <a href="<%= response.encodeURL("AdminController?action=set_deadline") %>" class="dashboard-card">
            <div class="dashboard-card-title">TA Application Deadline</div>
            <div class="dashboard-card-meta">Set when TAs can no longer submit or modify applications.</div>
        </a>

        <a href="<%= response.encodeURL("AdminController?action=set_mo_deadline") %>" class="dashboard-card">
            <div class="dashboard-card-title">MO Course Modification Deadline</div>
            <div class="dashboard-card-meta">Set when MOs can no longer publish or edit course recruitment information.</div>
        </a>

        <a href="<%= response.encodeURL("AdminController?action=logout") %>" class="dashboard-card">
            <div class="dashboard-card-title">Log out</div>
            <div class="dashboard-card-meta">Leave the Admin workspace and return to the Admin login page.</div>
        </a>
    </div>
</main>
</body>
</html>
