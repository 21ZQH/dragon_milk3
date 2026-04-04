package chain;

import model.*;
import utils.UserStore;

import java.util.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;


public class AccountController extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String email = request.getParameter("email");

        if ("Register".equalsIgnoreCase(action)) {
            try {
                if (UserStore.isEmailRegistered(email)) {
                    request.setAttribute("error", "the email is already registered");
                    request.getRequestDispatcher("logo.jsp").forward(request, response);
                   
            }
                User user;
                switch (role) {
                    case "TA":
                        user = new TA(password, email);
                        break;
                    case "Admin":
                        user = new Admin(password, email);
                        break;
                    case "Mo":
                        user = new Mo(password, email);
                        break;
                    default:
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown role");
                        return;
                }
                UserStore.saveUser(user);
                request.getSession().setAttribute("user", user);
                request.setAttribute("name", name);
                request.setAttribute("email", email);
                // 根据角色跳转到不同的页面
                if ("Mo".equalsIgnoreCase(role)) {
                 request.getRequestDispatcher("/MO_dashboard.jsp").forward(request, response);
                } else {
                // TA 和 Admin 暂时默认跳回原有的 logo.jsp
                request.getRequestDispatcher("/logo.jsp").forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(500, "Registration failed");
            }
        } else if ("Login".equalsIgnoreCase(action)) {
            try {
                User user = UserStore.validateUser(password, role, email);
                if (user != null) {
                    request.getSession().setAttribute("user", user);
                    request.setAttribute("name", name);
                    request.setAttribute("email", email);
                     // 根据角色跳转到不同的页面
                    if ("Mo".equalsIgnoreCase(role)) {
                    request.getRequestDispatcher("/MO_dashboard.jsp").forward(request, response);
                    } else {
                    // TA 和 Admin 暂时默认跳回原有的 logo.jsp
                    request.getRequestDispatcher("/logo.jsp").forward(request, response);
                    }
                } else {
                    request.setAttribute("error", "Invalid password and email");
                    request.getRequestDispatcher("/logo.jsp").forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(500, "Login failed");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }
}
