package com.digitalnoticeboard.service;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;
import com.digitalnoticeboard.auth.dto.RegisterRequest;
import com.digitalnoticeboard.auth.model.User;
import com.digitalnoticeboard.auth.repository.UserRepository;
import com.digitalnoticeboard.auth.service.AuthService;
import com.digitalnoticeboard.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentLoginServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void validStudentCredentialsAreAcceptedForStudentPortal() {
        User student = new User("student1", "encoded-password", "ROLE_STUDENT");
        student.setDepartment("Computer Science");
        student.setAcademicYear("2026-2027");
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        LoginResponse response = authService.loginAsRole(new LoginRequest("student1", "password123"), "STUDENT");

        assertTrue(response.isAuthenticated());
        assertEquals("student1", response.getUsername());
        assertEquals("ROLE_STUDENT", response.getRole());
    }

    @Test
    void adminAccountCannotAuthenticateThroughStudentPortal() {
        User admin = new User("admin", "encoded-password", "ROLE_ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        LoginResponse response = authService.loginAsRole(new LoginRequest("admin", "password123"), "STUDENT");

        assertFalse(response.isAuthenticated());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void studentAccountCannotAuthenticateThroughAdminPortal() {
        User student = new User("student1", "encoded-password", "ROLE_STUDENT");
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        LoginResponse response = authService.loginAsRole(new LoginRequest("student1", "password123"), "ADMIN");

        assertFalse(response.isAuthenticated());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void incorrectStudentPasswordIsRejected() {
        User student = new User("student1", "encoded-password", "ROLE_STUDENT");
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        LoginResponse response = authService.loginAsRole(new LoginRequest("student1", "wrong-password"), "STUDENT");

        assertFalse(response.isAuthenticated());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void studentRegistrationStoresStudentRoleAndAcademicDetails() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("student1");
        request.setPassword("password123");
        request.setDepartment("Computer Science");
        request.setAcademicYear("2026-2027");
        when(userRepository.existsByUsername("student1")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerStudent(request);

        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("ROLE_STUDENT", savedUser.getRole());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("Computer Science", savedUser.getDepartment());
        assertEquals("2026-2027", savedUser.getAcademicYear());
    }

    @Test
    void duplicateStudentUsernameIsRejected() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("student1");
        request.setPassword("password123");
        request.setDepartment("Computer Science");
        request.setAcademicYear("2026-2027");
        when(userRepository.existsByUsername("student1")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.registerStudent(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
