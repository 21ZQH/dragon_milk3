package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Mo;
import service.ApplicationReviewService;
import service.MOProjectService;
import service.impl.ApplicationReviewServiceImpl;
import service.impl.CourseServiceImpl;
import service.impl.MOProjectServiceImpl;
import service.impl.UserProfileServiceImpl;
import service.CourseService;
import service.DeadlineService;
import service.impl.DeadlineServiceImpl;

/**
 * Servlet controller handling all MO (Module Organiser) operations within the
 * TA Recruitment System.
 * <p>
 * This controller manages MO workflows including course management, recruitment
 * publishing, applicant review, course draft saving, and project management.
 * It ensures all requests come from an authenticated MO session before processing.
 *
 *
 * <p>Supported actions in GET requests:
 * <ul>
 *   <li>{@code dashboard} - display the MO dashboard</li>
 *   <li>{@code create_class} - create or modify a course listing</li>
 *   <li>{@code personal_center} - display the MO personal centre</li>
 *   <li>{@code review_candidates} - review and select TA candidates</li>
 *   <li>{@code my_project} - view the MO's assigned courses</li>
 *   <li>{@code project_detail} - view details of a specific course project</li>
 * </ul>
 *
 *
 * <p>Supported actions in POST requests:
 * <ul>
 *   <li>{@code publish_course} - publish a course's TA recruitment</li>
 *   <li>{@code save_course_draft} - save course information as a draft</li>
 *   <li>{@code save_review_picks} - save selected TA candidates</li>
 *   <li>{@code publish_review} - publish review results</li>
 *   <li>{@code update_published} - update published course information</li>
 *   <li>{@code save_course_changes} - compatibility alias for updating published course information</li>
 * </ul>
 *
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 1.0
 * @see ApplicationReviewService
 * @see MOProjectService
 * @see CourseService
 * @see DeadlineService
 */
public class MOClassController extends HttpServlet {
    private final ApplicationReviewService applicationReviewService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;
    private final MOProjectService moProjectService;

    /**
     * Constructs a {@code MOClassController} with default service implementations.
     */
    public MOClassController() {
        this(
                new ApplicationReviewServiceImpl(),
                new CourseServiceImpl(),
                new DeadlineServiceImpl());
    }

    /**
     * Constructs a {@code MOClassController} with the specified application review service
     * and default implementations for other services.
     * <p>Package-private constructor used for dependency injection in unit tests.</p>
     *
     * @param applicationReviewService the service for reviewing applications
     */
    MOClassController(ApplicationReviewService applicationReviewService) {
        this(
                applicationReviewService,
                new CourseServiceImpl(),
                new DeadlineServiceImpl());
    }

    /**
     * Constructs a {@code MOClassController} with the specified services and a
     * default {@link MOProjectServiceImpl}.
     *
     * @param applicationReviewService the service for reviewing applications
     * @param courseService            the service for course data access
     * @param deadlineService          the service for deadline checks
     */
    MOClassController(
            ApplicationReviewService applicationReviewService,
            CourseService courseService,
            DeadlineService deadlineService) {
        this(
                applicationReviewService,
                courseService,
                deadlineService,
                new MOProjectServiceImpl(courseService, new UserProfileServiceImpl()));
    }

    /**
     * Constructs a {@code MOClassController} with all specified service instances.
     * <p>Full constructor used for dependency injection.</p>
     *
     * @param applicationReviewService the service for reviewing applications
     * @param courseService            the service for course data access
     * @param deadlineService          the service for deadline checks
     * @param moProjectService         the service for MO project management
     */
    MOClassController(
            ApplicationReviewService applicationReviewService,
            CourseService courseService,
            DeadlineService deadlineService,
            MOProjectService moProjectService) {
        this.applicationReviewService = applicationReviewService;
        this.courseService = courseService;
        this.deadlineService = deadlineService;
        this.moProjectService = moProjectService;
    }

