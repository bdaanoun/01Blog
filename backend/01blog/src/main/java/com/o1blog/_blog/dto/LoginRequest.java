package com.o1blog._blog.dto;

public class LoginRequest {
    private String identifier;
    private String password;

    public LoginRequest() {}
    
    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() { return identifier; }
    public String getPassword() { return password; }
    
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setPassword(String password) { this.password = password; }
}