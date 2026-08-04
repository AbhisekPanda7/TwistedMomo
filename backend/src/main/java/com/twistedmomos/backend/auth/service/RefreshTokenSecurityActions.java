package com.twistedmomos.backend.auth.service;

import com.twistedmomos.backend.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Split out from RefreshTokenService so the reuse-detection "revoke every token this user
 * holds" action runs in its own REQUIRES_NEW transaction. RefreshTokenService.rotate()
 * throws immediately after triggering this, and since throwing an unchecked exception
 * rolls back the caller's own @Transactional method, running this in the SAME transaction
 * would silently undo the very revocation meant to respond to suspected token theft.
 * REQUIRES_NEW only works across a real proxy boundary, hence the separate bean.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenSecurityActions {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllActiveForUser(Long userId) {
        refreshTokenRepository.revokeAllActiveForUser(userId);
    }
}
