package service.impl;

import java.security.SecureRandom;
import java.util.List;

import model.Admin;
import model.Mo;
import model.TA;
import model.User;
import repository.CourseRepository;
import repository.UserRepository;
import repository.impl.TxtCourseRepositoryImpl;
import repository.impl.TxtUserRepositoryImpl;
import service.AccountService;

/**
 * Implementation of the {@link AccountService} interface.
 * Provides concrete logic for user registration, role-based login, built-in
 * admin seeding, and access key generation for Teaching Assistant (TA) users.
 * Delegates persistence operations to {@link UserRepository} and
 * {@link CourseRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class AccountServiceImpl implements AccountService {
    /** The email suffix required for TA registration with a BUPT email address. */
    private static final String BUPT_EMAIL_SUFFIX = "@bupt.edu.cn";

    /** Pre-configured built-in Admin accounts. */
    private static final String[][] BUILT_IN_ADMIN_ACCOUNTS = {
            {"Built-in Admin 1", "admin1@bupt.edu.cn", "admin123456"},
            {"Built-in Admin 2", "admin2@bupt.edu.cn", "admin223456"},
            {"Built-in Admin 3", "admin3@bupt.edu.cn", "admin323456"},
            {"Built-in Admin 4", "admin4@bupt.edu.cn", "admin423456"},
            {"Built-in Admin 5", "admin5@bupt.edu.cn", "admin523456"}
    };

    /** Secure random generator for creating TA access keys. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Repository for user persistence operations. */
    private final UserRepository userRepository;

    /** Repository for course persistence operations. */
    private final CourseRepository courseRepository;

    /**
     * Constructs a new {@code AccountServiceImpl} with default
     * {@link TxtUserRepositoryImpl} and {@link TxtCourseRepositoryImpl}.
     */
    public AccountServiceImpl() {
        this(new TxtUserRepositoryImpl(), new TxtCourseRepositoryImpl());
    }

    /**
     * Constructs a new {@code AccountServiceImpl} with the given repositories.
     *
     * @param userRepository   the repository for user data access
     * @param courseRepository the repository for course data access
     */
    AccountServiceImpl(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Checks whether the given email address is already registered.
     *
     * @param email the email address to check
     * @return {@code true} if the email is already registered, {@code false} otherwise
     */
    @Override
    public boolean isEmailRegistered(String email) {
        return userRepository.isEmailRegistered(email);
    }

    /**
     * Persists a new user to the data store.
     *
     * @param user the user entity to save
     */
    @Override
    public void saveUser(User user) {
        userRepository.saveUser(user);
    }

    /**
     * Validates user credentials (password and email) without role filtering.
     *
     * @param password the user's password
     * @param email    the user's email address
     * @return the matching {@link User} if credentials are valid, or {@code null} otherwise
     */
    @Override
    public User validateUser(String password, String email) {
        return userRepository.validateUser(password, email);
    }

    /**
     * Validates user credentials (password, role, and email).
     *
     * @param password the user's password
     * @param role     the user's role (e.g., "TA", "Admin", "Mo")
     * @param email    the user's email address
     * @return the matching {@link User} if credentials are valid, or {@code null} otherwise
     */
    @Override
    public User validateUser(String password, String role, String email) {
        return userRepository.validateUser(password, role, email);
    }

    /**
     * Registers a new user with the given name, password, role, and email.
     *
     * @param name     the display name of the user
     * @param password the password for the new account
     * @param role     the role of the user (e.g., "TA", "Admin", "Mo")
     * @param email    the email address for the new account
     * @return an {@link AuthResult} indicating success or the specific failure reason
     */
    @Override
    public AuthResult register(String name, String password, String role, String email) {
        if (isEmailRegistered(email)) {
            return AuthResult.emailRegistered();
        }

        User user = buildUser(role, password, email);
        if (user == null) {
            return AuthResult.unknownRole();
        }

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        saveUser(user);
        return AuthResult.success(user);
    }

    /**
     * Authenticates a user by password, optionally filtering by role.
     *
     * @param password the user's password
     * @param role     the optional role filter (may be blank to skip)
     * @param email    the user's email address
     * @return the authenticated {@link User} if successful, or {@code null} otherwise
     */
    @Override
    public User login(String password, String role, String email) {
        if (role == null || role.trim().isEmpty()) {
            return validateUser(password, email);
        }
        return validateUser(password, role, email);
    }

    /**
     * Registers a TA using a BUPT email address and generates an access key.
     *
     * @param name  the display name of the TA
     * @param email the BUPT email address (must end with {@code @bupt.edu.cn})
     * @return an {@link AuthResult} indicating success or the specific failure reason
     */
    @Override
    public AuthResult registerTaWithBuptEmail(String name, String email) {
        if (email == null || !email.toLowerCase().endsWith(BUPT_EMAIL_SUFFIX)) {
            return AuthResult.invalidEmailDomain();
        }
        if (isEmailRegistered(email)) {
            return AuthResult.emailRegistered();
        }

        TA ta = new TA(generateAccessKey(), email);
        if (name != null && !name.trim().isEmpty()) {
            ta.setName(name.trim());
        }
        saveUser(ta);
        return AuthResult.success(ta);
    }

    /**
     * Authenticates a TA user by their access key.
     *
     * @param accessKey the access key to look up
     * @return the matching {@link User} if found, or {@code null} otherwise
     */
    @Override
    public User loginTaByAccessKey(String accessKey) {
        if (accessKey == null || accessKey.isBlank()) {
            return null;
        }
        List<TA> taUsers = userRepository.getTAList();
        for (TA ta : taUsers) {
            if (accessKey.trim().equals(ta.getPassword())) {
                return ta;
            }
        }
        return null;
    }

    /**
     * Logs in an MO account by email and password. MO accounts are created by
     * an Admin, while the method name is retained for controller compatibility.
     *
     * @param email    the MO's email address
     * @param password the MO's password
     * @return the authenticated {@link User} if successful, or {@code null} otherwise
     */
    @Override
    public User loginBuiltInMo(String email, String password) {
        return validateUser(password, "Mo", email);
    }

    /**
     * Logs in a built-in Admin account by email and password, ensuring built-in
     * accounts have been seeded first.
     *
     * @param email    the Admin's email address
     * @param password the Admin's password
     * @return the authenticated {@link User} if successful, or {@code null} otherwise
     */
    @Override
    public User loginBuiltInAdmin(String email, String password) {
        ensureBuiltInAccounts();
        return validateUser(password, "Admin", email);
    }

    /**
     * Ensures that all built-in Admin accounts exist in the data store.
     */
    @Override
    public void ensureBuiltInAccounts() {
        for (String[] account : BUILT_IN_ADMIN_ACCOUNTS) {
            if (!isEmailRegistered(account[1])) {
                Admin admin = new Admin(account[2], account[1]);
                admin.setName(account[0]);
                saveUser(admin);
            }
        }
    }

    /**
     * Builds a new {@link User} instance based on the given role.
     *
     * @param role     the role string ("TA", "Admin", or "Mo")
     * @param password the account password
     * @param email    the account email
     * @return a new {@link TA}, {@link Admin}, {@link Mo}, or {@code null} if the role is unknown
     */
    private User buildUser(String role, String password, String email) {
        if ("TA".equals(role)) {
            return new TA(password, email);
        }
        if ("Admin".equals(role)) {
            return new Admin(password, email);
        }
        if ("Mo".equals(role)) {
            return new Mo(password, email);
        }
        return null;
    }

    /**
     * Generates a random access key in the format {@code TA-XXXXXXXX}.
     *
     * @return a newly generated access key string
     */
    private String generateAccessKey() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder key = new StringBuilder("TA-");
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                key.append('-');
            }
            key.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return key.toString();
    }
}
