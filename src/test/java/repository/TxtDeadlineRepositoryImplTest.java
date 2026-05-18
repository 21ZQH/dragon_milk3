package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.impl.TxtDeadlineRepositoryImpl;
import testsupport.StoreTestSupport;

class TxtDeadlineRepositoryImplTest {
    @TempDir
    Path tempDir;

    private final DeadlineRepository repository = new TxtDeadlineRepositoryImpl();

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

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
