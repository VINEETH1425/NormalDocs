-- ==============================================================================
-- StoreConnect Omnichannel Retail Platform – PostgreSQL Setup Script
-- Tool: pgAdmin (Query Tool)
-- Database: PostgreSQL 14+ / 15+ / 16+
-- Instructions:
--   1. In pgAdmin, create a database named 'storeconnect' (or use your default db).
--   2. Open the 'Query Tool' on that database.
--   3. Paste this entire script and press Execute (F5 / Play button).
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- STEP 1: DROP TABLES (Clean Slate)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS store_inventory CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS stores CASCADE;

-- ------------------------------------------------------------------------------
-- STEP 2: CREATE RELATIONAL TABLES
-- ------------------------------------------------------------------------------

-- 1. Stores Table (Master Entity)
CREATE TABLE stores (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Categories Table (Master Entity)
CREATE TABLE categories (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 3. Products Table (Many-to-1 with Categories: Many Products belong to 1 Category)
CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Cardinality: Many-to-1 (N:1) -> Many products belong to 1 category
    CONSTRAINT fk_products_category 
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 4. Store Inventory Table
-- Cardinality: Many-to-Many (M:N) Bridge Table between Stores and Products!
-- (One Store carries Many Products; One Product is stocked at Many Stores)
CREATE TABLE store_inventory (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0 CHECK (quantity_available >= 0),
    quantity_reserved INT NOT NULL DEFAULT 0 CHECK (quantity_reserved >= 0),
    reorder_threshold INT DEFAULT 5,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_store_product UNIQUE (store_id, product_id),
    -- Foreign Key to Stores: Many Inventory rows belong to 1 Store (N:1)
    CONSTRAINT fk_inventory_store 
        FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    -- Foreign Key to Products: Many Inventory rows belong to 1 Product (N:1)
    CONSTRAINT fk_inventory_product 
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 5. Customers Table (Master Entity)
CREATE TABLE customers (
    id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Orders Table (Transactional Aggregate Root)
-- Cardinalities:
--   - Many-to-1 (N:1) with Customers: Many Orders placed by 1 Customer
--   - Many-to-1 (N:1) with Stores: Many Orders fulfilled by 1 Store
CREATE TABLE orders (
    id VARCHAR(50) PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    fulfillment_type VARCHAR(30) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (total_amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Foreign Key to Customers (N:1)
    CONSTRAINT fk_orders_customer 
        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    -- Foreign Key to Stores (N:1)
    CONSTRAINT fk_orders_store 
        FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE RESTRICT
);

-- 7. Order Items Table
-- Cardinality: Many-to-Many (M:N) Bridge Table between Orders and Products!
-- (One Order contains Many Products; One Product can appear in Many Orders)
CREATE TABLE order_items (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
    -- Foreign Key to Orders: Many items belong to 1 Order (N:1)
    CONSTRAINT fk_order_items_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    -- Foreign Key to Products: Many items reference 1 Product (N:1)
    CONSTRAINT fk_order_items_product 
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- ------------------------------------------------------------------------------
-- STEP 3: CREATE PERFORMANCE INDEXES
-- ------------------------------------------------------------------------------
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_inventory_store_product ON store_inventory(store_id, product_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_store ON orders(store_id);
CREATE INDEX idx_orders_status ON orders(status);

-- ------------------------------------------------------------------------------
-- STEP 4: INSERT SEED DATA
-- ------------------------------------------------------------------------------

-- 1. Insert Categories (4)
INSERT INTO categories (id, name, description) VALUES
('CAT-AUDIO', 'Audio & Sound', 'Noise-cancelling headphones, wireless earphones, and portable speakers'),
('CAT-WEAR',  'Wearables & Smart Tech', 'Smartwatches, fitness bands, and wearable health trackers'),
('CAT-COMP',  'Computing & Accessories', 'High-productivity monitors, mechanical keyboards, and precision mice'),
('CAT-LIFE',  'Lifestyle & Essentials', 'Performance athletic wear, ergonomic backpacks, and specialty coffee');

-- 2. Insert Stores (5 Locations in Delhi-NCR)
INSERT INTO stores (id, name, code, address, city, phone, is_active) VALUES
('STORE-GGN-01', 'StoreConnect CyberHub',      'STR-CH-01',  'DLF CyberHub, DLF Phase 2, Sector 24',   'Gurugram',    '+91 124 4567890', TRUE),
('STORE-GGN-02', 'StoreConnect Ambience',      'STR-AMB-02', 'Ambience Mall, NH-8, Ambience Island',   'Gurugram',    '+91 124 4987654', TRUE),
('STORE-DEL-01', 'StoreConnect CP Flagship',   'STR-CP-03',  'Inner Circle, Block B, Connaught Place', 'New Delhi',   '+91 11 23456789', TRUE),
('STORE-DEL-02', 'StoreConnect Select Citywalk','STR-SCW-04', 'Select Citywalk, A-3 District Centre, Saket', 'New Delhi', '+91 11 41234567', TRUE),
('STORE-NOI-01', 'StoreConnect Mall of India', 'STR-MOI-05', 'DLF Mall of India, Sector 18',           'Noida',       '+91 120 4567891', TRUE);

-- 3. Insert Products (12 Products)
INSERT INTO products (id, sku, name, description, category_id, price, is_active) VALUES
-- Audio & Sound
('PROD-001', 'SKU-SNY-WH1000', 'Sony WH-1000XM5 Headphones',     'Premium industry-leading wireless noise-cancelling over-ear headphones', 'CAT-AUDIO', 29999.00, TRUE),
('PROD-002', 'SKU-BSE-QC45',   'Bose QuietComfort 45',            'Iconic quiet comfort wireless headphones with world-class noise cancellation', 'CAT-AUDIO', 26900.00, TRUE),
('PROD-003', 'SKU-APL-APP2',   'Apple AirPods Pro (2nd Gen)',     'Active noise cancellation with MagSafe charging case (USB-C)', 'CAT-AUDIO', 24900.00, TRUE),

-- Wearables & Smart Tech
('PROD-004', 'SKU-APL-WCH9',   'Apple Watch Series 9 GPS 45mm',   'Smartwatch with advanced S9 SiP, double tap gesture, and health sensors', 'CAT-WEAR',  41900.00, TRUE),
('PROD-005', 'SKU-SAM-GW6',    'Samsung Galaxy Watch 6 LTE 44mm', 'Comprehensive wellness tracking with sleep coaching and sapphire crystal', 'CAT-WEAR',  29999.00, TRUE),
('PROD-006', 'SKU-FIT-CHG6',   'Fitbit Charge 6 Fitness Tracker', 'Advanced health & fitness tracker with Google apps and heart rate tracking', 'CAT-WEAR',  14999.00, TRUE),

-- Computing & Accessories
('PROD-007', 'SKU-LOG-MX3S',   'Logitech MX Master 3S Mouse',     'Performance wireless mouse with 8K DPI sensor and quiet click switches', 'CAT-COMP',  8995.00,  TRUE),
('PROD-008', 'SKU-KEY-K2V2',   'Keychron K2 Wireless Keyboard',   '75% compact mechanical Bluetooth keyboard with Gateron Brown switches', 'CAT-COMP',  7499.00,  TRUE),
('PROD-009', 'SKU-DEL-U2723',  'Dell UltraSharp 27 4K Monitor',   '27-inch 4K UHD USB-C Hub monitor with IPS Black technology (U2723QE)', 'CAT-COMP', 48500.00, TRUE),

-- Lifestyle & Essentials
('PROD-010', 'SKU-NIK-DRYFIT', 'Nike Dri-FIT Performance Tee',    'Breathable moisture-wicking athletic training t-shirt', 'CAT-LIFE',  1995.00,  TRUE),
('PROD-011', 'SKU-WLD-PRO28',  'Wildcraft Ergonomic Backpack 28L','Weather-resistant multi-compartment padded laptop backpack', 'CAT-LIFE',  2499.00,  TRUE),
('PROD-012', 'SKU-BTK-COFFEE', 'Blue Tokai Specialty Coffee 250g','Medium-dark roast Attikan Estate 100% Arabica whole beans', 'CAT-LIFE',   550.00,  TRUE);

-- 4. Insert Store Inventory (60 Rows: 5 Stores x 12 Products)
INSERT INTO store_inventory (id, store_id, product_id, quantity_available, quantity_reserved, reorder_threshold) VALUES
-- STORE 1: CyberHub (Gurugram)
('INV-01-01', 'STORE-GGN-01', 'PROD-001', 12, 1, 3),
('INV-01-02', 'STORE-GGN-01', 'PROD-002', 8,  0, 2),
('INV-01-03', 'STORE-GGN-01', 'PROD-003', 15, 2, 4),
('INV-01-04', 'STORE-GGN-01', 'PROD-004', 7,  1, 2),
('INV-01-05', 'STORE-GGN-01', 'PROD-005', 9,  0, 2),
('INV-01-06', 'STORE-GGN-01', 'PROD-006', 14, 0, 3),
('INV-01-07', 'STORE-GGN-01', 'PROD-007', 20, 1, 5),
('INV-01-08', 'STORE-GGN-01', 'PROD-008', 11, 0, 3),
('INV-01-09', 'STORE-GGN-01', 'PROD-009', 4,  0, 1),
('INV-01-10', 'STORE-GGN-01', 'PROD-010', 25, 0, 5),
('INV-01-11', 'STORE-GGN-01', 'PROD-011', 18, 0, 4),
('INV-01-12', 'STORE-GGN-01', 'PROD-012', 45, 3, 10),

-- STORE 2: Ambience Mall (Gurugram) - PROD-001 & PROD-009 are OUT OF STOCK
('INV-02-01', 'STORE-GGN-02', 'PROD-001', 0,  0, 3), -- OUT OF STOCK
('INV-02-02', 'STORE-GGN-02', 'PROD-002', 5,  0, 2),
('INV-02-03', 'STORE-GGN-02', 'PROD-003', 10, 1, 3),
('INV-02-04', 'STORE-GGN-02', 'PROD-004', 3,  0, 2),
('INV-02-05', 'STORE-GGN-02', 'PROD-005', 6,  0, 2),
('INV-02-06', 'STORE-GGN-02', 'PROD-006', 8,  0, 2),
('INV-02-07', 'STORE-GGN-02', 'PROD-007', 12, 0, 3),
('INV-02-08', 'STORE-GGN-02', 'PROD-008', 7,  0, 2),
('INV-02-09', 'STORE-GGN-02', 'PROD-009', 0,  0, 1), -- OUT OF STOCK
('INV-02-10', 'STORE-GGN-02', 'PROD-010', 30, 0, 5),
('INV-02-11', 'STORE-GGN-02', 'PROD-011', 15, 0, 3),
('INV-02-12', 'STORE-GGN-02', 'PROD-012', 20, 0, 5),

-- STORE 3: Connaught Place Flagship (Central Delhi)
('INV-03-01', 'STORE-DEL-01', 'PROD-001', 18, 0, 4),
('INV-03-02', 'STORE-DEL-01', 'PROD-002', 12, 1, 3),
('INV-03-03', 'STORE-DEL-01', 'PROD-003', 22, 0, 5),
('INV-03-04', 'STORE-DEL-01', 'PROD-004', 10, 0, 2),
('INV-03-05', 'STORE-DEL-01', 'PROD-005', 11, 0, 3),
('INV-03-06', 'STORE-DEL-01', 'PROD-006', 16, 0, 3),
('INV-03-07', 'STORE-DEL-01', 'PROD-007', 25, 0, 5),
('INV-03-08', 'STORE-DEL-01', 'PROD-008', 14, 1, 3),
('INV-03-09', 'STORE-DEL-01', 'PROD-009', 6,  0, 2),
('INV-03-10', 'STORE-DEL-01', 'PROD-010', 40, 0, 8),
('INV-03-11', 'STORE-DEL-01', 'PROD-011', 22, 0, 5),
('INV-03-12', 'STORE-DEL-01', 'PROD-012', 50, 0, 10),

-- STORE 4: Select Citywalk (Saket, South Delhi)
('INV-04-01', 'STORE-DEL-02', 'PROD-001', 6,  0, 2),
('INV-04-02', 'STORE-DEL-02', 'PROD-002', 4,  0, 2),
('INV-04-03', 'STORE-DEL-02', 'PROD-003', 14, 0, 4),
('INV-04-04', 'STORE-DEL-02', 'PROD-004', 8,  0, 2),
('INV-04-05', 'STORE-DEL-02', 'PROD-005', 5,  0, 2),
('INV-04-06', 'STORE-DEL-02', 'PROD-006', 10, 0, 3),
('INV-04-07', 'STORE-DEL-02', 'PROD-007', 9,  0, 2),
('INV-04-08', 'STORE-DEL-02', 'PROD-008', 5,  0, 2),
('INV-04-09', 'STORE-DEL-02', 'PROD-009', 2,  0, 1),
('INV-04-10', 'STORE-DEL-02', 'PROD-010', 50, 2, 10),
('INV-04-11', 'STORE-DEL-02', 'PROD-011', 35, 1, 5),
('INV-04-12', 'STORE-DEL-02', 'PROD-012', 60, 0, 10),

-- STORE 5: Mall of India (Sector 18, Noida)
('INV-05-01', 'STORE-NOI-01', 'PROD-001', 3,  0, 2),
('INV-05-02', 'STORE-NOI-01', 'PROD-002', 0,  0, 2), -- OUT OF STOCK
('INV-05-03', 'STORE-NOI-01', 'PROD-003', 8,  0, 2),
('INV-05-04', 'STORE-NOI-01', 'PROD-004', 4,  0, 2),
('INV-05-05', 'STORE-NOI-01', 'PROD-005', 3,  0, 2),
('INV-05-06', 'STORE-NOI-01', 'PROD-006', 7,  0, 2),
('INV-05-07', 'STORE-NOI-01', 'PROD-007', 15, 0, 3),
('INV-05-08', 'STORE-NOI-01', 'PROD-008', 6,  0, 2),
('INV-05-09', 'STORE-NOI-01', 'PROD-009', 1,  0, 1), -- LOW STOCK ALERT (1 unit)
('INV-05-10', 'STORE-NOI-01', 'PROD-010', 28, 0, 5),
('INV-05-11', 'STORE-NOI-01', 'PROD-011', 19, 0, 4),
('INV-05-12', 'STORE-NOI-01', 'PROD-012', 30, 0, 5);

-- 5. Insert Customers (5 Customers)
INSERT INTO customers (id, first_name, last_name, email, phone, address) VALUES
('CUST-001', 'Aarav',  'Sharma',    'aarav.sharma@example.com',    '+91 9876543210', 'Tower 4, DLF The Camellias, Golf Course Road, Gurugram'),
('CUST-002', 'Priya',  'Verma',     'priya.verma@example.com',     '+91 9811223344', 'Flat 302, Barakhamba Road, Connaught Place, New Delhi'),
('CUST-003', 'Rohan',  'Mehta',     'rohan.mehta@example.com',     '+91 9920334455', 'Sector 15A, Near City Park, Noida'),
('CUST-004', 'Ananya', 'Iyer',      'ananya.iyer@example.com',     '+91 9840112233', 'D-Block, Saket, South Delhi'),
('CUST-005', 'Vikram', 'Malhotra',  'vikram.malhotra@example.com', '+91 9822998877', 'Phase 5, Udyog Vihar, Gurugram');

-- 6. Insert Sample Historic Orders (4 Orders across Lifecycle States)
INSERT INTO orders (id, order_number, customer_id, store_id, status, fulfillment_type, total_amount, created_at, updated_at) VALUES
('ORD-1001', 'ORD-20261001-1001', 'CUST-001', 'STORE-GGN-01', 'COMPLETED',        'STORE_PICKUP',  31099.00, '2026-10-01 10:30:00', '2026-10-01 16:45:00'),
('ORD-1002', 'ORD-20261002-1002', 'CUST-002', 'STORE-DEL-01', 'READY_FOR_PICKUP', 'STORE_PICKUP',  41900.00, '2026-10-02 11:15:00', '2026-10-02 14:20:00'),
('ORD-1003', 'ORD-20261003-1003', 'CUST-003', 'STORE-NOI-01', 'PROCESSING',       'STORE_PICKUP',  16494.00, '2026-10-03 09:00:00', '2026-10-03 10:30:00'),
('ORD-1004', 'ORD-20261004-1004', 'CUST-004', 'STORE-DEL-02', 'CONFIRMED',        'HOME_DELIVERY', 26895.00, '2026-10-04 14:10:00', '2026-10-04 14:12:00');

-- 7. Insert Order Line Items (7 Items)
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, subtotal) VALUES
-- ORD-1001 Items (Sony Headphones + 2 Blue Tokai Coffee)
('ITEM-101', 'ORD-1001', 'PROD-001', 1, 29999.00, 29999.00),
('ITEM-102', 'ORD-1001', 'PROD-012', 2, 550.00,   1100.00),

-- ORD-1002 Items (Apple Watch Series 9)
('ITEM-103', 'ORD-1002', 'PROD-004', 1, 41900.00, 41900.00),

-- ORD-1003 Items (Logitech Mouse + Keychron Keyboard)
('ITEM-104', 'ORD-1003', 'PROD-007', 1, 8995.00,  8995.00),
('ITEM-105', 'ORD-1003', 'PROD-008', 1, 7499.00,  7499.00),

-- ORD-1004 Items (AirPods Pro 2 + Nike Tee)
('ITEM-106', 'ORD-1004', 'PROD-003', 1, 24900.00, 24900.00),
('ITEM-107', 'ORD-1004', 'PROD-010', 1, 1995.00,  1995.00);

-- ------------------------------------------------------------------------------
-- STEP 5: VERIFICATION QUERY (Run to verify counts)
-- ------------------------------------------------------------------------------
SELECT 'stores' AS table_name, COUNT(*) AS row_count FROM stores
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL
SELECT 'store_inventory', COUNT(*) FROM store_inventory
UNION ALL
SELECT 'customers', COUNT(*) FROM customers
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM order_items;

-- ------------------------------------------------------------------------------
-- STEP 6: VERIFY ALL FOREIGN KEY RELATIONSHIPS (Run to verify in pgAdmin)
-- ------------------------------------------------------------------------------
SELECT
    tc.table_name AS child_table,
    kcu.column_name AS foreign_key_column,
    tc.constraint_name,
    ccu.table_name AS parent_table,
    ccu.column_name AS primary_key_column
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name, kcu.column_name;

