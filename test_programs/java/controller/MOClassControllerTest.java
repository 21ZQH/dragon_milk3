package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
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
import model.ApplicationForm;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import store.ApplicationFormStore;
import store.CourseStore;
import store.UserStore;
import testsupport.StoreTestSupport;

/**
 * Unit tests for {@link MOClassController} in the TA Recruitment system.
 * Tests cover MO dashboard, course management, class creation, deadline enforcement,
 * candidate review, review publishing, and profile/error handling scenarios.
 *
 * @author BUPT-TA-Recruitment-Group33
 */
class MOClassControllerTest {
    @TempDir
    Path tempDir;

    /**
     * Clears store overrides and system properties after each test to ensure test isolation.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
        System.clearProperty(ApplicationFormStore.FILE_PATH_PROPERTY);
    }

    /**
     * Creates a fully initialized MO user with name, degree, and college fields populated.
     *
     * @param email the email address for the MO user
     * @return a configured Mo instance
     */
    private Mo createCompleteMo(String email) {
        Mo mo = new Mo("secret123", email);
        mo.setName("Molly");
        mo.setDegree("Master of Science");
        mo.setCollege("School of Software");
        return mo;
    }

    /**
     * Configures the application form store file path to a temp directory location.
     */
    private void useApplicationFormStore() {
        System.setProperty(ApplicationFormStore.FILE_PATH_PROPERTY, tempDir.resolve("application-forms.txt").toString());
    }

    /**
     * Saves a submitted application form for a given email and course ID.
     *
     * @param email    the applicant's email
     * @param courseId the course identifier
     */
    private void saveSubmittedForm(String email, String courseId) {
        ApplicationForm form = new ApplicationForm(email, courseId);
        form.setApplicantName(email);
        form.setEmail(email);
        form.setEducation("BSc Software Engineering");
        form.setSkills("Java");
        form.setRelevantExperience("Lab support");
        form.setProjectExperience("Course project");
        form.setCourseFit("Strong fit");
        form.setFeedback("Private feedback");
        form.setSubmitted(true);
        ApplicationFormStore.saveOrUpdate(form);
    }

    /**
     * Tests that the "create_class" action forwards the MO user to the my-project page.
     */
    @Test
    void createClassActionForwardsToMyProjectPage() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = createCompleteMo("mo@example.com");

        when(request.getParameter("action")).thenReturn("create_class");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/my-project.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "create_class" action works for MO users who have not completed
     * their profile, forwarding them to the my-project page without requiring full profile data.
     */
    @Test
    void createClassDoesNotRequireCompleteProfileAndUsesMyProjectPage() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("create_class");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/my-project.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "dashboard" action forwards the MO user to the dashboard page
     * with the modify state set to open and no locked modal shown.
     */
    @Test
    void dashboardActionForwardsToDashboardPage() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("moModifyOpen", true);
        verify(request).setAttribute("showModifyLockedModal", false);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the "personal_center" action forwards the MO user to the personal center page
     * with the review stage open by default.
     */
    @Test
    void personalCenterActionForwardsToPersonalCenterPage() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("personal_center");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("reviewStageOpen", true);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the removed "profile_center" action returns an HTTP 400 Bad Request error.
     */
    @Test
    void removedProfileCenterActionReturnsBadRequest() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = createCompleteMo("mo@example.com");

