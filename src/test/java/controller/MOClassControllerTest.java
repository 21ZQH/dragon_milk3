package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import store.CourseStore;
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

        verify(dispatcher).forward(request, response);
    }

    @Test
    void publishCourseSavesCourseAndReturnsToDashboard() throws Exception {
        StoreTestSupport.useCourseStore(tempDir);
        MOClassController controller = new MOClassController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("action")).thenReturn("publish_course");
        when(request.getParameter("courseName")).thenReturn("Software Engineering");
        when(request.getParameter("jobTitle")).thenReturn("TA");
        when(request.getParameter("workingHours")).thenReturn("10 hours/week");
        when(request.getParameter("jobDescription")).thenReturn("Support lectures");
        when(request.getParameter("jobRequirement")).thenReturn("Communication skills");
        when(request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp")).thenReturn(dispatcher);

        controller.doPost(request, response);

        List<Course> courses = CourseStore.getCourseList();
        assertEquals(1, courses.size());
        assertEquals("Software Engineering", courses.get(0).getCourseName());
        assertEquals("Support lectures", courses.get(0).getJobDescription());
        assertTrue(courses.get(0).getSalary().contains("TBD"));
        verify(dispatcher).forward(request, response);
    }
}
