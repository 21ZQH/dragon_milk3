package model;

/**
 * Base type for every account in the system.
 *
 * <p>Concrete subclasses provide the role name used by controllers and
 * services for access checks. The shared fields are intentionally small because
 * TA, MO, and Admin accounts store role-specific state separately.</p>
 */
public abstract class User {
    private String name;
    private String password;
    private String email;

    public User(String password, String email) {
    
        this.password = password;
        this.email = email;
    }

   
    public String getName() {  return name; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }


    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

 
    public abstract String getRole();
}

