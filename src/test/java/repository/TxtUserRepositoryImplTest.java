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

class TxtUserRepositoryImplTest {
    @TempDir
    Path tempDir;

    private final UserRepository repository = new TxtUserRepositoryImpl();

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

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
