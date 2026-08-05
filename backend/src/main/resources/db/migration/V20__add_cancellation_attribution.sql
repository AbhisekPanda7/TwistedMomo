-- Both nullable: existing CANCELLED rows honestly record that we do not know who cancelled.
ALTER TABLE orders
    ADD COLUMN cancelled_by VARCHAR(20) NULL,
    ADD COLUMN cancellation_reason VARCHAR(255) NULL;
