package repository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import model.TA;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.impl.TxtUserRepositoryImpl;
import testsupport.StoreTestSupport;

/**
 * Unit tests for the {@link TxtUserRepositoryImpl} class in the TA Recruitment system.
 * Verifies that user persistence operations are correctly delegated to the underlying
 * text-based store and that data is properly written to and read from the file system.
 *
 * @author TA Recruitment System
 */
class TxtUserRepositoryImplTest {
    /**
     * Temporary directory for isolated test file storage.
     */
    @TempDir
    Path tempDir;

    /**
     * The repository instance under test.
     */
    private final UserRepository repository = new TxtUserRepositoryImpl();

    /**
     * Cleans up store system property overrides after each test.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Verifies that saving a user, checking email registration, and validating user
     * credentials delegates to the underlying TxtUserStore, and that the persisted
     * data is written to the file system correctly.
     */
    @Test
    void delegatesUserPersistenceToTxtStore() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        TA ta = new TA("TA-1234-5678", "alice@bupt.edu.cn");

        repository.saveUser(ta);

        assertTrue(repository.isEmailRegistered("alice@bupt.edu.cn"));
        assertInstanceOf(TA.class, repository.validateUser("TA-1234-5678", "alice@bupt.edu.cn"));
        assertTrue(Files.readString(usersFile).contains("alice@bupt.edu.cn"));
    }
}
