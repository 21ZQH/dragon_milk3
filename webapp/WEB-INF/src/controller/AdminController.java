package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.TA;
import model.User;
import service.AdminManagementService;
import service.CourseService;
import service.DeadlineService;
import service.impl.AdminManagementServiceImpl;
import service.impl.CourseServiceImpl;
import service.impl.DeadlineServiceImpl;

/**
 * Servlet controller handling all Admin operations within the TA Recruitment System.
 * <p>
 * This controller manages administrative workflows including MO account management,
 * candidate (TA) management, deadline configuration for both TA applications and
 * MO course modifications, recruitment cycle resets, and resume viewing.
 * All actions require an authenticated Admin session with the "Admin" role.
 *
 *
 * <p>Supported actions in GET requests:
 * <ul>
 *   <li>{@code dashboard} - display the admin dashboard</li>
 *   <li>{@code manage_mo} - manage MO accounts and course assignments</li>
 *   <li>{@code candidate_management} - view and manage TA candidates</li>
 *   <li>{@code view_resume} - view a TA's uploaded resume PDF</li>
 *   <li>{@code set_deadline} - display the TA application deadline configuration page</li>
 *   <li>{@code set_mo_deadline} - display the MO modification deadline configuration page</li>
 *   <li>{@code reset_cycle} - display the recruitment cycle reset page</li>
 * </ul>
 *
 *
 * <p>Supported actions in POST requests:
 * <ul>
 *   <li>{@code save_deadline} - save the TA application deadline</li>
 *   <li>{@code save_mo_deadline} - save the MO course modification deadline</li>
 *   <li>{@code create_mo} - create a new MO account with course assignments</li>
 *   <li>{@code reset_cycle_confirm} - confirm and execute the recruitment cycle reset</li>
 * </ul>
 *
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 1.0
 * @see AdminManagementService
 * @see CourseService
 * @see DeadlineService
 */
public class AdminController extends HttpServlet {
    private final AdminManagementService adminManagementService;
    private final CourseService courseService;
    private final DeadlineService deadlineService;

    /**
     * Constructs an {@code AdminController} with default service implementations.
     */
    public AdminController() {
        this(new AdminManagementServiceImpl(), new CourseServiceImpl(), new DeadlineServiceImpl());
    }

    /**
     * Constructs an {@code AdminController} with the specified admin management service
     * and default implementations for other services.
     * <p>Package-private constructor used for dependency injection in unit tests.</p>
     *
     * @param adminManagementService the service for admin management operations
     */
    AdminController(AdminManagementService adminManagementService) {
        this(adminManagementService, new CourseServiceImpl(), new DeadlineServiceImpl());
    }

    /**
     * Constructs an {@code AdminController} with all specified service instances.
     *
     * @param adminManagementService the service for admin management operations
     * @param courseService          the service for course data access
     * @param deadlineService        the service for deadline checks and persistence
     */
    AdminController(AdminManagementService adminManagementService, CourseService courseService,
            DeadlineService deadlineService) {
        this.adminManagementService = adminManagementService;
        this.courseService = courseService;
        this.deadlineService = deadlineService;
    }

