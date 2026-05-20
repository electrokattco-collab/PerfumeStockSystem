-- Insert default admin user (password: admin123)
-- BCrypt hash for 'admin123' (strength 10)
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
    'admin',
    'admin@perfumestock.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQzL87mJgPTCPqRmmPAVKGfL1VvG',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert sample manager user (password: manager123)
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
    'manager',
    'manager@perfumestock.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQzL87mJgPTCPqRmmPAVKGfL1VvG',
    'MANAGER',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert sample sales rep (password: sales123)
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
    'sales',
    'sales@perfumestock.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQzL87mJgPTCPqRmmPAVKGfL1VvG',
    'SALES_REP',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert sample products from original CSV data
INSERT INTO products (product_id, name, category, size, retail_price, rewards_price, gold_price, vip_price, stock_quantity, low_stock_threshold, created_at)
VALUES 
    ('PP001', '50mL Superior Perfume [Eau de Parfum]', 'Perfume', '50mL', 199.00, 109.00, 98.00, 95.00, 10, 5, CURRENT_TIMESTAMP),
    ('PP002', '30mL Superior Perfume [Eau de Parfum]', 'Perfume', '30mL', 149.00, 89.00, 79.00, 75.00, 15, 5, CURRENT_TIMESTAMP),
    ('PP003', '50mL Essentials Perfume [Eau de Toilette]', 'Perfume', '50mL', 129.00, 79.00, 69.00, 65.00, 8, 5, CURRENT_TIMESTAMP),
    ('BC001', 'Luxury Body Lotion', 'Body Care', '250mL', 89.00, 59.00, 49.00, 45.00, 20, 5, CURRENT_TIMESTAMP),
    ('BC002', 'Body Spray Collection', 'Body Care', '150mL', 69.00, 45.00, 39.00, 35.00, 12, 5, CURRENT_TIMESTAMP),
    ('RO001', 'Roll-On Deodorant Fresh', 'Roll On', '50mL', 35.00, 25.00, 22.00, 20.00, 25, 10, CURRENT_TIMESTAMP),
    ('RO002', 'Roll-On Deodorant Sport', 'Roll On', '50mL', 35.00, 25.00, 22.00, 20.00, 30, 10, CURRENT_TIMESTAMP);
