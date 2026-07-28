package com.twistedmomos.backend.service.impl;

import com.twistedmomos.backend.dto.request.LoginRequest;
import com.twistedmomos.backend.dto.request.RefreshRequest;
import com.twistedmomos.backend.dto.request.RegisterRequest;
import com.twistedmomos.backend.dto.response.AuthResponse;
import com.twistedmomos.backend.entity.RefreshToken;
import com.twistedmomos.backend.entity.Role;
import com.twistedmomos.backend.entity.RoleName;
import com.twistedmomos.backend.entity.User;
import com.twistedmomos.backend.exception.DuplicateResourceException;
import com.twistedmomos.backend.mapper.UserMapper;
import com.twistedmomos.backend.repository.RoleRepository;
import com.twistedmomos.backend.repository.UserRepository;
import com.twistedmomos.backend.security.CustomUserDetails;
import com.twistedmomos.backend.security.JwtService;
import com.twistedmomos.backend.security.LoginRateLimiter;
import com.twistedmomos.backend.service.AuthService;
import com.twistedmomos.backend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final LoginRateLimiter loginRateLimiter;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role is not seeded — check V2 migration"));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(customerRole)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        loginRateLimiter.checkAllowed(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            loginRateLimiter.recordSuccess(request.email());
            User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
            return buildAuthResponse(user);
        } catch (AuthenticationException ex) {
            loginRateLimiter.recordFailure(request.email());
            throw ex;
        }
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        User user = newRefreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(
                accessToken,
                newRefreshToken.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toResponse(user));
    }

    @Override
    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toResponse(user));
    }
}
