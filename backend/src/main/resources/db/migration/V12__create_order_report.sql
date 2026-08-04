-- Denormalized read model for reporting. One row per order line, so revenue and
-- item popularity are a GROUP BY rather than a join across orders and order_items.
--
-- Not a second source of truth: it is derived from OrderPlacedEvent and can be
-- rebuilt from the orders tables if it is ever lost. Written by an async listener,
-- never inside the order transaction — a slow or failing report must not reject a
-- paying customer.

CREATE TABLE order_report
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    user_id      BIGINT         NOT NULL,
    -- No FK to menu_items: an admin can hard-delete an item, and this row records
    -- what was sold at the time regardless. Nullable for the same reason.
    menu_item_id BIGINT,
    item_name    VARCHAR(150)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    line_total   DECIMAL(10, 2) NOT NULL,
    placed_at    DATETIME(6)    NOT NULL,
    -- The listener retries on failure, so a replay must not double-count a line.
    CONSTRAINT uk_order_report_line UNIQUE (order_id, item_name)
) ENGINE = InnoDB;

-- Reports filter by date first and group by item second.
CREATE INDEX idx_order_report_placed_at ON order_report (placed_at);
CREATE INDEX idx_order_report_menu_item ON order_report (menu_item_id);
