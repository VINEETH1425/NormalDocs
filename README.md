# StoreConnect – Omnichannel Retail Platform Backend

> **Enterprise Microservices Architecture | Spring Boot 3.4.1 | Spring Cloud 2024.0.0 | Java 25 | PostgreSQL | Apache Kafka**  
> **Strict Environment Specification:** Native Local Execution (**Zero Docker / No Containers**), Windows KRaft Kafka, Centralized Secrets Management (`.env`).

---

## 🏗️ 1. Architecture Overview

StoreConnect is an omnichannel retail backend platform that bridges digital e-commerce channels with physical retail store networks. It enables customers to check store-level inventory in real-time, reserve stock atomically, place Buy-Online-Pickup-In-Store (BOPIS) and home-delivery orders, and track fulfillment lifecycles via event-driven messaging.

```
                                  [ Client / Postman / Frontend ]
                                                 │
                                                 ▼
                             ┌───────────────────────────────────────┐
                             │       API Gateway (Port 8080)         │
                             │  - JWT Authentication & RBAC Filter   │
                             │  - Rate Limiting & Path Routing       │
                             └───────────────────┬───────────────────┘
                                                 │
                 ┌───────────────────────────────┼───────────────────────────────┐
                 │                               │                               │
                 ▼                               ▼                               ▼
     ┌───────────────────────┐       ┌───────────────────────┐       ┌───────────────────────┐
     │     User Service      │       │  Inventory Service    │       │     Order Service     │
     │      (Port 8081)      │       │      (Port 8082)      │       │      (Port 8083)      │
     │  - Auth & Customer    │       │  - Catalog & Stores   │◄──────┤  - Checkout & Orders  │
     │  - Shopping Cart      │       │  - Atomic Multi-Store │ Feign │  - State Machine      │
     └───────────┬───────────┘       │    Inventory & Lock   │       └───────────┬───────────┘
                 │                   └───────────┬───────────┘                   │
                 │                               │                               │
                 │                               │ Kafka Events                  │ Kafka Events
                 │                               ▼ (`order-events`)              ▼ (`order-events`)
                 │                 ┌──────────────────────────────────────────────────┐
                 │                 │       Apache Kafka Broker (Port 9092)            │
                 │                 │        - order-events   - payment-events         │
                 │                 └─────────────────────────┬────────────────────────┘
                 │                                           │
                 │                                           ▼
                 │                               ┌───────────────────────┐
                 │                               │    Payment Service    │
                 │                               │      (Port 8084)      │
                 │                               │  - Simulated Gateway  │
                 │                               │  - Async Ledger Audit │
                 │                               └───────────┬───────────┘
                 │                                           │
                 ▼                                           ▼
  ┌─────────────────────────────────────────────────────────────────────────────────────────┐
  │                           Local PostgreSQL Database (Port 5432)                         │
  │   - user_schema           - inventory_schema        - order_schema     - payment_schema │
  └─────────────────────────────────────────────────────────────────────────────────────────┘
                                                 ▲
                                                 │
                             ┌───────────────────┴───────────────────┐
                             │     Eureka Discovery Server (8761)    │
                             └───────────────────────────────────────┘
```

---

## 📦 2. Microservices Directory & Port Allocation

| Service | Port | Database Schema | Description |
| :--- | :---: | :---: | :--- |
| **eureka-server** | `8761` | *N/A* | Netflix Eureka Service Registry & Health Dashboard |
| **api-gateway** | `8080` | *N/A* | Reactive Gateway with JWT Validation, Header forwarding & CORS |
| **user-service** | `8081` | `user_schema` | Customer Auth (BCrypt + JWT), Profiles, Multi-item Persistent Cart |
| **store-inventory-service** | `8082` | `inventory_schema` | Store Locations, Products, Real-Time Atomic Stock Reserves |
| **order-service** | `8083` | `order_schema` | Order Placement, BOPIS Fulfillment, Strict State Machine |
| **payment-service** | `8084` | `payment_schema` | Simulated Gateway (95% success rate), Kafka Event Ledger |

---

## 🔒 3. Security & Exception Handling Architecture

1. **Gateway Token Validation & Header Enrichment**:
   - The API Gateway verifies the JWT bearer signature (`HS256`, 24-hour expiration) using `JWT_SECRET`.
   - On valid token verification, it extracts `sub` (User ID), `role`, and `email`, stripping any spoofed headers and forwarding authenticated context downstream via HTTP headers:
     - `X-User-Id`: Authenticated Customer ID
     - `X-User-Role`: Role (`ROLE_CUSTOMER`, `ROLE_STORE_MANAGER`, `ROLE_ADMIN`)
2. **Downward Microservice Security**:
   - Downstream services inspect `X-User-Id` and `X-User-Role` via `InternalHeaderAuthFilter`, establishing `SecurityContext` without re-verifying JWT signatures or hitting central stores.
   - Public paths (`/api/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`) bypass validation.
3. **Password Security**:
   - Customer passwords hashed with BCrypt (strength 12) before persistence.
4. **Standardized Exception Envelope**:
   - Every service implements a `GlobalExceptionHandler` intercepting all domain, validation, and runtime exceptions into a uniform `ApiErrorResponse` JSON record:
   ```json
   {
     "timestamp": "2026-10-17T20:30:00",
     "status": 400,
     "error": "Bad Request",
     "message": "Insufficient stock for product PROD-001 in store STORE-GGN-01",
     "path": "/api/orders",
     "traceId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
   }
   ```

---

## ⚙️ 4. Prerequisites & Environment Setup (Zero Docker)

### A. Local Software Requirements
- **Java**: OpenJDK 25 / Java 21+ (`java -version`)
- **Maven**: Version 3.9+ (`mvn -version`)
- **PostgreSQL**: Installed locally on port `5432` (`postgres/postgres`)
- **Apache Kafka**: Native Windows KRaft installation (no Zookeeper required)

