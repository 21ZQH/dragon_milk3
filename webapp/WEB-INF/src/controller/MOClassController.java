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

public class MOClassController extends HttpServlet {
    private final ApplicationReviewService applicationReviewService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;
    private final MOProjectService moProjectService;

    public MOClassController() {
        this(
                new ApplicationReviewServiceImpl(),
                new CourseServiceImpl(),
                new DeadlineServiceImpl());
    }

    MOClassController(ApplicationReviewService applicationReviewService) {
        this(
                applicationReviewService,
                new CourseServiceImpl(),
                new DeadlineServiceImpl());
    }

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

        } else if ("save_course_changes".equals(action)) {
            save_course_changes(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }

    }

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

    private void show_personal_center(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("reviewStageOpen", isReviewStageOpen(request));
        request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp").forward(request, response);
    }

    private void create_class(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isMoModifyOpen(request)) {
            redirectMoModifyLocked(request, response);
            return;
        }
        show_my_project(request, response);
    }

    private void show_my_project(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Course> courseList = getCurrentMoCourseList(request);
        request.setAttribute("courseList", courseList);
        request.getRequestDispatcher("/WEB-INF/views/mo/my-project.jsp").forward(request, response);
    }

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
        moProjectService.updateCourse(
                mo,
                assignedCourse,
                assignedCourse.getCourseName(),
                request.getParameter("jobTitle"),
                "",
                request.getParameter("jobDescription"),
                request.getParameter("jobRequirement"));
        refreshCurrentMoSession(request, mo);

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                + courseIndexParam + "&success=1");
    }

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

        Mo mo = getCurrentMo(request);
        moProjectService.saveCourseDraft(
                mo,
                assignedCourse,
                assignedCourse.getCourseName(),
                request.getParameter("jobTitle"),
                request.getParameter("jobDescription"),
                request.getParameter("jobRequirement"));
        refreshCurrentMoSession(request, mo);

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                + courseIndexParam + "&draftSaved=1");
    }

    private void save_course_changes(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException {
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

            Mo mo = getCurrentMo(request);
            moProjectService.updateCourse(
                    mo,
                    courseList.get(courseIndex),
                    courseName,
                    jobTitle,
                    "",
                    jobDescription,
                    jobRequirement);
            refreshCurrentMoSession(request, mo);

            response.sendRedirect(
                request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex=" + courseIndex + "&success=1");

        } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
            }
    }


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
            applicationReviewService.publishReview(course, request.getParameterValues("pickedEmail"));
            syncCurrentMoCourse(request, course);
        }

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates&courseIndex="
                + courseIndex + "&published=1");
    }

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

    private List<Course> getCurrentMoCourseList(HttpServletRequest request) {
        Mo mo = getCurrentMo(request);
        if (mo != null) {
            List<Course> freshOwnedCourses = moProjectService.refreshOwnedCourses(mo);
            refreshCurrentMoSession(request, mo);
            return freshOwnedCourses;
        }
        return List.of();
    }

    private Course getCourseForReview(HttpServletRequest request) {
        List<Course> courseList = getCurrentMoCourseList(request);
        if (courseList.isEmpty()) {
            return null;
        }
        int courseIndex = resolveCourseIndexForReview(request, courseList);
        return courseList.get(courseIndex);
    }

    private int resolveCourseIndexForReview(HttpServletRequest request, List<Course> courseList) {
        return moProjectService.resolveCourseIndexForReview(request.getParameter("courseIndex"), courseList);
    }

    private void syncCurrentMoCourse(HttpServletRequest request, Course course) {
        Mo mo = getCurrentMo(request);
        moProjectService.syncOwnedCourse(mo, course);
        refreshCurrentMoSession(request, mo);
    }

    private void refreshCurrentMoSession(HttpServletRequest request, Mo mo) {
        if (mo == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("user", mo);
        }
    }

    private void prepareProjectDetailAttributes(HttpServletRequest request, Course selectedCourse, String courseIndex) {
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("courseIndex", courseIndex);
        request.setAttribute("moModifyOpen", isMoModifyOpen(request));
        request.setAttribute("moModifyDeadline", resolveMoModifyDeadline(request));
    }

    private boolean isMoModifyOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveMoModifyDeadline(request);
        return deadlineService.isMoModifyOpen(deadline);
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

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/mo");
    }

    private boolean isReviewStageOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveApplicationDeadline(request);
        return deadlineService.isReviewStageOpen(deadline);
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

    private void redirectReviewLocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=personal_center&reviewLocked=1");
    }

    private void redirectMoModifyLocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=dashboard&modifyLocked=1");
    }

}
