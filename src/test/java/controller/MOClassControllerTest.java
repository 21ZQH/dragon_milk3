package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import store.CourseStore;
import store.UserStore;
import testsupport.StoreTestSupport;

class MOClassControllerTest {
    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void createClassActionForwardsToCreateProjectPage() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("action")).thenReturn("create_class");
        when(request.getRequestDispatcher("/WEB-INF/views/mo/create-project.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void personalCenterActionForwardsToPersonalCenterPage() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("action")).thenReturn("personal_center");
        when(request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("reviewStageOpen", true);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void personalCenterMarksReviewUnavailableBeforeDeadline() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletContext servletContext = mock(ServletContext.class);

        when(request.getParameter("action")).thenReturn("personal_center");
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().plusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("reviewStageOpen", false);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void publishCourseSavesCourseAndReturnsToDashboard() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Molly,secret123,Mo,mo@example.com,");
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.setName("Molly");

        when(request.getParameter("action")).thenReturn("publish_course");
        when(request.getParameter("courseName")).thenReturn("Software Engineering");
        when(request.getParameter("jobTitle")).thenReturn("TA");
        when(request.getParameter("workingHours")).thenReturn("10 hours/week");
        when(request.getParameter("jobDescription")).thenReturn("Support lectures");
        when(request.getParameter("jobRequirement")).thenReturn("Communication skills");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        List<Course> courses = CourseStore.getCourseList();
        assertEquals(1, courses.size());
        assertTrue(courses.get(0).getId() != null && !courses.get(0).getId().isBlank());
        assertEquals("Software Engineering", courses.get(0).getCourseName());
        assertEquals("Support lectures", courses.get(0).getJobDescription());
        assertTrue(courses.get(0).getSalary().contains("TBD"));
        assertEquals(1, mo.getOwnedCourses().size());
        assertEquals(courses.get(0).getId(), mo.getOwnedCourses().get(0).getId());
        assertEquals("Molly,secret123,Mo,mo@example.com," + courses.get(0).getId(), java.nio.file.Files.readAllLines(usersFile).get(0));
        verify(session).setAttribute("user", mo);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void reviewCandidatesLoadsSelectedCourseApplicants() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@0");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("review_candidates");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/review.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        ArgumentCaptor<Object> selectedCourseCaptor = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(org.mockito.ArgumentMatchers.eq("selectedCourse"), selectedCourseCaptor.capture());
        Object selectedCourse = selectedCourseCaptor.getValue();
        assertInstanceOf(Course.class, selectedCourse);
        Course course = (Course) selectedCourse;
        assertEquals("course-1", course.getId());
        assertEquals(1, course.getTaApplicants().size());
        assertEquals("alice@example.com", course.getTaApplicants().get(0).getEmail());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void reviewCandidatesBeforeDeadlineRedirectsToPersonalCenter() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("review_candidates");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().plusHours(1));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/MOclasscontroller?action=personal_center&reviewLocked=1");
    }

    @Test
    void reviewCandidatesWithoutCourseIndexDefaultsToFirstOwnedCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                "course-2,Database,TA,8 hours/week,TBD,Mark assignments,SQL");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));
        mo.addOwnedCourse(new Course("course-2", "Database", "TA", "8 hours/week", "TBD", "Mark assignments", "SQL"));

        when(request.getParameter("action")).thenReturn("review_candidates");
        when(request.getParameter("courseIndex")).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/review.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("courseIndex", "0");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void saveReviewPicksStoresPickedApplicantEmailsOnCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@0");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("save_review_picks");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getParameterValues("pickedEmail")).thenReturn(new String[] {"alice@example.com"});
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<String> lines = Files.readAllLines(courseFile);
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills,alice@example.com,false",
                lines.get(0));
        verify(response).sendRedirect("/SE/MOclasscontroller?action=review_candidates&courseIndex=0&saved=1");
    }

    @Test
    void saveReviewPicksBeforeDeadlineRedirectsWithoutChangingCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@0");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("save_review_picks");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getParameterValues("pickedEmail")).thenReturn(new String[] {"alice@example.com"});
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().plusHours(1));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<String> lines = Files.readAllLines(courseFile);
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                lines.get(0));
        verify(response).sendRedirect("/SE/MOclasscontroller?action=personal_center&reviewLocked=1");
    }

    @Test
    void publishReviewUpdatesTaStatusesAndLocksCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@0",
                "Bob,pass123,TA,bob@example.com,School of Computer Science,C++,course-1,course-1@D:\\resume\\course-1-bob@0");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("publish_review");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getParameterValues("pickedEmail")).thenReturn(new String[] {"alice@example.com"});
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<String> courseLines = Files.readAllLines(courseFile);
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills,alice@example.com,true",
                courseLines.get(0));

        TA alice = (TA) UserStore.validateUser("pass123", "alice@example.com");
        TA bob = (TA) UserStore.validateUser("pass123", "bob@example.com");
        Assertions.assertNotNull(alice);
        Assertions.assertNotNull(bob);
        assertEquals(ResumeSubmission.STATUS_APPROVED, alice.getResumeStatusForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_REJECTED, bob.getResumeStatusForCourse("course-1"));
        assertTrue(alice.hasUnreadReviewUpdates());
        assertTrue(bob.hasUnreadReviewUpdates());
        verify(response).sendRedirect("/SE/MOclasscontroller?action=review_candidates&courseIndex=0&published=1");
    }

    @Test
    void publishReviewBeforeDeadlineRedirectsWithoutPublishing() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1@0");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("publish_review");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getParameterValues("pickedEmail")).thenReturn(new String[] {"alice@example.com"});
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().plusHours(1));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<String> courseLines = Files.readAllLines(courseFile);
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                courseLines.get(0));
        TA alice = (TA) UserStore.validateUser("pass123", "alice@example.com");
        Assertions.assertNotNull(alice);
        assertEquals(ResumeSubmission.STATUS_PENDING, alice.getResumeStatusForCourse("course-1"));
        verify(response).sendRedirect("/SE/MOclasscontroller?action=personal_center&reviewLocked=1");
    }
}
