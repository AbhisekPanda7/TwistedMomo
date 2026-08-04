-- Users hold many roles. Kitchen and delivery staff are also customers, so a
-- single role_id forces either duplicate accounts or a hierarchy hack.
--
-- Expand half of expand/contract: users.role_id stays for now so the previous
-- release still runs against this schema and a rollback does not need a DB
-- restore. V9 drops it once no deployed image reads it.

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

-- The new entity no longer maps role_id, so nothing populates it on insert.
-- It has to stop being NOT NULL or every registration fails; the previous
-- release still reads it happily for users that predate this migration.
ALTER TABLE users MODIFY COLUMN role_id BIGINT NULL;
