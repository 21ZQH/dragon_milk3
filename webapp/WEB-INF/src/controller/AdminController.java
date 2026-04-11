package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.TA;
import model.User;
import store.DeadlineStore;
import store.UserStore;

public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

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

        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

        if ("dashboard".equals(action)) {
            show_dashboard(request, response);
        } else if ("candidate_management".equals(action)) {
            manage_candidates(request, response);
        } else if ("set_deadline".equals(action)) {
            show_set_deadline(request, response);
        } else if ("set_mo_deadline".equals(action)) {
            show_set_mo_deadline(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }

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

        if ("save_deadline".equals(action)) {
            save_deadline(request, response);
        } else if ("save_mo_deadline".equals(action)) {
            save_mo_deadline(request, response);
        } else {
            doGet(request, response);
        }
    }

    private void show_dashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }

    private void manage_candidates(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<TA> taList = UserStore.getTAList();
        request.setAttribute("taList", taList);
        request.getRequestDispatcher("/WEB-INF/views/admin/candidate-management.jsp").forward(request, response);
    }

    private void show_set_deadline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object savedDeadline = getServletContext().getAttribute("applicationDeadline");
        request.setAttribute("savedDeadline", savedDeadline);
        request.getRequestDispatcher("/WEB-INF/views/admin/set-deadline.jsp").forward(request, response);
    }

    private void show_set_mo_deadline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object savedMoDeadline = getServletContext().getAttribute("moCourseModifyDeadline");
        request.setAttribute("savedMoDeadline", savedMoDeadline);
        request.getRequestDispatcher("/WEB-INF/views/admin/set-mo-deadline.jsp").forward(request, response);
    }

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

            DeadlineStore.saveDeadline(deadline);
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

            DeadlineStore.saveMoModifyDeadline(deadline);
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

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/start.html");
    }
}
