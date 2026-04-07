package store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import model.Mo;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StoreTestSupport;

class UserStoreTest {
    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void saveUserAndValidateByRoleUsesIsolatedTestFile() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        Mo user = new Mo("secret123", "mo@example.com");
        user.setName("Molly");

        UserStore.saveUser(user);

        assertTrue(Files.exists(usersFile));

        List<String> lines = Files.readAllLines(usersFile);
        assertEquals(1, lines.size());
        assertEquals("Molly,secret123,Mo,mo@example.com", lines.get(0));

        User savedUser = UserStore.validateUser("secret123", "Mo", "mo@example.com");
        assertNotNull(savedUser);
        assertEquals("Mo", savedUser.getRole());
        assertEquals("mo@example.com", savedUser.getEmail());
        assertEquals("Molly", savedUser.getName());
    }

    @Test
    void validateUserWithoutRoleMatchesEmailAndPassword() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(usersFile, "Alice,pass123,TA,alice@example.com");

        User user = UserStore.validateUser("pass123", "alice@example.com");

        assertNotNull(user);
        assertEquals("TA", user.getRole());
        assertEquals("Alice", user.getName());
    }

    @Test
    void missingUserFileBehavesLikeNoRegisteredUsers() {
        StoreTestSupport.useUserStore(tempDir);

        assertFalse(UserStore.isEmailRegistered("nobody@example.com"));
        assertNull(UserStore.validateUser("secret", "TA", "nobody@example.com"));
        assertNull(UserStore.validateUser("secret", "nobody@example.com"));
    }
}
