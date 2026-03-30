package model;

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
