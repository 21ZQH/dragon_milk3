package listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import testsupport.StoreTestSupport;

/**
 * Unit tests for {@link AppContextListener} in the TA Recruitment system.
 * Tests cover the context initialization behavior for loading deadlines
 * into the servlet context.
 *
 * @author BUPT-TA-Recruitment-Group33
 */
class AppContextListenerTest {

    @TempDir
    java.nio.file.Path tempDir;

    /**
     * Clears store overrides after each test to ensure test isolation.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Tests that the contextInitialized method loads both the application deadline
     * and the MO course modification deadline from the persistent store and sets them
     * as servlet context attributes.
     */
    @Test
    void contextInitializedLoadsDeadlinesIntoServletContext() {
        StoreTestSupport.useApplicationDeadlineStore(tempDir);
        StoreTestSupport.useMoDeadlineStore(tempDir);

        LocalDateTime applicationDeadline = LocalDateTime.of(2026, 4, 20, 18, 0);
        LocalDateTime moDeadline = LocalDateTime.of(2026, 4, 22, 12, 30);
        store.DeadlineStore.saveDeadline(applicationDeadline);
        store.DeadlineStore.saveMoModifyDeadline(moDeadline);

        AppContextListener listener = new AppContextListener();
        ServletContext servletContext = mock(ServletContext.class);
        ServletContextEvent event = mock(ServletContextEvent.class);
        when(event.getServletContext()).thenReturn(servletContext);

        listener.contextInitialized(event);

        verify(servletContext).setAttribute("applicationDeadline", applicationDeadline);
        verify(servletContext).setAttribute("moCourseModifyDeadline", moDeadline);
    }
}
