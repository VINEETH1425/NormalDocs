# StoreConnect: Postman End-to-End Testing & Next Steps Guide

> **Target**: Step-by-Step API Validation via Postman through the API Gateway  
> **Base URL**: `http://localhost:8080` (API Gateway)  
> **Eureka Registry**: `http://localhost:8761`  

---

## 1. Prerequisites Checklist

Before executing requests in Postman, ensure all 6 microservices are running:

| Service | Port | Direct URL | Role |
|---|:---:|---|---|
| **eureka-server** | `8761` | `http://localhost:8761` | Service Discovery & Registry |
| **api-gateway** | `8080` | `http://localhost:8080` | **All Postman requests must target this port!** |
| **user-service** | `8081` | `http://localhost:8081` | Customer Accounts, Auth & Cart |
| **store-inventory-service** | `8082` | `http://localhost:8082` | Stores, Categories, Products & Stock |
| **order-service** | `8083` | `http://localhost:8083` | BOPIS Orders & Reservation Sync |
| **payment-service** | `8084` | `http://localhost:8084` | Transaction Ledger & Kafka Consumer |

> 🔍 **Verify Eureka**: Open `http://localhost:8761` in your browser. Confirm that all 5 application instances (`API-GATEWAY`, `USER-SERVICE`, `STORE-INVENTORY-SERVICE`, `ORDER-SERVICE`, `PAYMENT-SERVICE`) show status **UP**.

---

## 2. Phase 1: Catalog & Store Setup (Admin Flow)

*If your PostgreSQL tables are completely fresh and empty, run this section first to populate the catalog using the built-in Admin account.*

### Request 1.1: Admin Login (Get Admin JWT Token)
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/login`
* **Headers**: `Content-Type: application/json`
* **Body (raw JSON)**:
```json
{
  "email": "admin@storeconnect.com",
  "password": "Admin@123"
}
```
* **Expected Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000,
  "customer": {
    "id": "CUST-ADMIN-01",
    "email": "admin@storeconnect.com",
    "role": "ADMIN"
  }
}
```
> 📌 **Action**: Copy the `token` string. This is your `ADMIN_TOKEN`.

---

### Request 1.2: Create Product Category
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/categories`
* **Headers**:
  * `Authorization`: `Bearer <ADMIN_TOKEN>`
  * `Content-Type`: `application/json`
* **Body (raw JSON)**:
```json
{
  "id": "CAT-AUDIO",
  "name": "Audio & Sound",
  "description": "Noise-cancelling headphones and wireless earbuds"
}
```
* **Expected Response**: `201 Created`

---

### Request 1.3: Create Physical Retail Store
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/stores`
* **Headers**:
  * `Authorization`: `Bearer <ADMIN_TOKEN>`
  * `Content-Type`: `application/json`
* **Body (raw JSON)**:
```json
{
  "id": "STORE-GGN-01",
  "name": "StoreConnect CyberHub",
  "code": "STR-CH-01",
  "address": "DLF CyberHub, DLF Phase 2, Sector 24",
  "city": "Gurugram",
  "phone": "+91 124 4567890",
  "isActive": true
}
```
* **Expected Response**: `201 Created`

---

### Request 1.4: Create Product in Catalog
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/products`
* **Headers**:
  * `Authorization`: `Bearer <ADMIN_TOKEN>`
  * `Content-Type`: `application/json`
* **Body (raw JSON)**:
```json
{
  "id": "PROD-AUD-001",
  "sku": "SKU-SNY-WH1000XM5",
  "name": "Sony WH-1000XM5 ANC Headphones",
  "description": "Industry-leading noise canceling with Auto NC Optimizer",
  "categoryId": "CAT-AUDIO",
  "price": 29990.00,
  "isActive": true
}
```
* **Expected Response**: `201 Created`

---

### Request 1.5: Set Initial Store Inventory Stock
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/inventory/stock`
* **Headers**:
  * `Authorization`: `Bearer <ADMIN_TOKEN>`
  * `Content-Type`: `application/json`
* **Body (raw JSON)**:
```json
{
  "storeId": "STORE-GGN-01",
  "productId": "PROD-AUD-001",
  "quantityChange": 15,
  "reason": "INITIAL_STOCK"
}
```
* **Expected Response**: `200 OK` (Available stock set to 15).

---

## 3. Phase 2: Complete Customer Journey (Demo & Viva Walkthrough)

*This is the core end-to-end customer journey you will demonstrate in your evaluation.*

### Request 2.1: Customer Registration
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/register`
* **Headers**: `Content-Type: application/json`
* **Body (raw JSON)**:
```json
{
  "firstName": "Vineeth",
  "lastName": "Kumar",
  "email": "vineeth.demo@example.com",
  "password": "Password@123",
  "phone": "+91 9876543210",
  "address": "Sector 56, Gurugram, Haryana"
}
```
* **Expected Response (`201 Created`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000,
  "customer": {
    "id": "CUST-XXXXXX",
    "firstName": "Vineeth",
    "lastName": "Kumar",
    "email": "vineeth.demo@example.com",
    "role": "CUSTOMER"
  }
}
```
> 📌 **Action**: Copy the `token` string. This is your `CUSTOMER_TOKEN` for the remaining steps.

---

### Request 2.2: Customer Login (Alternative to Registration)
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/login`
* **Headers**: `Content-Type: application/json`
* **Body (raw JSON)**:
```json
{
  "email": "vineeth.demo@example.com",
  "password": "Password@123"
}
```
* **Expected Response**: `200 OK` with customer token.

---

### Request 2.3: Browse Stores
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/stores`
* **Expected Response**: `200 OK` list containing `STORE-GGN-01`.

