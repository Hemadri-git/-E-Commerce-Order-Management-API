-- Seed data: Categories and Products will be inserted on first run if not exists
-- Admin user is created programmatically via DataInitializer
-- This file can be used to populate additional test data

-- Categories (Only if empty)
INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Electronics', 'Gadgets, phones, laptops and accessories'),
(2, 'Clothing', 'Men, women and kids fashion'),
(3, 'Books', 'Fiction, non-fiction, academic and more'),
(4, 'Home & Kitchen', 'Appliances, furniture and decor'),
(5, 'Sports', 'Equipment, clothing and accessories');
