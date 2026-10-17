# StoreConnect – Actual Database Seed & Sample Data

**Project:** StoreConnect – Omnichannel Retail Platform  
**Jira Subtask:** `ST-DB-02: Actual Database (Data-Rough) & Fixed Seed Dataset`  
**SQL Script:** [`src/main/resources/seed_data.sql`](file:///d:/PS_CPS_Project/src/main/resources/seed_data.sql)  

---

## 1. Confirmed Entity Counts & Scope

To ensure realistic testing of omnichannel availability, search, and order fulfillment, the dataset scope is fixed as follows:

| Entity | Confirmed Count | Purpose in Sprint 1 & Future Sprints |
|:---|:---:|:---|
| **Stores** | **5 Locations** | Physical stores across Gurugram, Delhi, and Noida |
| **Categories** | **4 Departments** | Core retail catalog taxonomy |
| **Products** | **12 SKUs** | Realistic electronics, wearables, computing, and essentials |
| **Store Inventory** | **60 Records** | 5 stores $\times$ 12 products (includes in-stock, low-stock, and out-of-stock items) |
| **Customers** | **5 Accounts** | Registered test customers |
| **Active Carts** | **1 Cart** | Persisted shopping basket awaiting checkout (`CUST-005`) |
| **Cart Items** | **2 Line Items** | Active products inside shopping cart |
| **Sample Orders** | **4 Orders** | Representative orders across different lifecycle states |
| **Order Items** | **7 Line Items** | Line item entries showing snapshot unit prices |

---

## 2. Table: `categories` (4 Departments)

| ID | Name | Description |
|:---|:---|:---|
| `CAT-AUDIO` | Audio & Sound | Noise-cancelling headphones, wireless earphones, and portable speakers |
| `CAT-WEAR` | Wearables & Smart Tech | Smartwatches, fitness bands, and wearable health trackers |
| `CAT-COMP` | Computing & Accessories | High-productivity monitors, mechanical keyboards, and precision mice |
| `CAT-LIFE` | Lifestyle & Essentials | Performance athletic wear, ergonomic backpacks, and specialty coffee |

---

## 3. Table: `stores` (5 Physical Retail Locations)

| ID | Code | Store Name | City | Address | Phone | Active |
|:---|:---|:---|:---|:---|:---|:---:|
| `STORE-GGN-01` | `STR-CH-01` | StoreConnect CyberHub | Gurugram | DLF CyberHub, DLF Phase 2, Sector 24 | +91 124 4567890 | TRUE |
| `STORE-GGN-02` | `STR-AMB-02` | StoreConnect Ambience | Gurugram | Ambience Mall, NH-8, Ambience Island | +91 124 4987654 | TRUE |
| `STORE-DEL-01` | `STR-CP-03` | StoreConnect CP Flagship | New Delhi | Inner Circle, Block B, Connaught Place | +91 11 23456789 | TRUE |
| `STORE-DEL-02` | `STR-SCW-04` | StoreConnect Select Citywalk | New Delhi | Select Citywalk, A-3 District Centre, Saket | +91 11 41234567 | TRUE |
| `STORE-NOI-01` | `STR-MOI-05` | StoreConnect Mall of India | Noida | DLF Mall of India, Sector 18 | +91 120 4567891 | TRUE |

---

## 4. Table: `products` (12 Products)

| ID | SKU | Product Name | Category ID | Price (INR) | Active |
|:---|:---|:---|:---|:---:|:---:|
| `PROD-001` | `SKU-SNY-WH1000` | Sony WH-1000XM5 Headphones | `CAT-AUDIO` | ₹29,999.00 | TRUE |
| `PROD-002` | `SKU-BSE-QC45` | Bose QuietComfort 45 | `CAT-AUDIO` | ₹26,900.00 | TRUE |
| `PROD-003` | `SKU-APL-APP2` | Apple AirPods Pro (2nd Gen) | `CAT-AUDIO` | ₹24,900.00 | TRUE |
| `PROD-004` | `SKU-APL-WCH9` | Apple Watch Series 9 GPS 45mm | `CAT-WEAR` | ₹41,900.00 | TRUE |
| `PROD-005` | `SKU-SAM-GW6` | Samsung Galaxy Watch 6 LTE 44mm | `CAT-WEAR` | ₹29,999.00 | TRUE |
| `PROD-006` | `SKU-FIT-CHG6` | Fitbit Charge 6 Fitness Tracker | `CAT-WEAR` | ₹14,999.00 | TRUE |
| `PROD-007` | `SKU-LOG-MX3S` | Logitech MX Master 3S Mouse | `CAT-COMP` | ₹8,995.00 | TRUE |
| `PROD-008` | `SKU-KEY-K2V2` | Keychron K2 Wireless Keyboard | `CAT-COMP` | ₹7,499.00 | TRUE |
| `PROD-009` | `SKU-DEL-U2723` | Dell UltraSharp 27 4K Monitor | `CAT-COMP` | ₹48,500.00 | TRUE |
| `PROD-010` | `SKU-NIK-DRYFIT` | Nike Dri-FIT Performance Tee | `CAT-LIFE` | ₹1,995.00 | TRUE |
| `PROD-011` | `SKU-WLD-PRO28` | Wildcraft Ergonomic Backpack 28L | `CAT-LIFE` | ₹2,499.00 | TRUE |
| `PROD-012` | `SKU-BTK-COFFEE` | Blue Tokai Specialty Coffee 250g | `CAT-LIFE` | ₹550.00 | TRUE |

---

## 5. Table: `store_inventory` (Matrix of 5 Stores $\times$ 12 Products)

| Store | Product | Available | Reserved | Threshold | Status Indicator |
|:---|:---|:---:|:---:|:---:|:---|
| **STORE-GGN-01** (CyberHub) | `PROD-001` (Sony Headphones) | 12 | 1 | 3 | Normal Stock |
| | `PROD-002` (Bose QC45) | 8 | 0 | 2 | Normal Stock |
| | `PROD-003` (AirPods Pro) | 15 | 2 | 4 | High Stock |
| | `PROD-004` (Apple Watch 9) | 7 | 1 | 2 | Normal Stock |
| | `PROD-005` (Galaxy Watch 6) | 9 | 0 | 2 | Normal Stock |
| | `PROD-006` (Fitbit Charge 6) | 14 | 0 | 3 | Normal Stock |
| | `PROD-007` (Logitech Mouse) | 20 | 1 | 5 | High Stock |
| | `PROD-008` (Keychron Keyboard) | 11 | 0 | 3 | Normal Stock |
| | `PROD-009` (Dell 4K Monitor) | 4 | 0 | 1 | Normal Stock |
| | `PROD-010` (Nike Tee) | 25 | 0 | 5 | High Stock |
| | `PROD-011` (Wildcraft Bag) | 18 | 0 | 4 | Normal Stock |
| | `PROD-012` (Blue Tokai Coffee) | 45 | 3 | 10 | High Stock |
| **STORE-GGN-02** (Ambience) | `PROD-001` (Sony Headphones) | **0** | 0 | 3 | 🔴 **OUT OF STOCK** |
| | `PROD-002` (Bose QC45) | 5 | 0 | 2 | Normal Stock |
| | `PROD-003` (AirPods Pro) | 10 | 1 | 3 | Normal Stock |
| | `PROD-004` (Apple Watch 9) | 3 | 0 | 2 | Normal Stock |
| | `PROD-005` (Galaxy Watch 6) | 6 | 0 | 2 | Normal Stock |
| | `PROD-006` (Fitbit Charge 6) | 8 | 0 | 2 | Normal Stock |
| | `PROD-007` (Logitech Mouse) | 12 | 0 | 3 | Normal Stock |
| | `PROD-008` (Keychron Keyboard) | 7 | 0 | 2 | Normal Stock |
| | `PROD-009` (Dell 4K Monitor) | **0** | 0 | 1 | 🔴 **OUT OF STOCK** |
| | `PROD-010` (Nike Tee) | 30 | 0 | 5 | High Stock |
| | `PROD-011` (Wildcraft Bag) | 15 | 0 | 3 | Normal Stock |
| | `PROD-012` (Blue Tokai Coffee) | 20 | 0 | 5 | Normal Stock |
| **STORE-DEL-01** (CP Flagship) | `PROD-001` through `PROD-012` | 6 – 50 | 0 – 1 | 2 – 10 | Fully Stocked Flagship Hub |
| **STORE-DEL-02** (Select Citywalk) | `PROD-001` through `PROD-012` | 2 – 60 | 0 – 2 | 1 – 10 | Lifestyle Focused Inventory |
| **STORE-NOI-01** (Mall of India) | `PROD-002` (Bose QC45) | **0** | 0 | 2 | 🔴 **OUT OF STOCK** |
| | `PROD-009` (Dell 4K Monitor) | **1** | 0 | 1 | ⚠️ **LOW STOCK ALERT** |
| | Other 10 items | 3 – 30 | 0 | 2 – 5 | Standard Stock |

---

## 6. Table: `customers` (5 Test Accounts)

| ID | Name | Email | Phone | Shipping / Billing Address |
|:---|:---|:---|:---|:---|
| `CUST-001` | Aarav Sharma | `aarav.sharma@example.com` | +91 9876543210 | Tower 4, DLF The Camellias, Golf Course Road, Gurugram |
| `CUST-002` | Priya Verma | `priya.verma@example.com` | +91 9811223344 | Flat 302, Barakhamba Road, Connaught Place, New Delhi |
| `CUST-003` | Rohan Mehta | `rohan.mehta@example.com` | +91 9920334455 | Sector 15A, Near City Park, Noida |
| `CUST-004` | Ananya Iyer | `ananya.iyer@example.com` | +91 9840112233 | D-Block, Saket, South Delhi |
| `CUST-005` | Vikram Malhotra | `vikram.malhotra@example.com` | +91 9822998877 | Phase 5, Udyog Vihar, Gurugram |

---

## 7. Table: `carts` (1 Active Shopping Basket)

Persisted active shopping basket demonstrating cross-device cart persistence awaiting customer checkout:

| ID | Customer ID | Selected Store ID | Created At | Last Updated |
|:---|:---|:---|:---|:---|
| `CART-001` | `CUST-005` (Vikram Malhotra) | `STORE-GGN-01` (CyberHub) | 2026-10-04 15:00:00 | 2026-10-04 15:10:00 |

---

## 8. Table: `cart_items` (2 Active Line Items)

Items currently held in `CART-001` awaiting checkout:

| ID | Cart ID | Product ID | Quantity | Added At |
|:---|:---|:---|:---:|:---|
| `CITEM-001` | `CART-001` | `PROD-002` (Bose QuietComfort 45) | 1 | 2026-10-04 15:02:00 |
| `CITEM-002` | `CART-001` | `PROD-011` (Wildcraft Ergonomic Backpack 28L) | 1 | 2026-10-04 15:08:00 |

---

## 9. Tables: `orders` and `order_items` (Sample Historic Orders)

| Order Number | Customer | Fulfillment Store | Status | Channel | Total (INR) | Line Items Summary |
|:---|:---|:---|:---|:---|:---:|:---|
| `ORD-20261001-1001` | Aarav Sharma | CyberHub (`STORE-GGN-01`) | **COMPLETED** | Store Pickup | ₹31,099.00 | 1x Sony Headphones (₹29,999) + 2x Blue Tokai Coffee (₹1,100) |
| `ORD-20261002-1002` | Priya Verma | CP Flagship (`STORE-DEL-01`) | **READY_FOR_PICKUP** | Store Pickup | ₹41,900.00 | 1x Apple Watch Series 9 (₹41,900) |
| `ORD-20261003-1003` | Rohan Mehta | Mall of India (`STORE-NOI-01`)| **PROCESSING** | Store Pickup | ₹16,494.00 | 1x Logitech MX Master 3S (₹8,995) + 1x Keychron K2 (₹7,499) |
| `ORD-20261004-1004` | Ananya Iyer | Select Citywalk (`STORE-DEL-02`)| **CONFIRMED** | Home Delivery | ₹26,895.00 | 1x AirPods Pro 2 (₹24,900) + 1x Nike Tee (₹1,995) |

---

## 10. Database Table Verification Summary

| Table Name | Row Count | Integrity Constraint Checked |
|:---|:---:|:---|
| `stores` | 5 | Unique store codes, non-null city |
| `categories` | 4 | Unique primary keys |
| `products` | 12 | Foreign key to `categories`, non-negative price |
| `store_inventory` | 60 | Unique `(store_id, product_id)`, non-negative stock |
| `customers` | 5 | Unique email address |
| `carts` | 1 | Unique `customer_id` (1:1), Foreign key to `stores` |
| `cart_items` | 2 | Unique `(cart_id, product_id)`, quantity > 0 |
| `orders` | 4 | Foreign keys to `customers` and `stores`, valid status |
| `order_items` | 7 | Foreign keys to `orders` (CASCADE) and `products`, snapshot price |
