package chain;


import model.*;
import utils.CourseStore;

import java.util.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;


public class TAclasscontroller extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("view_information".equals(action)) {
            view_information(request, response);
        }
        // 其他action...
    }

    private void view_information(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 读取Course列表
        List<Course> courseList = CourseStore.getCourseList();
        // 存入session
        request.getSession().setAttribute("courseList", courseList);

        // 转发到JSP
        request.getRequestDispatcher("/globalclass.jsp").forward(request, response);
    }

   
}
