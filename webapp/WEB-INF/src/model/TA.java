package model;

public class TA extends User {
    private String role="TA";
    public TA(String password,String email) {
        super(password, email);
    }
    @Override
    public String getRole() { 
        return role; 
        
        
    }
}