package controller;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Admin;
import model.TA;
import testsupport.StoreTestSupport;

class AdminControllerTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        // 测试结束后清理系统属性，防止污染其他测试用例
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void unauthenticatedUserRedirectsToStartHtml() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 模拟未登录状态 (没有 session)
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/SE");

        controller.doGet(request, response);

        // 验证重定向到了登录页
        verify(response).sendRedirect("/SE/start.html");
    }

    @Test
    void nonAdminUserGetsForbiddenError() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        // 模拟一个普通 TA 用户的 Session
        TA taUser = new TA("secret", "ta@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(taUser);

        controller.doGet(request, response);

        // 验证非 Admin 用户访问会被拒绝 (403 Forbidden)
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required");
    }

    @Test
    void dashboardActionForwardsToAdminDashboard() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        // 模拟 Admin 用户登录
        Admin adminUser = new Admin("admin123", "admin@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);

        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        // 验证正确转发到了 dashboard 页面
        verify(dispatcher).forward(request, response);
    }

    @Test
    void candidateManagementActionFetchesTAsAndForwards() throws Exception {
        // 使用 StoreTestSupport 在沙盒环境中准备测试数据
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        
        // 写入一个测试课程和一个测试 TA (申请了该课程)
        StoreTestSupport.writeLines(courseFile, 
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(usersFile, 
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,");

        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        // 模拟 Admin 用户登录
        Admin adminUser = new Admin("admin123", "admin@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);

        when(request.getParameter("action")).thenReturn("candidate_management");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/candidate-management.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        // 验证 Store 的数据被正确获取并塞入 request attribute 中 (应该包含1个叫 Alice 的 TA)
        verify(request).setAttribute(eq("taList"), argThat(list -> {
            if (!(list instanceof List)) return false;
            List<?> taList = (List<?>) list;
            if (taList.size() != 1) return false;
            TA ta = (TA) taList.get(0);
            return "Alice".equals(ta.getName()) && "alice@example.com".equals(ta.getEmail());
        }));

        // 验证正确转发到了管理页面
        verify(dispatcher).forward(request, response);
    }

    @Test
    void unknownActionReturnsBadRequest() throws Exception {
        AdminController controller = new AdminController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        // 模拟 Admin 用户登录
        Admin adminUser = new Admin("admin123", "admin@example.com");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);

        when(request.getParameter("action")).thenReturn("UnknownActionXYZ");

        controller.doGet(request, response);

        // 验证未知的 action 会返回 400 错误
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }
}
