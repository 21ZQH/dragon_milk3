package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import store.CourseStore;
import store.DeadlineStore;
import store.UserStore;

public class MOClassController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String action = request.getParameter("action");

        if ("logout".equals(action)) {
            logout(request, response);
        } else if ("create_class".equals(action)) {
            create_class(request, response);
        } else if ("personal_center".equals(action)) {
            show_personal_center(request, response);
        } else if ("profile_center".equals(action)) {
            profile_center(request, response);
        } else if ("review_candidates".equals(action)) {
            show_review_candidates(request, response);
        } else if ("view_resume".equals(action)) {
            view_resume(request, response);
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
        } else if ("publish_course".equals(action)) {
            String courseName = request.getParameter("courseName");
            String jobTitle = request.getParameter("jobTitle");
            String workingHours = request.getParameter("workingHours");
            String jobDescription = request.getParameter("jobDescription");
            String jobRequirement = request.getParameter("jobRequirement");

            String salary = "TBD";
            String courseId = UUID.randomUUID().toString();

            Course newCourse = new Course(
                    courseId,
                    courseName,
                    jobTitle,
                    workingHours,
                    salary,
                    jobDescription,
                    jobRequirement);

            CourseStore.saveCourse(newCourse);
            Mo mo = getCurrentMo(request);
            if (mo != null) {
                mo.addOwnedCourse(newCourse);
                UserStore.updateOwnedCourseIds(mo);
                request.getSession().setAttribute("user", mo);
            }
            request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp").forward(request, response);

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
            response.sendRedirect(request.getContextPath() + "/start.html");
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
            request.setAttribute("selectedCourse", selectedCourse);
            request.setAttribute("courseIndex", String.valueOf(courseIndex));
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
                        request.setAttribute("selectedCourse", courseList.get(courseIndex));
                        request.setAttribute("courseIndex", courseIndexParam);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            request.setAttribute("error", "The deadline for MO to modify course information has passed.");
            request.setAttribute("moModifyDeadline", resolveMoModifyDeadline(request));
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
                courseName,
                jobTitle,
                workingHours,
                salary,
                jobDescription,
                jobRequirement);
            updatedCourse.setPickedApplicantEmails(oldCourse.getPickedApplicantEmails());
            updatedCourse.setReviewPublished(oldCourse.isReviewPublished());

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

        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, request.getParameterValues("pickedEmail")));
        CourseStore.updateCourse(course);
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
            course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, request.getParameterValues("pickedEmail")));
            applyPublishedStatuses(course);
            course.setReviewPublished(true);
            CourseStore.updateCourse(course);
            syncCurrentMoCourse(request, course);
        }

        response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates&courseIndex="
                + courseIndex + "&published=1");
    }

    private void view_resume(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Course course = getCourseForReview(request);
        if (course == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
            return;
        }

        String applicantEmail = request.getParameter("applicantEmail");
        if (applicantEmail == null || applicantEmail.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Applicant email is required");
            return;
        }

        File resumeFile = null;
        for (int i = 0; i < course.getTaApplicants().size(); i++) {
            TA applicant = course.getTaApplicants().get(i);
            if (applicant == null || !applicantEmail.equals(applicant.getEmail())) {
                continue;
            }

            String resumeDirectory = applicant.getResumeDirectoryForCourse(course.getId());
            if ((resumeDirectory == null || resumeDirectory.isBlank()) && i < course.getApplicantResumes().size()) {
                resumeDirectory = course.getApplicantResumes().get(i);
            }

            resumeFile = buildResumeFile(applicant, resumeDirectory);
            break;
        }

        if (resumeFile == null || !resumeFile.exists() || !resumeFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume not found");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + resumeFile.getName() + "\"");
        response.setContentLengthLong(resumeFile.length());

        try (InputStream inputStream = new FileInputStream(resumeFile);
                ServletOutputStream outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        }
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
            request.getSession().setAttribute("user", mo);
            return freshOwnedCourses;
        }
        return CourseStore.getCourseList();
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

    private List<String> resolvePickedApplicantEmails(Course course, String[] pickedEmails) {
        Set<String> validApplicantEmails = new LinkedHashSet<>();
        for (TA applicant : course.getTaApplicants()) {
            if (applicant != null && applicant.getEmail() != null && !applicant.getEmail().isBlank()) {
                validApplicantEmails.add(applicant.getEmail());
            }
        }

        List<String> resolvedPickedEmails = new ArrayList<>();
        if (pickedEmails == null) {
            return resolvedPickedEmails;
        }

        for (String pickedEmail : pickedEmails) {
            if (pickedEmail != null && validApplicantEmails.contains(pickedEmail) && !resolvedPickedEmails.contains(pickedEmail)) {
                resolvedPickedEmails.add(pickedEmail);
            }
        }
        return resolvedPickedEmails;
    }

    private void applyPublishedStatuses(Course course) {
        for (int i = 0; i < course.getTaApplicants().size(); i++) {
            TA applicant = course.getTaApplicants().get(i);
            if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()) {
                continue;
            }

            String resumeDirectory = applicant.getResumeDirectoryForCourse(course.getId());
            if ((resumeDirectory == null || resumeDirectory.isBlank()) && i < course.getApplicantResumes().size()) {
                resumeDirectory = course.getApplicantResumes().get(i);
            }
            if (resumeDirectory == null || resumeDirectory.isBlank()) {
                continue;
            }

            int status = course.isApplicantPicked(applicant.getEmail())
                    ? ResumeSubmission.STATUS_APPROVED
                    : ResumeSubmission.STATUS_REJECTED;
            applicant.addClass(course);
            applicant.addOrUpdateResume(course, resumeDirectory, status);
            course.addApplication(applicant, resumeDirectory);
            UserStore.updateAppliedCourseIds(applicant);
        }
    }

    private File buildResumeFile(TA applicant, String resumeDirectory) {
        if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()
                || resumeDirectory == null || resumeDirectory.isBlank()) {
            return null;
        }

        String normalizedEmail = applicant.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(resumeDirectory, normalizedEmail + ".pdf");
    }

    private void syncCurrentMoCourse(HttpServletRequest request, Course course) {
        Mo mo = getCurrentMo(request);
        if (mo == null || course == null) {
            return;
        }

        mo.replaceOwnedCourse(course);
        request.getSession().setAttribute("user", mo);
    }

    private boolean isMoModifyOpen(HttpServletRequest request) {
        LocalDateTime deadline = resolveMoModifyDeadline(request);
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    private LocalDateTime resolveMoModifyDeadline(HttpServletRequest request) {
        Object deadline = request.getServletContext().getAttribute("moCourseModifyDeadline");
        if (deadline instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return DeadlineStore.getMoModifyDeadline();
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/start.html");
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
}


