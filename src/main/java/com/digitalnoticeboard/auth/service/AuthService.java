package com.digitalnoticeboard.auth.service;

import com.digitalnoticeboard.auth.dto.LoginRequest;
import com.digitalnoticeboard.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
