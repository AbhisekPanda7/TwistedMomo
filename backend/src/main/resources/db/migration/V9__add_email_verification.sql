-- Email verification. Gates privilege, not purchase: an unverified user still
-- browses and orders, but cannot be linked to a Google identity or hold a
-- non-CUSTOMER role.

ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Everyone who registered before this feature existed is treated as verified —
-- they never had the chance to confirm, and flipping them to false would lock
-- live accounts out of the privileges above.
UPDATE users SET email_verified = TRUE;

CREATE TABLE email_verification_tokens
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    -- SHA-256 of the token, never the token itself: a leaked table would
    -- otherwise be a set of working verification links.
    token_hash VARCHAR(64)  NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    used_at    DATETIME(6)  DEFAULT NULL,
    created_at DATETIME(6)  NOT NULL,
    CONSTRAINT uk_email_verification_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);
