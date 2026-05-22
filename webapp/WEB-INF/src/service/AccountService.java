package service;

import model.User;

/**
 * Service interface for managing user accounts including registration,
 * login, authentication, and built-in account initialization.
 * Provides methods for validating user credentials and handling
 * registration with various role types.
 *
 * @version 1.0
 * @since 2025
 */
public interface AccountService {
    /**
     * Checks whether the given email address is already registered in the system.
     *
     * @param email the email address to check
     * @return {@code true} if the email is already registered, {@code false} otherwise
     */
    boolean isEmailRegistered(String email);

    /**
     * Saves a new user to the data store.
     *
     * @param user the User object to persist
     */
    void saveUser(User user);

    /**
     * Validates a user's credentials by password and email,
     * returning the corresponding User if authentication succeeds.
     *
     * @param password the user's password
     * @param email    the user's email address
     * @return the authenticated User, or {@code null} if credentials are invalid
     */
    User validateUser(String password, String email);

    /**
     * Validates a user's credentials by password, role, and email,
     * returning the corresponding User if authentication succeeds.
     *
     * @param password the user's password
     * @param role     the user's role type
     * @param email    the user's email address
     * @return the authenticated User, or {@code null} if credentials are invalid
     */
    User validateUser(String password, String role, String email);

    /**
     * Registers a new user with the given details and returns the authentication result.
     *
     * @param name     the display name of the user
     * @param password the chosen password
     * @param role     the role to assign to the new user
     * @param email    the email address for the new account
     * @return an {@link AuthResult} indicating success or the specific failure reason
     */
    AuthResult register(String name, String password, String role, String email);

    /**
     * Authenticates a user and returns the corresponding User object on success.
     *
     * @param password the user's password
     * @param role     the user's role
     * @param email    the user's email address
     * @return the authenticated User, or {@code null} if login fails
     */
    User login(String password, String role, String email);

    /**
     * Registers a Teaching Assistant account using a BUPT email domain,
     * typically triggered during initial TA setup.
     *
     * @param name  the display name of the TA
     * @param email the BUPT email address for the TA
     * @return an {@link AuthResult} indicating success or the specific failure reason
     */
    AuthResult registerTaWithBuptEmail(String name, String email);

    /**
     * Authenticates a Teaching Assistant using an access key
     * instead of a password.
     *
     * @param accessKey the access key assigned to the TA
     * @return the authenticated User, or {@code null} if the access key is invalid
     */
    User loginTaByAccessKey(String accessKey);

    /**
     * Authenticates a built-in MO (Module Officer) account using email and password.
     *
     * @param email    the MO account email
     * @param password the MO account password
     * @return the authenticated User, or {@code null} if authentication fails
     */
    User loginBuiltInMo(String email, String password);

    /**
     * Authenticates a built-in admin account using email and password.
     *
     * @param email    the admin account email
     * @param password the admin account password
     * @return the authenticated User, or {@code null} if authentication fails
     */
    User loginBuiltInAdmin(String email, String password);

    /**
     * Ensures that all built-in system accounts (admin, MO defaults, etc.)
     * exist in the data store, creating them if necessary.
     */
    void ensureBuiltInAccounts();

    /**
     * Enumerates the possible outcomes of an authentication or registration operation.
     */
    enum AuthStatus {
        /** Authentication or registration completed successfully. */
        SUCCESS,
        /** The email address is already associated with an existing account. */
        EMAIL_REGISTERED,
        /** The specified role is not recognized by the system. */
        UNKNOWN_ROLE,
        /** The email domain is not permitted for the requested operation. */
        INVALID_EMAIL_DOMAIN
    }

    /**
     * Represents the result of an authentication or registration attempt,
     * encapsulating both the status code and the associated User if successful.
     */
    final class AuthResult {
        private final AuthStatus status;
        private final User user;

        /**
         * Constructs an AuthResult with the given status and user.
         *
         * @param status the authentication status
         * @param user   the authenticated user, or {@code null} on failure
         */
        private AuthResult(AuthStatus status, User user) {
            this.status = status;
            this.user = user;
        }

        /**
         * Creates a successful authentication result with the given user.
         *
         * @param user the successfully authenticated user
         * @return an AuthResult with status {@link AuthStatus#SUCCESS}
         */
        public static AuthResult success(User user) {
            return new AuthResult(AuthStatus.SUCCESS, user);
        }

        /**
         * Creates a failure result indicating the email is already registered.
         *
         * @return an AuthResult with status {@link AuthStatus#EMAIL_REGISTERED}
         */
        public static AuthResult emailRegistered() {
            return new AuthResult(AuthStatus.EMAIL_REGISTERED, null);
        }

        /**
         * Creates a failure result indicating an unknown role was specified.
         *
         * @return an AuthResult with status {@link AuthStatus#UNKNOWN_ROLE}
         */
        public static AuthResult unknownRole() {
            return new AuthResult(AuthStatus.UNKNOWN_ROLE, null);
        }

        /**
         * Creates a failure result indicating the email domain is not allowed.
         *
         * @return an AuthResult with status {@link AuthStatus#INVALID_EMAIL_DOMAIN}
         */
        public static AuthResult invalidEmailDomain() {
            return new AuthResult(AuthStatus.INVALID_EMAIL_DOMAIN, null);
        }

        /**
         * Returns the authentication status of this result.
         *
         * @return the {@link AuthStatus} value
         */
        public AuthStatus getStatus() {
            return status;
        }

        /**
         * Returns the user associated with this result, if authentication succeeded.
         *
         * @return the authenticated User, or {@code null} if authentication failed
         */
        public User getUser() {
            return user;
        }
    }
}
