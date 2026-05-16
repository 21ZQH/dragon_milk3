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

class ResumeStorageServiceImplTest {
    @TempDir
    Path tempDir;

    private final ResumeStorageService service = new ResumeStorageServiceImpl();

    @Test
    void buildsStableResumeFileNameFromEmail() {
        TA ta = new TA("secret123", "ta+test@example.com");

        assertEquals("ta_test_example.com.pdf", service.buildStoredResumeFileName(ta));
    }

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
