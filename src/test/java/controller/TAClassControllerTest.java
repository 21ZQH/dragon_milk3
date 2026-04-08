package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Course;
import model.Mo;
import model.TA;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import testsupport.StoreTestSupport;

class TAClassControllerTest {
    @TempDir
    Path tempDir;

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

        when(request.getParameter("action")).thenReturn("view_information");
        when(request.getSession()).thenReturn(session);
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

        List<Course> courses = List.of(
                new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"),
                new Course("Java Programming", "TA", "8 hours/week", "TBD", "Mark labs", "Java basics"));

        when(request.getParameter("action")).thenReturn("show_all_information");
        when(request.getParameter("courseIndex")).thenReturn("1");
        when(request.getSession()).thenReturn(session);
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

        List<Course> courses = List.of(
                new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("show_all_information");
        when(request.getParameter("courseIndex")).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        verify(response).sendRedirect("/SE/TAclasscontroller?action=view_information");
    }

    @Test
    void goApplySetsSelectedCourseAndForwards() throws Exception {
        TAClassController controller = new TAClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        List<Course> courses = List.of(
                new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        when(request.getParameter("action")).thenReturn("go_apply");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute(eq("selectedCourse"), eq(courses.get(0)));
        verify(request).setAttribute("courseIndex", "0");
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

        Course course = new Course("Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        List<Course> courses = List.of(course);

        when(request.getParameter("action")).thenReturn("upload_resume");
        when(request.getParameter("courseIndex")).thenReturn("0");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("courseList")).thenReturn(courses);
        when(session.getAttribute("user")).thenReturn(new Mo("secret123", "mo@example.com"));
        when(request.getContextPath()).thenReturn("/SE");

        controller.doPost(request, response);

        verify(response).sendRedirect("/SE/TAclasscontroller?action=view_information");
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
        assertEquals(
                tempDir.resolve("webapps").resolve("SE").resolve("WEB-INF").resolve("file").resolve("resume").resolve("course-1").toString(),
                ta.getResumeDirectoryForCourse("course-1"));
        assertEquals(
                "Alice,secret123,TA,ta@example.com,School of Software,Java,course-1,course-1@" + ta.getResumeDirectoryForCourse("course-1"),
                Files.readAllLines(usersFile).get(0));
        verify(dispatcher).forward(request, response);
    }
}
