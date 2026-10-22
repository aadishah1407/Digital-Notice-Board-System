package com.digitalnoticeboard.auth.service;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;
import com.digitalnoticeboard.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse loginAsRole(LoginRequest request, String expectedRole);

    void register(RegisterRequest request);

    void registerStudent(RegisterRequest request);
}
