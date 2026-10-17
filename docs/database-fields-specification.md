# StoreConnect – Relational Database Schema & Fields Specification

**Project:** StoreConnect – Omnichannel Retail Platform  
**Jira Subtask:** `ST-DB-01: Database Fields & Schema Specification Documentation`  
**Database Engines:** PostgreSQL 15+ / ANSI SQL (Production & Development)  
**DDL Source:** [`src/main/resources/schema.sql`](file:///d:/PS_CPS_Project/src/main/resources/schema.sql)  

---

## 1. Schema Overview

The StoreConnect relational model bridges physical retail stores with digital commerce. The schema is normalized into 9 tables with strict check constraints, foreign keys, and indexes to enforce data integrity, provide multi-device cart persistence, and prevent overselling.

```
[Master Data]                [Inventory Bridge]             [Persistent Cart]             [Transactional Flow]
categories ──► products ──┐                                                               
                          ├──► store_inventory ◄── stores ◄─── customers ──► carts ──► orders ──► order_items
                          │                          │                     │    │        ▲            │
                          │                          └─────────────────────┼────┴────────┘            ▼
                          │                                                ▼                   (Snapshot Price)
                          │                                           cart_items ◄── (Active items)
                          └────────────────────────────────────────────────┘
```

---

## 2. Table-by-Table Field Specification

### 2.1 Table: `stores`
Stores physical brick-and-mortar retail locations acting as fulfillment hubs (BOPIS / Ship-from-Store).

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Unique system identifier (e.g. `STORE-GGN-01`) |
| `name` | VARCHAR(150) | NO | - | - | Display name of the retail store (e.g. `StoreConnect CyberHub`) |
| `code` | VARCHAR(20) | NO | **UNIQUE** | - | Business short code (e.g. `STR-CH-01`) |
| `address` | VARCHAR(255) | NO | - | - | Physical street address |
| `city` | VARCHAR(100) | NO | - | - | Operating city (e.g. `Gurugram`, `New Delhi`, `Noida`) |
| `phone` | VARCHAR(20) | YES | - | NULL | Store contact phone number |
| `is_active` | BOOLEAN | NO | - | `TRUE` | Operating status flag (`TRUE` = open for orders) |
| `created_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Store creation timestamp |

---

### 2.2 Table: `categories`
Organizes products into retail departments for customer browsing and search filtering.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Unique category ID (e.g. `CAT-AUDIO`) |
| `name` | VARCHAR(100) | NO | - | - | Department name (e.g. `Audio & Sound`) |
| `description` | VARCHAR(255) | YES | - | NULL | Overview of products classified within category |

---

### 2.3 Table: `products`
The centralized master catalog containing global product information and pricing.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Master product ID (e.g. `PROD-001`) |
| `sku` | VARCHAR(50) | NO | **UNIQUE** | - | Global Stock Keeping Unit (e.g. `SKU-SNY-WH1000`) |
| `name` | VARCHAR(200) | NO | - | - | Product display name |
| `description` | TEXT | YES | - | NULL | Long product description and specifications |
| `category_id` | VARCHAR(50) | YES | **FOREIGN KEY** $\rightarrow$ `categories(id)` | NULL | Associated retail category |
| `price` | DECIMAL(10,2) | NO | `CHECK (price >= 0)` | - | Current catalog selling price in INR |
| `is_active` | BOOLEAN | NO | - | `TRUE` | Visibility in customer search and catalog |
| `created_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Record creation timestamp |

---

### 2.4 Table: `store_inventory`
The core omnichannel entity. Manages real-time stock levels independently for every store and product combination.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Unique inventory record identifier (e.g. `INV-01-01`) |
| `store_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `stores(id)` | - | Target retail store location |
| `product_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `products(id)` | - | Target catalog product |
| `quantity_available` | INT | NO | `CHECK (quantity_available >= 0)` | 0 | Shelf stock available for new customer orders |
| `quantity_reserved` | INT | NO | `CHECK (quantity_reserved >= 0)` | 0 | Stock allocated to confirmed orders awaiting pick |
| `reorder_threshold` | INT | NO | - | 5 | Threshold count triggering automated low-stock warnings |
| `updated_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Timestamp of latest stock level modification |
| *Composite Constraint* | - | - | **UNIQUE(`store_id`, `product_id`)** | - | Enforces exactly one stock record per product per store |

---

### 2.5 Table: `customers`
Registered customer accounts eligible for omnichannel ordering and order tracking.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Customer identifier (e.g. `CUST-001`) |
| `first_name` | VARCHAR(100) | NO | - | - | Customer given name |
| `last_name` | VARCHAR(100) | NO | - | - | Customer surname |
| `email` | VARCHAR(150) | NO | **UNIQUE** | - | User login and notification email address |
| `phone` | VARCHAR(20) | YES | - | NULL | Customer contact mobile number |
| `address` | VARCHAR(255) | YES | - | NULL | Primary shipping/billing address |
| `created_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Registration date and time |

---

### 2.6 Table: `carts`
Stores the active shopping basket for each customer. Enforces a 1:1 relationship with customers and links to the selected store for real-time inventory validation.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Unique cart ID (e.g. `CART-001`) |
| `customer_id` | VARCHAR(50) | NO | **UNIQUE**, **FOREIGN KEY** $\rightarrow$ `customers(id)`<br/>`ON DELETE CASCADE` | - | Owning customer (enforces 1 active cart per customer) |
| `store_id` | VARCHAR(50) | YES | **FOREIGN KEY** $\rightarrow$ `stores(id)`<br/>`ON DELETE SET NULL` | NULL | Preferred fulfillment store for stock checking |
| `created_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Cart initiation timestamp |
| `updated_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Last cart modification timestamp |

---

### 2.7 Table: `cart_items`
Individual products and quantities currently placed inside the customer's active shopping cart.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Cart line item ID (e.g. `CITEM-001`) |
| `cart_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `carts(id)`<br/>`ON DELETE CASCADE` | - | Parent shopping cart reference |
| `product_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `products(id)`<br/>`ON DELETE CASCADE` | - | Referenced catalog product |
| `quantity` | INT | NO | `CHECK (quantity > 0)` | - | Quantity selected by customer |
| `created_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Time item was added to cart |
| *Constraint* | `uq_cart_product` | NO | **UNIQUE** `(cart_id, product_id)` | - | Ensures one aggregated row per product per cart |

---

### 2.8 Table: `orders`
The aggregate root for customer orders, associating customer demand with specific physical store fulfillment.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Internal system order ID (e.g. `ORD-1001`) |
| `order_number` | VARCHAR(50) | NO | **UNIQUE** | - | Human-readable tracking number (e.g. `ORD-20261001-1001`) |
| `customer_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `customers(id)` | - | Ordering customer |
| `store_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `stores(id)` | - | Assigned physical fulfillment store |
| `status` | VARCHAR(30) | NO | - | - | Order lifecycle state (`PENDING`, `CONFIRMED`, `PROCESSING`, `READY_FOR_PICKUP`, `COMPLETED`, `CANCELLED`) |
| `fulfillment_type` | VARCHAR(30) | NO | - | - | Fulfillment channel (`STORE_PICKUP` or `HOME_DELIVERY`) |
| `total_amount` | DECIMAL(10,2) | NO | `CHECK (total_amount >= 0)` | 0.00 | Total payable amount calculated from order items |
| `created_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Checkout placement timestamp |
| `updated_at` | TIMESTAMP | NO | - | `CURRENT_TIMESTAMP` | Timestamp of latest status transition |

---

### 2.9 Table: `order_items`
Individual product line items within an order.

| Column Name | Data Type | Nullable | Key / Constraint | Default | Business Description |
|:---|:---|:---:|:---:|:---:|:---|
| `id` | VARCHAR(50) | NO | **PRIMARY KEY** | - | Line item ID (e.g. `ITEM-101`) |
| `order_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `orders(id)`<br/>`ON DELETE CASCADE` | - | Parent order reference |
| `product_id` | VARCHAR(50) | NO | **FOREIGN KEY** $\rightarrow$ `products(id)` | - | Purchased catalog product |
| `quantity` | INT | NO | `CHECK (quantity > 0)` | - | Ordered quantity count |
| `unit_price` | DECIMAL(10,2) | NO | `CHECK (unit_price >= 0)` | - | Immutable snapshot price at purchase moment |
| `subtotal` | DECIMAL(10,2) | NO | `CHECK (subtotal >= 0)` | - | Calculated item total: `quantity * unit_price` |

---

## 3. Database Indexes

To support rapid query response during customer browsing and store operator operations, the following indexes are defined:

| Index Name | Table | Columns | Purpose |
|:---|:---|:---|:---|
| `idx_products_category` | `products` | `(category_id)` | Accelerates catalog filtering by retail category |
| `idx_inventory_store_product` | `store_inventory` | `(store_id, product_id)` | Fast real-time store stock availability lookups |
| `idx_carts_customer` | `carts` | `(customer_id)` | Fast lookup of active customer shopping cart |
| `idx_cart_items_cart` | `cart_items` | `(cart_id)` | Rapid retrieval of line items inside a cart |
| `idx_cart_items_product` | `cart_items` | `(product_id)` | Foreign key indexing for product removals/cascades |
| `idx_orders_customer` | `orders` | `(customer_id)` | Speeds up "My Orders" customer order history |
| `idx_orders_store` | `orders` | `(store_id)` | Optimizes store operator dashboard of incoming store orders |
| `idx_orders_status` | `orders` | `(status)` | Supports store picking queues filtered by order state |

---

## 4. Secondary NoSQL Audit Schema (MongoDB)

For non-blocking high-throughput operational event tracking, events are written to MongoDB database `storeconnect_audit`, collection `audit_events`.

### Document Structure:
```json
{
  "_id": "ObjectId('6703b415a1b2c3d4e5f60718')",
  "eventId": "EVT-9A4B2C1D",
  "eventType": "ORDER_STATUS_CHANGED",
  "entityType": "Order",
  "entityId": "ORD-1002",
  "actor": "OPERATOR-DEL-01",
  "details": {
    "orderNumber": "ORD-20261002-1002",
    "previousStatus": "PROCESSING",
    "newStatus": "READY_FOR_PICKUP",
    "storeId": "STORE-DEL-01"
  },
  "timestamp": "2026-10-02T14:20:00.000Z"
}
```

### Indexed Fields in MongoDB:
- `{ "eventId": 1 }` (Unique)
- `{ "entityId": 1, "timestamp": -1 }` (Audit history by order)
- `{ "timestamp": -1 }` (Chronological operational log)
