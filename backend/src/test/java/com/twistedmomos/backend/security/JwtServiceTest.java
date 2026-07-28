package com.twistedmomos.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.twistedmomos.backend.entity.Role;
import com.twistedmomos.backend.entity.RoleName;
import com.twistedmomos.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-only-secret-key-must-be-at-least-32-bytes-long-for-hs256",
                900_000L,
                2_592_000_000L);
        jwtService = new JwtService(properties);

        Role role = Role.builder().id(1L).name(RoleName.CUSTOMER).build();
        user = User.builder().id(7L).name("Jwt Tester").email("jwt.tester@example.com").role(role).build();
    }

    @Test
    void generateAccessToken_producesATokenTheServiceAcceptsAsValid() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractEmail_returnsTheSubjectTheTokenWasIssuedFor() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("jwt.tester@example.com");
    }

    @Test
    void isTokenValid_rejectsGarbageInput() {
        assertThat(jwtService.isTokenValid("this-is-not-a-jwt")).isFalse();
    }

    @Test
    void isTokenValid_rejectsATokenSignedWithADifferentSecret() {
        JwtProperties otherProperties = new JwtProperties(
                "a-completely-different-secret-key-also-32-bytes-plus",
                900_000L,
                2_592_000_000L);
        JwtService otherService = new JwtService(otherProperties);
        String tokenFromOtherService = otherService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(tokenFromOtherService)).isFalse();
    }

    @Test
    void expirationHelpers_reflectConfiguredValues() {
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(900L);
        assertThat(jwtService.getRefreshTokenExpirationMs()).isEqualTo(2_592_000_000L);
    }
}
