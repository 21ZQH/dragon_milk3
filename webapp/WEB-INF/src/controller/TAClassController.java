package controller;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
import model.ResumeSubmission;
import model.TA;
import model.User;
import service.ApplicationFormService;
import service.impl.ApplicationFormServiceImpl;
import store.CourseStore;
import store.DeadlineStore;
import store.UserStore;

@MultipartConfig
public class TAClassController extends HttpServlet {
    private final ApplicationFormService applicationFormService = new ApplicationFormServiceImpl();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if (!ensureTaSession(request, response)) {
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
        } else if ("view_resume".equals(action)) {
            view_resume(request, response);
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

        if (!ensureTaSession(request, response)) {
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

    private boolean ensureTaSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession();
        }

        Object currentUser = session == null ? null : session.getAttribute("user");
        if (currentUser instanceof TA) {
            return true;
        }

        if (currentUser == null) {
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
        request.setAttribute("applicationOpen", isApplicationOpen(request));
        request.getRequestDispatcher("/WEB-INF/views/ta/home.jsp").forward(request, response);
    }

    private void view_information(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA)) {
            response.sendRedirect(request.getContextPath() + "/ta?action=auth");
            return;
        }

        if (!isApplicationOpen(request)) {
            request.setAttribute("applicationOpen", false);
            request.setAttribute("showDeadlineModal", true);
            request.getRequestDispatcher("/WEB-INF/views/ta/home.jsp").forward(request, response);
            return;
        }

        // Load all courses.
        List<Course> courseList = CourseStore.getCourseList();
        // Cache list in session.
        request.getSession().setAttribute("courseList", courseList);