### B. Environment Configuration (`.env`)
Create/verify the `.env` file in the root workspace folder (automatically ignored by git):
```ini
DB_HOST=localhost
DB_PORT=5432
DB_NAME=storeconnect
DB_USER=postgres
DB_PASSWORD=postgres

JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

KAFKA_BOOTSTRAP_SERVERS=localhost:9092
EUREKA_SERVER_URL=http://localhost:8761/eureka/

GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
INVENTORY_SERVICE_PORT=8082
ORDER_SERVICE_PORT=8083
PAYMENT_SERVICE_PORT=8084
```

### C. Database Initialization
Run the provided [init-database.sql](file:///d:/PS_CPS_Project/init-database.sql) against PostgreSQL using `psql` or pgAdmin:
```powershell
# In PowerShell:
psql -U postgres -h localhost -c "CREATE DATABASE storeconnect;"
psql -U postgres -h localhost -d storeconnect -f d:\PS_CPS_Project\init-database.sql
```
*(Each service will also auto-create its respective schema and seed initial stores and products on first boot via `schema.sql` and `data.sql`).*

### D. Starting Apache Kafka in KRaft Mode (Windows Native)
In your local Apache Kafka installation directory:
```powershell
# 1. Generate a cluster ID (one time only)
.\bin\windows\kafka-storage.bat random-uuid

# 2. Format log directory using the UUID
.\bin\windows\kafka-storage.bat format -t <GENERATED_UUID> -c .\config\kraft\server.properties

# 3. Start Kafka in KRaft mode
.\bin\windows\kafka-server-start.bat .\config\kraft\server.properties
```

---

## 🚀 5. How to Build & Run

### Step 1: Compile and Package All Modules
From the root project directory:
```powershell
mvn clean package -DskipTests
```
*Builds all 6 microservice JARs in under 35 seconds with 0 warnings or reflection errors.*

### Step 2: Launch All Services
You can launch all 6 services with proper staggered boot sequences using the provided runner script:
```cmd
.\start-all-services.bat
```
Or launch them manually in separate terminal tabs in this order:
1. `mvn spring-boot:run -pl eureka-server` (wait 15s)
2. `mvn spring-boot:run -pl api-gateway` (wait 10s)
3. `mvn spring-boot:run -pl store-inventory-service`
4. `mvn spring-boot:run -pl payment-service`
5. `mvn spring-boot:run -pl user-service`
6. `mvn spring-boot:run -pl order-service`

### Step 3: Verify Eureka Discovery
Open your browser to:
**http://localhost:8761**  
Ensure `API-GATEWAY`, `USER-SERVICE`, `STORE-INVENTORY-SERVICE`, `ORDER-SERVICE`, and `PAYMENT-SERVICE` appear under **Instances currently registered with Eureka**.

---

## 🧪 6. Automated End-to-End Verification

A comprehensive automated verification script is provided at [test-api.ps1](file:///d:/PS_CPS_Project/test-api.ps1). It executes the complete omnichannel lifecycle:

```powershell
powershell -ExecutionPolicy Bypass -File .\test-api.ps1
```

### Test Steps Executed by the Script:
1. **Eureka Health Verification**
2. **Customer Registration** (`POST /api/auth/register`)
3. **Customer Authentication & JWT Generation** (`POST /api/auth/login`)
4. **Physical Store Catalog Discovery** (`GET /api/stores`)
5. **Product & Real-Time Store Inventory Query** (`GET /api/inventory/store/{storeId}/product/{productId}`)
6. **Shopping Cart Management** (`POST /api/cart/items`)
7. **Omnichannel Order Placement (BOPIS)** (`POST /api/orders`)
8. **Simulated Payment Gateway & Kafka Event Processing** (`GET /api/orders/{id}`)

---

## 📡 7. Interactive API Endpoints & Swagger Documentation

Once the services are running, interactive Swagger UI documentation is available for each service:

- **User Service Swagger**: `http://localhost:8081/swagger-ui/index.html`
- **Inventory Service Swagger**: `http://localhost:8082/swagger-ui/index.html`
- **Order Service Swagger**: `http://localhost:8083/swagger-ui/index.html`
- **Payment Service Swagger**: `http://localhost:8084/swagger-ui/index.html`

### Core Gateway Routes (`http://localhost:8080`)

#### Customer & Auth
- `POST /api/auth/register` – Register new customer
- `POST /api/auth/login` – Login & receive JWT
- `GET /api/users/profile` – Get customer profile *(requires Bearer token)*
- `GET /api/cart` – View active cart *(requires Bearer token)*
- `POST /api/cart/items` – Add item to cart *(requires Bearer token)*
- `DELETE /api/cart/items/{productId}` – Remove item from cart *(requires Bearer token)*

#### Stores & Inventory
- `GET /api/stores` – List all physical retail store locations
- `GET /api/categories` – List product categories
- `GET /api/products` – List product catalog
- `GET /api/inventory/store/{storeId}/product/{productId}` – Check stock for a specific store & product
- `GET /api/inventory/product/{productId}/availability` – Multi-store stock availability lookup

#### Orders & Fulfillment
- `POST /api/orders` – Place order (atomic stock reservation + `order-events` Kafka publication)
- `GET /api/orders/my-orders` – List customer orders
- `GET /api/orders/{id}` – View order status & details
- `PATCH /api/orders/{id}/status` – Transition order status (`PROCESSING` $\rightarrow$ `READY_FOR_PICKUP` $\rightarrow$ `COMPLETED`)

#### Payments
- `GET /api/payments/order/{orderId}` – Check payment audit record for an order
