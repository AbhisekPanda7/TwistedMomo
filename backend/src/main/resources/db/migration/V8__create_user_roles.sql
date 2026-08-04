-- Users hold many roles. Kitchen and delivery staff are also customers, so a
-- single role_id forces either duplicate accounts or a hierarchy hack.

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;

INSERT INTO user_roles (user_id, role_id)
SELECT id, role_id FROM users;

ALTER TABLE users DROP FOREIGN KEY fk_users_role;
ALTER TABLE users DROP COLUMN role_id;
