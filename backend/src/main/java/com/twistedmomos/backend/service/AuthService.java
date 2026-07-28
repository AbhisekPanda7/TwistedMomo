package com.twistedmomos.backend.service;

import com.twistedmomos.backend.dto.request.LoginRequest;
import com.twistedmomos.backend.dto.request.RefreshRequest;
import com.twistedmomos.backend.dto.request.RegisterRequest;
import com.twistedmomos.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(RefreshRequest request);
}
