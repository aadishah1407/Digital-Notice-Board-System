package com.digitalnoticeboard.auth.dto;

public class LoginResponse {

    private boolean authenticated;
    private String username;
    private String role;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(boolean authenticated, String username, String role, String message) {
        this.authenticated = authenticated;
        this.username = username;
        this.role = role;
        this.message = message;
    }

    public static LoginResponse success(String username, String role) {
        return new LoginResponse(true, username, role, "Login successful");
    }

    public static LoginResponse failed(String message) {
        return new LoginResponse(false, null, null, message);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
