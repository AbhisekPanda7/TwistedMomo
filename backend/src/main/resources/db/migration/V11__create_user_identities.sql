-- One user, many sign-in identities. A separate table rather than provider columns on
-- users, so Apple or Facebook later cost nothing.

CREATE TABLE user_identities
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(20)  NOT NULL,
    -- Google's stable subject id. Keyed on this rather than email, which can change.
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(190) NOT NULL,
    linked_at        DATETIME(6)  NOT NULL,
    CONSTRAINT uk_user_identities_provider_subject UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_user_identities_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_user_identities_user_id ON user_identities (user_id);

-- Accounts created through Google never have one. Password login already rejects a null
-- hash, so this cannot become a way in.
ALTER TABLE users MODIFY COLUMN password VARCHAR(100) NULL;
