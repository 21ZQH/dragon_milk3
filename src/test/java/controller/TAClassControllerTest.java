package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
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
}
