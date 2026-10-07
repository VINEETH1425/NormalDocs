-- ==============================================================================
-- StoreConnect Omnichannel Retail Platform – Microsoft SQL Server (SSMS) Setup
-- Tool: SQL Server Management Studio (SSMS) / Azure Data Studio
-- Database: Microsoft SQL Server 2016 / 2019 / 2022+ / Azure SQL
-- Purpose:
--   Creates all 9 relational tables with explicit Primary Keys (PK) and 
--   Foreign Keys (FK) so that SSMS "Database Diagrams" automatically renders
--   the traditional relational schema diagram with key links and 1:N lines.
--
-- How to generate diagram in SSMS:
--   1. Run this entire script in SSMS Query Window (press F5).
--   2. In Object Explorer, expand 'storeconnect' database.
--   3. Right-click 'Database Diagrams' -> Click 'New Database Diagram'.
--   4. Select all 9 tables and click 'Add'.
--   5. SSMS will automatically lay out the visual relational ER diagram!
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- STEP 1: CLEAN SLATE (DROP EXISTING TABLES IN REVERSE DEPENDENCY ORDER)
-- ------------------------------------------------------------------------------
IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID('dbo.carts', 'U') IS NOT NULL DROP TABLE dbo.carts;
IF OBJECT_ID('dbo.order_items', 'U') IS NOT NULL DROP TABLE dbo.order_items;
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL DROP TABLE dbo.customers;
IF OBJECT_ID('dbo.store_inventory', 'U') IS NOT NULL DROP TABLE dbo.store_inventory;
IF OBJECT_ID('dbo.products', 'U') IS NOT NULL DROP TABLE dbo.products;
IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;
IF OBJECT_ID('dbo.stores', 'U') IS NOT NULL DROP TABLE dbo.stores;
GO

-- ------------------------------------------------------------------------------
-- STEP 2: CREATE RELATIONAL TABLES WITH EXPLICIT PK / FK CONSTRAINTS
-- ------------------------------------------------------------------------------

-- 1. Stores Table (Master Physical Location)
CREATE TABLE dbo.stores (
    id VARCHAR(50) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    code VARCHAR(20) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    city NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_stores PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_stores_code UNIQUE (code)
);
GO

-- 2. Categories Table (Master Catalog Taxonomy)
CREATE TABLE dbo.categories (
    id VARCHAR(50) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    CONSTRAINT pk_categories PRIMARY KEY CLUSTERED (id)
);
GO

-- 3. Products Table (Master Global Catalog)
CREATE TABLE dbo.products (
    id VARCHAR(50) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX) NULL,
    category_id VARCHAR(50) NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_products PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT fk_products_categories 
        FOREIGN KEY (category_id) REFERENCES dbo.categories(id) ON DELETE SET NULL
);
GO

-- 4. Store Inventory Table (Store-Level Stock Bridge)
-- Many-to-Many Bridge Table linking Stores and Products
CREATE TABLE dbo.store_inventory (
    id VARCHAR(50) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    reorder_threshold INT NOT NULL DEFAULT 5,
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_store_inventory PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_store_product UNIQUE (store_id, product_id),
    CONSTRAINT chk_inventory_avail CHECK (quantity_available >= 0),
    CONSTRAINT chk_inventory_resv CHECK (quantity_reserved >= 0),
    CONSTRAINT fk_inventory_stores 
        FOREIGN KEY (store_id) REFERENCES dbo.stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_products 
        FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE
);
GO

-- 5. Customers Table (Master Registered Users)
CREATE TABLE dbo.customers (
    id VARCHAR(50) NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NULL,
    address NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_customers PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_customers_email UNIQUE (email)
);
GO

-- 6. Carts Table (Customer Active Shopping Cart)
-- 1-to-1 relationship with Customers; N-to-1 with Stores
CREATE TABLE dbo.carts (
    id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    store_id VARCHAR(50) NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_carts PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_carts_customer UNIQUE (customer_id),
    CONSTRAINT fk_carts_customers 
        FOREIGN KEY (customer_id) REFERENCES dbo.customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_carts_stores 
        FOREIGN KEY (store_id) REFERENCES dbo.stores(id) ON DELETE SET NULL
);
GO

-- 7. Cart Items Table (Active Line Items)
-- Many-to-Many Bridge Table linking Carts and Products
CREATE TABLE dbo.cart_items (
    id VARCHAR(50) NOT NULL,
    cart_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_cart_items PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_cart_items_product UNIQUE (cart_id, product_id),
    CONSTRAINT chk_cart_items_qty CHECK (quantity > 0),
    CONSTRAINT fk_cart_items_carts 
        FOREIGN KEY (cart_id) REFERENCES dbo.carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_products 
        FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE
);
GO

