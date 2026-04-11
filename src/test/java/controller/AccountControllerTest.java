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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StoreTestSupport;

class AccountControllerTest {
    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void registeringMoUserSavesAccountAndForwardsToMoDashboard() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("action")).thenReturn("Register");
        when(request.getParameter("name")).thenReturn("Molly");
        when(request.getParameter("password")).thenReturn("secret123");
        when(request.getParameter("role")).thenReturn("Mo");
        when(request.getParameter("email")).thenReturn("mo@example.com");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(session).setAttribute(eq("user"), argThat(value ->
                value instanceof User
                        && "Mo".equals(((User) value).getRole())
                        && "mo@example.com".equals(((User) value).getEmail())));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());

        assertTrue(Files.exists(usersFile));
        assertEquals(1, Files.readAllLines(usersFile).size());
    }

    @Test
    void duplicateRegistrationShowsErrorOnTaHomePage() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Alice,pass123,TA,alice@example.com");

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("action")).thenReturn("Register");
        when(request.getParameter("name")).thenReturn("Alice");
        when(request.getParameter("password")).thenReturn("pass123");
        when(request.getParameter("role")).thenReturn("TA");
        when(request.getParameter("email")).thenReturn("alice@example.com");
        when(request.getRequestDispatcher("/WEB-INF/views/ta/home.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute("error", "the email is already registered");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void loginWithoutRoleUsesEmailAndPasswordLookup() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Molly,secret123,Mo,mo@example.com");

        AccountController controller = new AccountController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("action")).thenReturn("Login");
        when(request.getParameter("name")).thenReturn("Molly");
        when(request.getParameter("password")).thenReturn("secret123");
        when(request.getParameter("role")).thenReturn("");
        when(request.getParameter("email")).thenReturn("mo@example.com");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(session).setAttribute(eq("user"), argThat(value ->
                value instanceof User
                        && "Mo".equals(((User) value).getRole())
                        && "Molly".equals(((User) value).getName())));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void taLoginRedirectsThroughTaControllerHome() throws Exception {
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
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(session).setAttribute(eq("user"), argThat(value ->
                value instanceof User
                        && "TA".equals(((User) value).getRole())
                        && "Alice Zhang".equals(((User) value).getName())));
        verify(session).setAttribute("username", "Alice Zhang");
        verify(response).sendRedirect("/SE/TAclasscontroller?action=home");
    }

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
