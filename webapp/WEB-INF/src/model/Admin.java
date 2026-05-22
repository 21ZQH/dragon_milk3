package model;

/**
 * Administrator account.
 *
 * <p>Admins manage MO accounts, inspect candidates, and configure the two
 * recruitment deadlines used by TA and MO workflows.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class Admin extends User {
    /** The fixed role identifier for administrator accounts. */
    private String role="Admin";

    /**
     * Constructs an Admin account with the specified credentials.
     *
     * @param password the login password for the admin account
     * @param email    the email address to associate with this admin account
     */
    public Admin(String password,String email) {
        super(password, email);
    }

    /**
     * Returns the role identifier for this administrator.
     *
     * @return the string "Admin" indicating this is an administrator account
     */
    @Override
    public String getRole() {
        return role;
    }
}
