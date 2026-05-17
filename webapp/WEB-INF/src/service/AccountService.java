package service;

import model.User;

public interface AccountService {
    boolean isEmailRegistered(String email);

    void saveUser(User user);

    User validateUser(String password, String email);

    User validateUser(String password, String role, String email);

    AuthResult register(String name, String password, String role, String email);

    User login(String password, String role, String email);

    AuthResult registerTaWithBuptEmail(String name, String email);

    User loginTaByAccessKey(String accessKey);

    User loginBuiltInMo(String email, String password);

    User loginBuiltInAdmin(String email, String password);

    void ensureBuiltInAccounts();

    enum AuthStatus {
        SUCCESS,
        EMAIL_REGISTERED,
        UNKNOWN_ROLE,
        INVALID_EMAIL_DOMAIN
    }

    final class AuthResult {
        private final AuthStatus status;
        private final User user;

        private AuthResult(AuthStatus status, User user) {
            this.status = status;
            this.user = user;
        }

        public static AuthResult success(User user) {
            return new AuthResult(AuthStatus.SUCCESS, user);
        }

        public static AuthResult emailRegistered() {
            return new AuthResult(AuthStatus.EMAIL_REGISTERED, null);
        }

        public static AuthResult unknownRole() {
            return new AuthResult(AuthStatus.UNKNOWN_ROLE, null);
        }

        public static AuthResult invalidEmailDomain() {
            return new AuthResult(AuthStatus.INVALID_EMAIL_DOMAIN, null);
        }

        public AuthStatus getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }
    }
}
