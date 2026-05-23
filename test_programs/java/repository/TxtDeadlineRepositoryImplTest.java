package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.impl.TxtDeadlineRepositoryImpl;
import testsupport.StoreTestSupport;

/**
 * Unit tests for the {@link TxtDeadlineRepositoryImpl} class in the TA Recruitment system.
 * Verifies that deadline persistence operations are correctly delegated to the underlying
 * text-based store, including both application deadlines and MO modification deadlines.
 *
 * @author TA Recruitment System
 */
class TxtDeadlineRepositoryImplTest {
    /**
     * Temporary directory for isolated test file storage.
     */
    @TempDir
    Path tempDir;

    /**
     * The repository instance under test.
     */
    private final DeadlineRepository repository = new TxtDeadlineRepositoryImpl();

    /**
     * Cleans up store system property overrides after each test.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Verifies that saving and retrieving application deadlines and MO modification
     * deadlines delegates to the underlying TxtDeadlineStore, and that the persisted
     * deadline data is stored and retrieved correctly.
     */
    @Test
    void delegatesDeadlinePersistenceToTxtStore() {
        StoreTestSupport.useApplicationDeadlineStore(tempDir);
        StoreTestSupport.useMoDeadlineStore(tempDir);
        LocalDateTime applicationDeadline = LocalDateTime.of(2026, 5, 20, 9, 30);
        LocalDateTime moDeadline = LocalDateTime.of(2026, 5, 21, 10, 45);

        repository.saveApplicationDeadline(applicationDeadline);
        repository.saveMoModifyDeadline(moDeadline);

        assertEquals(applicationDeadline, repository.getApplicationDeadline());
        assertEquals(moDeadline, repository.getMoModifyDeadline());
    }
}
