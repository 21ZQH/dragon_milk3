package controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import service.AccountService;
import service.CourseService;
import service.DeadlineService;

class EntryControllerTest {

    private AccountService accountService;
    private CourseService courseService;
    private DeadlineService deadlineService;
    private EntryController controller;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private ServletContext servletContext;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        // 1. Mock all Service dependencies
        accountService = mock(AccountService.class);
        courseService = mock(CourseService.class);
        deadlineService = mock(DeadlineService.class);
        
        // 2. Instantiate Controller using the three-parameter constructor (perfectly adapts to your changes)
        controller = new EntryController(accountService, courseService, deadlineService);

        // 3. Mock Servlet API
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);
        dispatcher = mock(RequestDispatcher.class);

        // 4. Set up basic stubbing
        when(request.getSession()).thenReturn(session);
        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    void testDoGetMoPath() throws Exception {
        // Simulate accessing /mo
        when(request.getServletPath()).thenReturn("/mo");

        controller.doGet(request, response);

        // Verify that built-in accounts are initialized and properly forwarded to the role login page
        verify(accountService).ensureBuiltInAccounts();
        verify(request).setAttribute("role", "Mo");
        verify(request).setAttribute("title", "MO Login");
        verify(request).getRequestDispatcher("/WEB-INF/views/entry/role-login.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetAdminPath() throws Exception {
        // Simulate accessing /admin
        when(request.getServletPath()).thenReturn("/admin");

        controller.doGet(request, response);

        // Verify that built-in accounts are initialized and properly forwarded to the role login page
        verify(accountService).ensureBuiltInAccounts();
        verify(request).setAttribute("role", "Admin");
        verify(request).setAttribute("title", "Admin Login");
        verify(request).getRequestDispatcher("/WEB-INF/views/entry/role-login.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetUnknownPath() throws Exception {
        // Simulate accessing an unknown path
        when(request.getServletPath()).thenReturn("/unknown");

        controller.doGet(request, response);

        // Verify that a 404 error is returned
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testShowTaEntryDefaultShowsPublishedCourses() throws Exception {
        // Simulate accessing /ta without any action
        when(request.getServletPath()).thenReturn("/ta");
        when(request.getParameter("action")).thenReturn(null);

        // Simulate one published course and one unpublished course
        Course publishedCourse = new Course("id1", "Published", "Title", "10h", "100", "desc", "req");
        publishedCourse.setRecruitmentPublished(true);
        Course unpublishedCourse = new Course("id2", "Unpublished", "Title", "10h", "100", "desc", "req");
        unpublishedCourse.setRecruitmentPublished(false);

        when(courseService.getCourseList()).thenReturn(Arrays.asList(publishedCourse, unpublishedCourse));

        controller.doGet(request, response);

        // Verify that unpublished courses are filtered out, and only publishedCourse is stored in request and session
        verify(request).setAttribute(eq("courseList"), argThat(list -> 
            ((List<?>) list).size() == 1 && ((List<?>) list).contains(publishedCourse)
        ));
        verify(request).getRequestDispatcher("/WEB-INF/views/entry/ta-public.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testShowTaEntryAuthAction() throws Exception {
        // Simulate accessing /ta?action=auth
        when(request.getServletPath()).thenReturn("/ta");
        when(request.getParameter("action")).thenReturn("auth");

        controller.doGet(request, response);

        // Verify correct forwarding to the TA authorization page
        verify(request).getRequestDispatcher("/WEB-INF/views/entry/ta-auth.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testShowTaEntryDetailUsesServletContextDeadline() throws Exception {
        // Simulate accessing /ta?action=detail&courseIndex=0
        when(request.getServletPath()).thenReturn("/ta");
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("courseIndex")).thenReturn("0");

        Course publishedCourse = new Course("id1", "C1", "Title", "10h", "100", "desc", "req");
        publishedCourse.setRecruitmentPublished(true);
        when(courseService.getCourseList()).thenReturn(Arrays.asList(publishedCourse));

        // Simulate that a future deadline already exists in the ServletContext
        LocalDateTime futureDeadline = LocalDateTime.now().plusDays(2);
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(futureDeadline);

        controller.doGet(request, response);

        // Verify the page state is applicationOpen = true
        verify(request).setAttribute("selectedCourse", publishedCourse);
        verify(request).setAttribute("courseIndex", "0");
        verify(request).setAttribute("applicationOpen", true);
        verify(request).getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testShowTaEntryDetailTreatsMissingServletContextDeadlineAsOpen() throws Exception {
        // Simulate accessing /ta?action=detail&courseIndex=0
        when(request.getServletPath()).thenReturn("/ta");
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("courseIndex")).thenReturn("0");

        Course publishedCourse = new Course("id1", "C1", "Title", "10h", "100", "desc", "req");
        publishedCourse.setRecruitmentPublished(true);
        when(courseService.getCourseList()).thenReturn(Arrays.asList(publishedCourse));

        // Simulate that there is no deadline in the ServletContext.
        when(servletContext.getAttribute("applicationDeadline")).thenReturn(null);

        controller.doGet(request, response);

        verify(request).setAttribute("applicationOpen", true);
        verify(dispatcher).forward(request, response);
    }
}
