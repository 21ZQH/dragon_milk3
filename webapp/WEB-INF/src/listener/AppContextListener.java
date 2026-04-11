package listener;

import java.time.LocalDateTime;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import store.DeadlineStore;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        LocalDateTime applicationDeadline = DeadlineStore.getDeadline();
        LocalDateTime moModifyDeadline = DeadlineStore.getMoModifyDeadline();

        context.setAttribute("applicationDeadline", applicationDeadline);
        context.setAttribute("moCourseModifyDeadline", moModifyDeadline);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no action needed
    }
}
