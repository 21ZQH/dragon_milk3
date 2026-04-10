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

        LocalDateTime deadline = DeadlineStore.getDeadline();
        context.setAttribute("applicationDeadline", deadline);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no action needed
    }
}

