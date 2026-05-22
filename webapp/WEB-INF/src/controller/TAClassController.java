package controller;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.TA;
import model.User;
import service.ApplicationFormService;
import service.CourseService;
import service.DeadlineService;
import service.TAApplicationService;
import service.impl.ApplicationFormServiceImpl;
import service.impl.CourseServiceImpl;
import service.impl.DeadlineServiceImpl;
import service.impl.ResumeStorageServiceImpl;
import service.impl.TAApplicationServiceImpl;
import service.impl.UserProfileServiceImpl;

@MultipartConfig
public class TAClassController extends HttpServlet {
    private final ApplicationFormService applicationFormService;
    private final TAApplicationService taApplicationService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;

    public TAClassController() {
        this(
                new ApplicationFormServiceImpl(),
                new TAApplicationServiceImpl(new UserProfileServiceImpl(), new ResumeStorageServiceImpl()),
                new CourseServiceImpl(),
                new DeadlineServiceImpl());
    }

    TAClassController(ApplicationFormService applicationFormService, TAApplicationService taApplicationService,
            CourseService courseService, DeadlineService deadlineService) {
        this.applicationFormService = applicationFormService;
        this.taApplicationService = taApplicationService;
        this.courseService = courseService;
        this.deadlineService = deadlineService;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if (!ensureTaSession(request, response, true)) {
            return;
        }

        if ("view_information".equals(action)) {
            view_information(request, response);
        } else if ("home".equals(action)) {
            home(request, response);
        } else if ("show_all_information".equals(action)) {
            show_all_information(request, response);
        } else if ("go_apply".equals(action)) {
            go_apply(request, response);
        } else if ("go_apply_by_id".equals(action)) {
            go_apply_by_id(request, response);
        } else if ("generate_application_form".equals(action)) {
            generate_application_form(request, response);
        } else if ("edit_application_form".equals(action)) {
            edit_application_form(request, response);
        } else if ("view_master_resume".equals(action)) {
            view_master_resume(request, response);
        } else if ("personal_centre".equals(action)) {
            personal_centre(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if (!ensureTaSession(request, response, false)) {
            return;
        }

        if ("upload_resume".equals(action)) {
            upload_resume(request, response);
        } else if ("upload_master_resume".equals(action)) {
            upload_master_resume(request, response);
        } else if ("withdraw_application".equals(action)) {
            withdraw_application(request, response);
        } else if ("save_application_form".equals(action)) {
            save_application_form(request, response, false);
        } else if ("submit_application_form".equals(action)) {
            save_application_form(request, response, true);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    private boolean ensureTaSession(HttpServletRequest request, HttpServletResponse response, boolean redirectToLogin)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession();
        }

        Object currentUser = session == null ? null : session.getAttribute("user");
        if (currentUser instanceof TA ta) {
            session.setAttribute("user", taApplicationService.refreshTa(ta));
            return true;
        }

        if (currentUser == null || redirectToLogin) {
            response.sendRedirect(request.getContextPath() + "/ta?action=auth");
            return false;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: TA role required");
        return false;
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/ta");
    }

    private void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/ta");
    }

    private void view_information(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA)) {
            response.sendRedirect(request.getContextPath() + "/ta?action=auth");
            return;
        }

        if (!isApplicationOpen(request)) {
            response.sendRedirect(request.getContextPath() + "/ta");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/ta");
    }

    private void show_all_information(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Course course = getCourseFromSession(request);
        if (course == null) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", request.getParameter("courseIndex"));
        request.setAttribute("applicationOpen", isApplicationOpen(request));
    request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
    }

    private void go_apply(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Course course = getCourseFromSession(request);
        if (course == null) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        if (!isApplicationOpen(request) && user instanceof TA ta) {
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "The application deadline has passed. You can no longer submit or modify applications.",
                    course.getId());
            return;
        }

        populateCurrentResumeAttributes(request, course);
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", request.getParameter("courseIndex"));
        request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
    }

