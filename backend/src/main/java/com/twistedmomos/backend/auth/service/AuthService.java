package com.twistedmomos.backend.auth.service;

import com.twistedmomos.backend.auth.dto.request.LoginRequest;
import com.twistedmomos.backend.auth.dto.request.RefreshRequest;
import com.twistedmomos.backend.auth.dto.request.RegisterRequest;
import com.twistedmomos.backend.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(RefreshRequest request);
}