-- 8. Orders Table (Transactional Aggregate Root)
CREATE TABLE dbo.orders (
    id VARCHAR(50) NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    fulfillment_type VARCHAR(30) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_orders PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_orders_order_number UNIQUE (order_number),
    CONSTRAINT chk_orders_total CHECK (total_amount >= 0),
    CONSTRAINT fk_orders_customers 
        FOREIGN KEY (customer_id) REFERENCES dbo.customers(id),
    CONSTRAINT fk_orders_stores 
        FOREIGN KEY (store_id) REFERENCES dbo.stores(id)
);
GO

-- 9. Order Items Table (Snapshot Order Line Items)
CREATE TABLE dbo.order_items (
    id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY CLUSTERED (id),
    CONSTRAINT chk_order_items_qty CHECK (quantity > 0),
    CONSTRAINT chk_order_items_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_order_items_orders 
        FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_products 
        FOREIGN KEY (product_id) REFERENCES dbo.products(id)
);
GO

-- ------------------------------------------------------------------------------
-- STEP 3: CREATE PERFORMANCE INDEXES
-- ------------------------------------------------------------------------------
CREATE NONCLUSTERED INDEX idx_products_category ON dbo.products(category_id);
CREATE NONCLUSTERED INDEX idx_inventory_store_product ON dbo.store_inventory(store_id, product_id);
CREATE NONCLUSTERED INDEX idx_carts_customer ON dbo.carts(customer_id);
CREATE NONCLUSTERED INDEX idx_cart_items_cart ON dbo.cart_items(cart_id);
CREATE NONCLUSTERED INDEX idx_cart_items_product ON dbo.cart_items(product_id);
CREATE NONCLUSTERED INDEX idx_orders_customer ON dbo.orders(customer_id);
CREATE NONCLUSTERED INDEX idx_orders_store ON dbo.orders(store_id);
CREATE NONCLUSTERED INDEX idx_orders_status ON dbo.orders(status);
GO

-- ------------------------------------------------------------------------------
-- STEP 4: SEED DATA INSERTION
-- ------------------------------------------------------------------------------

-- Categories (4)
INSERT INTO dbo.categories (id, name, description) VALUES
('CAT-AUDIO', N'Audio & Sound', N'Noise-cancelling headphones, wireless earphones, and portable speakers'),
('CAT-WEAR',  N'Wearables & Smart Tech', N'Smartwatches, fitness bands, and wearable health trackers'),
('CAT-COMP',  N'Computing & Accessories', N'High-productivity monitors, mechanical keyboards, and precision mice'),
('CAT-LIFE',  N'Lifestyle & Essentials', N'Performance athletic wear, ergonomic backpacks, and specialty coffee');
GO

-- Stores (5)
INSERT INTO dbo.stores (id, name, code, address, city, phone, is_active) VALUES
('STORE-GGN-01', N'StoreConnect CyberHub',      'STR-CH-01',  N'DLF CyberHub, DLF Phase 2, Sector 24',   N'Gurugram',    '+91 124 4567890', 1),
('STORE-GGN-02', N'StoreConnect Ambience',      'STR-AMB-02', N'Ambience Mall, NH-8, Ambience Island',   N'Gurugram',    '+91 124 4987654', 1),
('STORE-DEL-01', N'StoreConnect CP Flagship',   'STR-CP-03',  N'Inner Circle, Block B, Connaught Place', N'New Delhi',   '+91 11 23456789', 1),
('STORE-DEL-02', N'StoreConnect Select Citywalk','STR-SCW-04', N'Select Citywalk, A-3 District Centre, Saket', N'New Delhi', '+91 11 41234567', 1),
('STORE-NOI-01', N'StoreConnect Mall of India', 'STR-MOI-05', N'DLF Mall of India, Sector 18',           N'Noida',       '+91 120 4567891', 1);
GO

