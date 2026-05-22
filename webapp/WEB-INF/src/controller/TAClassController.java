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

/**
 * Servlet controller handling all TA (Teaching Assistant) operations within the
 * TA Recruitment System.
 * <p>
 * This controller manages TA workflows including viewing course details, submitting
 * and editing applications, uploading resumes, managing personal centres, and
 * generating application forms. It is configured with {@link MultipartConfig} to
 * support file upload functionality for resume submission.
 * </p>
 *
 * <p>Supported actions in GET requests:
 * <ul>
 *   <li>{@code logout} - invalidate the session and redirect</li>
 *   <li>{@code view_information} - view course information</li>
 *   <li>{@code home} - redirect to the TA entry page</li>
 *   <li>{@code show_all_information} - display full course details</li>
 *   <li>{@code go_apply} - navigate to the application form for a course</li>
 *   <li>{@code go_apply_by_id} - navigate to the application form by course ID</li>
 *   <li>{@code generate_application_form} - generate a new application form</li>
 *   <li>{@code edit_application_form} - edit an existing application form</li>
 *   <li>{@code view_master_resume} - view or download the master resume PDF</li>
 *   <li>{@code personal_centre} - display the TA personal centre</li>
 * </ul>
 * </p>
 *
 * <p>Supported actions in POST requests:
 * <ul>
 *   <li>{@code upload_resume} - upload a resume during application</li>
 *   <li>{@code upload_master_resume} - upload a master resume from personal centre</li>
 *   <li>{@code withdraw_application} - withdraw a submitted application</li>
 *   <li>{@code save_application_form} - save an application form as draft</li>
 *   <li>{@code submit_application_form} - submit an application form</li>
 * </ul>
 * </p>
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 1.0
 * @see ApplicationFormService
 * @see TAApplicationService
 * @see CourseService
 * @see DeadlineService
 */
@MultipartConfig
public class TAClassController extends HttpServlet {
    private final ApplicationFormService applicationFormService;
    private final TAApplicationService taApplicationService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;

    /**
     * Constructs a {@code TAClassController} with default service implementations.
     */
    public TAClassController() {
        this(
                new ApplicationFormServiceImpl(),
                new TAApplicationServiceImpl(new UserProfileServiceImpl(), new ResumeStorageServiceImpl()),
                new CourseServiceImpl(),
                new DeadlineServiceImpl());
    }

    /**
     * Constructs a {@code TAClassController} with the specified service instances.
     * <p>Package-private constructor used for dependency injection in unit tests.</p>
     *
     * @param applicationFormService the service for application form operations
     * @param taApplicationService   the service for TA application processing
     * @param courseService          the service for course data access
     * @param deadlineService        the service for deadline checks
     */
    TAClassController(ApplicationFormService applicationFormService, TAApplicationService taApplicationService,
            CourseService courseService, DeadlineService deadlineService) {
        this.applicationFormService = applicationFormService;
        this.taApplicationService = taApplicationService;
        this.courseService = courseService;
        this.deadlineService = deadlineService;
    }

    /**
     * Handles HTTP GET requests for TA operations.
     * <p>Dispatches to the appropriate handler based on the {@code action} parameter.
     * Most actions require an authenticated TA session.</p>
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be processed
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Handles HTTP POST requests for TA operations including file uploads.
     * <p>Dispatches to the appropriate handler based on the {@code action} parameter.
     * Most actions require an authenticated TA session.</p>
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be processed
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Ensures a valid TA session exists before processing the request.
     * <p>If the current user is a TA, their session data is refreshed. Otherwise,
     * they are either redirected to the login page or receive a 403 error.</p>
     *
     * @param request          the {@link HttpServletRequest}
     * @param response         the {@link HttpServletResponse}
     * @param redirectToLogin  whether to redirect to login on missing session
     * @return {@code true} if a valid TA session is present, {@code false} otherwise
     * @throws IOException if redirection or error sending fails
     */
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

