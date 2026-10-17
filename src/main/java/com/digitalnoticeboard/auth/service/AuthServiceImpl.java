package com.digitalnoticeboard.auth.service;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;
import com.digitalnoticeboard.auth.dto.RegisterRequest;
import com.digitalnoticeboard.auth.model.User;
import com.digitalnoticeboard.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            return LoginResponse.failed("Username and password are required");
        }

        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty()) {
            return LoginResponse.failed("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            return LoginResponse.failed("Password is required");
        }

        Optional<User> userOptional = userRepository.findByUsername(username.trim());
        if (userOptional.isEmpty()) {
            return LoginResponse.failed("Invalid username or password");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return LoginResponse.failed("Invalid username or password");
        }

        return LoginResponse.success(user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        String username = request.getUsername().trim();
        if (username.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be 100 characters or fewer");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered");
        }

        userRepository.save(new User(username, passwordEncoder.encode(request.getPassword()), "ROLE_ADMIN"));
    }
}
