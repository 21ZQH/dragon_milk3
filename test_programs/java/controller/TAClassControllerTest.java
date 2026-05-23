package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import store.UserStore;
import store.ApplicationFormStore;
import testsupport.StoreTestSupport;

/**
 * Unit tests for {@link TAClassController} in the TA Recruitment system.
 * Tests cover course information viewing, resume upload, application form generation,
 * personal centre management, withdrawal of applications, and various error/edge cases.
 *
 * @author BUPT-TA-Recruitment-Group33
 */
class TAClassControllerTest {
    @TempDir
    Path tempDir;

    /**
     * A helper servlet output stream that captures written bytes into a byte array for verification.
     */
    private static class CapturingServletOutputStream extends ServletOutputStream {
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
            // no-op for test
        }

        byte[] toByteArray() {
            return delegate.toByteArray();
        }
    }

    /**
     * Clears store overrides and system properties after each test to ensure test isolation.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
        System.clearProperty(ApplicationFormStore.FILE_PATH_PROPERTY);
    }

    /**
     * Tests that the "view_information" action redirects the TA to the unified TA home page.
     */
    @Test
    void viewInformationRedirectsToUnifiedTaHome() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                "Java Programming,TA,8 hours/week,TBD,Mark labs,Java basics");

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        TA ta = new TA("secret123", "ta@example.com");

        when(request.getParameter("action")).thenReturn("view_information");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/ta");
    }

    /**
     * Tests that the "show_all_information" action sets the selected course from the session
     * course list based on the provided index and forwards to the specific-class JSP page.
     */
    @Test
    void showAllInformationSetsSelectedCourseAndForwards() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        List<Course> courses = List.of(
                new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"),
                new Course("Java Programming", "TA", "8 hours/week", "TBD", "Mark labs", "Java basics"));

        when(request.getParameter("action")).thenReturn("show_all_information");
        when(request.getParameter("courseIndex")).thenReturn("1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute(eq("selectedCourse"), eq(courses.get(1)));
        verify(request).setAttribute("courseIndex", "1");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "show_all_information" action redirects back to view_information
     * when no course index parameter is provided.
     */
    @Test
    void showAllInformationRedirectsWhenCourseIndexMissing() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        TA ta = new TA("secret123", "ta@example.com");

        List<Course> courses = List.of(
                new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("show_all_information");
        when(request.getParameter("courseIndex")).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/TAclasscontroller?action=view_information");
    }

    /**
     * Tests that the "home" action redirects the TA to the unified TA home page.
     */
    @Test
    void homeActionRedirectsToUnifiedTaHome() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        TA ta = new TA("secret123", "ta@example.com");

        when(request.getParameter("action")).thenReturn("home");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/ta");
    }

    /**
     * Tests that the "logout" action invalidates the current session
     * and redirects the TA to the TA entry page.
     */
    @Test
    void logoutActionInvalidatesSessionAndRedirectsToTaEntry() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("logout");
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/SE/ta");
    }

    /**
     * Tests that the "go_apply" action sets the selected course from the session
     * course list based on the provided index and forwards to the application JSP page.
     */
    @Test
    void goApplySetsSelectedCourseAndForwards() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        List<Course> courses = List.of(
                new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("go_apply");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute(eq("selectedCourse"), eq(courses.get(0)));
        verify(request).setAttribute("courseIndex", "0");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "go_apply" action sets the current resume attributes when the TA
     * already has a submitted application form for the selected course, including
     * the resume availability flag and file name.
     */
    @Test
    void goApplyWithSubmittedFormSetsCurrentApplicationAttributes() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        List<Course> courses = List.of(course);
        TA ta = new TA("secret123", "ta@example.com");

        Path resumeDirectory = tempDir.resolve("resume").resolve("master");
        Files.createDirectories(resumeDirectory);
        Files.write(resumeDirectory.resolve("ta_example.com.pdf"), "pdf".getBytes());
        ta.setMasterResumeDirectory(resumeDirectory.toString());
        ta.addOrUpdateApplication(course, course.getId());

        when(request.getParameter("action")).thenReturn("go_apply");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("hasCurrentResume", true);
        verify(request).setAttribute("currentResumeFileName", "Submitted application form");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that uploading a resume without selecting a file shows an error
     * on the application page and does not proceed with the upload.
     */
    @Test
    void uploadResumeWithoutFileShowsErrorOnApplicationPage() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getPart("resumeFile")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute(eq("selectedCourse"), eq(course));
        verify(request).setAttribute("courseIndex", "0");
        verify(request).setAttribute("error", "Please upload your resume before submitting.");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that uploading a non-PDF file is rejected with an appropriate error message.
     */
    @Test
    void uploadResumeRejectsNonPdfFile() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Part resumePart = mock(Part.class);

        Course course = new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getPart("resumeFile")).thenReturn(resumePart);
        when(resumePart.getSize()).thenReturn(128L);
        when(resumePart.getSubmittedFileName()).thenReturn("resume.docx");
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute(eq("selectedCourse"), eq(course));
        verify(request).setAttribute("courseIndex", "0");
        verify(request).setAttribute("error", "Only PDF resumes are accepted.");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that uploading a resume is rejected when the TA already has a master resume
     * directory set, requiring the TA to replace their resume from the Personal Centre instead.
     */
    @Test
    void uploadResumeRejectsReplacementWhenMasterResumeAlreadyExists() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Part resumePart = mock(Part.class);

        Course course = new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        ta.setMasterResumeDirectory("D:\\resume\\master");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getPart("resumeFile")).thenReturn(resumePart);
        when(resumePart.getSize()).thenReturn(128L);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertEquals("D:\\resume\\master", ta.getMasterResumeDirectory());
        assertEquals(0, ta.getResumeSubmissions().size());
        verify(request).setAttribute(eq("selectedCourse"), eq(course));
        verify(request).setAttribute("courseIndex", "0");
        verify(request).setAttribute("error", "Please replace your resume from Personal Centre.");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that a non-TA user attempting to upload a resume receives an HTTP 403 Forbidden error.
     */
    @Test
    void uploadResumeRedirectsWhenCurrentUserIsNotTa() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Course course = new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(new Mo("secret123", "mo@example.com"));

        controller.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: TA role required");
    }

    /**
     * Tests that a non-TA session accessing the "generate_application_form" action
     * is redirected to the TA authentication page.
     */
    @Test
    void generateApplicationFormRedirectsNonTaSessionToTaLogin() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("generate_application_form");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Mo("secret123", "mo@example.com"));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/ta?action=auth");
    }

    /**
     * Tests that uploading a resume successfully stores the resume directory path against
     * the course, persists the TA user data, creates an application form, and forwards
     * to the application form page.
     */
    @Test
    void uploadResumeStoresResumeDirectoryAgainstCourseAndPersistsUserData() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        Path formsFile = tempDir.resolve("application-forms.txt");
        System.setProperty(ApplicationFormStore.FILE_PATH_PROPERTY, formsFile.toString());
        System.setProperty("catalina.base", tempDir.toString());

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(usersFile, "Alice,secret123,TA,ta@example.com,School of Software,Java,,");

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Part resumePart = mock(Part.class);

        TA ta = new TA("secret123", "ta@example.com");
        ta.setName("Alice");
        ta.setCollege("School of Software");
        ta.setSkill("Java");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getPart("resumeFile")).thenReturn(resumePart);
        when(resumePart.getSize()).thenReturn(128L);
        when(resumePart.getSubmittedFileName()).thenReturn("resume.pdf");
        doNothing().when(resumePart).write(org.mockito.ArgumentMatchers.anyString());
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application-form.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertEquals(0, ta.getAppliedClasses().size());
        assertEquals(0, ta.getResumeSubmissions().size());
        assertEquals(
                tempDir.resolve("webapps").resolve("SE").resolve("WEB-INF").resolve("file").resolve("resume").resolve("master").toString(),
                ta.getMasterResumeDirectory());
        assertEquals(
                "Alice,secret123,TA,ta@example.com,School of Software,Java,,,"
                        + ta.getMasterResumeDirectory(),
                Files.readAllLines(usersFile).get(0));
        ApplicationForm form = ApplicationFormStore.findForm("ta@example.com", "course-1");
        assertNotNull(form);
        assertEquals("ta@example.com", form.getEmail());
        assertFalse(form.isSubmitted());
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "personal_centre" action forwards to the personal centre page
     * with the TA's applied courses, selected course details, application status,
     * and the application open flag set to true.
     */
    @Test
    void personalCentreForwardsWithAppliedCourses() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        TA ta = new TA("secret123", "ta@example.com");
        ta.addClass(course);

        when(request.getParameter("action")).thenReturn("personal_centre");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("appliedCourses", ta.getAppliedClasses());
        verify(request).setAttribute("selectedCourse", course);
        verify(request).setAttribute("selectedCourseId", "course-1");
        verify(request).setAttribute("applicationOpen", true);
        verify(request).setAttribute("selectedStatus", ResumeSubmission.STATUS_PENDING);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the personal centre page marks applications as closed when the
     * application deadline has passed.
     */
    @Test
    void personalCentreMarksApplicationsClosedAfterDeadline() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletContext servletContext = mock(ServletContext.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        ta.addClass(course);

        when(request.getParameter("action")).thenReturn("personal_centre");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("applicationOpen", false);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that accessing the personal centre clears any unread review update flags
     * for the TA, persists this change to the user store, and updates the session attribute.
     */
    @Test
    void personalCentreClearsUnreadReviewUpdatesAndPersistsThem() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@1@true");

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        TA ta = (TA) UserStore.validateUser("secret123", "ta@example.com");
        assertTrue(ta.hasUnreadReviewUpdates());

        when(request.getParameter("action")).thenReturn("personal_centre");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        assertFalse(ta.hasUnreadReviewUpdates());
        assertEquals("Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@1@false",
                Files.readAllLines(usersFile).get(0));
        verify(session).setAttribute("user", ta);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that viewing the master resume streams the PDF file inline with the
     * correct content type and Content-Disposition header.
     */
    @Test
    void viewMasterResumeStreamsPdfInline() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("resume").resolve("master");
        Files.createDirectories(resumeDirectory);
        byte[] pdfBytes = "%PDF-test".getBytes();
        Files.write(resumeDirectory.resolve("ta_example.com.pdf"), pdfBytes);
        ta.setMasterResumeDirectory(resumeDirectory.toString());

        CapturingServletOutputStream outputStream = new CapturingServletOutputStream();

        when(request.getParameter("action")).thenReturn("view_master_resume");
        when(request.getParameter("download")).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(response.getOutputStream()).thenReturn(outputStream);

        controller.doGet(request, response);

        verify(response).setContentType("application/pdf");
        verify(response).setHeader("Content-Disposition", "inline; filename=\"ta_example.com.pdf\"");
        assertArrayEquals(pdfBytes, outputStream.toByteArray());
    }

    /**
     * Tests that viewing the master resume with a "download=true" parameter
     * streams the PDF as an attachment (download) rather than inline display.
     */
    @Test
    void viewMasterResumeStreamsPdfAsAttachmentWhenDownloadTrue() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("resume").resolve("master");
        Files.createDirectories(resumeDirectory);
        Files.write(resumeDirectory.resolve("ta_example.com.pdf"), "pdf".getBytes());
        ta.setMasterResumeDirectory(resumeDirectory.toString());

        CapturingServletOutputStream outputStream = new CapturingServletOutputStream();

        when(request.getParameter("action")).thenReturn("view_master_resume");
        when(request.getParameter("download")).thenReturn("true");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(response.getOutputStream()).thenReturn(outputStream);

        controller.doGet(request, response);

        verify(response).setHeader("Content-Disposition", "attachment; filename=\"ta_example.com.pdf\"");
    }

    /**
     * Tests that the "go_apply_by_id" action resolves the course index from the course ID,
     * sets the selected course as a request attribute, and forwards to the application page.
     */
    @Test
    void goApplyByIdResolvesCourseIndexAndForwardsToApplication() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        List<Course> courses = List.of(
                new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"),
                new Course("course-2", "Java Programming", "TA", "8 hours/week", "TBD", "Mark labs", "Java basics"));

        when(request.getParameter("action")).thenReturn("go_apply_by_id");
        when(request.getParameter("courseId")).thenReturn("course-2");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute(eq("selectedCourse"), eq(courses.get(1)));
        verify(request).setAttribute("courseIndex", "1");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "go_apply_by_id" action shows an error and sets applications as closed
     * when the application deadline has already passed.
     */
    @Test
    void goApplyByIdAfterDeadlineForwardsToPersonalCentreWithError() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletContext servletContext = mock(ServletContext.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        ta.addClass(course);

        when(request.getParameter("action")).thenReturn("go_apply_by_id");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(List.of(course));
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("error", "The application deadline has passed. You can no longer submit or modify applications.");
        verify(request).setAttribute("applicationOpen", false);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that withdrawing an application removes all TA and course references,
     * persists the updated user data to the store, and forwards to the personal centre page.
     */
    @Test
    void withdrawApplicationRemovesTaAndCourseReferencesAndPersistsUserData() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        ta.setName("Alice");
        ta.setCollege("School of Software");
        ta.setSkill("Java");
        ta.addOrUpdateResume(course, "D:\\resume\\course-1");
        course.addApplication(ta, "D:\\resume\\course-1");
        UserStore.saveUser(ta);

        when(request.getParameter("action")).thenReturn("withdraw_application");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(List.of(course));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertEquals(0, ta.getAppliedClasses().size());
        assertEquals(0, ta.getResumeSubmissions().size());
        assertNull(ta.getApplicationFormIdForCourse("course-1"));
        assertEquals(0, course.getTaApplicants().size());
        assertEquals(0, course.getApplicantFormIds().size());
        assertEquals("Alice,secret123,TA,ta@example.com,School of Software,Java,,", Files.readAllLines(usersFile).get(0));
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that withdrawing an application preserves the master resume file on disk
     * even after removing the course-specific references.
     */
    @Test
    void withdrawApplicationKeepsMasterResumeFileOnDisk() throws Exception {
        StoreTestSupport.useUserStore(tempDir);

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("resume").resolve("course-1");
        Files.createDirectories(resumeDirectory);
        Path resumeFile = resumeDirectory.resolve("ta_example.com.pdf");
        Files.write(resumeFile, "pdf".getBytes());

        ta.setMasterResumeDirectory(resumeDirectory.toString());
        ta.addOrUpdateApplication(course, course.getId());
        course.addApplication(ta, course.getId());

        when(request.getParameter("action")).thenReturn("withdraw_application");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(List.of(course));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertTrue(Files.exists(resumeFile));
        assertNull(ta.getApplicationFormIdForCourse("course-1"));
        verify(request).setAttribute("success", "Application withdrawn successfully.");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that withdrawing an application from the course detail page returns
     * to the course detail page with the selected course and a success message.
     */
    @Test
    void withdrawApplicationFromCourseDetailReturnsToCourseDetail() throws Exception {
        StoreTestSupport.useUserStore(tempDir);

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        ta.addOrUpdateApplication(course, course.getId());
        course.addApplication(ta, course.getId());

        when(request.getParameter("action")).thenReturn("withdraw_application");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getParameter("returnTo")).thenReturn("course_detail");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(List.of(course));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertNull(ta.getApplicationFormIdForCourse("course-1"));
        verify(request).setAttribute("success", "Application withdrawn successfully.");
        verify(request).setAttribute("selectedCourse", course);
        verify(request).setAttribute("courseIndex", "0");
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that withdrawing an application after the deadline has passed
     * is rejected with an error message, and the application data is preserved.
     */
    @Test
    void withdrawApplicationAfterDeadlineDoesNotRemoveApplication() throws Exception {
        StoreTestSupport.useUserStore(tempDir);

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletContext servletContext = mock(ServletContext.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        ta.addOrUpdateApplication(course, course.getId());
        course.addApplication(ta, course.getId());

        when(request.getParameter("action")).thenReturn("withdraw_application");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals("course-1", ta.getApplicationFormIdForCourse("course-1"));
        verify(request).setAttribute("error", "The application deadline has passed. You can no longer withdraw or modify applications.");
        verify(dispatcher).forward(request, response);
    }
}
