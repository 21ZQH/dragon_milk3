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

class TxtApplicationFormRepositoryImplTest {
    @TempDir
    Path tempDir;

    private final ApplicationFormRepository repository = new TxtApplicationFormRepositoryImpl();

    @AfterEach
    void tearDown() {
        System.clearProperty(ApplicationFormStore.FILE_PATH_PROPERTY);
    }

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
