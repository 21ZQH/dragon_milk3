package listener;

import java.time.LocalDateTime;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import service.AccountService;
import service.DeadlineService;
import service.impl.AccountServiceImpl;
import service.impl.DeadlineServiceImpl;
import store.ApplicationFormStore;
import store.CourseStore;
import store.DeadlineStore;
import store.UserStore;

public class AppContextListener implements ServletContextListener {
    private final AccountService accountService;
    private final DeadlineService deadlineService;

    public AppContextListener() {
        this(new AccountServiceImpl(), new DeadlineServiceImpl());
    }

    AppContextListener(AccountService accountService, DeadlineService deadlineService) {
        this.accountService = accountService;
        this.deadlineService = deadlineService;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        configureRuntimeStorePaths(context);
        accountService.ensureBuiltInAccounts();

        LocalDateTime applicationDeadline = deadlineService.getApplicationDeadline();
        LocalDateTime moModifyDeadline = deadlineService.getMoModifyDeadline();

        context.setAttribute("applicationDeadline", applicationDeadline);
        context.setAttribute("moCourseModifyDeadline", moModifyDeadline);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no action needed
    }

    private void configureRuntimeStorePaths(ServletContext context) {
        configureStorePath(context, UserStore.FILE_PATH_PROPERTY, "/WEB-INF/file/users.txt");
        configureStorePath(context, CourseStore.FILE_PATH_PROPERTY, "/WEB-INF/file/courses.txt");
        configureStorePath(context, DeadlineStore.FILE_PATH_PROPERTY, "/WEB-INF/file/deadline.txt");
        configureStorePath(context, DeadlineStore.MO_FILE_PATH_PROPERTY, "/WEB-INF/file/mo-deadline.txt");
        configureStorePath(context, ApplicationFormStore.FILE_PATH_PROPERTY, "/WEB-INF/file/application-forms.txt");
    }

    private void configureStorePath(ServletContext context, String propertyName, String relativePath) {
        if (context == null) {
            return;
        }

        String realPath = context.getRealPath(relativePath);
        if (realPath != null && !realPath.isBlank()) {
            System.setProperty(propertyName, realPath);
        }
    }
}


