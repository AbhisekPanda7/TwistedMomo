CREATE TABLE role_audit (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id     BIGINT NOT NULL,
    subject_id   BIGINT NOT NULL,
    -- A string, not an FK: an audit row must outlive the role it names.
    role_name    VARCHAR(30) NOT NULL,
    action       VARCHAR(10) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id),
    CONSTRAINT fk_role_audit_subject FOREIGN KEY (subject_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_role_audit_subject ON role_audit (subject_id, created_at);
