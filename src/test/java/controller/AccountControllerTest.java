package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StoreTestSupport;

/**
 * Unit tests for {@link AccountController} in the TA Recruitment system.
 * Tests cover user registration, login, session management, and error handling
 * for various account-related HTTP requests.
 *
 * @author BUPT-TA-Recruitment-Group33
 */
class AccountControllerTest {
    @TempDir
    Path tempDir;

    /**
     * Clears store overrides after each test to ensure test isolation.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Tests that registering a user with the "Mo" (Module Officer) role is forbidden
     * and results in an HTTP 403 Forbidden error, as registration is only available for TA.
     */
    @Test
    void registeringMoUserIsForbidden() throws Exception {
        StoreTestSupport.useUserStore(tempDir);
        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("action")).thenReturn("Register");
        when(request.getParameter("name")).thenReturn("Molly");
        when(request.getParameter("password")).thenReturn("secret123");
        when(request.getParameter("role")).thenReturn("Mo");
        when(request.getParameter("email")).thenReturn("mo@example.com");

        controller.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Registration is only available for TA.");
    }

    /**
     * Tests that attempting to register a user with an email that already exists
     * redirects back to the TA authentication page with an appropriate error message.
     */
    @Test
    void duplicateRegistrationRedirectsBackToTaAuthWithError() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Alice,pass123,TA,alice@example.com");

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("action")).thenReturn("Register");
        when(request.getParameter("name")).thenReturn("Alice");
        when(request.getParameter("password")).thenReturn("pass123");
        when(request.getParameter("role")).thenReturn("TA");
        when(request.getParameter("email")).thenReturn("alice@example.com");
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(response).sendRedirect("/SE/ta?action=auth&error=The+email+is+already+registered.");
    }

    /**
     * Tests that a login attempt without a specified role performs look-up by email and password,
     * creates a user session, and redirects the MO user to their dashboard.
     */
    @Test
    void loginWithoutRoleUsesEmailAndPasswordLookup() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Molly,secret123,Mo,mo@example.com");

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("Login");
        when(request.getParameter("name")).thenReturn("Molly");
        when(request.getParameter("password")).thenReturn("secret123");
        when(request.getParameter("role")).thenReturn("");
        when(request.getParameter("email")).thenReturn("mo@example.com");
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(session).setAttribute(eq("user"), argThat(value ->
                value instanceof User
                        && "Mo".equals(((User) value).getRole())
                        && "Molly".equals(((User) value).getName())));
        verify(session).setAttribute("username", "Molly");
        verify(response).sendRedirect("/SE/MOclasscontroller?action=dashboard");
    }

    /**
     * Tests that a TA login attempt correctly creates a user session with the TA role
     * and redirects the TA to their unified home page.
     */
    @Test
    void taLoginRedirectsToUnifiedTaHome() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Alice Zhang,secret123,TA,alice@example.com");

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("Login");
        when(request.getParameter("name")).thenReturn("");
        when(request.getParameter("password")).thenReturn("secret123");
        when(request.getParameter("role")).thenReturn("TA");
        when(request.getParameter("email")).thenReturn("alice@example.com");
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(session).setAttribute(eq("user"), argThat(value ->
                value instanceof User
                        && "TA".equals(((User) value).getRole())
                        && "Alice Zhang".equals(((User) value).getName())));
        verify(session).setAttribute("username", "Alice Zhang");
        verify(response).sendRedirect("/SE/ta");
    }

    /**
     * Tests that a successful login invalidates any existing session before creating
     * a new one and storing the authenticated user. This ensures session fixation prevention.
     */
    @Test
    void successfulLoginInvalidatesPreviousSessionBeforeStoringUser() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Alice Zhang,secret123,TA,alice@example.com");

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("Login");
        when(request.getParameter("name")).thenReturn("");
        when(request.getParameter("password")).thenReturn("secret123");
        when(request.getParameter("role")).thenReturn("TA");
        when(request.getParameter("email")).thenReturn("alice@example.com");
        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(oldSession).invalidate();
        verify(newSession).setAttribute(eq("user"), argThat(value ->
                value instanceof User
                        && "TA".equals(((User) value).getRole())
                        && "Alice Zhang".equals(((User) value).getName())));
        verify(newSession).setAttribute("username", "Alice Zhang");
        verify(response).sendRedirect("/SE/ta");
    }

    /**
     * Tests that a login attempt with invalid credentials (wrong password or non-existent email)
     * redirects back to the TA authentication page with an error message.
     */
    @Test
    void invalidLoginRedirectsBackToTaAuthWithError() throws Exception {
        StoreTestSupport.useUserStore(tempDir);

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("action")).thenReturn("Login");
        when(request.getParameter("name")).thenReturn("");
        when(request.getParameter("password")).thenReturn("wrong");
        when(request.getParameter("role")).thenReturn("TA");
        when(request.getParameter("email")).thenReturn("missing@example.com");
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(response).sendRedirect("/SE/ta?action=auth&error=Invalid+password+or+email.");
    }

    /**
     * Tests that an unknown action parameter results in an HTTP 400 Bad Request error.
     */
    @Test
    void unknownActionReturnsBadRequest() throws Exception {
        StoreTestSupport.useUserStore(tempDir);

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("action")).thenReturn("Delete");

        controller.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }
}
