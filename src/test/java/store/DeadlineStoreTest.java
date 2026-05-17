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

class DeadlineStoreTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void saveAndLoadApplicationDeadlineUsesOverridePath() throws Exception {
        Path deadlineFile = StoreTestSupport.useApplicationDeadlineStore(tempDir);
        LocalDateTime expected = LocalDateTime.of(2026, 4, 30, 18, 15);

        DeadlineStore.saveDeadline(expected);

        assertTrue(Files.exists(deadlineFile));
        assertEquals(expected, DeadlineStore.getDeadline());
    }

    @Test
    void saveAndLoadMoModifyDeadlineUsesOverridePath() throws Exception {
        Path deadlineFile = StoreTestSupport.useMoDeadlineStore(tempDir);
        LocalDateTime expected = LocalDateTime.of(2026, 5, 2, 9, 45);

        DeadlineStore.saveMoModifyDeadline(expected);

        assertTrue(Files.exists(deadlineFile));
        assertEquals(expected, DeadlineStore.getMoModifyDeadline());
    }

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
