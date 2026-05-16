package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;

import model.Mo;
import model.TA;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.impl.AccountServiceImpl;
import testsupport.StoreTestSupport;

class AccountServiceImplTest {
    @TempDir
    Path tempDir;

    private final AccountService service = new AccountServiceImpl();

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void registerBuildsRoleSpecificUserAndPersistsIt() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);

        AccountService.AuthResult result = service.register(" Alice ", "secret123", "TA", "ta@example.com");

        assertEquals(AccountService.AuthStatus.SUCCESS, result.getStatus());
        assertInstanceOf(TA.class, result.getUser());
        assertEquals("Alice", result.getUser().getName());
        assertEquals("Alice,secret123,TA,ta@example.com,,,,", java.nio.file.Files.readAllLines(usersFile).get(0));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        StoreTestSupport.useUserStore(tempDir);
        service.register("Molly", "secret123", "Mo", "mo@example.com");

        AccountService.AuthResult result = service.register("Other", "secret123", "TA", "mo@example.com");

        assertEquals(AccountService.AuthStatus.EMAIL_REGISTERED, result.getStatus());
        assertNull(result.getUser());
    }

    @Test
    void loginSupportsOptionalRole() {
        StoreTestSupport.useUserStore(tempDir);
        service.register("Molly", "secret123", "Mo", "mo@example.com");

        User withoutRole = service.login("secret123", "", "mo@example.com");
        User withRole = service.login("secret123", "Mo", "mo@example.com");

        assertInstanceOf(Mo.class, withoutRole);
        assertInstanceOf(Mo.class, withRole);
    }
}