        // Forward to TA list page.
    request.getRequestDispatcher("/WEB-INF/views/ta/job-list.jsp").forward(request, response);
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
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "The application deadline has passed. You can no longer submit or modify applications.",
                    course.getId());
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

        ApplicationForm form = applicationFormService.getForm(ta.getEmail(), course.getId());
        if (form == null) {
            form = applicationFormService.generateInitialForm(ta, course);
        }
        forwardApplicationForm(request, response, course, String.valueOf(courseIndex), form, null, null);
    }

    private void view_resume(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        String courseId = request.getParameter("courseId");
        if (courseId == null || courseId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course id is required");
            return;
        }

        String resumeDirectory = ta.getResumeDirectoryForCourse(courseId);
        if (resumeDirectory == null || resumeDirectory.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume not found");
            return;
        }

        File resumeFile = new File(resumeDirectory, buildStoredResumeFileName(ta));
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume file not found");
            return;
        }

        boolean download = "true".equalsIgnoreCase(request.getParameter("download"));
        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                (download ? "attachment" : "inline") + "; filename=\"" + buildStoredResumeFileName(ta) + "\"");

        try (InputStream inputStream = new FileInputStream(resumeFile);
                OutputStream outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        }
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

        File resumeFile = new File(resumeDirectory, buildStoredResumeFileName(ta));
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume file not found");
            return;
        }

        boolean download = "true".equalsIgnoreCase(request.getParameter("download"));
        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                (download ? "attachment" : "inline") + "; filename=\"" + buildStoredResumeFileName(ta) + "\"");

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
            request.setAttribute("masterResumeFileName", buildStoredResumeFileName(ta));
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
            String submittedFileName = resumePart.getSubmittedFileName();
            submittedFileName = submittedFileName == null ? "" : new File(submittedFileName).getName();

            if (!submittedFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                request.setAttribute("selectedCourse", course);
                request.setAttribute("courseIndex", request.getParameter("courseIndex"));
                request.setAttribute("error", "Only PDF resumes are accepted.");
                request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
                return;
            }

            String normalizedEmail = (ta.getEmail() == null || ta.getEmail().trim().isEmpty())
                    ? "unknown"
                    : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");

            String safeFileName = normalizedEmail + ".pdf";
            String uploadDirectoryPath = resolveResumeUploadDirectory() + File.separator + "master";
            File uploadDirectory = new File(uploadDirectoryPath);
            if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
                throw new IOException("Failed to create upload directory.");
            }

            File targetFile = new File(uploadDirectory, safeFileName);
            if (targetFile.exists() && !targetFile.delete()) {
                throw new IOException("Failed to replace existing resume file.");
            }
            resumePart.write(targetFile.getAbsolutePath());
            ta.setMasterResumeDirectory(uploadDirectory.getAbsolutePath());
            UserStore.updateTaProfile(ta);
            resumeName = submittedFileName;
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
                request.getParameter("courseFit"),
                request.getParameter("feedback"),
                submitted);
        applicationFormService.saveForm(form);

        if (submitted) {
            String resumeDirectory = ta.getMasterResumeDirectory();
            ta.addOrUpdateResume(course, resumeDirectory, ResumeSubmission.STATUS_PENDING);
            course.addApplication(ta, resumeDirectory);
            UserStore.updateAppliedCourseIds(ta);
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
        if (resumePart == null || resumePart.getSize() <= 0) {
            forwardPersonalCentre(request, response, ta, null, "Please upload your resume before submitting.", request.getParameter("courseId"));
            return;
        }

        String submittedFileName = resumePart.getSubmittedFileName();
        submittedFileName = submittedFileName == null ? "" : new File(submittedFileName).getName();
        if (!submittedFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            forwardPersonalCentre(request, response, ta, null, "Only PDF resumes are accepted.", request.getParameter("courseId"));
            return;
        }

        String normalizedEmail = (ta.getEmail() == null || ta.getEmail().trim().isEmpty())
                ? "unknown"
                : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        File uploadDirectory = new File(resolveResumeUploadDirectory(), "master");
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new IOException("Failed to create upload directory.");
        }

        File targetFile = new File(uploadDirectory, normalizedEmail + ".pdf");
        if (targetFile.exists() && !targetFile.delete()) {
            throw new IOException("Failed to replace existing resume file.");
        }
        resumePart.write(targetFile.getAbsolutePath());

        ta.setMasterResumeDirectory(uploadDirectory.getAbsolutePath());
        UserStore.updateTaProfile(ta);
        session.setAttribute("user", ta);
        forwardPersonalCentre(request, response, ta, "Resume uploaded successfully: " + submittedFileName, null, request.getParameter("courseId"));
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

        String resumeDirectory = ta.getResumeDirectoryForCourse(courseId);
        boolean isMasterResume = resumeDirectory != null && resumeDirectory.equals(ta.getMasterResumeDirectory());
        boolean resumeDeleted = isMasterResume || deleteStoredResumeFileIfPresent(ta, resumeDirectory);

        ta.withdrawApplication(courseId);

        Course course = getCourseById(request, courseId);
        if (course != null) {
            course.removeApplicationByTaEmail(ta.getEmail());
        }

        UserStore.updateAppliedCourseIds(ta);
        session.setAttribute("user", ta);

        if (!resumeDeleted) {
            forwardPersonalCentre(
                    request,
                    response,
                    ta,
                    null,
                    "Application withdrawn, but failed to delete resume file from disk.",
                    null);
        } else {
            forwardPersonalCentre(request, response, ta, "Application withdrawn successfully.", null, null);
        }
    }
    @SuppressWarnings("unchecked")
    private Course getCourseFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<Course> courseList = (List<Course>) session.getAttribute("courseList");
        if (courseList == null) {
            courseList = CourseStore.getCourseList();
            session.setAttribute("courseList", courseList);
        }

        String courseIndexParam = request.getParameter("courseIndex");
        if (courseIndexParam == null) {
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

    private String resolveResumeUploadDirectory() {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return catalinaBase + File.separator + "webapps" + File.separator + "SE"
                    + File.separator + "WEB-INF" + File.separator + "file" + File.separator + "resume";
        }

        return System.getProperty("user.dir") + File.separator + "webapp"
                + File.separator + "WEB-INF" + File.separator + "file" + File.separator + "resume";
    }

    @SuppressWarnings("unchecked")
    private List<Course> getCourseListFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<Course> courseList = (List<Course>) session.getAttribute("courseList");
        if (courseList == null) {
            courseList = CourseStore.getCourseList();
            session.setAttribute("courseList", courseList);
        }
        return courseList;
    }

    private Course getCourseById(HttpServletRequest request, String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        for (Course course : getCourseListFromSession(request)) {
            if (courseId.equals(course.getId())) {
                return course;
            }
        }
        return null;
    }

    private Integer findCourseIndexById(List<Course> courseList, String courseId) {
        if (courseList == null || courseId == null || courseId.isBlank()) {
            return null;
        }

        for (int i = 0; i < courseList.size(); i++) {
            Course course = courseList.get(i);
            if (course != null && courseId.equals(course.getId())) {
                return i;
            }
        }
        return null;
    }

    private void populateCurrentResumeAttributes(HttpServletRequest request, Course course) {
        if (course == null) {
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            return;
        }

        request.setAttribute("hasMasterResume", hasMasterResume(ta));
        request.setAttribute("masterResumeFileName", buildStoredResumeFileName(ta));

        String resumeDirectory = ta.getResumeDirectoryForCourse(course.getId());
        if (resumeDirectory == null || resumeDirectory.isBlank()) {
            return;
        }

        File resumeFile = new File(resumeDirectory, buildStoredResumeFileName(ta));
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            return;
        }

        request.setAttribute("hasCurrentResume", true);
        request.setAttribute("currentResumeFileName", resumeFile.getName());
    }

    private String buildStoredResumeFileName(TA ta) {
        String normalizedEmail = (ta.getEmail() == null || ta.getEmail().trim().isEmpty())
                ? "unknown"
                : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizedEmail + ".pdf";
    }

    private boolean deleteStoredResumeFileIfPresent(TA ta, String resumeDirectory) {
        if (ta == null || resumeDirectory == null || resumeDirectory.isBlank()) {
            return true;
        }

        File resumeFile = new File(resumeDirectory, buildStoredResumeFileName(ta));
        if (resumeFile.exists() && !resumeFile.delete()) {
            return false;
        }

        File directory = new File(resumeDirectory);
        String[] files = directory.list();
        if (directory.exists() && directory.isDirectory() && files != null && files.length == 0) {
            directory.delete();
        }

        return true;
    }

    private void forwardPersonalCentre(HttpServletRequest request, HttpServletResponse response, TA ta,
            String success, String error, String selectedCourseId) throws ServletException, IOException {
        if (ta.markAllReviewUpdatesRead()) {
            UserStore.updateAppliedCourseIds(ta);
            request.getSession().setAttribute("user", ta);
        }

        List<Course> appliedCourses = ta.getAppliedClasses();
        Course selectedCourse = resolveSelectedAppliedCourse(appliedCourses, selectedCourseId);

        request.setAttribute("appliedCourses", appliedCourses);
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("selectedCourseId", selectedCourse == null ? null : selectedCourse.getId());
        request.setAttribute("applicationOpen", isApplicationOpen(request));
        request.setAttribute("applicationDeadline", resolveApplicationDeadline(request));
        request.setAttribute("hasMasterResume", hasMasterResume(ta));
        request.setAttribute("masterResumeFileName", buildStoredResumeFileName(ta));
        if (selectedCourse != null) {
            request.setAttribute("selectedStatus", resolveResumeStatus(ta, selectedCourse.getId()));
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

    private Course resolveSelectedAppliedCourse(List<Course> appliedCourses, String selectedCourseId) {
        if (appliedCourses == null || appliedCourses.isEmpty()) {
            return null;
        }

        if (selectedCourseId != null && !selectedCourseId.isBlank()) {
            for (Course course : appliedCourses) {
                if (course != null && selectedCourseId.equals(course.getId())) {
                    return course;
                }
            }
        }

        return appliedCourses.get(0);
    }

    private int resolveResumeStatus(TA ta, String courseId) {
        Integer status = ta.getResumeStatusForCourse(courseId);
        return status == null ? ResumeSubmission.STATUS_PENDING : status;
    }

    private boolean hasMasterResume(TA ta) {
        if (ta == null || ta.getMasterResumeDirectory() == null || ta.getMasterResumeDirectory().isBlank()) {
            return false;
        }
        File resumeFile = new File(ta.getMasterResumeDirectory(), buildStoredResumeFileName(ta));
        return resumeFile.exists() && resumeFile.isFile();
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






