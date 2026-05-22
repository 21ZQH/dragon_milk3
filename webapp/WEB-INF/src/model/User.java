package model;

/**
 * Base type for every account in the system.
 *
 * <p>Concrete subclasses provide the role name used by controllers and
 * services for access checks. The shared fields are intentionally small because
 * TA, MO, and Admin accounts store role-specific state separately.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public abstract class User {
    /** The display name of the user. */
    private String name;
    /** The login password for the user account. */
    private String password;
    /** The email address used as the user's unique identifier for login. */
    private String email;

    /**
     * Constructs a User with the specified password and email.
     *
     * @param password the login password for the user account
     * @param email    the email address to associate with this account
     */
    public User(String password, String email) {

        this.password = password;
        this.email = email;
    }


    /**
     * Returns the display name of the user.
     *
     * @return the user's display name, or {@code null} if not set
     */
    public String getName() {  return name; }

    /**
     * Returns the login password of the user.
     *
     * @return the user's password string
     */
    public String getPassword() { return password; }

    /**
     * Returns the email address of the user.
     *
     * @return the user's email address
     */
    public String getEmail() { return email; }


    /**
     * Sets the display name of the user.
     *
     * @param name the display name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the login password of the user.
     *
     * @param password the new password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email the new email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }


    /**
     * Returns the role identifier for this user.
     * <p>Subclasses must implement this method to return their specific role name
     * (e.g., "Admin", "TA", "Mo").</p>
     *
     * @return the role name string identifying the user type
     */
    public abstract String getRole();
}

