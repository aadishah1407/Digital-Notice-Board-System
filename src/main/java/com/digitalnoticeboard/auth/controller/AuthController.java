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
        LoginResponse response = authService.login(request);
        if (!response.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new RegistrationResponse("Registration successful. Please log in."));
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
