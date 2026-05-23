package controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.AccountService;
import service.impl.AccountServiceImpl;

/**
 * Servlet controller handling user account operations including registration and login
 * for all roles (TA, MO, Admin) in the TA Recruitment System.
 * <p>
 * This controller processes POST requests with an {@code action} parameter to determine
 * the specific operation: TA registration via BUPT email, TA login via access key,
 * MO/Admin built-in account login, and general registration/login flows.
 *
 *
 * <p>Supported actions:
 * <ul>
 *   <li>{@code RegisterTA} - register a TA using a @bupt.edu.cn email address</li>
 *   <li>{@code LoginTA} - authenticate a TA using their access key</li>
 *   <li>{@code LoginMo} - authenticate an Admin-created MO account</li>
 *   <li>{@code LoginAdmin} - authenticate a built-in Admin account</li>
 *   <li>{@code Register} - general registration (TA only)</li>
 *   <li>{@code Login} - general login with name, password, role, and email</li>
 * </ul>
 *
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 1.0
 */
public class AccountController extends HttpServlet {
    private final AccountService accountService;

    /**
     * Constructs an {@code AccountController} with a default {@link AccountServiceImpl}.
     */
    public AccountController() {
        this(new AccountServiceImpl());
    }

    /**
     * Constructs an {@code AccountController} with the specified {@link AccountService}.
     * <p>Package-private constructor used for dependency injection in unit tests.</p>
     *
     * @param accountService the account service for authentication and registration
     */
    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Handles HTTP POST requests for user registration and login operations.
     * <p>Dispatches to the appropriate handler based on the {@code action} parameter.</p>
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be processed
     * @throws IOException      if an I/O error occurs during forwarding or redirection
     */
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

    /**
     * Handles TA registration with name, password, role, and email.
     * <p>Only TA role registrations are permitted. On success, the authenticated user
     * is stored in the session and the request is forwarded to the role-specific page.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param name     the applicant's name
     * @param password the chosen password
     * @param role     the role (must be "TA")
     * @param email    the applicant's email address
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Handles general login with name, password, role, and email credentials.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param name     the applicant's name
     * @param password the provided password
     * @param role     the role identifier
     * @param email    the applicant's email address
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Handles TA registration using a BUPT email address.
     * <p>An access key is generated upon successful registration and displayed to the user.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param email    the BUPT email address for registration
     * @throws IOException      if redirection fails
     * @throws ServletException if forwarding fails
     */
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

    /**
     * Handles TA login using their unique access key.
     *
     * @param request   the {@link HttpServletRequest}
     * @param response  the {@link HttpServletResponse}
     * @param accessKey the TA's access key for authentication
     * @throws IOException if redirection fails
     */
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

    /**
     * Handles login for MO and built-in Admin accounts.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param email    the account email
     * @param password the account password
     * @param role     the role ("Mo" or "Admin")
     * @throws IOException      if redirection fails
     * @throws ServletException if forwarding fails
     */
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

    /**
     * Stores the authenticated user in a fresh session and sets the username attribute.
     * <p>The previous session, if any, is invalidated to prevent session fixation.</p>
     *
     * @param request the {@link HttpServletRequest}
     * @param user    the authenticated {@link User} to store
     */
    private void storeAuthenticatedUser(HttpServletRequest request, User user) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        String username = user.getName();
        if (username != null && !username.trim().isEmpty()) {
            session.setAttribute("username", username.trim());
        }
    }

    /**
     * Redirects the user back to the role-specific entry page with an error message.
     *
     * @param request      the {@link HttpServletRequest}
     * @param response     the {@link HttpServletResponse}
     * @param role         the role to determine the redirect target
     * @param errorMessage the error message to include in the redirect URL
     * @throws IOException if redirection fails
     */
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

    /**
     * Redirects the TA to the authentication page with an error message.
     *
     * @param request      the {@link HttpServletRequest}
     * @param response     the {@link HttpServletResponse}
     * @param errorMessage the error message to include in the redirect URL
     * @throws IOException if redirection fails
     */
    private void redirectToTaAuthWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/ta?action=auth&error=" + encodedMessage);
    }

    /**
     * Forwards or redirects the user to the appropriate page based on their role.
     * <p>MO users are redirected to their dashboard, Admin users to the admin controller,
     * and TA users to the public TA entry page.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param role     the user's role
     * @throws ServletException if forwarding fails
     * @throws IOException      if redirection fails
     */
    private void forwardByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws ServletException, IOException {
        if ("Mo".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=dashboard");
        }else if ("Admin".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/AdminController?action=dashboard");
        }else {
            response.sendRedirect(request.getContextPath() + "/ta");
        }
    }
}