    /**
     * Handles HTTP GET requests for Admin operations.
     * <p>All actions require a valid Admin session. Dispatches to the appropriate
     * handler based on the {@code action} parameter.</p>
     *
     * @param request  the {@link HttpServletRequest} containing the client request
     * @param response the {@link HttpServletResponse} containing the response
     * @throws ServletException if the request cannot be processed
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"Admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required");
            return;
        }

        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if ("dashboard".equals(action)) {
            show_dashboard(request, response);
        } else if ("manage_mo".equals(action)) {
            manage_mo(request, response);
        } else if ("candidate_management".equals(action)) {
            manage_candidates(request, response);
        } else if ("view_resume".equals(action)) {
            view_resume(request, response);
        }else if ("set_deadline".equals(action)) {
            show_set_deadline(request, response);
        } else if ("set_mo_deadline".equals(action)) {
            show_set_mo_deadline(request, response);
        } else if ("reset_cycle".equals(action)) {
            show_reset_cycle(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    /**
     * Handles HTTP POST requests for Admin operations.
     * <p>All actions require a valid Admin session. Dispatches to the appropriate
     * handler based on the {@code action} parameter.</p>
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

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"Admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required");
            return;
        }

        if ("save_deadline".equals(action)) {
            save_deadline(request, response);
        } else if ("save_mo_deadline".equals(action)) {
            save_mo_deadline(request, response);
        } else if ("create_mo".equals(action)) {
            create_mo(request, response);
        } else if ("reset_cycle_confirm".equals(action)) {
            reset_cycle(request, response);
        } else {
            doGet(request, response);
        }
    }

    /**
     * Displays the admin dashboard page.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_dashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }

    /**
     * Displays the candidate (TA) management page with the list of all TA users.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void manage_candidates(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<TA> taList = adminManagementService.getTAList();
        request.setAttribute("taList", taList);
        request.getRequestDispatcher("/WEB-INF/views/admin/candidate-management.jsp").forward(request, response);
    }

    /**
     * Displays the MO management page with the list of MO accounts and all courses.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void manage_mo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("moList", adminManagementService.getMOList());
        request.setAttribute("courseList", courseService.getCourseList());
        request.getRequestDispatcher("/WEB-INF/views/admin/mo-management.jsp").forward(request, response);
    }

    /**
     * Displays the TA application deadline configuration page with the currently saved deadline.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_set_deadline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object savedDeadline = getServletContext().getAttribute("applicationDeadline");
        request.setAttribute("savedDeadline", savedDeadline);
        request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp").forward(request, response);
    }

    /**
     * Displays the MO course modification deadline configuration page with the currently saved deadline.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_set_mo_deadline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object savedMoDeadline = getServletContext().getAttribute("moCourseModifyDeadline");
        request.setAttribute("savedMoDeadline", savedMoDeadline);
        request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp").forward(request, response);
    }

    /**
     * Displays the recruitment cycle reset confirmation page.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void show_reset_cycle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin/reset-cycle.jsp").forward(request, response);
    }

    /**
     * Saves the TA application deadline parsed from the request parameters.
     * <p>Validates that both date and time are provided and correctly formatted
     * before persisting the deadline through the service layer and updating the
     * servlet context attribute.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void save_deadline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String deadlineDate = request.getParameter("deadlineDate");
        String deadlineTime = request.getParameter("deadlineTime");

        if (deadlineDate == null || deadlineDate.isBlank() || deadlineTime == null || deadlineTime.isBlank()) {
            request.setAttribute("error", "Please complete both deadline date and deadline time.");
            request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp").forward(request, response);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(deadlineDate);
            LocalTime time = LocalTime.parse(deadlineTime);
            LocalDateTime deadline = LocalDateTime.of(date, time);

            deadlineService.saveApplicationDeadline(deadline);
            getServletContext().setAttribute("applicationDeadline", deadline);

            request.setAttribute("success", "TA resume submission deadline has been saved successfully.");
            request.setAttribute("savedDeadline", deadline);
            request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Invalid deadline format.");
            request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp").forward(request, response);
        }
    }

    /**
     * Saves the MO course modification deadline parsed from the request parameters.
     * <p>Validates that both date and time are provided and correctly formatted
     * before persisting the deadline through the service layer and updating the
     * servlet context attribute.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void save_mo_deadline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String deadlineDate = request.getParameter("deadlineDate");
        String deadlineTime = request.getParameter("deadlineTime");

        if (deadlineDate == null || deadlineDate.isBlank() || deadlineTime == null || deadlineTime.isBlank()) {
            request.setAttribute("error", "Please complete both deadline date and deadline time.");
            request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp").forward(request, response);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(deadlineDate);
            LocalTime time = LocalTime.parse(deadlineTime);
            LocalDateTime deadline = LocalDateTime.of(date, time);

            deadlineService.saveMoModifyDeadline(deadline);
            getServletContext().setAttribute("moCourseModifyDeadline", deadline);

            request.setAttribute("success", "MO course modification deadline has been saved successfully.");
            request.setAttribute("savedMoDeadline", deadline);
            request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Invalid deadline format.");
            request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp").forward(request, response);
        }
    }

    /**
     * Creates a new MO account with the specified account credentials and course assignments.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void create_mo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AdminManagementService.CreateMoResult result = adminManagementService.createMoAccount(
                request.getParameter("account"),
                request.getParameter("password"),
                request.getParameter("courseNames"));

        request.setAttribute(result.isSuccess() ? "success" : "error", result.getMessage());
        if (result.isSuccess()) {
            request.setAttribute("generatedCourseDrafts", result.getAssignedCourses());
        }
        manage_mo(request, response);
    }

    /**
     * Resets the entire recruitment cycle after confirmation.
     * <p>Requires the {@code confirmation} parameter to be the literal string "RESET".
     * On success, the servlet context deadline attributes are removed and the dashboard
     * is displayed with a notice.</p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void reset_cycle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String confirmation = request.getParameter("confirmation");
        if (!"RESET".equals(confirmation)) {
            request.setAttribute("error", "Please type RESET to confirm the recruitment cycle reset.");
            show_reset_cycle(request, response);
            return;
        }

        AdminManagementService.ResetRecruitmentCycleResult result =
                adminManagementService.resetRecruitmentCycle();
        if (result.isSuccess()) {
            getServletContext().removeAttribute("applicationDeadline");
            getServletContext().removeAttribute("moCourseModifyDeadline");
            request.setAttribute("notice", result.getMessage());
            show_dashboard(request, response);
            return;
        }

        request.setAttribute("error", result.getMessage());
        show_reset_cycle(request, response);
    }

    /**
     * Resolves the base directory path for storing uploaded resume files.
     * <p>Attempts to use the Tomcat {@code catalina.base} system property first,
     * falling back to the current working directory if not available.</p>
     *
     * @return the absolute path to the resume storage directory
     */
    private String resolveResumeUploadDirectory() {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return catalinaBase + File.separator + "webapps" + File.separator + "SE"
                 + File.separator + "WEB-INF" + File.separator + "file" + File.separator + "resume";
        }
            return System.getProperty("user.dir") + File.separator + "webapp"
                + File.separator + "WEB-INF" + File.separator + "file" + File.separator + "resume";
    }

    /**
     * Locates the resume file for a specific TA applicant and course.
     *
     * @param email    the TA applicant's email address
     * @param courseId the course identifier
     * @return a {@link File} representing the resume PDF, or {@code null} if parameters are invalid
     */
    private File getResumeFile(String email, String courseId) {
        if (email == null || courseId == null) return null;


        String normalizedEmail = email.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filePath = resolveResumeUploadDirectory() + File.separator + courseId + File.separator + normalizedEmail + ".pdf";

        return new File(filePath);
    }

    /**
     * Streams a TA's resume PDF file to the response for inline viewing.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws IOException if the resume file cannot be read or the response cannot be written
     */
    private void view_resume(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String applicantEmail = request.getParameter("applicantEmail");
        String courseId = request.getParameter("courseId");

        File resumeFile = getResumeFile(applicantEmail, courseId);

        if (resumeFile == null || !resumeFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume file not found on server.");
            return;
        }


        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + resumeFile.getName() + "\"");
        response.setContentLengthLong(resumeFile.length());


         try (FileInputStream fis = new FileInputStream(resumeFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    /**
     * Logs out the current Admin by invalidating the session and redirecting to the Admin login page.
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
        response.sendRedirect(request.getContextPath() + "/admin");
    }

}