---

### Request 2.4: Browse Product Catalog
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/products`
* **Expected Response**: `200 OK` list containing `PROD-AUD-001` (Sony WH-1000XM5, Price: 29990.00).

---

### Request 2.5: Check Real-Time Stock at Physical Store
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/inventory/store/STORE-GGN-01/product/PROD-AUD-001`
* **Expected Response (`200 OK`)**:
```json
{
  "storeId": "STORE-GGN-01",
  "productId": "PROD-AUD-001",
  "quantityAvailable": 15,
  "quantityReserved": 0
}
```
*(Remember this number: 15 available!)*

---

### Request 2.6: Add Item to Shopping Cart
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/cart/items`
* **Headers**:
  * `Authorization`: `Bearer <CUSTOMER_TOKEN>`
  * `Content-Type`: `application/json`
* **Body (raw JSON)**:
```json
{
  "productId": "PROD-AUD-001",
  "quantity": 1,
  "storeId": "STORE-GGN-01"
}
```
* **Expected Response (`200 OK`)**:
```json
{
  "id": "CART-XXXXXX",
  "customerId": "CUST-XXXXXX",
  "storeId": "STORE-GGN-01",
  "totalAmount": 29990.00,
  "items": [
    {
      "productId": "PROD-AUD-001",
      "productName": "Sony WH-1000XM5 ANC Headphones",
      "quantity": 1,
      "unitPrice": 29990.00,
      "subtotal": 29990.00
    }
  ]
}
```

---

### Request 2.7: View Current Cart Summary
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/cart`
* **Headers**:
  * `Authorization`: `Bearer <CUSTOMER_TOKEN>`
* **Expected Response**: `200 OK` with the active cart details.

---

### Request 2.8: Execute BOPIS Checkout (Place Order)
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/orders/checkout`
* **Headers**:
  * `Authorization`: `Bearer <CUSTOMER_TOKEN>`
  * `Content-Type`: `application/json`
* **Body (raw JSON)**:
```json
{
  "storeId": "STORE-GGN-01",
  "fulfillmentType": "STORE_PICKUP"
}
```
* **Expected Response (`200 OK`)**:
```json
{
  "id": "ORD-XXXXXX",
  "orderNumber": "ORD-20261018-XXXXXX",
  "customerId": "CUST-XXXXXX",
  "storeId": "STORE-GGN-01",
  "status": "CREATED",
  "fulfillmentType": "STORE_PICKUP",
  "totalAmount": 29990.00,
  "items": [
    {
      "productId": "PROD-AUD-001",
      "quantity": 1,
      "unitPrice": 29990.00,
      "subtotal": 29990.00
    }
  ]
}
```

---

### Request 2.9: Verify Stock Decrement (Atomic Reservation Proof)
Call the stock check endpoint again to show real-time synchronization between services:
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/inventory/store/STORE-GGN-01/product/PROD-AUD-001`
* **Expected Response (`200 OK`)**:
```json
{
  "storeId": "STORE-GGN-01",
  "productId": "PROD-AUD-001",
  "quantityAvailable": 14,
  "quantityReserved": 1
}
```
> 💡 **What happened here**: `order-service` synchronously invoked `store-inventory-service` via **OpenFeign**. It decremented `quantityAvailable` from **15 $\rightarrow$ 14** and incremented `quantityReserved` from **0 $\rightarrow$ 1**, reserving the item at the physical store without overselling!

---

### Request 2.10: View Customer Order History
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/orders`
* **Headers**:
  * `Authorization`: `Bearer <CUSTOMER_TOKEN>`
* **Expected Response**: `200 OK` list containing the newly placed order.

---

## 4. Phase 3: Live PostgreSQL Database Verification

To show the evaluator that data was committed across all database schemas, open PowerShell:

```powershell
$env:PGPASSWORD = "JaiShriRam321@"
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -d storeconnect -h 127.0.0.1
```

Run these SQL queries:

```sql
-- 1. Check newly registered customer
SELECT id, email, first_name, last_name, role, created_at 
FROM user_schema.customers 
ORDER BY created_at DESC LIMIT 1;

-- 2. Check placed order
SELECT id, order_number, customer_id, store_id, status, fulfillment_type, total_amount, created_at 
FROM order_schema.orders 
ORDER BY created_at DESC LIMIT 1;

-- 3. Check order items
SELECT id, order_id, product_id, quantity, unit_price, subtotal 
FROM order_schema.order_items 
ORDER BY id DESC LIMIT 1;

-- 4. Check store inventory stock update
SELECT store_id, product_id, quantity_available, quantity_reserved, updated_at 
FROM inventory_schema.store_inventory 
WHERE store_id = 'STORE-GGN-01';
```

---

## 5. Summary of Architecture Points for Your Demo

1. **All requests pass through Port 8080 (API Gateway)**: The gateway inspects the HMAC-SHA256 JWT, injects `X-User-Id` and `X-User-Role`, and routes via Netty non-blocking WebFlux event loops.
2. **Zero SQL Scripts Used**: Hibernate auto-generated the DDL tables, and all data was populated dynamically via standard REST APIs and Spring Data JPA.
3. **Synchronous Stock Reservation**: `order-service` calls `store-inventory-service` using an OpenFeign declarative interface to ensure zero overselling.
4. **Asynchronous Events**: Once the order is committed to PostgreSQL, an `ORDER_PLACED` event is broadcast to Kafka for transaction processing.
