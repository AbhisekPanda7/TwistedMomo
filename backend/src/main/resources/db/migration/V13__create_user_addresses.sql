-- Addresses a customer has delivered to, so checkout can offer them instead of
-- asking for seven fields again. Owned by the user, not by any one order:
-- orders keep their own copied address columns so a later edit here cannot
-- rewrite where a past order went.

CREATE TABLE user_addresses
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    address_line1  VARCHAR(200) NOT NULL,
    address_line2  VARCHAR(200),
    city           VARCHAR(100) NOT NULL,
    postal_code    VARCHAR(20)  NOT NULL,
    -- Lowercased and whitespace-collapsed line1+line2+city+postal_code. Without
    -- it "1 Test  St " and "1 test st" become two entries a customer cannot
    -- tell apart.
    -- 600 = worst case 200+200+100+20 source columns plus delimiters, rounded up.
    -- Do not narrow this: MySQL strict mode rejects an overflowing insert outright,
    -- and Modulith retries the failing event forever rather than dropping it.
    normalized_key VARCHAR(600) NOT NULL,
    last_used_at   DATETIME(6)  NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    CONSTRAINT uk_user_addresses_key UNIQUE (user_id, normalized_key),
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- The picker reads the caller's addresses newest-used first.
CREATE INDEX idx_user_addresses_user_recent ON user_addresses (user_id, last_used_at DESC);
