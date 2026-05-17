package listener;

import java.time.LocalDateTime;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import service.AccountService;
import service.DeadlineService;
import service.impl.AccountServiceImpl;
import service.impl.DeadlineServiceImpl;

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
}


