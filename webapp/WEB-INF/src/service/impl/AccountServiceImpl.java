package service.impl;

import java.security.SecureRandom;
import java.util.List;

import model.Admin;
import model.Mo;
import model.TA;
import model.User;
import service.AccountService;
import store.UserStore;

public class AccountServiceImpl implements AccountService {
    private static final String BUPT_EMAIL_SUFFIX = "@bupt.edu.cn";
    private static final String[][] BUILT_IN_MO_ACCOUNTS = {
            {"Built-in MO 1", "mo1@bupt.edu.cn", "mo123456"},
            {"Built-in MO 2", "mo2@bupt.edu.cn", "mo223456"},
            {"Built-in MO 3", "mo3@bupt.edu.cn", "mo323456"},
            {"Built-in MO 4", "mo4@bupt.edu.cn", "mo423456"},
            {"Built-in MO 5", "mo5@bupt.edu.cn", "mo523456"}
    };
    private static final String[][] BUILT_IN_ADMIN_ACCOUNTS = {
            {"Built-in Admin 1", "admin1@bupt.edu.cn", "admin123456"},
            {"Built-in Admin 2", "admin2@bupt.edu.cn", "admin223456"},
            {"Built-in Admin 3", "admin3@bupt.edu.cn", "admin323456"},
            {"Built-in Admin 4", "admin4@bupt.edu.cn", "admin423456"},
            {"Built-in Admin 5", "admin5@bupt.edu.cn", "admin523456"}
    };
    private static final SecureRandom RANDOM = new SecureRandom();

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

    @Override
    public User loginTaByAccessKey(String accessKey) {
        if (accessKey == null || accessKey.isBlank()) {
            return null;
        }
        List<TA> taUsers = UserStore.getTAList();
        for (TA ta : taUsers) {
            if (accessKey.trim().equals(ta.getPassword())) {
                return ta;
            }
        }
        return null;
    }

    @Override
    public User loginBuiltInMo(String email, String password) {
        ensureBuiltInAccounts();
        return validateUser(password, "Mo", email);
    }

    @Override
    public User loginBuiltInAdmin(String email, String password) {
        ensureBuiltInAccounts();
        return validateUser(password, "Admin", email);
    }

    @Override
    public void ensureBuiltInAccounts() {
        for (String[] account : BUILT_IN_MO_ACCOUNTS) {
            if (!isEmailRegistered(account[1])) {
                Mo mo = new Mo(account[2], account[1]);
                mo.setName(account[0]);
                saveUser(mo);
            }
        }
        for (String[] account : BUILT_IN_ADMIN_ACCOUNTS) {
            if (!isEmailRegistered(account[1])) {
                Admin admin = new Admin(account[2], account[1]);
                admin.setName(account[0]);
                saveUser(admin);
            }
        }
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
