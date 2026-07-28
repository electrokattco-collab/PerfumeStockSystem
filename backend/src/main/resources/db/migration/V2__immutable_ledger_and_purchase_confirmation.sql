-- Immutable business ledger and procurement confirmation workflow

ALTER TABLE purchases
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW';

ALTER TABLE purchases
    ADD COLUMN receipt_reference VARCHAR(255);

ALTER TABLE purchases
    ADD COLUMN ocr_text TEXT;

ALTER TABLE purchases
    ADD COLUMN ocr_confidence DECIMAL(5,2);

ALTER TABLE purchases
    ADD COLUMN confirmed_at TIMESTAMP;

ALTER TABLE purchases
    ADD COLUMN confirmed_by VARCHAR(100);

ALTER TABLE stock_movements
    ADD COLUMN event_id BIGINT;

CREATE TABLE business_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    reference_type VARCHAR(40) NOT NULL,
    reference_id BIGINT NOT NULL,
    customer_id BIGINT REFERENCES customers(id),
    product_id BIGINT REFERENCES products(id),
    amount DECIMAL(12,2) DEFAULT 0,
    quantity INTEGER DEFAULT 0,
    notes TEXT,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_business_events_type ON business_events(event_type);
CREATE INDEX idx_business_events_created_at ON business_events(created_at);
CREATE INDEX idx_business_events_reference ON business_events(reference_type, reference_id);
