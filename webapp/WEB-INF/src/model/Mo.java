package model;

public class Mo extends User {
    private String role="Mo";
    public Mo(String password,String email) {
        super(password, email);
    }
    @Override
    public String getRole() { return role; }
}