-- Align schema with immutable ledger model

ALTER TABLE customers
    DROP COLUMN IF EXISTS outstanding_balance;

CREATE UNIQUE INDEX IF NOT EXISTS uk_business_events_reference
    ON business_events(event_type, reference_type, reference_id);

CREATE INDEX IF NOT EXISTS idx_business_events_customer_id
    ON business_events(customer_id);

CREATE INDEX IF NOT EXISTS idx_business_events_created_at
    ON business_events(created_at);

CREATE INDEX IF NOT EXISTS idx_business_events_reference
    ON business_events(reference_type, reference_id);
