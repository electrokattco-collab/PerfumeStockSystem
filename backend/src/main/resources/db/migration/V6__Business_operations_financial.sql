-- V6: Business Operations, Financial Planning & OCR Support

-- Suppliers table
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(200),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Stock movements (audit trail for inventory changes)
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    movement_type VARCHAR(20) NOT NULL, -- PURCHASE, SALE, ADJUSTMENT, RETURN, DAMAGE
    quantity INTEGER NOT NULL,
    unit_cost NUMERIC(10,2),
    reference_id BIGINT,
    reference_type VARCHAR(30),
    notes TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Business transactions (income tracking)
CREATE TABLE business_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(30) NOT NULL, -- STIPEND, CASH_INJECTED, MONEY_COLLECTED, OTHER_INCOME, EXPENSE
    category VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    amount NUMERIC(10,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reference_id BIGINT,
    reference_type VARCHAR(30),
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Purchase receipts (OCR scanned receipts)
CREATE TABLE purchase_receipts (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50),
    supplier_name VARCHAR(100),
    supplier_id BIGINT REFERENCES suppliers(id),
    total_amount NUMERIC(10,2),
    tax_amount NUMERIC(10,2) DEFAULT 0,
    subtotal NUMERIC(10,2),
    receipt_date TIMESTAMP,
    image_url VARCHAR(500),
    ocr_raw_text TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSED, REJECTED
    processed_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Purchase receipt items
CREATE TABLE purchase_receipt_items (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL REFERENCES purchase_receipts(id) ON DELETE CASCADE,
    product_name VARCHAR(100) NOT NULL,
    product_id BIGINT REFERENCES products(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_cost NUMERIC(10,2),
    total_cost NUMERIC(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Payment history for customers
CREATE TABLE payment_history (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    sale_id BIGINT REFERENCES sales(id),
    amount NUMERIC(10,2) NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- FULL, PARTIAL, OVERPAYMENT
    payment_method VARCHAR(20) DEFAULT 'CASH', -- CASH, CARD, EFT, OTHER
    notes TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Supplier fields on products
ALTER TABLE products ADD COLUMN supplier VARCHAR(100);
ALTER TABLE products ADD COLUMN supplier_id BIGINT REFERENCES suppliers(id);
ALTER TABLE products ADD COLUMN purchase_date TIMESTAMP;
ALTER TABLE products ADD COLUMN quantity_purchased INTEGER DEFAULT 0;
ALTER TABLE products ADD COLUMN quantity_remaining INTEGER DEFAULT 0;

-- Address and notes on customers
ALTER TABLE customers ADD COLUMN address VARCHAR(200);
ALTER TABLE customers ADD COLUMN notes TEXT;

-- Indexes
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type);
CREATE INDEX idx_stock_movements_date ON stock_movements(created_at);
CREATE INDEX idx_business_transactions_type ON business_transactions(transaction_type);
CREATE INDEX idx_business_transactions_date ON business_transactions(transaction_date);
CREATE INDEX idx_purchase_receipts_status ON purchase_receipts(status);
CREATE INDEX idx_purchase_receipt_items_receipt ON purchase_receipt_items(receipt_id);
CREATE INDEX idx_payment_history_customer ON payment_history(customer_id);
CREATE INDEX idx_suppliers_name ON suppliers(name);
