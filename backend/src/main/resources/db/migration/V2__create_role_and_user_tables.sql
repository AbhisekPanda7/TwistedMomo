CREATE TABLE roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB;

INSERT INTO roles (name) VALUES ('CUSTOMER'), ('ADMIN');

-- email is capped at 190 (not 191+) to stay safely under InnoDB's 767-byte
-- index key limit once combined with a utf8mb4 (4 bytes/char) unique index.
CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(190) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    role_id    BIGINT NOT NULL,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;

CREATE TABLE refresh_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
