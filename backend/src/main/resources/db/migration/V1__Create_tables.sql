-- ArthurFord Gold Agent Business Manager
-- Clean schema for single-owner business

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_combo BOOLEAN NOT NULL DEFAULT FALSE,
    buy_price DECIMAL(10,2) NOT NULL,
    sell_price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 5,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_bundles (
    id BIGSERIAL PRIMARY KEY,
    combo_product_id BIGINT NOT NULL REFERENCES products(id),
    component_product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    UNIQUE(combo_product_id, component_product_id)
);

CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    purchase_date TIMESTAMP NOT NULL DEFAULT NOW(),
    source_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT NOT NULL REFERENCES purchases(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    is_combo_item BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    notes TEXT,
    outstanding_balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    sale_date TIMESTAMP NOT NULL DEFAULT NOW(),
    total_amount DECIMAL(12,2) NOT NULL,
    cost_of_goods_sold DECIMAL(12,2) NOT NULL DEFAULT 0,
    profit DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_type VARCHAR(20) NOT NULL,
    amount_paid DECIMAL(12,2) NOT NULL DEFAULT 0,
    amount_owing DECIMAL(12,2) NOT NULL DEFAULT 0,
    customer_id BIGINT REFERENCES customers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL REFERENCES sales(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    sale_id BIGINT REFERENCES sales(id),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    movement_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(30),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);


CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_purchases_date ON purchases(purchase_date);
CREATE INDEX idx_sales_date ON sales(sale_date);
CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sales_payment_type ON sales(payment_type);
CREATE INDEX idx_sale_items_product ON sale_items(product_id);
CREATE INDEX idx_payments_customer ON payments(customer_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type);
