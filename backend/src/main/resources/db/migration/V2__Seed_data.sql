-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
    'admin',
    'admin@perfumestock.com',
    '$2a$10$2QrAVzIk0bIQ7WEHznEqF.D2.Oc7ejF0aIfkKFSQWNcdSz/DgLaJ.',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert sample manager user (password: manager123)
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
    'manager',
    'manager@perfumestock.com',
    '$2a$10$b6YTdX1HfJGV3Dl0RO3gVenhDKnul4SZ2OlGqU/kzMPzqjRfegzvq',
    'MANAGER',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert sample sales rep (password: sales123)
INSERT INTO users (username, email, password, role, active, created_at)
VALUES (
    'sales',
    'sales@perfumestock.com',
    '$2a$10$ceKRFbOAUFYvP8symEvbdeHBtt9gbX4wbACvYUOGk8KeaQzfYDhHC',
    'SALES_REP',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Insert sample products with buy and sell prices
INSERT INTO products (product_id, name, category, size, buy_price, sell_price, stock_quantity, low_stock_threshold, created_at)
VALUES
    ('PP001', '50mL Superior Perfume [Eau de Parfum]', 'Perfume', '50mL', 85.00, 199.00, 10, 5, CURRENT_TIMESTAMP),
    ('PP002', '30mL Superior Perfume [Eau de Parfum]', 'Perfume', '30mL', 60.00, 149.00, 15, 5, CURRENT_TIMESTAMP),
    ('PP003', '50mL Essentials Perfume [Eau de Toilette]', 'Perfume', '50mL', 50.00, 129.00, 8, 5, CURRENT_TIMESTAMP),
    ('BC001', 'Luxury Body Lotion', 'Body Care', '250mL', 35.00, 89.00, 20, 5, CURRENT_TIMESTAMP),
    ('BC002', 'Body Spray Collection', 'Body Care', '150mL', 28.00, 69.00, 12, 5, CURRENT_TIMESTAMP),
    ('RO001', 'Roll-On Deodorant Fresh', 'Roll On', '50mL', 12.00, 35.00, 25, 10, CURRENT_TIMESTAMP),
    ('RO002', 'Roll-On Deodorant Sport', 'Roll On', '50mL', 12.00, 35.00, 30, 10, CURRENT_TIMESTAMP);