    /**
     * Logs out the current user by invalidating the session and redirecting to the TA entry page.
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
        response.sendRedirect(request.getContextPath() + "/ta");
    }

    /**
     * Redirects the user to the TA entry page.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if redirection fails
     */
    private void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/ta");
    }

    /**
     * Displays course information after verifying the session and application deadline.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Displays detailed information for a specific course, including the application form status.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Navigates to the application page for the course selected in the session.
     * <p>If the application deadline has passed, the user is redirected to their
     * personal centre with an appropriate error message.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Navigates to the application page for a specific course identified by its course ID.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Generates a new application form for the selected course.
     * <p>Validates that the application deadline is still open, the TA has not exceeded
     * the application limit, and that a master resume has been uploaded before proceeding.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Edits an existing application form for a specific course.
     * <p>If no existing form is found, a new initial form is generated.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Streams the TA's master resume PDF file to the response output.
     * <p>Supports both inline viewing and attachment download via the {@code download} parameter.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if the resume file cannot be read or the response cannot be written
     */
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

    /**
     * Displays the TA's personal centre with applied courses, application statuses,
     * and resume management options.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void personal_centre(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        forwardPersonalCentre(request, response, ta, null, null, request.getParameter("courseId"));
    }

    /**
     * Handles resume upload during the application process.
     * <p>Validates the application deadline, application limits, and existing master resume
     * before allowing a new upload. If the TA already has a master resume, they are directed
     * to use the personal centre to replace it.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Saves an application form, either as a draft or as a submitted application.
     * <p>When {@code submitted} is {@code true}, the form is validated and submitted
     * through the TA application service. Otherwise it is saved as a draft.</p>
     *
     * @param request   the {@link HttpServletRequest}
     * @param response  the {@link HttpServletResponse}
     * @param submitted whether the form should be submitted or saved as a draft
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Handles master resume upload from the TA personal centre.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Withdraws a previously submitted application for a specific course.
     * <p>If the application deadline has passed, withdrawal is not permitted.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Retrieves the course selected by the user from the session based on the course index parameter.
     *
     * @param request the {@link HttpServletRequest}
     * @return the selected {@link Course}, or {@code null} if not found
     */
    private Course getCourseFromSession(HttpServletRequest request) {
        return taApplicationService.getCourseByIndex(
                getCourseListFromSession(request),
                request.getParameter("courseIndex"));
    }

    /**
     * Retrieves the list of courses from the session, loading it from the course service if not present.
     *
     * @param request the {@link HttpServletRequest}
     * @return the list of {@link Course} objects
     */
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

    /**
     * Retrieves a course by its course ID from the session course list.
     *
     * @param request  the {@link HttpServletRequest}
     * @param courseId the unique course identifier
     * @return the matching {@link Course}, or {@code null} if not found
     */
    private Course getCourseById(HttpServletRequest request, String courseId) {
        return taApplicationService.getCourseById(getCourseListFromSession(request), courseId);
    }

    /**
     * Finds the index of a course within the list by its course ID.
     *
     * @param courseList the list of courses
     * @param courseId   the unique course identifier
     * @return the index of the course, or {@code null} if not found
     */
    private Integer findCourseIndexById(List<Course> courseList, String courseId) {
        return taApplicationService.findCourseIndexById(courseList, courseId);
    }

    /**
     * Populates the request attributes with current resume information for the specified course.
     *
     * @param request the {@link HttpServletRequest}
     * @param course  the course being applied to
     */
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

    /**
     * Forwards to the TA personal centre page with the provided data and messages.
     *
     * @param request          the {@link HttpServletRequest}
     * @param response         the {@link HttpServletResponse}
     * @param ta               the authenticated TA
     * @param success          an optional success message, may be {@code null}
     * @param error            an optional error message, may be {@code null}
     * @param selectedCourseId the ID of the currently selected course, may be {@code null}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Forwards to the application form page with the provided form data and messages.
     *
     * @param request     the {@link HttpServletRequest}
     * @param response    the {@link HttpServletResponse}
     * @param course      the course being applied to
     * @param courseIndex the course index for navigation
     * @param form        the {@link ApplicationForm} to display
     * @param success     an optional success message, may be {@code null}
     * @param error       an optional error message, may be {@code null}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Forwards to the course details page with basic application status information.
     *
     * @param request         the {@link HttpServletRequest}
     * @param response        the {@link HttpServletResponse}
     * @param course          the course to display
     * @param courseIndex     the course index for navigation
     * @param applicationOpen whether the application period is open
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void forwardCourseDetails(HttpServletRequest request, HttpServletResponse response, Course course,
            String courseIndex, boolean applicationOpen) throws ServletException, IOException {
        forwardCourseDetails(request, response, course, courseIndex, applicationOpen, null);
    }

    /**
     * Forwards to the course details page with application status and an optional error message.
     *
     * @param request         the {@link HttpServletRequest}
     * @param response        the {@link HttpServletResponse}
     * @param course          the course to display
     * @param courseIndex     the course index for navigation
     * @param applicationOpen whether the application period is open
     * @param error           an optional error message, may be {@code null}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Checks whether the TA has uploaded a master resume.
     *
     * @param ta the TA to check
     * @return {@code true} if a master resume exists
     */
    private boolean hasMasterResume(TA ta) {
        return taApplicationService.hasMasterResume(ta);
    }

    /**
     * Checks whether the application period is currently open.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@code true} if the deadline has not passed or no deadline is set
     */
    private boolean isApplicationOpen(HttpServletRequest request) {
        return deadlineService.isApplicationOpen(resolveApplicationDeadline(request));
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

}
