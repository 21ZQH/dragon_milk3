package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import store.UserStore;
import org.mockito.ArgumentCaptor;
import testsupport.StoreTestSupport;

class TAClassControllerTest {
    @TempDir
    Path tempDir;

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

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void viewInformationLoadsCourseListIntoSessionAndForwards() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                "Java Programming,TA,8 hours/week,TBD,Mark labs,Java basics");

        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        when(request.getParameter("action")).thenReturn("view_information");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/job-list.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        ArgumentCaptor<Object> courseListCaptor = ArgumentCaptor.forClass(Object.class);
        verify(session).setAttribute(eq("courseList"), courseListCaptor.capture());
        Object capturedValue = courseListCaptor.getValue();
        assertInstanceOf(List.class, capturedValue);

        @SuppressWarnings("unchecked")
        List<Course> courses = (List<Course>) capturedValue;

        assertEquals(2, courses.size());
        assertEquals("Software Engineering", courses.get(0).getCourseName());
        verify(dispatcher).forward(request, response);
    }

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

    @Test
    void homeActionForwardsToTaHomePage() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        when(request.getParameter("action")).thenReturn("home");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/home.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void logoutActionInvalidatesSessionAndRedirectsToStartPage() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("logout");
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/SE/start.html");
    }

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

    @Test
    void personalCenterForwardsForTaUser() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        when(request.getParameter("action")).thenReturn("profile_center");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/profile-center.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void editSkillActionForwardsToEditSkillPage() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TA ta = new TA("secret123", "ta@example.com");

        when(request.getParameter("action")).thenReturn("edit_skill");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/edit-skill.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void goApplyWithExistingResumeSetsCurrentResumeAttributes() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        List<Course> courses = List.of(course);
        TA ta = new TA("secret123", "ta@example.com");

        Path resumeDirectory = tempDir.resolve("resume").resolve("course-1");
        Files.createDirectories(resumeDirectory);
        Files.write(resumeDirectory.resolve("ta_example.com.pdf"), "pdf".getBytes());
        ta.addOrUpdateResume(course, resumeDirectory.toString());

        when(request.getParameter("action")).thenReturn("go_apply");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("hasCurrentResume", true);
        verify(request).setAttribute("currentResumeFileName", "ta_example.com.pdf");
        verify(dispatcher).forward(request, response);
    }

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

    @Test
    void uploadResumeRedirectsWhenCurrentUserIsNotTa() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Course course = new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(new Mo("secret123", "mo@example.com"));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/home.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute("error", "Login has expired. Please log in again.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void uploadResumeStoresResumeDirectoryAgainstCourseAndPersistsUserData() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
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
        when(request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals(1, ta.getResumeSubmissions().size());
        assertEquals(course, ta.getResumeSubmissions().get(0).getCourse());
        assertNotNull(ta.getResumeDirectoryForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_PENDING, ta.getResumeStatusForCourse("course-1"));
        assertEquals(
                tempDir.resolve("webapps").resolve("SE").resolve("WEB-INF").resolve("file").resolve("resume").resolve("course-1").toString(),
                ta.getResumeDirectoryForCourse("course-1"));
        assertEquals(
                "Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@"
                        + ta.getResumeDirectoryForCourse("course-1") + "@0@false",
                Files.readAllLines(usersFile).get(0));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void savePersonalInformationUpdatesSessionAndForwards() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1");

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
        ta.addClass(course);
        ta.addOrUpdateResume(course, "D:\\resume\\course-1");

        when(request.getParameter("action")).thenReturn("save_personal_information");
        when(request.getParameter("name")).thenReturn("  Alice Zhang  ");
        when(request.getParameter("college")).thenReturn("  New College  ");
        when(request.getParameterValues("skill")).thenReturn(new String[] {"Java", "Python", "SQL"});
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/profile-center.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(session).setAttribute("user", ta);
        verify(session).setAttribute("username", "Alice Zhang");
        verify(request).setAttribute("success", "Personal information saved successfully.");
        verify(dispatcher).forward(request, response);
        assertEquals("Alice Zhang,secret123,TA,ta@example.com,New College,Java  Python  SQL,course-1,course-1@D:\\resume\\course-1@0@false",
                Files.readAllLines(usersFile).get(0));
    }

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

    @Test
    void personalCentreClearsUnreadReviewUpdatesAndPersistsThem() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@1@true");

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
        assertEquals("Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@1@false",
                Files.readAllLines(usersFile).get(0));
        verify(session).setAttribute("user", ta);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void viewResumeStreamsPdfInline() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("resume").resolve("course-1");
        Files.createDirectories(resumeDirectory);
        byte[] pdfBytes = "%PDF-test".getBytes();
        Files.write(resumeDirectory.resolve("ta_example.com.pdf"), pdfBytes);
        ta.addOrUpdateResume(course, resumeDirectory.toString());

        CapturingServletOutputStream outputStream = new CapturingServletOutputStream();

        when(request.getParameter("action")).thenReturn("view_resume");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getParameter("download")).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(response.getOutputStream()).thenReturn(outputStream);

        controller.doGet(request, response);

        verify(response).setContentType("application/pdf");
        verify(response).setHeader("Content-Disposition", "inline; filename=\"ta_example.com.pdf\"");
        assertArrayEquals(pdfBytes, outputStream.toByteArray());
    }

    @Test
    void viewResumeStreamsPdfAsAttachmentWhenDownloadTrue() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("resume").resolve("course-1");
        Files.createDirectories(resumeDirectory);
        Files.write(resumeDirectory.resolve("ta_example.com.pdf"), "pdf".getBytes());
        ta.addOrUpdateResume(course, resumeDirectory.toString());

        CapturingServletOutputStream outputStream = new CapturingServletOutputStream();

        when(request.getParameter("action")).thenReturn("view_resume");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getParameter("download")).thenReturn("true");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(response.getOutputStream()).thenReturn(outputStream);

        controller.doGet(request, response);

        verify(response).setHeader("Content-Disposition", "attachment; filename=\"ta_example.com.pdf\"");
    }

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
        assertNull(ta.getResumeDirectoryForCourse("course-1"));
        assertEquals(0, course.getTaApplicants().size());
        assertEquals(0, course.getApplicantResumes().size());
        assertEquals("Alice,secret123,TA,ta@example.com,School of Software,Java,,", Files.readAllLines(usersFile).get(0));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void withdrawApplicationDeletesStoredResumeFileFromDisk() throws Exception {
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

        ta.addOrUpdateResume(course, resumeDirectory.toString());
        course.addApplication(ta, resumeDirectory.toString());

        when(request.getParameter("action")).thenReturn("withdraw_application");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(session.getAttribute("courseList")).thenReturn(List.of(course));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertFalse(Files.exists(resumeFile));
        verify(request).setAttribute("success", "Application withdrawn successfully.");
        verify(dispatcher).forward(request, response);
    }

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
        ta.addOrUpdateResume(course, "D:\\resume\\course-1");
        course.addApplication(ta, "D:\\resume\\course-1");

        when(request.getParameter("action")).thenReturn("withdraw_application");
        when(request.getParameter("courseId")).thenReturn("course-1");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(ta);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals("D:\\resume\\course-1", ta.getResumeDirectoryForCourse("course-1"));
        verify(request).setAttribute("error", "The application deadline has passed. You can no longer withdraw or modify applications.");
        verify(dispatcher).forward(request, response);
    }
}

