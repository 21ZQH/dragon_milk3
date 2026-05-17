package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import service.impl.ApplicationReviewServiceImpl;
import store.CourseStore;
import store.DeadlineStore;
import store.UserStore;

public class MOClassController extends HttpServlet {
    private final ApplicationReviewService applicationReviewService = new ApplicationReviewServiceImpl();

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
        } else if ("profile_center".equals(action)) {
            profile_center(request, response);
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
            if (!isMoModifyOpen(request)) {
                redirectMoModifyLocked(request, response);
                return;
            }
            if (!isMoProfileComplete(request)) {
                redirectProfileIncomplete(request, response);
                return;
            }
            List<Course> courseList = getCurrentMoCourseList(request);
            Course assignedCourse = getCourseByIndex(courseList, request.getParameter("courseIndex"));
            if (assignedCourse == null) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
                return;
            }

            String jobTitle = request.getParameter("jobTitle");
            String workingHours = request.getParameter("workingHours");
            String jobDescription = request.getParameter("jobDescription");
            String jobRequirement = request.getParameter("jobRequirement");

            Course updatedCourse = new Course(
                    assignedCourse.getId(),
                    assignedCourse.getCourseName(),
                    jobTitle,
                    workingHours,
                    assignedCourse.getSalary() == null ? "TBD" : assignedCourse.getSalary(),
                    jobDescription,
                    jobRequirement);
            updatedCourse.setPickedApplicantEmails(assignedCourse.getPickedApplicantEmails());
            updatedCourse.setReviewPublished(assignedCourse.isReviewPublished());
            updatedCourse.setRecruitmentPublished(true);
            CourseStore.updateCourse(updatedCourse);
            Mo mo = getCurrentMo(request);
            if (mo != null) {
                mo.replaceOwnedCourse(updatedCourse);
                request.getSession().setAttribute("user", mo);
            }
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=project_detail&courseIndex="
                    + request.getParameter("courseIndex") + "&success=1");

        } else if ("save_review_picks".equals(action)) {
            save_review_picks(request, response);
        } else if ("publish_review".equals(action)) {
            publish_review(request, response);

        } else if ("save_course_changes".equals(action)) {
            save_course_changes(request, response);

        } else if ("save_personal_information".equals(action)) {
            save_personal_information(request, response);

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
        request.setAttribute("moProfileComplete", isMoProfileComplete(request));
        request.setAttribute("moModifyOpen", isMoModifyOpen(request));
        request.setAttribute("showModifyLockedModal", "1".equals(request.getParameter("modifyLocked")));
        request.setAttribute("showProfileIncompleteModal", "1".equals(request.getParameter("profileIncomplete")));
        request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp").forward(request, response);
    }

    private void show_personal_center(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("reviewStageOpen", isReviewStageOpen(request));
        request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp").forward(request, response);
    }

    private void profile_center(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/mo/profile-center.jsp").forward(request, response);
    }

    private void save_personal_information(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Mo mo = getCurrentMo(request);
        if (mo == null) {
            response.sendRedirect(request.getContextPath() + "/mo");
            return;
        }

        mo.setName(trimValue(request.getParameter("name")));
        mo.setDegree(trimValue(request.getParameter("degree")));
        mo.setCollege(trimValue(request.getParameter("college")));
        UserStore.updateMoProfile(mo);

        request.getSession().setAttribute("user", mo);
        request.setAttribute("success", "Personal information saved successfully.");
        request.getRequestDispatcher("/WEB-INF/views/mo/profile-center.jsp").forward(request, response);
    }

    private void create_class(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isMoModifyOpen(request)) {
            redirectMoModifyLocked(request, response);
            return;
        }
        if (!isMoProfileComplete(request)) {
            redirectProfileIncomplete(request, response);
            return;
        }
        request.setAttribute("courseList", getCurrentMoCourseList(request));
        request.getRequestDispatcher("/WEB-INF/views/mo/create-project.jsp").forward(request, response);
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

        if (!isMoProfileComplete(request)) {
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

            request.setAttribute("error", "Please complete your personal information before creating or modifying course projects.");
            request.setAttribute("showProfileIncompleteModal", true);
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
            String workingHours = request.getParameter("workingHours");
            String jobDescription = request.getParameter("jobDescription");
            String jobRequirement = request.getParameter("jobRequirement");

            List<Course> courseList = getCurrentMoCourseList(request);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=my_project");
                return;
            }

            Course oldCourse = courseList.get(courseIndex);
            String salary = oldCourse.getSalary() == null ? "TBD" : oldCourse.getSalary();

            Course updatedCourse = new Course(
                oldCourse.getId(),
                oldCourse.getCourseName(),
                jobTitle,
                workingHours,
                salary,
                jobDescription,
                jobRequirement);
            updatedCourse.setPickedApplicantEmails(oldCourse.getPickedApplicantEmails());
            updatedCourse.setReviewPublished(oldCourse.isReviewPublished());
            updatedCourse.setRecruitmentPublished(true);

            CourseStore.updateCourse(updatedCourse);
            Mo mo = getCurrentMo(request);
            if (mo != null) {
                mo.replaceOwnedCourse(updatedCourse);
                request.getSession().setAttribute("user", mo);
            }       

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
            List<Course> allCourses = CourseStore.getCourseList();
            List<Course> freshOwnedCourses = new ArrayList<>();
            for (Course ownedCourse : mo.getOwnedCourses()) {
                if (ownedCourse == null || ownedCourse.getId() == null) {
                    continue;
                }
                for (Course course : allCourses) {
                    if (ownedCourse.getId().equals(course.getId())) {
                        freshOwnedCourses.add(course);
                        break;
                    }
                }
            }
            mo.setOwnedCourses(freshOwnedCourses);
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("user", mo);
            }
            return freshOwnedCourses;
        }
        return new ArrayList<>();
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
        String courseIndexParam = request.getParameter("courseIndex");
        if (courseIndexParam == null) {
            return 0;
        }

        try {
            int courseIndex = Integer.parseInt(courseIndexParam);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                return 0;
            }
            return courseIndex;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Course getCourseByIndex(List<Course> courseList, String courseIndexParam) {
        if (courseList == null || courseIndexParam == null) {
            return null;
        }
        try {
            int courseIndex = Integer.parseInt(courseIndexParam);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                return null;
            }
            return courseList.get(courseIndex);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void syncCurrentMoCourse(HttpServletRequest request, Course course) {
        Mo mo = getCurrentMo(request);
        if (mo == null || course == null) {
            return;
        }

        mo.replaceOwnedCourse(course);
        request.getSession().setAttribute("user", mo);
    }

    private void prepareProjectDetailAttributes(HttpServletRequest request, Course selectedCourse, String courseIndex) {
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("courseIndex", courseIndex);
        request.setAttribute("moProfileComplete", isMoProfileComplete(request));
        request.setAttribute("moModifyOpen", isMoModifyOpen(request));
        request.setAttribute("moModifyDeadline", resolveMoModifyDeadline(request));
    }

    private boolean isMoProfileComplete(HttpServletRequest request) {
        Mo mo = getCurrentMo(request);
        return mo != null && mo.hasCompleteProfile();
    }

    private boolean isMoModifyOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveMoModifyDeadline(request);
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    private LocalDateTime resolveMoModifyDeadline(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        if (servletContext != null) {
            Object deadline = servletContext.getAttribute("moCourseModifyDeadline");
            if (deadline instanceof LocalDateTime localDateTime) {
                return localDateTime;
            }
        }
        return DeadlineStore.getMoModifyDeadline();
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/mo");
    }

    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isReviewStageOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveApplicationDeadline(request);
        return deadline == null || LocalDateTime.now().isAfter(deadline);
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

    private void redirectReviewLocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=personal_center&reviewLocked=1");
    }

    private void redirectMoModifyLocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=dashboard&modifyLocked=1");
    }

    private void redirectProfileIncomplete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=dashboard&profileIncomplete=1");
    }
}


