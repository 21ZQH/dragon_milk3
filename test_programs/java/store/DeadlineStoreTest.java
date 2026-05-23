package store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import testsupport.StoreTestSupport;

/**
 * Unit tests for the {@link DeadlineStore} class in the TA Recruitment system.
 * Verifies that application deadlines and MO modification deadlines are correctly
 * persisted to and loaded from the file system, including edge case handling for
 * blank or missing files.
 *
 * @author TA Recruitment System
 */
class DeadlineStoreTest {

    /**
     * Temporary directory for isolated test file storage.
     */
    @TempDir
    Path tempDir;

    /**
     * Cleans up store system property overrides after each test.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Verifies that saving and loading the application deadline uses the overridden
     * file path in the temporary directory, and that the persisted deadline value
     * is retrieved correctly.
     */
    @Test
    void saveAndLoadApplicationDeadlineUsesOverridePath() throws Exception {
        Path deadlineFile = StoreTestSupport.useApplicationDeadlineStore(tempDir);
        LocalDateTime expected = LocalDateTime.of(2026, 4, 30, 18, 15);

        DeadlineStore.saveDeadline(expected);

        assertTrue(Files.exists(deadlineFile));
        assertEquals(expected, DeadlineStore.getDeadline());
    }

    /**
     * Verifies that saving and loading the MO modification deadline uses the
     * overridden file path in the temporary directory, and that the persisted
     * deadline value is retrieved correctly.
     */
    @Test
    void saveAndLoadMoModifyDeadlineUsesOverridePath() throws Exception {
        Path deadlineFile = StoreTestSupport.useMoDeadlineStore(tempDir);
        LocalDateTime expected = LocalDateTime.of(2026, 5, 2, 9, 45);

        DeadlineStore.saveMoModifyDeadline(expected);

        assertTrue(Files.exists(deadlineFile));
        assertEquals(expected, DeadlineStore.getMoModifyDeadline());
    }

    /**
     * Verifies that blank or whitespace-only deadline files cause the store to return
     * null for both the application deadline and the MO modification deadline.
     */
    @Test
    void blankDeadlineFilesReturnNull() throws Exception {
        Path appDeadlineFile = StoreTestSupport.useApplicationDeadlineStore(tempDir);
        Path moDeadlineFile = StoreTestSupport.useMoDeadlineStore(tempDir);
        Files.writeString(appDeadlineFile, "");
        Files.writeString(moDeadlineFile, "   ");

        assertNull(DeadlineStore.getDeadline());
        assertNull(DeadlineStore.getMoModifyDeadline());
    }
}
