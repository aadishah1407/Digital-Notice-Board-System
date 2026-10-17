package com.digitalnoticeboard.auth.service;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;
import com.digitalnoticeboard.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
