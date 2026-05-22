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

/**
 * Application lifecycle listener for the TA Recruitment System.
 * <p>
 * This {@link ServletContextListener} initialises system-wide resources when the
 * web application starts up. It performs the following initialisation tasks:
 * </p>
 * <ul>
 *   <li>Configures runtime file store paths for persistent data (users, courses,
 *       deadlines, application forms) relative to the servlet context's real path</li>
 *   <li>Ensures built-in MO and Admin accounts exist in the system</li>
 *   <li>Loads persisted deadlines (TA application deadline and MO modification
 *       deadline) into the servlet context attributes for runtime access</li>
 * </ul>
 *
 * <p>On application shutdown, no specific cleanup is required as the file-based
 * stores persist data across restarts.</p>
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 1.0
 * @see ServletContextListener
 * @see AccountService
 * @see DeadlineService
 */
public class AppContextListener implements ServletContextListener {
    private final AccountService accountService;
    private final DeadlineService deadlineService;

    /**
     * Constructs an {@code AppContextListener} with default service implementations.
     */
    public AppContextListener() {
        this(new AccountServiceImpl(), new DeadlineServiceImpl());
    }

    /**
     * Constructs an {@code AppContextListener} with the specified service instances.
     * <p>Package-private constructor used for dependency injection in unit tests.</p>
     *
     * @param accountService  the service for account initialisation
     * @param deadlineService the service for deadline persistence
     */
    AppContextListener(AccountService accountService, DeadlineService deadlineService) {
        this.accountService = accountService;
        this.deadlineService = deadlineService;
    }

    /**
     * Initialises the application context on web application startup.
     * <p>Configures runtime file store paths, ensures built-in accounts exist,
     * and loads persisted deadlines into the servlet context attributes.</p>
     *
     * @param sce the {@link ServletContextEvent} containing the servlet context
     */
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

    /**
     * Performs cleanup on web application shutdown.
     * <p>No action is currently required as all data is persisted via file stores.</p>
     *
     * @param sce the {@link ServletContextEvent} containing the servlet context
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no action needed
    }

    /**
     * Configures the file store paths for data persistence by setting system properties
     * with the resolved real paths from the servlet context.
     *
     * @param context the {@link ServletContext} used to resolve real file paths
     */
    private void configureRuntimeStorePaths(ServletContext context) {
        configureStorePath(context, UserStore.FILE_PATH_PROPERTY, "/WEB-INF/file/users.txt");
        configureStorePath(context, CourseStore.FILE_PATH_PROPERTY, "/WEB-INF/file/courses.txt");
        configureStorePath(context, DeadlineStore.FILE_PATH_PROPERTY, "/WEB-INF/file/deadline.txt");
        configureStorePath(context, DeadlineStore.MO_FILE_PATH_PROPERTY, "/WEB-INF/file/mo-deadline.txt");
        configureStorePath(context, ApplicationFormStore.FILE_PATH_PROPERTY, "/WEB-INF/file/application-forms.txt");
    }

    /**
     * Resolves the real filesystem path for a given relative web path and sets it
     * as a system property for use by the data store classes.
     *
     * @param context      the {@link ServletContext} used to resolve the real path
     * @param propertyName the system property name to set
     * @param relativePath the relative web application path to the file
     */
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