    /**
     * Handles HTTP GET requests for MO operations.
     * <p>Dispatches to the appropriate handler based on the {@code action} parameter.
     * All actions except logout require an authenticated MO session.</p>
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be processed
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String action = request.getParameter("action");

        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if (!ensureMoSession(request, response)) {
            return;
        }

        if ("dashboard".equals(action)) {
            show_dashboard(request, response);
        } else if ("create_class".equals(action)) {
            create_class(request, response);
        } else if ("personal_center".equals(action)) {
            show_personal_center(request, response);
        } else if ("review_candidates".equals(action)) {
            show_review_candidates(request, response);
        } else if ("my_project".equals(action)) {
            show_my_project(request, response);
        } else if ("project_detail".equals(action)) {
            show_project_detail(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }

    }

    /**
     * Handles HTTP POST requests for MO operations.
     * <p>Dispatches to the appropriate handler based on the {@code action} parameter.
     * All actions except logout require an authenticated MO session.</p>
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be processed
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if (!ensureMoSession(request, response)) {
            return;
        }

        if ("publish_course".equals(action)) {
            publish_course(request, response);

        } else if ("save_course_draft".equals(action)) {
            save_course_draft(request, response);

        } else if ("save_review_picks".equals(action)) {
            save_review_picks(request, response);
        } else if ("publish_review".equals(action)) {
            publish_review(request, response);

        } else if ("update_published".equals(action) || "save_course_changes".equals(action)) {
            update_published_course(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }

    }

    /**
     * Ensures the current session belongs to an authenticated MO user.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @return {@code true} if an authenticated MO session exists, {@code false} otherwise
     * @throws IOException if redirection or error sending fails
     */
    private boolean ensureMoSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/mo");
            return false;
        }

        Object currentUser = session.getAttribute("user");
        if (currentUser instanceof Mo) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: MO role required");
        return false;
    }

    /**
     * Displays the MO dashboard with deadline information.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_dashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LocalDateTime applicationDeadline = resolveApplicationDeadline(request);
        LocalDateTime moModifyDeadline = resolveMoModifyDeadline(request);
        request.setAttribute("applicationDeadline", applicationDeadline);
        request.setAttribute("applicationOpen", deadlineService.isApplicationOpen(applicationDeadline));
        request.setAttribute("reviewStageOpen", deadlineService.isReviewStageOpen(applicationDeadline));
        request.setAttribute("moModifyDeadline", moModifyDeadline);
        request.setAttribute("moModifyOpen", deadlineService.isMoModifyOpen(moModifyDeadline));
        request.setAttribute("showModifyLockedModal", "1".equals(request.getParameter("modifyLocked")));
        request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp").forward(request, response);
    }

    /**
     * Displays the MO personal centre page.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_personal_center(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("reviewStageOpen", isReviewStageOpen(request));
        request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp").forward(request, response);
    }

    /**
     * Handles the create class action. If MO modification is locked, redirects to
     * the dashboard. Otherwise delegates to the my project page.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void create_class(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isMoModifyOpen(request)) {
            redirectMoModifyLocked(request, response);
            return;
        }
        show_my_project(request, response);
    }

    /**
     * Displays the MO's list of assigned courses.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_my_project(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Course> courseList = getCurrentMoCourseList(request);
        request.setAttribute("courseList", courseList);
        request.getRequestDispatcher("/WEB-INF/views/mo/my-project.jsp").forward(request, response);
    }

    /**
     * Displays detailed information for a specific course project.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_project_detail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Course> courseList = getCurrentMoCourseList(request);
        String courseIndexParam = request.getParameter("courseIndex");

        if (courseIndexParam == null) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            return;
        }

        try {
            int courseIndex = Integer.parseInt(courseIndexParam);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
                return;
            }

            Course selectedCourse = courseList.get(courseIndex);
            prepareProjectDetailAttributes(request, selectedCourse, String.valueOf(courseIndex));
            request.getRequestDispatcher("/WEB-INF/views/mo/project-detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
        }
    }

    /**
     * Publishes a course's TA recruitment by updating the course's job details.
     * MO modification lock is checked before proceeding.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void publish_course(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isMoModifyOpen(request)) {
            redirectMoModifyLocked(request, response);
            return;
        }

        String courseIndexParam = request.getParameter("courseIndex");
        List<Course> courseList = getCurrentMoCourseList(request);
        Course assignedCourse = moProjectService.getCourseByIndex(courseList, courseIndexParam);
        if (assignedCourse == null) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            return;
        }

        Mo mo = getCurrentMo(request);
        int taPositions = parseTaPositions(request.getParameter("taPositions"));
        if (taPositions <= 0) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                    + courseIndexParam + "&error=positions");
            return;
        }
        moProjectService.updateCourse(
                mo,
                assignedCourse,
                assignedCourse.getCourseName(),
                request.getParameter("jobTitle"),
                "",
                request.getParameter("jobDescription"),
                request.getParameter("jobRequirement"),
                taPositions);
        refreshCurrentMoSession(request, mo);

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                + courseIndexParam + "&success=1");
    }

    /**
     * Saves course information as a draft without publishing.
     * MO modification lock is checked before proceeding.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void save_course_draft(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isMoModifyOpen(request)) {
            redirectMoModifyLocked(request, response);
            return;
        }

        String courseIndexParam = request.getParameter("courseIndex");
        List<Course> courseList = getCurrentMoCourseList(request);
        Course assignedCourse = moProjectService.getCourseByIndex(courseList, courseIndexParam);
        if (assignedCourse == null) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            return;
        }

        if (assignedCourse.isRecruitmentPublished()) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                    + courseIndexParam + "&error=published");
            return;
        }

        Mo mo = getCurrentMo(request);
        moProjectService.saveCourseDraft(
                mo,
                assignedCourse,
                assignedCourse.getCourseName(),
                request.getParameter("jobTitle"),
                request.getParameter("jobDescription"),
                request.getParameter("jobRequirement"),
                parseTaPositions(request.getParameter("taPositions")));
        refreshCurrentMoSession(request, mo);

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                + courseIndexParam + "&draftSaved=1");
    }

    /**
     * Updates an already published course's recruitment information. If MO
     * modification is locked, an error is displayed on the project detail page
     * instead.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException      if redirection fails
     * @throws ServletException if forwarding fails
     */
    private void update_published_course(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException {
        if (!isMoModifyOpen(request)) {
            List<Course> courseList = getCurrentMoCourseList(request);
            String courseIndexParam = request.getParameter("courseIndex");
            if (courseIndexParam != null) {
                try {
                    int courseIndex = Integer.parseInt(courseIndexParam);
                    if (courseIndex >= 0 && courseIndex < courseList.size()) {
                        prepareProjectDetailAttributes(request, courseList.get(courseIndex), courseIndexParam);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            request.setAttribute("error", "The deadline for MO to modify course information has passed.");
            request.setAttribute("moModifyOpen", false);
            request.getRequestDispatcher("/WEB-INF/views/mo/project-detail.jsp").forward(request, response);
            return;
        }

        String courseIndexParam = request.getParameter("courseIndex");
        if (courseIndexParam == null) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            return;
        }

        try {
            int courseIndex = Integer.parseInt(courseIndexParam);

            String courseName = request.getParameter("courseName");
            String jobTitle = request.getParameter("jobTitle");
            String jobDescription = request.getParameter("jobDescription");
            String jobRequirement = request.getParameter("jobRequirement");

            List<Course> courseList = getCurrentMoCourseList(request);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
                return;
            }

            Course oldCourse = courseList.get(courseIndex);
            int taPositions = parseTaPositions(request.getParameter("taPositions"));
            if (taPositions <= 0) {
                taPositions = oldCourse.getTaPositions();
            }

            Mo mo = getCurrentMo(request);
            moProjectService.updateCourse(
                    mo,
                    oldCourse,
                    courseName,
                    jobTitle,
                    "",
                    jobDescription,
                    jobRequirement,
                    taPositions);
            refreshCurrentMoSession(request, mo);

            response.sendRedirect(
                request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex=" + courseIndex + "&success=1");

        } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            }
    }

    /**
     * Displays the review candidates page for a specific course, showing all submitted
     * application forms grouped by applicant email.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_review_candidates(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isReviewStageOpen(request)) {
            redirectReviewLocked(request, response);
            return;
        }

        List<Course> courseList = getCurrentMoCourseList(request);
        if (courseList.isEmpty()) {
            request.setAttribute("courseList", courseList);
            request.getRequestDispatcher("/WEB-INF/views/mo/review.jsp").forward(request, response);
            return;
        }

        int courseIndex = resolveCourseIndexForReview(request, courseList);
        Course course = courseList.get(courseIndex);
        request.setAttribute("courseList", courseList);
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", String.valueOf(courseIndex));
        request.setAttribute("applicationFormsByEmail", applicationReviewService.getSubmittedFormsByApplicantEmail(course));
        request.getRequestDispatcher("/WEB-INF/views/mo/review.jsp").forward(request, response);
    }

    /**
     * Saves the MO's selected TA candidates for a course without publishing the results.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void save_review_picks(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isReviewStageOpen(request)) {
            redirectReviewLocked(request, response);
            return;
        }

        Course course = getCourseForReview(request);
        if (course == null) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            return;
        }

        String courseIndex = request.getParameter("courseIndex");
        if (course.isReviewPublished()) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates&courseIndex="
                    + courseIndex + "&published=1");
            return;
        }

        applicationReviewService.saveReviewPicks(course, request.getParameterValues("pickedEmail"));
        syncCurrentMoCourse(request, course);
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates&courseIndex="
                + courseIndex + "&saved=1");
    }

    /**
     * Publishes the MO's review results for a course, notifying selected candidates.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void publish_review(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isReviewStageOpen(request)) {
            redirectReviewLocked(request, response);
            return;
        }

        Course course = getCourseForReview(request);
        if (course == null) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            return;
        }

        String courseIndex = request.getParameter("courseIndex");
        if (!course.isReviewPublished()) {
            boolean published = applicationReviewService.publishReview(course, request.getParameterValues("pickedEmail"));
            syncCurrentMoCourse(request, course);
            if (!published) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates&courseIndex="
                        + courseIndex + "&error=quota");
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates&courseIndex="
                + courseIndex + "&published=1");
    }

    /**
     * Retrieves the currently authenticated MO from the session.
     *
     * @param request the {@link HttpServletRequest}
     * @return the authenticated {@link Mo}, or {@code null} if not present
     */
    private Mo getCurrentMo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object currentUser = session.getAttribute("user");
        if (currentUser instanceof Mo mo) {
            return mo;
        }
        return null;
    }

    /**
     * Retrieves the list of courses owned by the current MO, refreshing from the data store.
     *
     * @param request the {@link HttpServletRequest}
     * @return the list of {@link Course} objects owned by the current MO
     */
    private List<Course> getCurrentMoCourseList(HttpServletRequest request) {
        Mo mo = getCurrentMo(request);
        if (mo != null) {
            List<Course> freshOwnedCourses = moProjectService.refreshOwnedCourses(mo);
            refreshCurrentMoSession(request, mo);
            return freshOwnedCourses;
        }
        return List.of();
    }

    /**
     * Retrieves the course being reviewed by the MO based on the course index parameter.
     *
     * @param request the {@link HttpServletRequest}
     * @return the {@link Course} being reviewed, or {@code null} if not found
     */
    private Course getCourseForReview(HttpServletRequest request) {
        List<Course> courseList = getCurrentMoCourseList(request);
        if (courseList.isEmpty()) {
            return null;
        }
        int courseIndex = resolveCourseIndexForReview(request, courseList);
        return courseList.get(courseIndex);
    }

    /**
     * Resolves the course index for the review page based on the request parameter.
     *
     * @param request    the {@link HttpServletRequest}
     * @param courseList the list of courses
     * @return the resolved course index
     */
    private int resolveCourseIndexForReview(HttpServletRequest request, List<Course> courseList) {
        return moProjectService.resolveCourseIndexForReview(request.getParameter("courseIndex"), courseList);
    }

    /**
     * Synchronises a course's data in the current MO's session after modifications.
     *
     * @param request the {@link HttpServletRequest}
     * @param course  the course to sync
     */
    private void syncCurrentMoCourse(HttpServletRequest request, Course course) {
        Mo mo = getCurrentMo(request);
        moProjectService.syncOwnedCourse(mo, course);
        refreshCurrentMoSession(request, mo);
    }

    /**
     * Refreshes the MO object stored in the current session.
     *
     * @param request the {@link HttpServletRequest}
     * @param mo      the {@link Mo} to store in the session
     */
    private void refreshCurrentMoSession(HttpServletRequest request, Mo mo) {
        if (mo == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("user", mo);
        }
    }

    /**
     * Prepares request attributes for the project detail page.
     *
     * @param request        the {@link HttpServletRequest}
     * @param selectedCourse the course being viewed
     * @param courseIndex    the course index for navigation
     */
    private void prepareProjectDetailAttributes(HttpServletRequest request, Course selectedCourse, String courseIndex) {
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("courseIndex", courseIndex);
        request.setAttribute("moModifyOpen", isMoModifyOpen(request));
        request.setAttribute("moModifyDeadline", resolveMoModifyDeadline(request));
    }

    /**
     * Checks whether the MO modification period is currently open.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@code true} if the MO modification deadline has not passed
     */
    private boolean isMoModifyOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveMoModifyDeadline(request);
        return deadlineService.isMoModifyOpen(deadline);
    }

    /**
     * Resolves the MO course modification deadline from the servlet context.
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

    /**
     * Logs out the current MO by invalidating the session and redirecting to the MO login page.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/mo");
    }

    /**
     * Checks whether the review stage is currently open.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@code true} if the application deadline has passed (review stage is open)
     */
    private boolean isReviewStageOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveApplicationDeadline(request);
        return deadlineService.isReviewStageOpen(deadline);
    }

    /**
     * Parses a positive TA position count from a request parameter.
     *
     * @param value the raw request parameter
     * @return a positive integer, or {@code 0} if missing/invalid
     */
    private int parseTaPositions(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Resolves the TA application deadline from the servlet context.
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
     * Redirects the MO to the personal centre with a review locked indicator.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void redirectReviewLocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=personal_center&reviewLocked=1");
    }

    /**
     * Redirects the MO to the dashboard with a modify locked indicator.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if redirection fails
     */
    private void redirectMoModifyLocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=dashboard&modifyLocked=1");
    }

}
