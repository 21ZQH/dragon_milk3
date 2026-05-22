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

/**
 * Servlet controller handling entry-point requests for the TA Recruitment System.
 * <p>
 * This controller manages navigation to the three main role-specific login/entry pages:
 * TA (Teaching Assistant) public entry, MO (Module Organiser) login, and Admin login.
 * It resolves public course listings, deadline information, and authenticated TA session refreshing.
 * </p>
 *
 * <p>URL mappings are determined by the servlet path:
 * <ul>
 *   <li>{@code /ta} - TA public entry with course listings and authentication</li>
 *   <li>{@code /mo} - MO login page</li>
 *   <li>{@code /admin} - Admin login page</li>
 * </ul>
 * </p>
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 1.0
 */
public class EntryController extends HttpServlet {
    private final AccountService accountService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;

    /**
     * Constructs an {@code EntryController} with default service implementations.
     */
    public EntryController() {
        this(new AccountServiceImpl(), new CourseServiceImpl(), new DeadlineServiceImpl());
    }

    /**
     * Constructs an {@code EntryController} with the specified service instances.
     * <p>Package-private constructor used for dependency injection in unit tests.</p>
     *
     * @param accountService  the account service for authentication operations
     * @param courseService   the course service for retrieving course data
     * @param deadlineService the deadline service for checking time constraints
     */
    EntryController(AccountService accountService, CourseService courseService, DeadlineService deadlineService) {
        this.accountService = accountService;
        this.courseService = courseService;
        this.deadlineService = deadlineService;
    }

    /**
     * Handles HTTP GET requests to serve the appropriate entry page based on the servlet path.
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be forwarded
     * @throws IOException      if an I/O error occurs during forwarding or redirection
     */
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

    /**
     * Displays the TA entry page with the public course listing and deadline attributes.
     * <p>Supports the following actions via the {@code action} parameter:
     * <ul>
     *   <li>{@code auth} - forward to the TA authentication page</li>
     *   <li>{@code detail} - forward to a specific course detail page</li>
     *   <li>default (no action) - forward to the public TA listing page</li>
     * </ul>
     * </p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Refreshes the authenticated TA's session data by re-fetching their user information
     * from the data store. If the current session user is a TA, their data is updated in place.
     *
     * @param request the {@link HttpServletRequest} containing the current session
     */
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

    /**
     * Resolves deadline information from the servlet context and sets the relevant
     * request attributes for use by the view layer.
     *
     * @param request the {@link HttpServletRequest} whose attributes will be populated
     */
    private void prepareDeadlineAttributes(HttpServletRequest request) {
        LocalDateTime applicationDeadline = resolveApplicationDeadline(request);
        LocalDateTime moModifyDeadline = resolveMoModifyDeadline(request);

        request.setAttribute("applicationDeadline", applicationDeadline);
        request.setAttribute("applicationOpen", isApplicationOpen(applicationDeadline));
        request.setAttribute("reviewStageOpen", isReviewStageOpen(applicationDeadline));
        request.setAttribute("moModifyDeadline", moModifyDeadline);
        request.setAttribute("moModifyOpen", isMoModifyOpen(moModifyDeadline));
    }

    /**
     * Checks whether the TA application period is currently open based on the given deadline.
     *
     * @param deadline the application deadline, may be {@code null} indicating no deadline
     * @return {@code true} if the deadline is {@code null} or has not yet passed
     */
    private boolean isApplicationOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Checks whether the review stage is open (i.e., the application deadline has passed).
     *
     * @param deadline the application deadline, may be {@code null}
     * @return {@code true} if the deadline is {@code null} or has already passed
     */
    private boolean isReviewStageOpen(LocalDateTime deadline) {
        return deadline == null || LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Checks whether MO course modification is currently allowed.
     *
     * @param deadline the MO modification deadline, may be {@code null}
     * @return {@code true} if the deadline is {@code null} or has not yet passed
     */
    private boolean isMoModifyOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Retrieves a {@link Course} from the provided list by its index parameter.
     *
     * @param courses    the list of available courses
     * @param indexParam the string representation of the course index
     * @return the {@link Course} at the given index, or {@code null} if the index is invalid
     */
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

    /**
     * Resolves the TA application deadline stored in the servlet context.
     *
     * @param request the {@link HttpServletRequest} used to obtain the servlet context
     * @return the {@link LocalDateTime} deadline, or {@code null} if not set
     */
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

    /**
     * Resolves the MO course modification deadline stored in the servlet context.
     *
     * @param request the {@link HttpServletRequest} used to obtain the servlet context
     * @return the {@link LocalDateTime} deadline, or {@code null} if not set
     */
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
