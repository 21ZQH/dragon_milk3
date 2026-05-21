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
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.User;
import service.AccountService;
import service.CourseService;
import service.DeadlineService;
import service.impl.AccountServiceImpl;
import service.impl.CourseServiceImpl;
import service.impl.DeadlineServiceImpl;

public class EntryController extends HttpServlet {
    private final AccountService accountService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;

    public EntryController() {
        this(new AccountServiceImpl(), new CourseServiceImpl(), new DeadlineServiceImpl());
    }

    EntryController(AccountService accountService, CourseService courseService, DeadlineService deadlineService) {
        this.accountService = accountService;
        this.courseService = courseService;
        this.deadlineService = deadlineService; 
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
        refreshAuthenticatedTa(request);
        List<Course> courses = courseService.getCourseList().stream()
                .filter(Course::isRecruitmentPublished)
                .collect(Collectors.toList());
        request.getSession().setAttribute("courseList", courses);
        prepareDeadlineAttributes(request);

        String action = request.getParameter("action");
        if ("auth".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/entry/ta-auth.jsp").forward(request, response);
            return;
        }

        if ("detail".equals(action)) {
            Course course = getCourseByIndex(courses, request.getParameter("courseIndex"));
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
            return;
        }

        request.setAttribute("courseList", courses);
        request.getRequestDispatcher("/WEB-INF/views/entry/ta-public.jsp").forward(request, response);
    }

    private void refreshAuthenticatedTa(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Object currentUser = session.getAttribute("user");
        if (currentUser instanceof model.TA ta) {
            User refreshedUser = accountService.loginTaByAccessKey(ta.getPassword());
            if (refreshedUser instanceof model.TA) {
                session.setAttribute("user", refreshedUser);
            }
        }
    }

    private void prepareDeadlineAttributes(HttpServletRequest request) {
        LocalDateTime applicationDeadline = resolveApplicationDeadline(request);
        LocalDateTime moModifyDeadline = resolveMoModifyDeadline(request);

        request.setAttribute("applicationDeadline", applicationDeadline);
        request.setAttribute("applicationOpen", isApplicationOpen(applicationDeadline));
        request.setAttribute("reviewStageOpen", isReviewStageOpen(applicationDeadline));
        request.setAttribute("moModifyDeadline", moModifyDeadline);
        request.setAttribute("moModifyOpen", isMoModifyOpen(moModifyDeadline));
    }

    private boolean isApplicationOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    private boolean isReviewStageOpen(LocalDateTime deadline) {
        return deadline == null || LocalDateTime.now().isAfter(deadline);
    }

    private boolean isMoModifyOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
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

    private LocalDateTime resolveApplicationDeadline(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        if (servletContext != null) {
            Object deadline = servletContext.getAttribute("applicationDeadline");
            if (deadline instanceof LocalDateTime localDateTime) {
                return localDateTime;
            }
        }
        return null;
    }

    private LocalDateTime resolveMoModifyDeadline(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        if (servletContext != null) {
            Object deadline = servletContext.getAttribute("moCourseModifyDeadline");
            if (deadline instanceof LocalDateTime localDateTime) {
                return localDateTime;
            }
        }
        return null;
    }
}
