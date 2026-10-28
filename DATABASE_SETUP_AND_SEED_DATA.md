# StoreConnect Database Setup & Seed Data Script

This document contains the complete SQL initialization and seed data script for the **StoreConnect** omnichannel retail microservices platform.

---

## 1. Overview & Architecture

StoreConnect microservices are split into isolated PostgreSQL schemas:

| Microservice | Target Schema | Managed Entities |
| :--- | :--- | :--- |
| **user-service** | `user_schema` | `customers`, `carts`, `cart_items` |
| **store-inventory-service** | `inventory_schema` | `categories`, `stores`, `products`, `store_inventory` |
| **order-service** | `order_schema` | `orders`, `order_items` |
| **payment-service** | `payment_schema` | *(Reserved for payment ledger / audit)* |

> **Note**: Java `DataSeeder` classes have been removed from the backend services. Running this SQL script gives you full control over database initialization and initial records.

---

## 2. Seed Credentials Reference

After running the seed script below, you can log in directly through the frontend UI using these credentials:

| Role | Email | Plaintext Password | Initial Identifier |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@storeconnect.com` | `Admin@123` | `CUST-ADMIN-01` |
| **Store Operator** | `operator.ggn@storeconnect.com` | `Operator@123` | `CUST-OPR-GGN` |
| **Customer** | `rahul.verma@example.com` | `Customer@123` | `CUST-001` |

*Passwords are stored as standard Spring Security BCrypt hashes (`$2a$12$...`).*

---

## 3. How to Execute

### Option A: Via pgAdmin or DBeaver
1. Connect to your PostgreSQL server.
2. Ensure the database `storeconnect` exists (or create it: `CREATE DATABASE storeconnect;`).
3. Open a **Query Tool** window connected to `storeconnect`.
4. Copy and paste the **All-in-One SQL Script** below and click **Execute** (or press `F5`).

### Option B: Via `psql` Command Line
```bash
# 1. Connect to PostgreSQL and create database (if not exists)
psql -U postgres -c "CREATE DATABASE storeconnect;"

# 2. Execute this script into the storeconnect database
psql -U postgres -d storeconnect -f DATABASE_SETUP_AND_SEED_DATA.sql
```

---

## 4. Complete All-in-One SQL Script

All statements are idempotent (`IF NOT EXISTS` and `ON CONFLICT DO NOTHING`), so running it multiple times is completely safe.

```sql
-- ============================================================================
-- STORECONNECT OMNICHANNEL PLATFORM: DATABASE INITIALIZATION & SEED SCRIPT
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. CREATE SCHEMAS
-- ----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS inventory_schema;
CREATE SCHEMA IF NOT EXISTS order_schema;
CREATE SCHEMA IF NOT EXISTS payment_schema;

-- ----------------------------------------------------------------------------
-- 2. DDL: USER SERVICE TABLES (user_schema)
-- ----------------------------------------------------------------------------

