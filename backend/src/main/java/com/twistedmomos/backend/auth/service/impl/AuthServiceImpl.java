package com.twistedmomos.backend.auth.service.impl;

import com.twistedmomos.backend.auth.dto.request.LoginRequest;
import com.twistedmomos.backend.auth.dto.request.RefreshRequest;
import com.twistedmomos.backend.auth.dto.request.RegisterRequest;
import com.twistedmomos.backend.auth.dto.response.AuthResponse;
import com.twistedmomos.backend.auth.entity.RefreshToken;
import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.RoleName;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.shared.exception.DuplicateResourceException;
import com.twistedmomos.backend.auth.mapper.UserMapper;
import com.twistedmomos.backend.auth.repository.RoleRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.auth.security.CustomUserDetails;
import com.twistedmomos.backend.auth.security.JwtService;
import com.twistedmomos.backend.auth.security.LoginRateLimiter;
import com.twistedmomos.backend.auth.service.AuthService;
import com.twistedmomos.backend.auth.service.EmailVerificationService;
import com.twistedmomos.backend.auth.service.RefreshTokenService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Log lines here deliberately carry no email address, only ids assigned after the
// fact — see the no-PII rule. That means failed logins cannot be grouped by target
// from logs alone; LoginRateLimiter holds that state instead.
@Slf4j
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
    private final EmailVerificationService emailVerificationService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user");

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected — email already registered");
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> {
                    log.error("CUSTOMER role missing — V2 migration has not been applied");
                    return new IllegalStateException("CUSTOMER role is not seeded — check V2 migration");
                });

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .roles(new HashSet<>(Set.of(customerRole)))
                .enabled(true)
                .build();
        user = userRepository.save(user);
        log.info("User registered: userId={} role={}", user.getId(), customerRole.getName());

        emailVerificationService.sendVerificationLink(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt");

        // Throws TooManyAttemptsException when the account is locked out; that path is
        // logged by LoginRateLimiter, which owns the attempt counters.
        loginRateLimiter.checkAllowed(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            loginRateLimiter.recordSuccess(request.email());
            User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
            log.info("Login succeeded: userId={} roles={}", user.getId(), user.getRoles().size());
            return buildAuthResponse(user);
        } catch (AuthenticationException ex) {
            // recordFailure logs the attempt count and any resulting lockout — it owns
            // the counters, so it is the only place that knows them.
            loginRateLimiter.recordFailure(request.email());
            throw ex;
        }
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        // rotate() logs rejection and reuse-detection; both are its own decisions.
        RefreshToken newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        User user = newRefreshToken.getUser();
        log.info("Access token refreshed: userId={}", user.getId());
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
        log.info("Logged out");
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

    @Override
    @Transactional
    public void resendVerification(String email) {
        // No exception when the address is unknown: a 404 here would confirm which
        // addresses are registered. The endpoint answers 204 either way.
        userRepository.findByEmail(email).ifPresent(emailVerificationService::sendVerificationLink);
    }
}
