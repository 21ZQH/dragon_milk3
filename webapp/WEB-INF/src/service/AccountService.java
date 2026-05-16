package service;

import model.User;

public interface AccountService {
    boolean isEmailRegistered(String email);

    void saveUser(User user);

    User validateUser(String password, String email);

    User validateUser(String password, String role, String email);

    AuthResult register(String name, String password, String role, String email);

    User login(String password, String role, String email);

    enum AuthStatus {
        SUCCESS,
        EMAIL_REGISTERED,
        UNKNOWN_ROLE
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

        public AuthStatus getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }
    }
}