-- Table: customers
CREATE TABLE IF NOT EXISTS user_schema.customers (
    id VARCHAR(64) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    address VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: carts
CREATE TABLE IF NOT EXISTS user_schema.carts (
    id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL UNIQUE,
    store_id VARCHAR(64),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES user_schema.customers(id) ON DELETE CASCADE
);

-- Table: cart_items
CREATE TABLE IF NOT EXISTS user_schema.cart_items (
    id VARCHAR(64) PRIMARY KEY,
    cart_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES user_schema.carts(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 3. DDL: INVENTORY SERVICE TABLES (inventory_schema)
-- ----------------------------------------------------------------------------

-- Table: categories
CREATE TABLE IF NOT EXISTS inventory_schema.categories (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Table: stores
CREATE TABLE IF NOT EXISTS inventory_schema.stores (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: products
CREATE TABLE IF NOT EXISTS inventory_schema.products (
    id VARCHAR(64) PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id VARCHAR(64),
    price NUMERIC(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES inventory_schema.categories(id) ON DELETE SET NULL
);

-- Table: store_inventory
CREATE TABLE IF NOT EXISTS inventory_schema.store_inventory (
    id VARCHAR(64) PRIMARY KEY,
    store_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    reorder_threshold INTEGER NOT NULL DEFAULT 5,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_store_product UNIQUE (store_id, product_id),
    CONSTRAINT fk_inventory_store FOREIGN KEY (store_id) REFERENCES inventory_schema.stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES inventory_schema.products(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 4. DDL: ORDER SERVICE TABLES (order_schema)
-- ----------------------------------------------------------------------------

-- Table: orders
CREATE TABLE IF NOT EXISTS order_schema.orders (
    id VARCHAR(64) PRIMARY KEY,
    order_number VARCHAR(64) NOT NULL UNIQUE,
    customer_id VARCHAR(64) NOT NULL,
    store_id VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    fulfillment_type VARCHAR(50) NOT NULL DEFAULT 'STORE_PICKUP',
    total_amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: order_items
CREATE TABLE IF NOT EXISTS order_schema.order_items (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES order_schema.orders(id) ON DELETE CASCADE
);


-- ============================================================================
-- 5. SEED DATA (DML)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Seed Customers (user_schema.customers)
-- ----------------------------------------------------------------------------
INSERT INTO user_schema.customers (id, first_name, last_name, email, phone, address, password_hash, role, created_at)
VALUES
    (
        'CUST-ADMIN-01',
        'Admin',
        'User',
        'admin@storeconnect.com',
        '+91 9999900001',
        'StoreConnect HQ, CyberCity',
        '$2a$12$YSMRaKUWTF6Rrwj8PZTCxe6DrlZ7c7gb2sVbvwuuHyHoCFStg4OKW',
        'ADMIN',
        CURRENT_TIMESTAMP
    ),
    (
        'CUST-OPR-GGN',
        'Gurugram',
        'Operator',
        'operator.ggn@storeconnect.com',
        '+91 9999900002',
        'DLF CyberHub Store',
        '$2a$12$x67r5/hIjtXmQNB2ra4/KuIatRh3Hv/fAHnKsYjtf.7gaPzcrvc3S',
        'STORE_OPERATOR',
        CURRENT_TIMESTAMP
    ),
    (
        'CUST-001',
        'Rahul',
        'Verma',
        'rahul.verma@example.com',
        '+91 9876543210',
        'Sector 56, Gurugram, Haryana',
        '$2a$12$wPbICrg8OVJVZhCvxP1E2u38ktzyPDRQjeIKwzfCW3AQUJemrhnVO',
        'CUSTOMER',
        CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- Seed Categories (inventory_schema.categories)
-- ----------------------------------------------------------------------------
INSERT INTO inventory_schema.categories (id, name, description)
VALUES
    ('CAT-PRODUCE', 'Fresh Produce', 'Organic fruits and farm vegetables'),
    ('CAT-DAIRY', 'Dairy & Bakery', 'Milk, artisanal cheese, bread, eggs'),
    ('CAT-BEVERAGES', 'Beverages', 'Juices, organic tea, craft kombucha'),
    ('CAT-SNACKS', 'Packaged Snacks', 'Healthy roasted snacks, nuts, chips')
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- Seed Stores (inventory_schema.stores)
-- ----------------------------------------------------------------------------
INSERT INTO inventory_schema.stores (id, name, code, address, city, phone, is_active, created_at)
VALUES
    ('STORE-GGN-01', 'DLF CyberHub SuperStore', 'GGN-01', 'Ground Floor, CyberHub', 'Gurugram', '+91 124 4567890', TRUE, CURRENT_TIMESTAMP),
    ('STORE-DEL-01', 'Connaught Place Flagship', 'DEL-01', 'Block B, Inner Circle, CP', 'New Delhi', '+91 11 23415678', TRUE, CURRENT_TIMESTAMP),
    ('STORE-BLR-01', 'Indiranagar Experience Hub', 'BLR-01', '100 Feet Road, HAL 2nd Stage', 'Bengaluru', '+91 80 41235678', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- Seed Products (inventory_schema.products - 25 Realistic Products)
-- ----------------------------------------------------------------------------
INSERT INTO inventory_schema.products (id, sku, name, description, category_id, price, is_active, created_at)
VALUES
    -- Fresh Produce (6 items)
    ('PROD-001', 'SKU-APP-001', 'Shimla Royal Apple (1kg)', 'Crisp, hand-picked sweet mountain apples from Himachal Pradesh', 'CAT-PRODUCE', 180.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-005', 'SKU-BAN-001', 'Robusta Farm Bananas (1 Dozen)', 'Naturally ripened nutrient-rich golden table bananas', 'CAT-PRODUCE', 65.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-006', 'SKU-SPN-001', 'Hydroponic Baby Spinach (250g)', 'Pesticide-free hydroponically grown tender spinach leaves', 'CAT-PRODUCE', 45.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-007', 'SKU-AVO-001', 'Hass Fresh Avocado (Pack of 2)', 'Creamy imported Hass avocados, rich in healthy omega fats', 'CAT-PRODUCE', 220.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-008', 'SKU-TOM-001', 'Organic Hybrid Tomatoes (1kg)', 'Vine-ripened juicy red organic salad tomatoes', 'CAT-PRODUCE', 40.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-009', 'SKU-ORG-001', 'Nagpur Sweet Oranges (1kg)', 'Juicy high-vitamin C authentic Nagpur table oranges', 'CAT-PRODUCE', 110.00, TRUE, CURRENT_TIMESTAMP),

    -- Dairy & Bakery (6 items)
    ('PROD-002', 'SKU-MILK-001', 'A2 Farm Fresh Whole Milk (1L)', 'Pure unadulterated cold-chain grass-fed cow milk in glass bottle', 'CAT-DAIRY', 75.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-010', 'SKU-YOG-001', 'Greek Natural Probiotic Yogurt (400g)', 'Thick strained probiotic yogurt with zero added sugar or preservatives', 'CAT-DAIRY', 140.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-011', 'SKU-BRD-001', 'Artisanal Sourdough Boule (450g)', 'Naturally fermented European-style country sourdough bread', 'CAT-DAIRY', 160.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-012', 'SKU-EGG-001', 'Organic Free-Range Eggs (6 Pcs)', 'Hormone-free certified pasture-raised brown hen eggs', 'CAT-DAIRY', 85.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-013', 'SKU-PAN-001', 'Fresh Malai Paneer (200g)', 'Soft artisanal cottage cheese made from fresh whole farm milk', 'CAT-DAIRY', 95.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-014', 'SKU-BUT-001', 'Farm Creamery Salted Butter (500g)', 'Traditional slow-churned pure milk butter lightly seasoned with sea salt', 'CAT-DAIRY', 275.00, TRUE, CURRENT_TIMESTAMP),

    -- Beverages (6 items)
    ('PROD-003', 'SKU-COF-001', 'Coorg Dark Roast Coffee Beans (250g)', 'Single-estate shade-grown Arabica roasted beans with chocolate notes', 'CAT-BEVERAGES', 350.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-015', 'SKU-TEA-001', 'Himalayan Loose Green Tea (100g)', 'Whole-leaf spring harvest antioxidant green tea from high altitude gardens', 'CAT-BEVERAGES', 240.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-016', 'SKU-JUIC-001', 'Cold-Pressed Valencia Orange Juice (1L)', '100% pure cold-pressed citrus juice with zero added water or sugar', 'CAT-BEVERAGES', 199.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-017', 'SKU-SPK-001', 'Himalayan Sparkling Water (750ml)', 'Naturally carbonated mineral water sourced from pristine mountain springs', 'CAT-BEVERAGES', 85.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-018', 'SKU-OAT-001', 'Barista Plant-Based Oat Milk (1L)', 'Creamy dairy-free oat drink perfect for specialty coffee and breakfast', 'CAT-BEVERAGES', 290.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-019', 'SKU-COC-001', 'Raw Tender Coconut Water (500ml)', 'Unpasteurized bio-secure bottled pure electrolyte coconut water', 'CAT-BEVERAGES', 60.00, TRUE, CURRENT_TIMESTAMP),

    -- Packaged Snacks (7 items)
    ('PROD-004', 'SKU-ALM-001', 'California Jumbo Roasted Almonds (500g)', 'Slow roasted, lightly salted California premium nonpareil almonds', 'CAT-SNACKS', 499.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-020', 'SKU-GRN-001', 'Honey Almond Protein Granola (400g)', 'Baked rolled oats tossed with raw honey, chia seeds and roasted nuts', 'CAT-SNACKS', 340.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-021', 'SKU-CHIP-001', 'Roasted Multigrain Crisps (150g)', 'Oil-free baked sorghum, quinoa and millet crisps with peri-peri herbs', 'CAT-SNACKS', 120.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-022', 'SKU-CHOC-001', 'Artisanal 70% Dark Chocolate Bar (80g)', 'Single-origin South Indian cacao bean chocolate with caramel notes', 'CAT-SNACKS', 180.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-023', 'SKU-CSH-001', 'W240 Whole Premium Cashews (500g)', 'Grade-A crunchy whole Indian cashew nuts, unpolished and sweet', 'CAT-SNACKS', 550.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-024', 'SKU-TRL-001', 'Superberry Seeds & Nut Trail Mix (200g)', 'Wholesome mix of cranberries, blueberries, pumpkin seeds and walnuts', 'CAT-SNACKS', 280.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-025', 'SKU-FOX-001', 'Salt & Pepper Roasted Foxnuts (100g)', 'Crunchy superfood lotus seed makhana slow-roasted in cold pressed olive oil', 'CAT-SNACKS', 150.00, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    category_id = EXCLUDED.category_id;

-- ----------------------------------------------------------------------------
-- Seed Store Inventory (inventory_schema.store_inventory)
-- 3 Physical Stores x 25 Products = 75 Stock Records
-- ----------------------------------------------------------------------------
INSERT INTO inventory_schema.store_inventory (id, store_id, product_id, quantity_available, quantity_reserved, reorder_threshold, updated_at)
VALUES
    -- Gurugram Hub (STORE-GGN-01)
    ('INV-GGN-PROD-001', 'STORE-GGN-01', 'PROD-001', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-002', 'STORE-GGN-01', 'PROD-002', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-003', 'STORE-GGN-01', 'PROD-003', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-004', 'STORE-GGN-01', 'PROD-004', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-005', 'STORE-GGN-01', 'PROD-005', 45, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-006', 'STORE-GGN-01', 'PROD-006', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-007', 'STORE-GGN-01', 'PROD-007', 18, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-008', 'STORE-GGN-01', 'PROD-008', 50, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-009', 'STORE-GGN-01', 'PROD-009', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-010', 'STORE-GGN-01', 'PROD-010', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-011', 'STORE-GGN-01', 'PROD-011', 22, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-012', 'STORE-GGN-01', 'PROD-012', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-013', 'STORE-GGN-01', 'PROD-013', 28, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-014', 'STORE-GGN-01', 'PROD-014', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-015', 'STORE-GGN-01', 'PROD-015', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-016', 'STORE-GGN-01', 'PROD-016', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-017', 'STORE-GGN-01', 'PROD-017', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-018', 'STORE-GGN-01', 'PROD-018', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-019', 'STORE-GGN-01', 'PROD-019', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-020', 'STORE-GGN-01', 'PROD-020', 28, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-021', 'STORE-GGN-01', 'PROD-021', 45, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-022', 'STORE-GGN-01', 'PROD-022', 50, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-023', 'STORE-GGN-01', 'PROD-023', 22, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-024', 'STORE-GGN-01', 'PROD-024', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-PROD-025', 'STORE-GGN-01', 'PROD-025', 38, 0, 5, CURRENT_TIMESTAMP),

    -- Delhi Flagship (STORE-DEL-01)
    ('INV-DEL-PROD-001', 'STORE-DEL-01', 'PROD-001', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-002', 'STORE-DEL-01', 'PROD-002', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-003', 'STORE-DEL-01', 'PROD-003', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-004', 'STORE-DEL-01', 'PROD-004', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-005', 'STORE-DEL-01', 'PROD-005', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-006', 'STORE-DEL-01', 'PROD-006', 15, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-007', 'STORE-DEL-01', 'PROD-007', 15, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-008', 'STORE-DEL-01', 'PROD-008', 45, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-009', 'STORE-DEL-01', 'PROD-009', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-010', 'STORE-DEL-01', 'PROD-010', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-011', 'STORE-DEL-01', 'PROD-011', 18, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-012', 'STORE-DEL-01', 'PROD-012', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-013', 'STORE-DEL-01', 'PROD-013', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-014', 'STORE-DEL-01', 'PROD-014', 18, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-015', 'STORE-DEL-01', 'PROD-015', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-016', 'STORE-DEL-01', 'PROD-016', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-017', 'STORE-DEL-01', 'PROD-017', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-018', 'STORE-DEL-01', 'PROD-018', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-019', 'STORE-DEL-01', 'PROD-019', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-020', 'STORE-DEL-01', 'PROD-020', 22, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-021', 'STORE-DEL-01', 'PROD-021', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-022', 'STORE-DEL-01', 'PROD-022', 45, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-023', 'STORE-DEL-01', 'PROD-023', 20, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-024', 'STORE-DEL-01', 'PROD-024', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-PROD-025', 'STORE-DEL-01', 'PROD-025', 30, 0, 5, CURRENT_TIMESTAMP),

    -- Bengaluru Experience Hub (STORE-BLR-01)
    ('INV-BLR-PROD-001', 'STORE-BLR-01', 'PROD-001', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-002', 'STORE-BLR-01', 'PROD-002', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-003', 'STORE-BLR-01', 'PROD-003', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-004', 'STORE-BLR-01', 'PROD-004', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-005', 'STORE-BLR-01', 'PROD-005', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-006', 'STORE-BLR-01', 'PROD-006', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-007', 'STORE-BLR-01', 'PROD-007', 22, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-008', 'STORE-BLR-01', 'PROD-008', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-009', 'STORE-BLR-01', 'PROD-009', 28, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-010', 'STORE-BLR-01', 'PROD-010', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-011', 'STORE-BLR-01', 'PROD-011', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-012', 'STORE-BLR-01', 'PROD-012', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-013', 'STORE-BLR-01', 'PROD-013', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-014', 'STORE-BLR-01', 'PROD-014', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-015', 'STORE-BLR-01', 'PROD-015', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-016', 'STORE-BLR-01', 'PROD-016', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-017', 'STORE-BLR-01', 'PROD-017', 45, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-018', 'STORE-BLR-01', 'PROD-018', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-019', 'STORE-BLR-01', 'PROD-019', 40, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-020', 'STORE-BLR-01', 'PROD-020', 30, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-021', 'STORE-BLR-01', 'PROD-021', 45, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-022', 'STORE-BLR-01', 'PROD-022', 50, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-023', 'STORE-BLR-01', 'PROD-023', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-024', 'STORE-BLR-01', 'PROD-024', 35, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-PROD-025', 'STORE-BLR-01', 'PROD-025', 40, 0, 5, CURRENT_TIMESTAMP)
ON CONFLICT (store_id, product_id) DO UPDATE SET
    quantity_available = EXCLUDED.quantity_available,
    quantity_reserved = EXCLUDED.quantity_reserved;
```

---

## 5. Verification & Sanity Checks

Run these quick queries to ensure all schemas and records are properly populated:

```sql
-- 1. Check user accounts
SELECT id, email, first_name, last_name, role FROM user_schema.customers;

-- 2. Check catalog counts
SELECT 'Categories' AS entity, COUNT(*) FROM inventory_schema.categories
UNION ALL
SELECT 'Stores' AS entity, COUNT(*) FROM inventory_schema.stores
UNION ALL
SELECT 'Products' AS entity, COUNT(*) FROM inventory_schema.products
UNION ALL
SELECT 'Store Inventory' AS entity, COUNT(*) FROM inventory_schema.store_inventory;

-- 3. Check stock availability across stores
SELECT 
    s.name AS store_name,
    p.name AS product_name,
    si.quantity_available,
    si.quantity_reserved,
    si.reorder_threshold
FROM inventory_schema.store_inventory si
JOIN inventory_schema.stores s ON si.store_id = s.id
JOIN inventory_schema.products p ON si.product_id = p.id
ORDER BY s.name, p.name;
```
