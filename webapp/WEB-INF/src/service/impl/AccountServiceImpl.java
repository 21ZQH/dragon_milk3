package service.impl;

import model.Admin;
import model.Mo;
import model.TA;
import model.User;
import service.AccountService;
import store.UserStore;

public class AccountServiceImpl implements AccountService {
    @Override
    public boolean isEmailRegistered(String email) {
        return UserStore.isEmailRegistered(email);
    }

    @Override
    public void saveUser(User user) {
        UserStore.saveUser(user);
    }

    @Override
    public User validateUser(String password, String email) {
        return UserStore.validateUser(password, email);
    }

    @Override
    public User validateUser(String password, String role, String email) {
        return UserStore.validateUser(password, role, email);
    }

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

    @Override
    public User login(String password, String role, String email) {
        if (role == null || role.trim().isEmpty()) {
            return validateUser(password, email);
        }
        return validateUser(password, role, email);
    }

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
}
