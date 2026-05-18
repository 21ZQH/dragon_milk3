package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Course;
import service.AccountService;
import service.CourseService;
import service.impl.AccountServiceImpl;
import service.impl.CourseServiceImpl;
import store.DeadlineStore;

public class EntryController extends HttpServlet {
    private final AccountService accountService;
    private final CourseService courseService;

    public EntryController() {
        this(new AccountServiceImpl(), new CourseServiceImpl());
    }

    EntryController(AccountService accountService, CourseService courseService) {
        this.accountService = accountService;
        this.courseService = courseService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/ta".equals(servletPath)) {
            showTaEntry(request, response);
        } else if ("/mo".equals(servletPath)) {
            accountService.ensureBuiltInAccounts();
            request.setAttribute("role", "Mo");
            request.setAttribute("title", "MO Login");
            request.getRequestDispatcher("/WEB-INF/views/entry/role-login.jsp").forward(request, response);
        } else if ("/admin".equals(servletPath)) {
            accountService.ensureBuiltInAccounts();
            request.setAttribute("role", "Admin");
            request.setAttribute("title", "Admin Login");
            request.getRequestDispatcher("/WEB-INF/views/entry/role-login.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showTaEntry(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Course> courses = courseService.getCourseList().stream()
                .filter(Course::isRecruitmentPublished)
                .collect(Collectors.toList());
        request.getSession().setAttribute("courseList", courses);

        String action = request.getParameter("action");
        if ("auth".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/entry/ta-auth.jsp").forward(request, response);
            return;
        }

        if ("detail".equals(action)) {
            Course course = getCourseByIndex(courses, request.getParameter("courseIndex"));
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.setAttribute("applicationOpen", isApplicationOpen(request));
            request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
            return;
        }

        request.setAttribute("courseList", courses);
        request.getRequestDispatcher("/WEB-INF/views/entry/ta-public.jsp").forward(request, response);
    }

    private Course getCourseByIndex(List<Course> courses, String indexParam) {
        if (courses == null || indexParam == null) {
            return null;
        }
        try {
            int index = Integer.parseInt(indexParam);
            if (index < 0 || index >= courses.size()) {
                return null;
            }
            return courses.get(index);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isApplicationOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveApplicationDeadline(request);
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    private LocalDateTime resolveApplicationDeadline(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        if (servletContext != null) {
            Object deadline = servletContext.getAttribute("applicationDeadline");
            if (deadline instanceof LocalDateTime localDateTime) {
                return localDateTime;
            }
        }
        return DeadlineStore.getDeadline();
    }
}
