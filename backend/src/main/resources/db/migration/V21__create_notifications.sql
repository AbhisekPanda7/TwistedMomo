CREATE TABLE notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    type       VARCHAR(30) NOT NULL,
    title      VARCHAR(120) NOT NULL,
    body       VARCHAR(500) NOT NULL,
    order_id   BIGINT NULL,
    read_at    TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    -- A deleted order must not delete the customer's history; the row stops being clickable.
    CONSTRAINT fk_notifications_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read_at);

-- The outbox retries an incomplete publication, so the same event can arrive twice.
-- Coupon rows carry a null order_id, and MySQL treats nulls as distinct here.
CREATE UNIQUE INDEX uk_notifications_order_title ON notifications (order_id, title);
