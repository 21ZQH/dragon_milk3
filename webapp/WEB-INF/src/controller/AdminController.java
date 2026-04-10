package controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import store.CourseStore;
import store.UserStore;
import model.Course;
import model.TA;


public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // 验证用户是否登录且为 Admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("start.html");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!"Admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required");
            return;
        }

        if ("dashboard".equals(action)) {
            show_dashboard(request, response);
        } else if ("candidate_management".equals(action)) {
            // TODO: 敏捷开发后续阶段 - 这里将处理 Candidate Management 的逻辑
            // 目前先暂时重定向回 dashboard 或者展示一个未完成提示
            manage_candidates(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 后续如果 Admin 有表单提交操作（如删除候选人、审批等），写在这里
        doGet(request, response);
    }

    private void show_dashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 转发到 Admin 的主页
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }

    private void manage_candidates(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<TA> taList = UserStore.getTAList();
        
      
        request.setAttribute("taList", taList);
        request.getRequestDispatcher("/WEB-INF/views/admin/candidate-management.jsp").forward(request, response);
    }
}