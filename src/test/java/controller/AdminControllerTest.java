package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Admin;
import model.Course;
import model.Mo;
import model.TA;
import store.CourseStore;
import store.DeadlineStore;
import store.UserStore;
import testsupport.StoreTestSupport;

class AdminControllerTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
        System.clearProperty("MO_COURSE_DRAFT_AI_PROVIDER");
    }

    @Test
    void unauthenticatedUserRedirectsToAdminEntry() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/admin");
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
            if (!(list instanceof List<?> taList) || taList.size() != 1) {
                return false;
            }
            TA ta = (TA) taList.get(0);
            return "Alice".equals(ta.getName()) && "alice@example.com".equals(ta.getEmail());
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void createMoCreatesAccountAndAssignedCourses() throws Exception {
        System.setProperty("MO_COURSE_DRAFT_AI_PROVIDER", "mock");
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);

        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("create_mo");
        when(request.getParameter("account")).thenReturn("mo-one");
        when(request.getParameter("password")).thenReturn("secret");
        when(request.getParameter("courseNames")).thenReturn("Software Engineering\nDatabase Systems");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/mo-management.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        List<Course> courses = CourseStore.getCourseList();
        assertEquals(2, courses.size());
        assertTrue(courses.stream().noneMatch(Course::isRecruitmentPublished));
        assertTrue(courses.stream().allMatch(course -> course.getJobDescription().contains(course.getCourseName())));

        Mo mo = (Mo) UserStore.validateUser("secret", "Mo", "mo-one");
        Assertions.assertNotNull(mo);
        assertEquals("mo-one", mo.getName());
        assertEquals(2, mo.getOwnedCourses().size());
        assertTrue(Files.readString(usersFile).contains("mo-one,secret,Mo,mo-one,"));
        assertTrue(Files.exists(courseFile));
        verify(request).setAttribute("success", "MO account created successfully.");
        verify(request).setAttribute(eq("generatedCourseDrafts"), argThat(value ->
                value instanceof List<?> drafts && drafts.size() == 2));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void setDeadlinePageForwardsWithSavedDeadline() throws Exception {
        AdminController controller = new AdminController();
        ServletContext servletContext = mock(ServletContext.class);
        initController(controller, servletContext);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");
        LocalDateTime deadline = LocalDateTime.of(2026, 4, 20, 18, 0);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("set_deadline");
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(deadline);
        when(request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("savedDeadline", deadline);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void saveDeadlineStoresValueInServletContextAndForwardsSuccess() throws Exception {
        Path deadlineFile = StoreTestSupport.useApplicationDeadlineStore(tempDir);

        AdminController controller = new AdminController();
        ServletContext servletContext = mock(ServletContext.class);
        initController(controller, servletContext);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");
        LocalDateTime expectedDeadline = LocalDateTime.of(2026, 4, 18, 23, 45);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("save_deadline");
        when(request.getParameter("deadlineDate")).thenReturn("2026-04-18");
        when(request.getParameter("deadlineTime")).thenReturn("23:45");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(servletContext).setAttribute("applicationDeadline", expectedDeadline);
        verify(request).setAttribute("success", "TA resume submission deadline has been saved successfully.");
        verify(request).setAttribute("savedDeadline", expectedDeadline);
        verify(dispatcher).forward(request, response);
        assertEquals(expectedDeadline, DeadlineStore.getDeadline());
        assertTrue(Files.exists(deadlineFile));
    }

    @Test
    void saveDeadlineRejectsBlankInput() throws Exception {
        AdminController controller = new AdminController();
        ServletContext servletContext = mock(ServletContext.class);
        initController(controller, servletContext);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("save_deadline");
        when(request.getParameter("deadlineDate")).thenReturn("");
        when(request.getParameter("deadlineTime")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute("error", "Please complete both deadline date and deadline time.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void setMoDeadlinePageForwardsWithSavedDeadline() throws Exception {
        AdminController controller = new AdminController();
        ServletContext servletContext = mock(ServletContext.class);
        initController(controller, servletContext);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");
        LocalDateTime deadline = LocalDateTime.of(2026, 4, 30, 12, 30);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("set_mo_deadline");
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(deadline);
        when(request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("savedMoDeadline", deadline);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void saveMoDeadlineStoresValueInServletContextAndForwardsSuccess() throws Exception {
        Path deadlineFile = StoreTestSupport.useMoDeadlineStore(tempDir);

        AdminController controller = new AdminController();
        ServletContext servletContext = mock(ServletContext.class);
        initController(controller, servletContext);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");
        LocalDateTime expectedDeadline = LocalDateTime.of(2026, 5, 1, 9, 15);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("save_mo_deadline");
        when(request.getParameter("deadlineDate")).thenReturn("2026-05-01");
        when(request.getParameter("deadlineTime")).thenReturn("09:15");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(servletContext).setAttribute("moCourseModifyDeadline", expectedDeadline);
        verify(request).setAttribute("success", "MO course modification deadline has been saved successfully.");
        verify(request).setAttribute("savedMoDeadline", expectedDeadline);
        verify(dispatcher).forward(request, response);
        assertEquals(expectedDeadline, DeadlineStore.getMoModifyDeadline());
        assertTrue(Files.exists(deadlineFile));
    }

    @Test
    void adminLogoutInvalidatesSessionAndRedirectsToAdminEntry() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Admin adminUser = new Admin("admin123", "admin@example.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/SE/admin");
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

    private void initController(AdminController controller, ServletContext servletContext) throws ServletException {
        ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(servletContext);
        controller.init(config);
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
