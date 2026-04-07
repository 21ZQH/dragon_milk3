package controller;


import model.*;
import store.CourseStore;

import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;

@MultipartConfig
public class TAClassController extends HttpServlet {
    private static final String RESUME_UPLOAD_DIRECTORY = "E:\\Tomcat\\webapps\\SE\\WEB-INF\\file\\resume";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("view_information".equals(action)) {
            view_information(request, response);
        } else if ("show_all_information".equals(action)) {
            show_all_information(request, response);
        } else if ("go_apply".equals(action)) {
            go_apply(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("upload_resume".equals(action)) {
            upload_resume(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
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

        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", request.getParameter("courseIndex"));
        request.getRequestDispatcher("/WEB-INF/views/ta/application.jsp").forward(request, response);
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
        String normalizedCourseName = (course.getCourseName() == null || course.getCourseName().trim().isEmpty())
                ? "unknown"
                : course.getCourseName().replaceAll("[^a-zA-Z0-9._-]", "_");

        String safeFileName = normalizedEmail + ".pdf";
        String uploadDirectoryPath = RESUME_UPLOAD_DIRECTORY + File.separator + normalizedCourseName;
        File uploadDirectory = new File(uploadDirectoryPath);
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new IOException("Failed to create upload directory.");
        }

        File targetFile = new File(uploadDirectory, safeFileName);
        resumePart.write(targetFile.getAbsolutePath());

        String resumeName = submittedFileName;
        String resume = targetFile.getAbsolutePath();

        ta.addResume(resumeName, resume);
        course.addApplication(ta, resume);
        ta.addClass(course);

        session.setAttribute("user", ta);
        request.setAttribute("success", "Resume submitted successfully: " + resumeName);
        request.setAttribute("selectedCourse", course);
        request.setAttribute("courseIndex", request.getParameter("courseIndex"));
        request.getRequestDispatcher("/WEB-INF/views/ta/specific-class.jsp").forward(request, response);
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

}