    private void go_apply_by_id(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String courseId = request.getParameter("courseId");
        Course course = getCourseById(request, courseId);
        Integer courseIndex = findCourseIndexById(getCourseListFromSession(request), courseId);

        if (course == null || courseIndex == null) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=personal_centre");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        if (!isApplicationOpen(request) && user instanceof TA ta) {
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "The application deadline has passed. You can no longer submit or modify applications.",
                    course.getId());
            return;
        }

        populateCurrentResumeAttributes(request, course);
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", String.valueOf(courseIndex));
        request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
    }

    private void generate_application_form(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Course course = getCourseFromSession(request);
        User user = (User) request.getSession().getAttribute("user");
        if (course == null || !(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        if (!isApplicationOpen(request)) {
            forwardCourseDetails(request, response, course, request.getParameter("courseIndex"), false);
            return;
        }

        TAApplicationService.SubmitApplicationResult applicationLimitResult =
                taApplicationService.validateApplicationLimit(ta, course);
        if (!applicationLimitResult.isSuccess()) {
            forwardCourseDetails(request, response, course, request.getParameter("courseIndex"), true,
                    applicationLimitResult.getErrorMessage());
            return;
        }

        if (!hasMasterResume(ta)) {
            populateCurrentResumeAttributes(request, course);
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.setAttribute("error", "Please upload your resume before generating the application form.");
            request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
            return;
        }

        ApplicationForm form = applicationFormService.generateInitialForm(ta, course);
        forwardApplicationForm(request, response, course, request.getParameter("courseIndex"), form, null, null);
    }

    private void edit_application_form(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String courseId = request.getParameter("courseId");
        Course course = getCourseById(request, courseId);
        Integer courseIndex = findCourseIndexById(getCourseListFromSession(request), courseId);
        User user = (User) request.getSession().getAttribute("user");
        if (course == null || courseIndex == null || !(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=personal_centre");
            return;
        }

        if (!isApplicationOpen(request)) {
            forwardCourseDetails(request, response, course, String.valueOf(courseIndex), false);
            return;
        }

        ApplicationForm form = applicationFormService.getForm(ta.getEmail(), course.getId());
        if (form == null) {
            form = applicationFormService.generateInitialForm(ta, course);
        }
        forwardApplicationForm(request, response, course, String.valueOf(courseIndex), form, null, null);
    }

    private void view_master_resume(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        String resumeDirectory = ta.getMasterResumeDirectory();
        if (resumeDirectory == null || resumeDirectory.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume not found");
            return;
        }

        File resumeFile = taApplicationService.getMasterResumeFile(ta);
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume file not found");
            return;
        }

        boolean download = "true".equalsIgnoreCase(request.getParameter("download"));
        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                (download ? "attachment" : "inline") + "; filename=\""
                        + taApplicationService.getStoredResumeFileName(ta) + "\"");

        try (InputStream inputStream = new FileInputStream(resumeFile);
                OutputStream outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        }
    }

    private void personal_centre(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        forwardPersonalCentre(request, response, ta, null, null, request.getParameter("courseId"));
    }

    private void upload_resume(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Course course = getCourseFromSession(request);
        User user = (User) session.getAttribute("user");

        if (course == null || !(user instanceof TA)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        TA ta = (TA) user;
        if (!isApplicationOpen(request)) {
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "The application deadline has passed. You can no longer submit or modify applications.",
                    course.getId());
            return;
        }

        TAApplicationService.SubmitApplicationResult applicationLimitResult =
                taApplicationService.validateApplicationLimit(ta, course);
        if (!applicationLimitResult.isSuccess()) {
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.setAttribute("error", applicationLimitResult.getErrorMessage());
            request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
            return;
        }

        Part resumePart = request.getPart("resumeFile");
        boolean hasMasterResume = ta.getMasterResumeDirectory() != null && !ta.getMasterResumeDirectory().isBlank();
        if (hasMasterResume && (resumePart == null || resumePart.getSize() <= 0)) {
            ApplicationForm form = applicationFormService.generateInitialForm(ta, course);
            forwardApplicationForm(request, response, course, request.getParameter("courseIndex"), form, null, null);
            return;
        }

        if (hasMasterResume && resumePart != null && resumePart.getSize() > 0) {
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.setAttribute("hasMasterResume", hasMasterResume(ta));
            request.setAttribute("masterResumeFileName", taApplicationService.getStoredResumeFileName(ta));
            request.setAttribute("error", "Please replace your resume from Personal Centre.");
            request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
            return;
        }

        if ((resumePart == null || resumePart.getSize() <= 0)
                && !hasMasterResume) {
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.setAttribute("error", "Please upload your resume before submitting.");
            request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
            return;
        }

        String resumeName = "profile resume";
        if (resumePart != null && resumePart.getSize() > 0) {
            TAApplicationService.SubmitResumeResult uploadResult =
                    taApplicationService.uploadMasterResume(ta, resumePart);
            if (!uploadResult.isSuccess()) {
                request.setAttribute("selectedCourse", course);
                request.setAttribute("courseIndex", request.getParameter("courseIndex"));
                request.setAttribute("error", uploadResult.getErrorMessage());
                request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
                return;
            }
            resumeName = uploadResult.getSubmittedFileName();
        }

        session.setAttribute("user", ta);
        ApplicationForm form = applicationFormService.generateInitialForm(ta, course);
        forwardApplicationForm(request, response, course, request.getParameter("courseIndex"), form,
                "Resume uploaded successfully: " + resumeName, null);
    }

    private void save_application_form(HttpServletRequest request, HttpServletResponse response, boolean submitted)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        String courseId = request.getParameter("courseId");
        Course course = getCourseById(request, courseId);
        Integer courseIndex = findCourseIndexById(getCourseListFromSession(request), courseId);
        if (course == null || courseIndex == null) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        if (!isApplicationOpen(request)) {
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "The application deadline has passed. You can no longer submit or modify applications.",
                    course.getId());
            return;
        }

        ApplicationForm form = applicationFormService.buildFormFromRequest(
                ta,
                course,
                request.getParameter("applicantName"),
                request.getParameter("email"),
                request.getParameter("education"),
                request.getParameter("skills"),
                request.getParameter("relevantExperience"),
                request.getParameter("projectExperience"),
                request.getParameter("feedback"),
                submitted);
        applicationFormService.saveForm(form);

        if (submitted) {
            TAApplicationService.SubmitApplicationResult submitResult =
                    taApplicationService.submitApplicationForm(ta, course, form);
            if (!submitResult.isSuccess()) {
                forwardApplicationForm(request, response, course, String.valueOf(courseIndex), form,
                        null, submitResult.getErrorMessage());
                return;
            }
            session.setAttribute("user", ta);
            request.setAttribute("success", "Application form submitted successfully.");
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", String.valueOf(courseIndex));
            request.setAttribute("applicationOpen", isApplicationOpen(request));
            request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
            return;
        }

        forwardApplicationForm(request, response, course, String.valueOf(courseIndex), form,
                "Application form saved.", null);
    }

    private void upload_master_resume(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/ta?action=auth");
            return;
        }

        Part resumePart = request.getPart("resumeFile");
        TAApplicationService.SubmitResumeResult uploadResult = taApplicationService.uploadMasterResume(ta, resumePart);
        if (!uploadResult.isSuccess()) {
            forwardPersonalCentre(request, response, ta, null, uploadResult.getErrorMessage(), request.getParameter("courseId"));
            return;
        }

        session.setAttribute("user", ta);
        forwardPersonalCentre(request, response, ta,
                "Resume uploaded successfully: " + uploadResult.getSubmittedFileName(), null, request.getParameter("courseId"));
    }

    private void withdraw_application(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        String courseId = request.getParameter("courseId");
        if (courseId == null || courseId.isBlank()) {
            request.setAttribute("error", "Course id is required.");
            forwardPersonalCentre(request, response, ta, null, "Course id is required.", null);
            return;
        }

        if (!isApplicationOpen(request)) {
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "The application deadline has passed. You can no longer withdraw or modify applications.",
                    courseId);
            return;
        }

        Course course = getCourseById(request, courseId);
        taApplicationService.withdrawApplication(ta, course);
        session.setAttribute("user", ta);

        if ("course_detail".equals(request.getParameter("returnTo")) && course != null) {
            Integer courseIndex = findCourseIndexById(getCourseListFromSession(request), courseId);
            request.setAttribute("success", "Application withdrawn successfully.");
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", courseIndex == null ? null : String.valueOf(courseIndex));
            request.setAttribute("applicationOpen", isApplicationOpen(request));
            request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
            return;
        }

        forwardPersonalCentre(request, response, ta, "Application withdrawn successfully.", null, null);
    }
    private Course getCourseFromSession(HttpServletRequest request) {
        return taApplicationService.getCourseByIndex(
                getCourseListFromSession(request),
                request.getParameter("courseIndex"));
    }

    @SuppressWarnings("unchecked")
    private List<Course> getCourseListFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<Course> courseList = (List<Course>) session.getAttribute("courseList");
        if (courseList == null) {
            courseList = courseService.getCourseList();
            session.setAttribute("courseList", courseList);
        }
        return courseList;
    }

    private Course getCourseById(HttpServletRequest request, String courseId) {
        return taApplicationService.getCourseById(getCourseListFromSession(request), courseId);
    }

    private Integer findCourseIndexById(List<Course> courseList, String courseId) {
        return taApplicationService.findCourseIndexById(courseList, courseId);
    }

    private void populateCurrentResumeAttributes(HttpServletRequest request, Course course) {
        if (course == null) {
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            return;
        }

        TAApplicationService.CurrentApplicationData applicationData =
                taApplicationService.prepareCurrentApplicationData(ta, course);
        request.setAttribute("hasMasterResume", applicationData.hasMasterResume());
        request.setAttribute("masterResumeFileName", applicationData.getMasterResumeFileName());
        if (applicationData.hasCurrentApplication()) {
            request.setAttribute("hasCurrentResume", true);
            request.setAttribute("currentResumeFileName", applicationData.getCurrentApplicationFileName());
        }
    }

    private void forwardPersonalCentre(HttpServletRequest request, HttpServletResponse response, TA ta,
            String success, String error, String selectedCourseId) throws ServletException, IOException {
        TAApplicationService.PersonalCentreData personalCentreData =
                taApplicationService.preparePersonalCentreData(ta, selectedCourseId, isApplicationOpen(request));
        request.getSession().setAttribute("user", ta);
        Course selectedCourse = personalCentreData.getSelectedCourse();

        request.setAttribute("appliedCourses", personalCentreData.getAppliedCourses());
        request.setAttribute("applicationCount", personalCentreData.getApplicationCount());
        request.setAttribute("applicationLimit", personalCentreData.getApplicationLimit());
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("selectedCourseId", selectedCourse == null ? null : selectedCourse.getId());
        request.setAttribute("unreadReviewCourseIds", personalCentreData.getUnreadReviewCourseIds());
        request.setAttribute("applicationOpen", personalCentreData.isApplicationOpen());
        request.setAttribute("applicationDeadline", resolveApplicationDeadline(request));
        request.setAttribute("hasMasterResume", taApplicationService.hasMasterResume(ta));
        request.setAttribute("masterResumeFileName", taApplicationService.getStoredResumeFileName(ta));
        if (personalCentreData.getSelectedStatus() != null) {
            request.setAttribute("selectedStatus", personalCentreData.getSelectedStatus());
        }
        if (success != null) {
            request.setAttribute("success", success);
        }
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp").forward(request, response);
    }

    private void forwardApplicationForm(HttpServletRequest request, HttpServletResponse response, Course course,
            String courseIndex, ApplicationForm form, String success, String error) throws ServletException, IOException {
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", courseIndex);
        request.setAttribute("applicationForm", form);
        request.setAttribute("applicationOpen", isApplicationOpen(request));
        if (success != null) {
            request.setAttribute("success", success);
        }
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher("/WEB-INF/views/ta/application-form.jsp").forward(request, response);
    }

    private void forwardCourseDetails(HttpServletRequest request, HttpServletResponse response, Course course,
            String courseIndex, boolean applicationOpen) throws ServletException, IOException {
        forwardCourseDetails(request, response, course, courseIndex, applicationOpen, null);
    }

    private void forwardCourseDetails(HttpServletRequest request, HttpServletResponse response, Course course,
            String courseIndex, boolean applicationOpen, String error) throws ServletException, IOException {
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", courseIndex);
        request.setAttribute("applicationOpen", applicationOpen);
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
    }

    private boolean hasMasterResume(TA ta) {
        return taApplicationService.hasMasterResume(ta);
    }

    private boolean isApplicationOpen(HttpServletRequest request) {
        return deadlineService.isApplicationOpen(resolveApplicationDeadline(request));
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

}






