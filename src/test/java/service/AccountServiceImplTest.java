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
