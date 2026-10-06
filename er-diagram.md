# StoreConnect – Entity Relationship Diagram (ERD)

**Project:** StoreConnect – Omnichannel Retail Platform  
**Architecture Theme:** Relational Database Model (PostgreSQL / ANSI SQL)  
**Schema DDL:** [`src/main/resources/schema.sql`](file:///d:/PS_CPS_Project/src/main/resources/schema.sql)  
**Seed Data:** [`src/main/resources/seed_data.sql`](file:///d:/PS_CPS_Project/src/main/resources/seed_data.sql)  

---

## 1. Visual Flow-Based ER Diagram (Left-to-Right Architecture)

This diagram organizes the relational database schema into three progressive operational phases, making the data lifecycle and foreign key relationships immediately clear:

```mermaid
flowchart LR
    %% Phase 1: Master Catalog & Store Infrastructure
    subgraph P1["PHASE 1: Master Catalog & Stores"]
        direction TB
        CAT["<b>CATEGORIES</b><br/>🔑 id (PK)<br/>• name<br/>• description"]
        PROD["<b>PRODUCTS</b><br/>🔑 id (PK)<br/>🔗 category_id (FK)<br/>• sku (UK)<br/>• name<br/>• price<br/>• is_active"]
        STR["<b>STORES</b><br/>🔑 id (PK)<br/>• code (UK)<br/>• name<br/>• city<br/>• address"]
        CAT -->|"1 : N (classifies)"| PROD
    end

    %% Phase 2: Omnichannel Inventory Mapping
    subgraph P2["PHASE 2: Store Inventory Bridge"]
        direction TB
        INV["<b>STORE_INVENTORY</b><br/>🔑 id (PK)<br/>🔗 store_id (FK)<br/>🔗 product_id (FK)<br/>• quantity_available<br/>• quantity_reserved<br/>• reorder_threshold"]
    end

    %% Phase 3: Customer Orders & Fulfillment
    subgraph P3["PHASE 3: Customer Orders & Fulfillment"]
        direction TB
        CUST["<b>CUSTOMERS</b><br/>🔑 id (PK)<br/>• email (UK)<br/>• first_name<br/>• last_name<br/>• phone"]
        ORD["<b>ORDERS</b><br/>🔑 id (PK)<br/>• order_number (UK)<br/>🔗 customer_id (FK)<br/>🔗 store_id (FK)<br/>• status<br/>• fulfillment_type<br/>• total_amount"]
        ITEM["<b>ORDER_ITEMS</b><br/>🔑 id (PK)<br/>🔗 order_id (FK)<br/>🔗 product_id (FK)<br/>• quantity<br/>• unit_price<br/>• subtotal"]
        CUST -->|"1 : N (places)"| ORD
        ORD -->|"1 : N (contains)"| ITEM
    end

    %% Cross-Phase Flow Connections
    PROD -->|"1 : N (stocked as)"| INV
    STR -->|"1 : N (maintains)"| INV
    STR -.->|"1 : N (fulfills order)"| ORD
    PROD -.->|"1 : N (line item)"| ITEM

    %% Styling
    style P1 fill:#1e293b,stroke:#3b82f6,stroke-width:2px,color:#93c5fd
    style P2 fill:#1e293b,stroke:#10b981,stroke-width:2px,color:#6ee7b7
    style P3 fill:#1e293b,stroke:#f59e0b,stroke-width:2px,color:#fcd34d

    style CAT fill:#0f172a,stroke:#60a5fa,color:#f8fafc
    style PROD fill:#0f172a,stroke:#60a5fa,color:#f8fafc
    style STR fill:#0f172a,stroke:#60a5fa,color:#f8fafc
    style INV fill:#0f172a,stroke:#34d399,color:#f8fafc
    style CUST fill:#0f172a,stroke:#fbbf24,color:#f8fafc
    style ORD fill:#0f172a,stroke:#fbbf24,color:#f8fafc
    style ITEM fill:#0f172a,stroke:#fbbf24,color:#f8fafc
```

---

## 2. Standard Crow's Foot Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    %% Master Catalog Relationships
    CATEGORY ||--o{ PRODUCT : "contains (1:N)"

    %% Multi-Store Inventory Bridge Relationships
    STORE ||--o{ STORE_INVENTORY : "maintains stock (1:N)"
    PRODUCT ||--o{ STORE_INVENTORY : "stocked in (1:N)"

    %% Customer & Order Placement Relationships
    CUSTOMER ||--o{ ORDERS : "places (1:N)"
    STORE ||--o{ ORDERS : "fulfills (1:N)"
    ORDERS ||--|{ ORDER_ITEM : "contains (1:N)"
    PRODUCT ||--o{ ORDER_ITEM : "ordered in (1:N)"

    CATEGORY {
        varchar id PK "Primary Key (e.g. CAT-ELEC)"
        varchar name "Category Name"
        varchar description "Category Details"
    }

    PRODUCT {
        varchar id PK "Primary Key (e.g. PROD-001)"
        varchar sku UK "Unique Stock Keeping Unit"
        varchar name "Product Display Name"
        varchar description "Product Specifications"
        varchar category_id FK "References CATEGORY(id)"
        decimal price "Selling Price (>= 0)"
        boolean is_active "Catalog Visibility Flag"
        timestamp created_at "Creation Timestamp"
    }

    STORE {
        varchar id PK "Primary Key (e.g. STORE-GGN-01)"
        varchar code UK "Unique Store Code"
        varchar name "Store Location Name"
        varchar address "Physical Address"
        varchar city "City (e.g. Gurugram)"
        varchar phone "Contact Phone Number"
        boolean is_active "Operational Status"
        timestamp created_at "Creation Timestamp"
    }

    STORE_INVENTORY {
        varchar id PK "Primary Key (e.g. INV-CH-01)"
        varchar store_id FK "References STORE(id)"
        varchar product_id FK "References PRODUCT(id)"
        int quantity_available "Available Stock for New Orders"
        int quantity_reserved "Committed Stock Awaiting Pickup"
        int reorder_threshold "Low Stock Alert Threshold"
        timestamp updated_at "Last Stock Update"
    }

    CUSTOMER {
        varchar id PK "Primary Key (e.g. CUST-001)"
        varchar first_name "First Name"
        varchar last_name "Last Name"
        varchar email UK "Unique Email Address"
        varchar phone "Contact Phone"
        varchar address "Customer Shipping Address"
        timestamp created_at "Registration Timestamp"
    }

    ORDERS {
        varchar id PK "Primary Key (e.g. ORD-001)"
        varchar order_number UK "Business Number (e.g. ORD-20261004-101)"
        varchar customer_id FK "References CUSTOMER(id)"
        varchar store_id FK "References STORE(id) for fulfillment"
        varchar status "PENDING|CONFIRMED|PROCESSING|READY|COMPLETED|CANCELLED"
        varchar fulfillment_type "STORE_PICKUP | HOME_DELIVERY"
        decimal total_amount "Order Total Sum"
        timestamp created_at "Order Placement Timestamp"
        timestamp updated_at "Status Last Updated"
    }

    ORDER_ITEM {
        varchar id PK "Primary Key (e.g. ITEM-001)"
        varchar order_id FK "References ORDERS(id) ON DELETE CASCADE"
        varchar product_id FK "References PRODUCT(id)"
        int quantity "Ordered Quantity (> 0)"
        decimal unit_price "Price at purchase time"
        decimal subtotal "quantity * unit_price"
    }
```

---

## 3. End-to-End Business Flow Walkthrough

The schema is organized to support a seamless, 4-step omnichannel lifecycle:

```
[1. Master Setup]          [2. Stock Allocation]         [3. Customer Order]         [4. Order Fulfillment]
CATEGORY ──► PRODUCT ──┐                                                              
                       ├──► STORE_INVENTORY ◄── STORE ◄─── CUSTOMER ──► ORDERS ──► ORDER_ITEMS
                       │                         │                         ▲              │
                       │                         └─────────────────────────┘              ▼
                       └─────────────────────────────────────────────────────────────► (Snapshot Price)
```

1. **Step 1: Catalog & Location Setup (Phase 1):**
   - Products are categorized under `CATEGORIES`.
   - Physical retail stores are defined in `STORES` with city, address, and contact details.

2. **Step 2: Omnichannel Inventory Mapping (Phase 2):**
   - Stock is decoupled from `PRODUCT` and mapped through `STORE_INVENTORY` using composite uniqueness on `(store_id, product_id)`.
   - Each physical store tracks its own `quantity_available` (for new sales) and `quantity_reserved` (for confirmed orders).

3. **Step 3: Customer Checkout & Atomic Stock Lock (Phase 3):**
   - A `CUSTOMER` selects a fulfillment store and checks out.
   - An `ORDERS` record is created with `store_id` (fulfillment store) and `customer_id`.
   - Stock in `STORE_INVENTORY` is atomically decremented: `quantity_available -= qty`, `quantity_reserved += qty`.

4. **Step 4: Order Line Items & Fulfillment (Phase 3):**
   - `ORDER_ITEMS` snapshot the exact `unit_price` at the moment of order placement.
   - Store operators at `STORE` fulfill the order through the state machine:
     `CONFIRMED` $\rightarrow$ `PROCESSING` (picking) $\rightarrow$ `READY_FOR_PICKUP` (packed) $\rightarrow$ `COMPLETED` (collected).
   - If cancelled, reserved stock is automatically returned to `quantity_available`.

---

## 4. Complete Relational Data Dictionary

| Table | Column | Type | Constraints | Description |
|:---|:---|:---|:---|:---|
| **`categories`** | `id` | VARCHAR(50) | PRIMARY KEY | Unique category identifier (e.g., `CAT-ELEC`) |
| | `name` | VARCHAR(100) | NOT NULL | Category display name |
| | `description` | VARCHAR(255) | | Category description |
| **`stores`** | `id` | VARCHAR(50) | PRIMARY KEY | Unique store identifier (e.g., `STORE-GGN-01`) |
| | `code` | VARCHAR(20) | UNIQUE, NOT NULL | Store short code (e.g., `STR-CH-01`) |
| | `name` | VARCHAR(150) | NOT NULL | Location name (e.g., `StoreConnect CyberHub`) |
| | `address` | VARCHAR(255) | NOT NULL | Street address |
| | `city` | VARCHAR(100) | NOT NULL | City location (e.g., `Gurugram`, `New Delhi`) |
| | `phone` | VARCHAR(20) | | Store phone number |
| | `is_active` | BOOLEAN | DEFAULT TRUE | Operating status |
| **`products`** | `id` | VARCHAR(50) | PRIMARY KEY | Unique product ID (e.g., `PROD-001`) |
| | `sku` | VARCHAR(50) | UNIQUE, NOT NULL | Stock Keeping Unit (e.g., `SKU-SNY-WH1000`) |
| | `name` | VARCHAR(200) | NOT NULL | Product title |
| | `description` | TEXT | | Detailed product description |
| | `category_id` | VARCHAR(50) | FK $\rightarrow$ `categories(id)` | Foreign key reference |
| | `price` | DECIMAL(10,2) | NOT NULL, CHECK $\ge 0$ | Current catalog selling price |
| | `is_active` | BOOLEAN | DEFAULT TRUE | Catalog visibility toggle |
| **`store_inventory`**| `id` | VARCHAR(50) | PRIMARY KEY | Unique inventory record ID |
| | `store_id` | VARCHAR(50) | FK $\rightarrow$ `stores(id)` | Target store location |
| | `product_id` | VARCHAR(50) | FK $\rightarrow$ `products(id)` | Target catalog product |
| | `quantity_available`| INT | NOT NULL, CHECK $\ge 0$ | Stock available for customer orders |
| | `quantity_reserved` | INT | NOT NULL, CHECK $\ge 0$ | Stock reserved for confirmed orders |
| | `reorder_threshold` | INT | DEFAULT 5 | Low stock trigger count |
| | *Constraint* | `uq_store_product` | UNIQUE(`store_id`, `product_id`) | Ensures 1 row per product per store |
| **`customers`** | `id` | VARCHAR(50) | PRIMARY KEY | Customer identifier (e.g., `CUST-001`) |
| | `first_name` | VARCHAR(100) | NOT NULL | Customer first name |
| | `last_name` | VARCHAR(100) | NOT NULL | Customer last name |
| | `email` | VARCHAR(150) | UNIQUE, NOT NULL | Account email address |
| | `phone` | VARCHAR(20) | | Contact number |
| | `address` | VARCHAR(255) | | Delivery address |
| **`orders`** | `id` | VARCHAR(50) | PRIMARY KEY | Order ID (e.g., `ORD-E1FDDA7A`) |
| | `order_number`| VARCHAR(50) | UNIQUE, NOT NULL | User-friendly number (`ORD-20261004-9446`) |
| | `customer_id` | VARCHAR(50) | FK $\rightarrow$ `customers(id)` | Placing customer |
| | `store_id` | VARCHAR(50) | FK $\rightarrow$ `stores(id)` | Fulfilling physical store |
| | `status` | VARCHAR(30) | NOT NULL | Lifecycle state (`PENDING` $\rightarrow$ `COMPLETED`) |
| | `fulfillment_type`| VARCHAR(30)| NOT NULL | `STORE_PICKUP` or `HOME_DELIVERY` |
| | `total_amount`| DECIMAL(10,2) | NOT NULL, CHECK $\ge 0$ | Sum of order items |
| **`order_items`** | `id` | VARCHAR(50) | PRIMARY KEY | Order item identifier |
| | `order_id` | VARCHAR(50) | FK $\rightarrow$ `orders(id)` | Parent order (ON DELETE CASCADE) |
| | `product_id` | VARCHAR(50) | FK $\rightarrow$ `products(id)` | Purchased product |
| | `quantity` | INT | NOT NULL, CHECK $> 0$ | Quantity ordered |
| | `unit_price` | DECIMAL(10,2) | NOT NULL, CHECK $\ge 0$ | Snapshot price at time of purchase |
| | `subtotal` | DECIMAL(10,2) | NOT NULL, CHECK $\ge 0$ | `quantity * unit_price` |

---

## 5. Secondary NoSQL Audit Log (MongoDB)

For compliance and auditability, high-frequency operational events are logged to MongoDB (`audit_events` collection) without burdening relational transactional tables:

```json
{
  "eventId": "EVT-9A4B2C1D",
  "eventType": "ORDER_STATUS_CHANGED",
  "entityType": "Order",
  "entityId": "ORD-20261004-9446",
  "actor": "OPERATOR-CH-01",
  "details": {
    "previousStatus": "PROCESSING",
    "newStatus": "READY_FOR_PICKUP"
  },
  "timestamp": "2026-10-04T12:06:15.123Z"
}
```
