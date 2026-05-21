package model;

/**
 * Administrator account.
 *
 * <p>Admins manage MO accounts, inspect candidates, and configure the two
 * recruitment deadlines used by TA and MO workflows.</p>
 */
public class Admin extends User {
    private String role="Admin";
    public Admin(String password,String email) {
        super(password, email);
    }
    @Override
    public String getRole() { 
        return role; 
    }
}
