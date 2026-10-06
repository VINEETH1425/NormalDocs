# StoreConnect – Problem Understanding, Scope & Architecture Foundation

**Project Name:** StoreConnect – Omnichannel Retail Platform  
**Batch:** PSI-2026 Sep ASDE GGN  
**Checkpoint 1 Delivery Date:** Tuesday, Oct 7, 2026 (Sprint 1 Check-in)  
**Final Capstone Demo:** Day 21  

---

## 1. Executive Summary & Problem Understanding

### 1.1 The Retail Reality
Traditional multi-channel retail organizations operate physical stores and online e-commerce channels as disconnected operational silos:
1. **Disconnected Catalog & Inventory:** Online shoppers see products listed as "in stock", but the nearest store or fulfillment hub may be out of stock, leading to fulfillment failures, customer dissatisfaction, and high cancellation rates.
2. **Lack of Store-Level Visibility:** Store floor associates have zero visibility into real-time online order commitments reserved against physical shelf inventory, causing accidental double-selling.
3. **Manual & Fragile Order Lifecycle:** Status changes (such as order confirmation, picking, packing, dispatch, or customer pickup) are handled through disjointed manual steps without state machine enforcement, creating data inconsistency.
4. **Audit Trail Blind Spots:** High-volume inventory state changes and order transitions lack centralized audit logging, making dispute resolution and inventory reconciliation difficult.

### 1.2 The StoreConnect Solution
**StoreConnect** creates a unified digital omnichannel retail platform that bridges the physical store network with digital commerce. It provides:
- A unified product catalog with real-time, store-level inventory visibility.
- Atomic stock reservations during checkout to prevent overselling.
- A formalized order lifecycle state machine preventing invalid status transitions.
- Operational fulfillment views for store operators to pick, pack, and mark orders ready for pickup or dispatch.
- Event audit logging capturing critical order and inventory events.

---

## 2. Target Personas & User Roles

| Persona | Role | Primary Responsibilities & Journeys |
|---------|------|-------------------------------------|
| **Customer** | End User | Registers/logs in, browses catalog, filters by category, checks store-specific availability, manages cart, places orders, and tracks real-time status. |
| **Store Operator** | Floor Associate | Monitors incoming orders assigned to their store, checks picking lists, updates fulfillment status (Confirm $\rightarrow$ Processing $\rightarrow$ Ready for Pickup), and updates local inventory levels. |
| **Retail Administrator** | Backoffice Manager | Manages product catalog, creates categories, sets pricing, oversees multi-store inventory levels, and reviews audit logs. |
| **System** | Automated Engine | Enforces transactional stock reservations, executes order state machine validation rules, and logs audit events. |

---

## 3. Project Scope Boundaries

### In-Scope for Sprint 1 (Foundation & Data Layer – Checkpoint 1: Tuesday)
- **Domain Modeling:** Complete Object-Oriented Domain Model in modern Java (Product, Category, Store, Inventory, Customer, Cart, Order, enums).
- **Business Rule Enforcement:**
  - Real-time store inventory validation before checkout.
  - Transactional inventory deduction on order confirmation.
  - Automatic stock restoration upon order cancellation.
  - Formalized Order State Machine (`PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `PROCESSING` $\rightarrow$ `READY_FOR_PICKUP` $\rightarrow$ `COMPLETED` / `CANCELLED`).
- **Core Persistence (PostgreSQL & JDBC):**
  - Relational schema (`schema.sql`) with tables, primary keys, foreign keys, and indexes.
  - Seed dataset (`seed_data.sql`) reflecting realistic Gurgaon/Delhi store locations and product catalog.
  - Robust JDBC DAOs managing CRUD operations and transactional boundaries.
- **NoSQL Audit Log (MongoDB):**
  - Document-based event log capturing order state transitions and inventory updates.
- **Quality & Automated Testing:**
  - JUnit 5 test suite verifying business rules, inventory invariants, and state transitions.
- **Runnable Live Demo:**
  - A console demo runner showcasing the end-to-end customer order journey and store operator fulfillment live.

### In-Scope for Later Sprints (Days 6–20)
- **Sprint 2:** Spring Boot REST APIs, OpenAPI/Swagger docs, Layered Architecture, Microservice boundaries.
- **Sprint 3:** Apache Kafka event integration (`OrderPlacedEvent`), React + TypeScript UI foundation.
- **Sprint 4:** Redux Toolkit state management, full-stack integration, BDD Cucumber/Selenium automated tests.

### Explicit Scope Exclusions (As per CPS Guidelines)
- ❌ Production Docker, Kubernetes, AWS deployments (expressly excluded by CPS prompt).
- ❌ Python backend (to be handled during post-Day 21 refactor module).
- ❌ Real third-party payment gateways, bank integrations, or POS hardware scanners.
- ❌ External shipping courier APIs.

---

## 4. Key Architectural Decisions & Patterns

1. **Layered Architecture:**
   - `domain`: Encapsulates business entities, value objects, and domain invariants.
   - `repository`: Data access interfaces and JDBC implementations.
   - `service`: Orchestrates business workflows (catalog search, cart operations, transactional order placement).
   - `audit`: MongoDB event logging for append-only audit trail.
2. **State Machine Pattern:**
   - Order transitions are guarded by an enum-driven state transition matrix. Any illegal transition throws an `IllegalStateException`.
3. **Resilience & Dual Database Support:**
   - The data layer is designed against standard ANSI SQL and PostgreSQL. A pluggable connection manager enables PostgreSQL execution while also supporting SQLite/in-memory mode for offline evaluation resilience.
