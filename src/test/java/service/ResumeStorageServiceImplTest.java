package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.TA;
import service.impl.ResumeStorageServiceImpl;

/**
 * Unit tests for {@link ResumeStorageServiceImpl} in the TA Recruitment system.
 * Verifies resume file naming and deletion behavior.
 */
class ResumeStorageServiceImplTest {
    /** Temporary directory used for resume file operations. */
    @TempDir
    Path tempDir;

    /** The service implementation under test. */
    private final ResumeStorageService service = new ResumeStorageServiceImpl();

    /**
     * Tests that the resume file name is derived from the TA's email address
     * with special characters replaced by underscores and a {@code .pdf}
     * extension appended.
     */
    @Test
    void buildsStableResumeFileNameFromEmail() {
        TA ta = new TA("secret123", "ta+test@example.com");

        assertEquals("ta_test_example.com.pdf", service.buildStoredResumeFileName(ta));
    }

    /**
     * Tests that deleting a stored resume file also removes the parent
     * directory if it becomes empty after deletion.
     */
    @Test
    void deletesStoredResumeAndEmptyDirectory() throws Exception {
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("resume").resolve("course-1");
        Files.createDirectories(resumeDirectory);
        Path resumeFile = resumeDirectory.resolve("ta_example.com.pdf");
        Files.writeString(resumeFile, "pdf");

        assertTrue(service.deleteStoredResumeFileIfPresent(ta, resumeDirectory.toString()));
        assertFalse(Files.exists(resumeFile));
        assertFalse(Files.exists(resumeDirectory));
    }
}