-- Products (12)
INSERT INTO dbo.products (id, sku, name, description, category_id, price, is_active) VALUES
('PROD-001', 'SKU-SNY-WH1000', N'Sony WH-1000XM5 Headphones',     N'Premium wireless noise-cancelling headphones', 'CAT-AUDIO', 29999.00, 1),
('PROD-002', 'SKU-BSE-QC45',   N'Bose QuietComfort 45',            N'Iconic quiet comfort wireless headphones',      'CAT-AUDIO', 26900.00, 1),
('PROD-003', 'SKU-APL-APP2',   N'Apple AirPods Pro (2nd Gen)',     N'Active noise cancellation with MagSafe USB-C',  'CAT-AUDIO', 24900.00, 1),
('PROD-004', 'SKU-APL-WCH9',   N'Apple Watch Series 9 GPS 45mm',   N'Smartwatch with S9 SiP and health sensors',     'CAT-WEAR',  41900.00, 1),
('PROD-005', 'SKU-SAM-GW6',    N'Samsung Galaxy Watch 6 LTE 44mm', N'Wellness tracking and sleep coaching',          'CAT-WEAR',  29999.00, 1),
('PROD-006', 'SKU-FIT-CHG6',   N'Fitbit Charge 6 Fitness Tracker', N'Advanced fitness tracker with Google apps',      'CAT-WEAR',  14999.00, 1),
('PROD-007', 'SKU-LOG-MX3S',   N'Logitech MX Master 3S Mouse',     N'Wireless mouse with 8K DPI sensor',             'CAT-COMP',  8995.00,  1),
('PROD-008', 'SKU-KEY-K2V2',   N'Keychron K2 Wireless Keyboard',   N'75% compact mechanical Bluetooth keyboard',     'CAT-COMP',  7499.00,  1),
('PROD-009', 'SKU-DEL-U2723',  N'Dell UltraSharp 27 4K Monitor',   N'27-inch 4K UHD USB-C Hub monitor',              'CAT-COMP', 48500.00, 1),
('PROD-010', 'SKU-NIK-DRYFIT', N'Nike Dri-FIT Performance Tee',    N'Breathable moisture-wicking athletic tee',      'CAT-LIFE',  1995.00,  1),
('PROD-011', 'SKU-WLD-PRO28',  N'Wildcraft Ergonomic Backpack 28L',N'Multi-compartment padded laptop backpack',       'CAT-LIFE',  2499.00,  1),
('PROD-012', 'SKU-BTK-COFFEE', N'Blue Tokai Specialty Coffee 250g',N'Attikan Estate 100% Arabica medium roast beans', 'CAT-LIFE',   550.00,  1);
GO

-- Store Inventory (Sample rows)
INSERT INTO dbo.store_inventory (id, store_id, product_id, quantity_available, quantity_reserved, reorder_threshold) VALUES
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
('INV-01-12', 'STORE-GGN-01', 'PROD-012', 45, 3, 10);
GO

-- Customers (5)
INSERT INTO dbo.customers (id, first_name, last_name, email, phone, address) VALUES
('CUST-001', N'Aarav',  N'Sharma',    'aarav.sharma@example.com',    '+91 9876543210', N'Tower 4, DLF The Camellias, Golf Course Road, Gurugram'),
('CUST-002', N'Priya',  N'Verma',     'priya.verma@example.com',     '+91 9811223344', N'Flat 302, Barakhamba Road, Connaught Place, New Delhi'),
('CUST-003', N'Rohan',  N'Mehta',     'rohan.mehta@example.com',     '+91 9920334455', N'Sector 15A, Near City Park, Noida'),
('CUST-004', N'Ananya', N'Iyer',      'ananya.iyer@example.com',     '+91 9840112233', N'D-Block, Saket, South Delhi'),
('CUST-005', N'Vikram', N'Malhotra',  'vikram.malhotra@example.com', '+91 9822998877', N'Phase 5, Udyog Vihar, Gurugram');
GO

-- Active Shopping Cart (1)
INSERT INTO dbo.carts (id, customer_id, store_id) VALUES
('CART-001', 'CUST-005', 'STORE-GGN-01');
GO

-- Active Cart Items (2)
INSERT INTO dbo.cart_items (id, cart_id, product_id, quantity) VALUES
('CITEM-001', 'CART-001', 'PROD-002', 1),
('CITEM-002', 'CART-001', 'PROD-011', 1);
GO

-- Orders (1 Sample)
INSERT INTO dbo.orders (id, order_number, customer_id, store_id, status, fulfillment_type, total_amount) VALUES
('ORD-1001', 'ORD-20261001-1001', 'CUST-001', 'STORE-GGN-01', 'COMPLETED', 'STORE_PICKUP', 31099.00);
GO

-- Order Items (2 Sample Items)
INSERT INTO dbo.order_items (id, order_id, product_id, quantity, unit_price, subtotal) VALUES
('ITEM-101', 'ORD-1001', 'PROD-001', 1, 29999.00, 29999.00),
('ITEM-102', 'ORD-1001', 'PROD-012', 2, 550.00,   1100.00);
GO

-- ------------------------------------------------------------------------------
-- STEP 5: VERIFICATION QUERY
-- ------------------------------------------------------------------------------
SELECT 'stores' AS table_name, COUNT(*) AS row_count FROM dbo.stores
UNION ALL
SELECT 'categories', COUNT(*) FROM dbo.categories
UNION ALL
SELECT 'products', COUNT(*) FROM dbo.products
UNION ALL
SELECT 'store_inventory', COUNT(*) FROM dbo.store_inventory
UNION ALL
SELECT 'customers', COUNT(*) FROM dbo.customers
UNION ALL
SELECT 'carts', COUNT(*) FROM dbo.carts
UNION ALL
SELECT 'cart_items', COUNT(*) FROM dbo.cart_items
UNION ALL
SELECT 'orders', COUNT(*) FROM dbo.orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM dbo.order_items;
GO