        when(request.getParameter("action")).thenReturn("profile_center");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);

        controller.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    /**
     * Tests that the personal center page marks the review stage as unavailable
     * when the application deadline has not yet passed (review cannot start before the deadline).
     */
    @Test
    void personalCenterMarksReviewUnavailableBeforeDeadline() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ServletContext servletContext = mock(ServletContext.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("personal_center");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(LocalDateTime.now().plusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("reviewStageOpen", false);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that the dashboard action ignores a removed "profileIncomplete" parameter
     * and proceeds to forward to the dashboard page without error.
     */
    @Test
    void dashboardIgnoresRemovedProfileIncompleteFlag() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getParameter("profileIncomplete")).thenReturn("1");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("moModifyOpen", true);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that an unauthenticated request (no session) to the MO controller
     * redirects to the MO entry page.
     */
    @Test
    void unauthenticatedMoRequestRedirectsToMoEntry() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/mo");
    }

    /**
     * Tests that a non-MO user (e.g., a TA) accessing the MO controller
     * receives an HTTP 403 Forbidden error.
     */
    @Test
    void nonMoRequestReturnsForbidden() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new TA("pass123", "alice@example.com"));

        controller.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: MO role required");
    }

    /**
     * Tests that publishing a course via "publish_course" persists the course details
     * (job title, working hours, description, requirements), marks recruitment as published,
     * and redirects to the project detail page with a success flag.
     */
    @Test
    void publishCoursePublishesAssignedCourseAndRedirectsToDetail() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,, ,TBD,,");
        StoreTestSupport.writeLines(usersFile, "Molly,secret123,Mo,mo@example.com,Master of Science,School of Software,course-1");
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = createCompleteMo("mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "", "", "TBD", "", ""));

        when(request.getParameter("action")).thenReturn("publish_course");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getParameter("jobTitle")).thenReturn("TA");
        when(request.getParameter("workingHours")).thenReturn("10 hours/week");
        when(request.getParameter("jobDescription")).thenReturn("Support lectures");
        when(request.getParameter("jobRequirement")).thenReturn("Communication skills");
        when(request.getParameter("taPositions")).thenReturn("2");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<Course> courses = CourseStore.getCourseList();
        assertEquals(1, courses.size());
        assertEquals("course-1", courses.get(0).getId());
        assertEquals("Software Engineering", courses.get(0).getCourseName());
        assertEquals("Support lectures", courses.get(0).getJobDescription());
        assertEquals(2, courses.get(0).getTaPositions());
        assertTrue(courses.get(0).isRecruitmentPublished());
        assertTrue(courses.get(0).getSalary().contains("TBD"));
        assertEquals(1, mo.getOwnedCourses().size());
        assertEquals(courses.get(0).getId(), mo.getOwnedCourses().get(0).getId());
        verify(session, atLeastOnce()).setAttribute("user", mo);
        verify(response).sendRedirect("/SE/MOclasscontroller?action=project_detail&courseIndex=0&success=1");
    }

    /**
     * Tests that the removed "save_personal_information" action returns an HTTP 400 Bad Request
     * and does not modify the user's personal information or the users file.
     */
    @Test
    void updatePublishedCourseSavesChangesAndKeepsPublishedStatus() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,,TBD,Old description,Old requirement,,false,true");
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = createCompleteMo("mo@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "", "TBD", "Old description", "Old requirement");
        course.setRecruitmentPublished(true);
        mo.addOwnedCourse(course);

        when(request.getParameter("action")).thenReturn("update_published");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getParameter("courseName")).thenReturn("Software Engineering");
        when(request.getParameter("jobTitle")).thenReturn("Lead TA");
        when(request.getParameter("jobDescription")).thenReturn("Updated published description");
        when(request.getParameter("jobRequirement")).thenReturn("Updated requirement");
        when(request.getParameter("taPositions")).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<Course> courses = CourseStore.getCourseList();
        assertEquals(1, courses.size());
        assertEquals("Lead TA", courses.get(0).getJobTitle());
        assertEquals("Updated published description", courses.get(0).getJobDescription());
        assertTrue(courses.get(0).isRecruitmentPublished());
        verify(response).sendRedirect("/SE/MOclasscontroller?action=project_detail&courseIndex=0&success=1");
    }

    @Test
    void saveDraftDoesNotUnpublishPublishedCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,,TBD,Published description,Requirement,,false,true");
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = createCompleteMo("mo@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "", "TBD", "Published description", "Requirement");
        course.setRecruitmentPublished(true);
        mo.addOwnedCourse(course);

        when(request.getParameter("action")).thenReturn("save_course_draft");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        List<Course> courses = CourseStore.getCourseList();
        assertEquals(1, courses.size());
        assertTrue(courses.get(0).isRecruitmentPublished());
        assertEquals("Published description", courses.get(0).getJobDescription());
        verify(response).sendRedirect("/SE/MOclasscontroller?action=project_detail&courseIndex=0&error=published");
    }

    @Test
    void removedSavePersonalInformationActionReturnsBadRequest() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Molly,secret123,Mo,mo@example.com,");

        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.setName("Molly");

        when(request.getParameter("action")).thenReturn("save_personal_information");
        when(request.getParameter("name")).thenReturn("  Molly Zhang  ");
        when(request.getParameter("degree")).thenReturn("  PhD  ");
        when(request.getParameter("college")).thenReturn("  School of Engineering  ");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);

        controller.doPost(request, response);

        assertEquals("Molly", mo.getName());
        assertEquals("Molly,secret123,Mo,mo@example.com,",
                Files.readAllLines(usersFile).get(0));
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    /**
     * Tests that attempting to publish a course without a selected course index
     * redirects without saving any data to the course store.
     */
    @Test
    void publishCourseWithoutSelectedCourseRedirectsWithoutSaving() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("publish_course");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        assertTrue(!Files.exists(courseFile) || Files.readAllLines(courseFile).isEmpty());
        verify(response).sendRedirect("/SE/MOclasscontroller?action=my_project");
    }

    /**
     * Tests that attempting to create a class after the MO course modification deadline
     * has passed redirects to the dashboard with a locked modification flag.
     */
    @Test
    void createClassAfterMoModifyDeadlineRedirectsToDashboard() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("create_class");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/MOclasscontroller?action=dashboard&modifyLocked=1");
    }

    /**
     * Tests that the dashboard page shows a locked modification modal when the
     * "modifyLocked" redirect flag is present and the deadline has passed.
     */
    @Test
    void dashboardShowsModifyLockedModalAfterRedirectFlag() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getParameter("modifyLocked")).thenReturn("1");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("moModifyOpen", false);
        verify(request).setAttribute("showModifyLockedModal", true);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that attempting to publish a course after the MO modification deadline
     * redirects without saving any changes to the course store.
     */
    @Test
    void publishCourseAfterMoModifyDeadlineRedirectsWithoutSaving() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);
        Mo mo = new Mo("secret123", "mo@example.com");

        when(request.getParameter("action")).thenReturn("publish_course");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        assertTrue(!Files.exists(courseFile) || Files.readAllLines(courseFile).isEmpty());
        verify(response).sendRedirect("/SE/MOclasscontroller?action=dashboard&modifyLocked=1");
    }

    /**
     * Tests that the "review_candidates" action loads the selected course with its
     * TA applicants and application forms, and forwards to the review page.
     */
    @Test
    void reviewCandidatesLoadsSelectedCourseApplicants() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        useApplicationFormStore();
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@0@false");
        saveSubmittedForm("alice@example.com", "course-1");

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

    /**
     * Tests that accessing the review candidates page before the application deadline
     * redirects to the personal center with a review locked indicator.
     */
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

    /**
     * Tests that accessing the review candidates page without specifying a course index
     * defaults to the first course owned by the MO user.
     */
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

    /**
     * Tests that the "project_detail" action exposes the MO modification availability
     * flag and the MO modification deadline as request attributes, along with forwarding
     * to the project detail page.
     */
    @Test
    void projectDetailExposesMoModifyAvailabilityAndDeadline() throws Exception {
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
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = createCompleteMo("mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));
        LocalDateTime moDeadline = LocalDateTime.now().plusHours(2);

        when(request.getParameter("action")).thenReturn("project_detail");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(moDeadline);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/project-detail.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("courseIndex", "0");
        verify(request).setAttribute("moModifyOpen", true);
        verify(request).setAttribute("moModifyDeadline", moDeadline);
        verify(dispatcher).forward(request, response);
    }

    /**
     * Tests that saving course changes via "save_course_changes" does not require
     * a complete MO profile and redirects to the project detail page on success.
     */
    @Test
    void saveCourseChangesDoesNotRequireCompleteProfile() throws Exception {
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
        LocalDateTime moDeadline = LocalDateTime.now().plusHours(1);

        when(request.getParameter("action")).thenReturn("save_course_changes");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(moDeadline);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(response).sendRedirect("/SE/MOclasscontroller?action=project_detail&courseIndex=0&success=1");
    }

    /**
     * Tests that saving course changes after the MO modification deadline has passed
     * does not persist the changes and forwards to the project detail page with a locked state
     * and an appropriate error message.
     */
    @Test
    void saveCourseChangesAfterMoModifyDeadlineForwardsWithLockedState() throws Exception {
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
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        Mo mo = new Mo("secret123", "mo@example.com");
        mo.addOwnedCourse(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));
        LocalDateTime moDeadline = LocalDateTime.now().minusHours(1);

        when(request.getParameter("action")).thenReturn("save_course_changes");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mo);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("moCourseModifyDeadline")).thenReturn(moDeadline);
        when(request.getRequestDispatcher("/WEB-INF/views/mo/project-detail.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        verify(request).setAttribute("error", "The deadline for MO to modify course information has passed.");
        verify(request, atLeastOnce()).setAttribute("moModifyOpen", false);
        verify(request).setAttribute("moModifyDeadline", moDeadline);
        verify(dispatcher).forward(request, response);
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                Files.readAllLines(courseFile).get(0));
    }

    /**
     * Tests that saving review picks ("save_review_picks") stores the selected applicant emails
     * on the course record and redirects to the review candidates page with a saved flag.
     */
    @Test
    void saveReviewPicksStoresPickedApplicantEmailsOnCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        useApplicationFormStore();
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@0@false");
        saveSubmittedForm("alice@example.com", "course-1");

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
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills,alice@example.com,false,false,0",
                lines.get(0));
        verify(response).sendRedirect("/SE/MOclasscontroller?action=review_candidates&courseIndex=0&saved=1");
    }

    /**
     * Tests that saving review picks before the application deadline has passed
     * redirects without modifying the course data, as reviews cannot be conducted yet.
     */
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

    /**
     * Tests that publishing a review ("publish_review") updates TA resume statuses
     * (approving selected candidates and rejecting others), marks the course review as published,
     * sets unread review flags for affected TAs, and redirects to the review candidates page.
     */
    @Test
    void publishReviewUpdatesTaStatusesAndLocksCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        useApplicationFormStore();
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@0@false",
                "Bob,pass123,TA,bob@example.com,School of Computer Science,C++,course-1,course-1@0@false");
        saveSubmittedForm("alice@example.com", "course-1");
        saveSubmittedForm("bob@example.com", "course-1");

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
        assertEquals("course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills,alice@example.com,true,false,0",
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

    /**
     * Tests that publishing a review before the application deadline has passed
     * redirects without changing any TA statuses or course data.
     */
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
