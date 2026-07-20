-- Add image URL and barcode fields to products
ALTER TABLE products ADD COLUMN image_url VARCHAR(500);
ALTER TABLE products ADD COLUMN barcode VARCHAR(100);
