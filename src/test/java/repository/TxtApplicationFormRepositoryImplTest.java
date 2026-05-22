package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import model.ApplicationForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.impl.TxtApplicationFormRepositoryImpl;
import store.ApplicationFormStore;

/**
 * Unit tests for the {@link TxtApplicationFormRepositoryImpl} class in the TA Recruitment system.
 * Verifies that application form persistence operations are correctly delegated to the underlying
 * text-based store and that form data is properly saved and retrieved.
 *
 * @author TA Recruitment System
 */
class TxtApplicationFormRepositoryImplTest {
    /**
     * Temporary directory for isolated test file storage.
     */
    @TempDir
    Path tempDir;

    /**
     * The repository instance under test.
     */
    private final ApplicationFormRepository repository = new TxtApplicationFormRepositoryImpl();

    /**
     * Clears the ApplicationFormStore file path system property after each test.
     */
    @AfterEach
    void tearDown() {
        System.clearProperty(ApplicationFormStore.FILE_PATH_PROPERTY);
    }

    /**
     * Verifies that saving and retrieving application forms delegates to the underlying
     * TxtApplicationFormStore, and that the persisted form data is stored and retrieved
     * correctly with all fields intact.
     */
    @Test
    void delegatesApplicationFormPersistenceToTxtStore() {
        System.setProperty(ApplicationFormStore.FILE_PATH_PROPERTY,
                tempDir.resolve("application-forms.txt").toString());
        ApplicationForm form = new ApplicationForm("alice@bupt.edu.cn", "course-1");
        form.setApplicantName("Alice");

        repository.saveOrUpdate(form);

        ApplicationForm savedForm = repository.findForm("alice@bupt.edu.cn", "course-1");
        assertNotNull(savedForm);
        assertEquals("Alice", savedForm.getApplicantName());
    }
}
