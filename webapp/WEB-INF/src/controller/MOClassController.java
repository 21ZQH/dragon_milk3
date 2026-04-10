package controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Candidate;
import model.Course;
import model.Mo;
import store.CandidateStore;
import store.CourseStore;
import store.UserStore;

public class MOClassController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create_class".equals(action)) {
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

        if ("publish_course".equals(action)) {
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

        } else if ("save_course_changes".equals(action)) {
            save_course_changes(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    private void show_personal_center(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp").forward(request, response);
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

    private void save_course_changes(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
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

            CourseStore.updateCourse(courseIndex, updatedCourse);
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
        List<Candidate> candidateList = CandidateStore.getCandidateList();
        request.setAttribute("candidateList", candidateList);
        request.getRequestDispatcher("/WEB-INF/views/mo/review.jsp").forward(request, response);
    }

    private void save_review_picks(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String[] pickedIndexes = request.getParameterValues("pickedIndex");
        CandidateStore.savePickedIndexes(pickedIndexes);

        String returnTo = request.getParameter("returnTo");
        if ("personal_center".equals(returnTo)) {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=personal_center");
        } else {
            response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates");
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
            return mo.getOwnedCourses();
        }
        return CourseStore.getCourseList();
    }
}
