-- Delivery area, for "where do we deliver most?". Street lines, recipient name
-- and phone stay out: personal data with no analytical value.
ALTER TABLE order_report ADD COLUMN city VARCHAR(100);
ALTER TABLE order_report ADD COLUMN postal_code VARCHAR(20);

-- Nullable because rows written before this migration have neither.
CREATE INDEX idx_order_report_postal_code ON order_report (postal_code);
