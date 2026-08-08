-- Add nullable amount_owing column to customers for simplified outstanding amount
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS amount_owing numeric(19,2) DEFAULT 0;
