package com.digitalnoticeboard.service;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;
import com.digitalnoticeboard.auth.model.User;
import com.digitalnoticeboard.auth.repository.UserRepository;
import com.digitalnoticeboard.auth.service.AuthService;
import com.digitalnoticeboard.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void validCredentialsAreAccepted() {
        String username = "admin";
        String password = "StrongPass123!";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User(username, passwordEncoder.encode(password), "ROLE_ADMIN")));

        LoginResponse response = authService.login(new LoginRequest(username, password));

        assertNotNull(response);
        assertTrue(response.isAuthenticated());
        assertEquals(username, response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());
    }

    @Test
    void invalidCredentialsAreRejected() {
        String username = "admin";
        String password = "WrongPassword";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User(username, passwordEncoder.encode("StrongPass123!"), "ROLE_ADMIN")));

        LoginResponse response = authService.login(new LoginRequest(username, password));

        assertNotNull(response);
        assertFalse(response.isAuthenticated());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void blankOrMissingCredentialsAreRejected() {
        LoginResponse emptyUsernameResponse = authService.login(new LoginRequest("  ", "StrongPass123!"));
        LoginResponse emptyPasswordResponse = authService.login(new LoginRequest("admin", "  "));
        LoginResponse missingPasswordResponse = authService.login(new LoginRequest("admin", null));

        assertFalse(emptyUsernameResponse.isAuthenticated());
        assertFalse(emptyPasswordResponse.isAuthenticated());
        assertFalse(missingPasswordResponse.isAuthenticated());
        assertTrue(emptyUsernameResponse.getMessage().contains("Username") || emptyUsernameResponse.getMessage().contains("required"));
        assertTrue(emptyPasswordResponse.getMessage().contains("Password") || emptyPasswordResponse.getMessage().contains("required"));
        assertTrue(missingPasswordResponse.getMessage().contains("Password") || missingPasswordResponse.getMessage().contains("required"));
    }
}
