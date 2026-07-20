-- Performance indexes for commonly queried columns

-- Products
CREATE INDEX IF NOT EXISTS idx_products_name ON products (name);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);
CREATE INDEX IF NOT EXISTS idx_products_product_id ON products (product_id);
CREATE INDEX IF NOT EXISTS idx_products_stock ON products (stock_quantity, low_stock_threshold);

-- Sales
CREATE INDEX IF NOT EXISTS idx_sales_created_at ON sales (created_at);
CREATE INDEX IF NOT EXISTS idx_sales_product_name ON sales (product_name);
CREATE INDEX IF NOT EXISTS idx_sales_customer_name ON sales (customer_name);
CREATE INDEX IF NOT EXISTS idx_sales_paid ON sales (paid);
CREATE INDEX IF NOT EXISTS idx_sales_sale_id ON sales (sale_id);

-- Sale Items
CREATE INDEX IF NOT EXISTS idx_sale_items_sale_id ON sale_items (sale_id);

-- Customers
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers (name);

-- Expenses
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses (category);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses (expense_date);

-- Users
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users (active);
