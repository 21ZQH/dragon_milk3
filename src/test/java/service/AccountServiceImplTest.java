package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import model.Mo;
import model.Course;
import model.TA;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.impl.AccountServiceImpl;
import testsupport.StoreTestSupport;

/**
 * Unit tests for {@link AccountServiceImpl} in the TA Recruitment system.
 * Verifies user registration, login, built-in account seeding, and email
 * validation behavior.
 */
class AccountServiceImplTest {
    /** Temporary directory used for file-based store overrides. */
    @TempDir
    Path tempDir;

    /** The service implementation under test. */
    private final AccountService service = new AccountServiceImpl();

    /**
     * Clears store overrides after each test to prevent state leakage between
     * test cases.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Tests that registering a user creates a role-specific instance ({@link TA})
     * and persists the user record to the store file with the correct fields.
     */
    @Test
    void registerBuildsRoleSpecificUserAndPersistsIt() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);

        AccountService.AuthResult result = service.register(" Alice ", "secret123", "TA", "ta@example.com");

        assertEquals(AccountService.AuthStatus.SUCCESS, result.getStatus());
        assertInstanceOf(TA.class, result.getUser());
        assertEquals("Alice", result.getUser().getName());
        assertEquals("Alice,secret123,TA,ta@example.com,,,,", java.nio.file.Files.readAllLines(usersFile).get(0));
    }

    /**
     * Tests that registering a user with an email address that already exists
     * in the store returns {@code EMAIL_REGISTERED} and does not create a user.
     */
    @Test
    void registerRejectsDuplicateEmail() {
        StoreTestSupport.useUserStore(tempDir);
        service.register("Molly", "secret123", "Mo", "mo@example.com");

        AccountService.AuthResult result = service.register("Other", "secret123", "TA", "mo@example.com");

        assertEquals(AccountService.AuthStatus.EMAIL_REGISTERED, result.getStatus());
        assertNull(result.getUser());
    }

    /**
     * Tests that login returns the correct user type regardless of whether the
     * role parameter is provided or empty, as long as the credentials match.
     */
    @Test
    void loginSupportsOptionalRole() {
        StoreTestSupport.useUserStore(tempDir);
        service.register("Molly", "secret123", "Mo", "mo@example.com");

        User withoutRole = service.login("secret123", "", "mo@example.com");
        User withRole = service.login("secret123", "Mo", "mo@example.com");

        assertInstanceOf(Mo.class, withoutRole);
        assertInstanceOf(Mo.class, withRole);
    }

    /**
     * Tests that TA registration with a non-{@code bupt.edu.cn} email domain
     * is rejected, while a valid {@code bupt.edu.cn} email succeeds and
     * generates an access key prefixed with {@code TA-}.
     */
    @Test
    void taRegistrationRequiresBuptEmailAndGeneratesAccessKey() {
        StoreTestSupport.useUserStore(tempDir);

        AccountService.AuthResult rejected = service.registerTaWithBuptEmail("Alice", "alice@example.com");
        AccountService.AuthResult accepted = service.registerTaWithBuptEmail("Alice", "alice@bupt.edu.cn");

        assertEquals(AccountService.AuthStatus.INVALID_EMAIL_DOMAIN, rejected.getStatus());
        assertEquals(AccountService.AuthStatus.SUCCESS, accepted.getStatus());
        assertInstanceOf(TA.class, accepted.getUser());
        assertTrue(accepted.getUser().getPassword().startsWith("TA-"));
        assertInstanceOf(TA.class, service.loginTaByAccessKey(accepted.getUser().getPassword()));
    }

    /**
     * Tests that the built-in Mo and Admin accounts are seeded correctly,
     * can be logged in with their default credentials, and that Mo accounts
     * own the expected courses with recruitment initially unpublished.
     * Verifies that incorrect passwords return null.
     */
    @Test
    void builtInMoAndAdminAccountsCanBeSeededAndLoggedIn() {
        StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);

        service.ensureBuiltInAccounts();

        Mo mo1 = (Mo) service.loginBuiltInMo("mo1@bupt.edu.cn", "mo123456");
        assertInstanceOf(Mo.class, mo1);
        assertEquals(2, mo1.getOwnedCourses().size());
        assertEquals("Software Engineering", mo1.getOwnedCourses().get(0).getCourseName());
        assertTrue(mo1.getOwnedCourses().stream().noneMatch(Course::isRecruitmentPublished));
        assertInstanceOf(Mo.class, service.loginBuiltInMo("mo2@bupt.edu.cn", "mo223456"));
        assertInstanceOf(Mo.class, service.loginBuiltInMo("mo3@bupt.edu.cn", "mo323456"));
        assertInstanceOf(Mo.class, service.loginBuiltInMo("mo4@bupt.edu.cn", "mo423456"));
        assertInstanceOf(Mo.class, service.loginBuiltInMo("mo5@bupt.edu.cn", "mo523456"));
        assertInstanceOf(model.Admin.class, service.loginBuiltInAdmin("admin1@bupt.edu.cn", "admin123456"));
        assertInstanceOf(model.Admin.class, service.loginBuiltInAdmin("admin2@bupt.edu.cn", "admin223456"));
        assertInstanceOf(model.Admin.class, service.loginBuiltInAdmin("admin3@bupt.edu.cn", "admin323456"));
        assertInstanceOf(model.Admin.class, service.loginBuiltInAdmin("admin4@bupt.edu.cn", "admin423456"));
        assertInstanceOf(model.Admin.class, service.loginBuiltInAdmin("admin5@bupt.edu.cn", "admin523456"));
        assertNull(service.loginBuiltInMo("mo1@bupt.edu.cn", "wrong"));
    }
}
