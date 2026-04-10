package controller;


import model.*;
import store.UserStore;
import store.CourseStore;

import java.util.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

@MultipartConfig
public class TAClassController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
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
        } else if ("view_resume".equals(action)) {
            view_resume(request, response);
        } else if ("personal_centre".equals(action)) {
            personal_centre(request, response);
        } else if ("profile_centre".equals(action)) {
            profile_centre(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("upload_resume".equals(action)) {
            upload_resume(request, response);
        } else if ("withdraw_application".equals(action)) {
            withdraw_application(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    private void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/ta/home.jsp").forward(request, response);
    }

    private void view_information(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
    }

        private void go_apply(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Course course = getCourseFromSession(request);
        if (course == null) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
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

        populateCurrentResumeAttributes(request, course);
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", String.valueOf(courseIndex));
        request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
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

    private void personal_centre(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!(user instanceof TA ta)) {
            response.sendRedirect(request.getContextPath() + "/TAclasscontroller?action=view_information");
            return;
        }

        request.setAttribute("appliedCourses", ta.getAppliedClasses());
        request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp").forward(request, response);
    }

    private void profile_centre(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/ta/profile-centre.jsp").forward(request, response);
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
        Part resumePart = request.getPart("resumeFile");
        if (resumePart == null || resumePart.getSize() <= 0) {
            request.setAttribute("selectedCourse", course);
            request.setAttribute("courseIndex", request.getParameter("courseIndex"));
            request.setAttribute("error", "Please upload your resume before submitting.");
            request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
            return;
        }

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
        String courseDirectoryName = (course.getId() == null || course.getId().trim().isEmpty())
                ? "unknown-course"
                : course.getId();

        String safeFileName = normalizedEmail + ".pdf";
        String uploadDirectoryPath = resolveResumeUploadDirectory() + File.separator + courseDirectoryName;
        File uploadDirectory = new File(uploadDirectoryPath);
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new IOException("Failed to create upload directory.");
        }

        File targetFile = new File(uploadDirectory, safeFileName);
        if (targetFile.exists() && !targetFile.delete()) {
            throw new IOException("Failed to replace existing resume file.");
        }
        resumePart.write(targetFile.getAbsolutePath());

        String resumeName = submittedFileName;
        String resumeDirectory = uploadDirectory.getAbsolutePath();

        ta.addOrUpdateResume(course, resumeDirectory);
        course.addApplication(ta, resumeDirectory);
        UserStore.updateAppliedCourseIds(ta);

        session.setAttribute("user", ta);
        request.setAttribute("success", "Resume submitted successfully: " + resumeName);
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", request.getParameter("courseIndex"));
        request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
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
            request.setAttribute("appliedCourses", ta.getAppliedClasses());
            request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp").forward(request, response);
            return;
        }

        String resumeDirectory = ta.getResumeDirectoryForCourse(courseId);
        boolean resumeDeleted = deleteStoredResumeFileIfPresent(ta, resumeDirectory);

        ta.withdrawApplication(courseId);

        Course course = getCourseById(request, courseId);
        if (course != null) {
            course.removeApplicationByTaEmail(ta.getEmail());
        }

        UserStore.updateAppliedCourseIds(ta);
        session.setAttribute("user", ta);

        if (!resumeDeleted) {
            request.setAttribute("error", "Application withdrawn, but failed to delete resume file from disk.");
        } else {
            request.setAttribute("success", "Application withdrawn successfully.");
        }
        request.setAttribute("appliedCourses", ta.getAppliedClasses());
        request.getRequestDispatcher("/WEB-INF/views/ta/personalCentre.jsp").forward(request, response);
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

}

