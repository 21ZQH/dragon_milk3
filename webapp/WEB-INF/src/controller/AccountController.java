package controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Admin;
import model.Mo;
import model.TA;
import model.User;
import store.UserStore;

public class AccountController extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String email = request.getParameter("email");

        if ("Register".equalsIgnoreCase(action)) {
            handleRegister(request, response, name, password, role, email);
        } else if ("Login".equalsIgnoreCase(action)) {
            handleLogin(request, response, name, password, role, email);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response,
            String name, String password, String role, String email)
            throws ServletException, IOException {
        try {
            if (UserStore.isEmailRegistered(email)) {
                redirectToStartWithError(request, response, "The email is already registered.");
                return;
            }

            User user = buildUser(role, password, email, response);
            if (user == null) {
                return;
            }

            if (name != null && !name.trim().isEmpty()) {
                user.setName(name.trim());
            }

            UserStore.saveUser(user);
            storeAuthenticatedUser(request, user);
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            forwardByRole(request, response, user.getRole());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Registration failed");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response,
            String name, String password, String role, String email)
            throws ServletException, IOException {
        try {
            User user;
            if (role == null || role.trim().isEmpty()) {
                user = UserStore.validateUser(password, email);
            } else {
                user = UserStore.validateUser(password, role, email);
            }

            if (user != null) {
                storeAuthenticatedUser(request, user);
                request.setAttribute("name", name);
                request.setAttribute("email", email);
                forwardByRole(request, response, user.getRole());
            } else {
                redirectToStartWithError(request, response, "Invalid password or email.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Login failed");
        }
    }

    private User buildUser(String role, String password, String email, HttpServletResponse response)
            throws IOException {
        if ("TA".equals(role)) {
            return new TA(password, email);
        }
        if ("Admin".equals(role)) {
            return new Admin(password, email);
        }
        if ("Mo".equals(role)) {
            return new Mo(password, email);
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown role");
        return null;
    }

    private void storeAuthenticatedUser(HttpServletRequest request, User user) {
        request.getSession().setAttribute("user", user);
        String username = user.getName();
        if (username != null && !username.trim().isEmpty()) {
            request.getSession().setAttribute("username", username.trim());
        }
    }

    private void redirectToStartWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/start.html?error=" + encodedMessage);
    }

    private void forwardByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws ServletException, IOException {
        if ("Mo".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=dashboard");
        }else if ("Admin".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/AdminController?action=dashboard");
        }else {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=home");
        }
    }
}


