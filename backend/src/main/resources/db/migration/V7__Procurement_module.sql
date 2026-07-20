-- V7: Procurement & OCR Foundation

CREATE TABLE procurements (
    id BIGSERIAL PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    supplier_contact VARCHAR(100),
    invoice_number VARCHAR(50),
    purchase_date TIMESTAMP NOT NULL,
    invoice_file_path VARCHAR(500),
    invoice_type VARCHAR(10), -- PDF, IMAGE
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    vat_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    uploaded_by VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, OCR_PENDING, REVIEWING, CONFIRMED, CANCELLED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE procurement_items (
    id BIGSERIAL PRIMARY KEY,
    procurement_id BIGINT NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    product_name VARCHAR(100) NOT NULL,
    brand VARCHAR(50),
    category VARCHAR(50),
    quantity_purchased INTEGER NOT NULL CHECK (quantity_purchased > 0),
    buy_price NUMERIC(10,2) NOT NULL CHECK (buy_price > 0),
    suggested_selling_price NUMERIC(10,2),
    expected_profit NUMERIC(10,2),
    barcode VARCHAR(100),
    expiry_date TIMESTAMP,
    batch_number VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_procurements_purchase_date ON procurements(purchase_date);
CREATE INDEX idx_procurements_supplier_name ON procurements(supplier_name);
CREATE INDEX idx_procurements_invoice_number ON procurements(invoice_number);
CREATE INDEX idx_procurements_status ON procurements(status);
CREATE INDEX idx_procurement_items_procurement ON procurement_items(procurement_id);
