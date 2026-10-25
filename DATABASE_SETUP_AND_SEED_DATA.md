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
-- Seed Products (inventory_schema.products)
-- ----------------------------------------------------------------------------
INSERT INTO inventory_schema.products (id, sku, name, description, category_id, price, is_active, created_at)
VALUES
    ('PROD-001', 'SKU-APP-001', 'Shimla Royal Apple (1kg)', 'Crisp, hand-picked sweet mountain apples', 'CAT-PRODUCE', 180.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-002', 'SKU-MILK-001', 'A2 Farm Fresh Whole Milk (1L)', 'Pure unadulterated cold-chain farm milk', 'CAT-DAIRY', 75.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-003', 'SKU-COF-001', 'Coorg Dark Roast Coffee Beans (250g)', 'Single-estate shade-grown Arabica beans', 'CAT-BEVERAGES', 350.00, TRUE, CURRENT_TIMESTAMP),
    ('PROD-004', 'SKU-ALM-001', 'California Jumbo Roasted Almonds (500g)', 'Slow roasted, lightly salted premium almonds', 'CAT-SNACKS', 499.00, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- Seed Store Inventory (inventory_schema.store_inventory)
-- 3 Physical Stores x 4 Products = 12 Stock Records
-- Initial stock: 25 available units per product per store
-- ----------------------------------------------------------------------------
INSERT INTO inventory_schema.store_inventory (id, store_id, product_id, quantity_available, quantity_reserved, reorder_threshold, updated_at)
VALUES
    -- Gurugram (GGN-01)
    ('INV-GGN-01-SKU-APP-001',  'STORE-GGN-01', 'PROD-001', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-01-SKU-MILK-001', 'STORE-GGN-01', 'PROD-002', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-01-SKU-COF-001',  'STORE-GGN-01', 'PROD-003', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-GGN-01-SKU-ALM-001',  'STORE-GGN-01', 'PROD-004', 25, 0, 5, CURRENT_TIMESTAMP),

    -- New Delhi (DEL-01)
    ('INV-DEL-01-SKU-APP-001',  'STORE-DEL-01', 'PROD-001', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-01-SKU-MILK-001', 'STORE-DEL-01', 'PROD-002', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-01-SKU-COF-001',  'STORE-DEL-01', 'PROD-003', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-DEL-01-SKU-ALM-001',  'STORE-DEL-01', 'PROD-004', 25, 0, 5, CURRENT_TIMESTAMP),

    -- Bengaluru (BLR-01)
    ('INV-BLR-01-SKU-APP-001',  'STORE-BLR-01', 'PROD-001', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-01-SKU-MILK-001', 'STORE-BLR-01', 'PROD-002', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-01-SKU-COF-001',  'STORE-BLR-01', 'PROD-003', 25, 0, 5, CURRENT_TIMESTAMP),
    ('INV-BLR-01-SKU-ALM-001',  'STORE-BLR-01', 'PROD-004', 25, 0, 5, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
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
