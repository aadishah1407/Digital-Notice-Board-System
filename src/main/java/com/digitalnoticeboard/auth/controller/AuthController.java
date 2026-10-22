package com.digitalnoticeboard.auth.controller;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;
import com.digitalnoticeboard.auth.dto.RegisterRequest;
import com.digitalnoticeboard.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return roleLogin(request, "ADMIN");
    }

    @PostMapping("/auth/admin/login")
    public ResponseEntity<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        return roleLogin(request, "ADMIN");
    }

    @PostMapping("/auth/student/login")
    public ResponseEntity<LoginResponse> studentLogin(@Valid @RequestBody LoginRequest request) {
        return roleLogin(request, "STUDENT");
    }

    @PostMapping({"/auth/register", "/auth/admin/register"})
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new RegistrationResponse("Registration successful. Please log in."));
    }

    @PostMapping("/auth/student/register")
    public ResponseEntity<RegistrationResponse> registerStudent(@Valid @RequestBody RegisterRequest request) {
        authService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new RegistrationResponse("Student registration successful. Please log in."));
    }

    private ResponseEntity<LoginResponse> roleLogin(LoginRequest request, String role) {
        LoginResponse response = authService.loginAsRole(request, role);
        if (!response.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    public static class RegistrationResponse {
        private final String message;

        public RegistrationResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
