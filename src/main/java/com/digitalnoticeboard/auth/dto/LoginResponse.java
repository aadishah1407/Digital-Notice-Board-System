package com.digitalnoticeboard.auth.dto;

public class LoginResponse {

    private boolean authenticated;
    private String username;
    private String role;
    private String message;
    private String department;
    private String academicYear;

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

    public static LoginResponse success(String username, String role, String department, String academicYear) {
        LoginResponse response = success(username, role);
        response.setDepartment(department);
        response.setAcademicYear(academicYear);
        return response;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}
