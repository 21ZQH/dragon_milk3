package controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Admin;
import model.TA;
import testsupport.StoreTestSupport;

class AdminControllerTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void unauthenticatedUserRedirectsToStartHtml() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/start.html");
    }

    @Test
    void nonAdminUserGetsForbiddenError() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        TA taUser = new TA("secret", "ta@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(taUser);

        controller.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required");
    }

    @Test
    void dashboardActionForwardsToAdminDashboard() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Admin adminUser = new Admin("admin123", "admin@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void candidateManagementActionFetchesTAsAndForwards() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,");

        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Admin adminUser = new Admin("admin123", "admin@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("candidate_management");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/candidate-management.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute(eq("taList"), argThat(list -> {
            if (!(list instanceof List)) {
                return false;
            }
            List<?> taList = (List<?>) list;
            if (taList.size() != 1) {
                return false;
            }
            TA ta = (TA) taList.get(0);
            return "Alice".equals(ta.getName()) && "alice@example.com".equals(ta.getEmail());
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void viewResumeStreamsPdfForAdmin() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        System.setProperty("catalina.base", tempDir.toString());
        Path resumeFile = tempDir.resolve("webapps").resolve("SE").resolve("WEB-INF").resolve("file")
                .resolve("resume").resolve("course-1").resolve("alice_example.com.pdf");
        Files.createDirectories(resumeFile.getParent());
        Files.write(resumeFile, "fake-pdf".getBytes(StandardCharsets.UTF_8));

        Admin adminUser = new Admin("admin123", "admin@example.com");
        ByteArrayServletOutputStream outputStream = new ByteArrayServletOutputStream();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("view_resume");
        when(request.getParameter("applicantEmail")).thenReturn("alice@example.com");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(response.getOutputStream()).thenReturn(outputStream);

        controller.doGet(request, response);

        verify(response).setContentType("application/pdf");
        verify(response).setHeader("Content-Disposition", "inline; filename=\"alice_example.com.pdf\"");
        verify(response).setContentLengthLong(resumeFile.toFile().length());
        Assertions.assertArrayEquals(Files.readAllBytes(resumeFile), outputStream.toByteArray());
    }

    @Test
    void unknownActionReturnsBadRequest() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Admin adminUser = new Admin("admin123", "admin@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("UnknownActionXYZ");

        controller.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    private static final class ByteArrayServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        byte[] toByteArray() {
            return delegate.toByteArray();
        }
    }
}
