package controller;


import model.*;
import store.CourseStore;

import java.util.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;


public class TAClassController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("view_information".equals(action)) {
            view_information(request, response);
        } else if ("show_all_information".equals(action)) {
            show_all_information(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }


    private void view_information(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 读取Course列表
        List<Course> courseList = CourseStore.getCourseList();
        // 存入session
        request.getSession().setAttribute("courseList", courseList);

        // 转发到JSP
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
    request.getRequestDispatcher("/WEB-INF/views/ta/specificclass.jsp").forward(request, response);
    }

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

