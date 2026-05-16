package controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.AccountService;
import service.impl.AccountServiceImpl;

public class AccountController extends HttpServlet {
    private final AccountService accountService;

    public AccountController() {
        this(new AccountServiceImpl());
    }

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String email = request.getParameter("email");

        if ("RegisterTA".equalsIgnoreCase(action)) {
            handleTaRegister(request, response, email);
        } else if ("LoginTA".equalsIgnoreCase(action)) {
            handleTaLogin(request, response, password);
        } else if ("LoginMo".equalsIgnoreCase(action)) {
            handleRoleLogin(request, response, email, password, "Mo");
        } else if ("LoginAdmin".equalsIgnoreCase(action)) {
            handleRoleLogin(request, response, email, password, "Admin");
        } else if ("Register".equalsIgnoreCase(action)) {
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
        if (!"TA".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Registration is only available for TA.");
            return;
        }
        try {
            AccountService.AuthResult result = accountService.register(name, password, role, email);
            if (result.getStatus() == AccountService.AuthStatus.EMAIL_REGISTERED) {
                redirectToTaAuthWithError(request, response, "The email is already registered.");
                return;
            }
            if (result.getStatus() == AccountService.AuthStatus.UNKNOWN_ROLE) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown role");
                return;
            }

            User user = result.getUser();
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
            User user = accountService.login(password, role, email);

            if (user != null) {
                storeAuthenticatedUser(request, user);
                request.setAttribute("name", name);
                request.setAttribute("email", email);
                forwardByRole(request, response, user.getRole());
            } else {
                redirectToRoleEntryWithError(request, response, role, "Invalid password or email.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Login failed");
        }
    }

    private void handleTaRegister(HttpServletRequest request, HttpServletResponse response, String email)
            throws IOException, ServletException {
        AccountService.AuthResult result = accountService.registerTaWithBuptEmail(null, email);
        if (result.getStatus() == AccountService.AuthStatus.INVALID_EMAIL_DOMAIN) {
            redirectToTaAuthWithError(request, response, "Only @bupt.edu.cn email addresses can register.");
            return;
        }
        if (result.getStatus() == AccountService.AuthStatus.EMAIL_REGISTERED) {
            redirectToTaAuthWithError(request, response, "The email is already registered.");
            return;
        }
        storeAuthenticatedUser(request, result.getUser());
        request.setAttribute("accessKey", result.getUser().getPassword());
        request.getRequestDispatcher("/WEB-INF/views/entry/ta-key.jsp").forward(request, response);
    }

    private void handleTaLogin(HttpServletRequest request, HttpServletResponse response, String accessKey)
            throws IOException {
        User user = accountService.loginTaByAccessKey(accessKey);
        if (user == null) {
            redirectToTaAuthWithError(request, response, "Invalid TA access key.");
            return;
        }
        storeAuthenticatedUser(request, user);
        response.sendRedirect(request.getContextPath() + "/ta");
    }

    private void handleRoleLogin(HttpServletRequest request, HttpServletResponse response,
            String email, String password, String role) throws IOException, ServletException {
        User user = "Admin".equals(role)
                ? accountService.loginBuiltInAdmin(email, password)
                : accountService.loginBuiltInMo(email, password);
        if (user == null) {
            String entry = "Admin".equals(role) ? "/admin" : "/mo";
            response.sendRedirect(request.getContextPath() + entry + "?error="
                    + URLEncoder.encode("Invalid account or password.", StandardCharsets.UTF_8));
            return;
        }
        storeAuthenticatedUser(request, user);
        forwardByRole(request, response, user.getRole());
    }

    private void storeAuthenticatedUser(HttpServletRequest request, User user) {
        request.getSession().setAttribute("user", user);
        String username = user.getName();
        if (username != null && !username.trim().isEmpty()) {
            request.getSession().setAttribute("username", username.trim());
        }
    }

    private void redirectToRoleEntryWithError(HttpServletRequest request, HttpServletResponse response,
            String role, String errorMessage)
            throws IOException {
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        if ("Admin".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/admin?error=" + encodedMessage);
        } else if ("Mo".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/mo?error=" + encodedMessage);
        } else {
            response.sendRedirect(request.getContextPath() + "/ta?action=auth&error=" + encodedMessage);
        }
    }

    private void redirectToTaAuthWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/ta?action=auth&error=" + encodedMessage);
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


